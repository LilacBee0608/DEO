package com.bili.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bili.demo.common.BusinessException;
import com.bili.demo.dto.VideoDetailVO;
import com.bili.demo.dto.VideoQueryDTO;
import com.bili.demo.dto.VideoUploadDTO;
import com.bili.demo.entity.VFavorite;
import com.bili.demo.entity.VLike;
import com.bili.demo.entity.VUser;
import com.bili.demo.entity.Video;
import com.bili.demo.mapper.VFavoriteMapper;
import com.bili.demo.mapper.VLikeMapper;
import com.bili.demo.mapper.VUserMapper;
import com.bili.demo.mapper.VideoMapper;
import com.bili.demo.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * 视频服务实现
 * 包含查询、详情、上传、修改、删除、点赞、收藏、播放量自增等业务
 * 所有需登录的操作均通过 currentUserId 校验权限
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoMapper videoMapper;
    private final VUserMapper vUserMapper;
    private final VLikeMapper vLikeMapper;
    private final VFavoriteMapper vFavoriteMapper;

    @Override
    public IPage<Video> queryPage(VideoQueryDTO dto) {
        Page<Video> page = new Page<>(dto.getPage(), dto.getSize());
        return videoMapper.selectVideoPage(page, dto.getTitle(), dto.getTags());
    }

    @Override
    public VideoDetailVO detail(String vId, Integer currentUserId) {
        Video video = videoMapper.selectById(vId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }
        VideoDetailVO vo = new VideoDetailVO();
        BeanUtils.copyProperties(video, vo);

        // 查询作者昵称
        if (video.getId() != null) {
            try {
                VUser author = vUserMapper.selectById(Integer.parseInt(video.getId()));
                if (author != null) {
                    vo.setAuthorName(author.getUserName());
                }
            } catch (NumberFormatException ignored) {
                // 作者id非数字则跳过
            }
        }

        // 查询当前用户是否已点赞/收藏(未登录则默认 false)
        if (currentUserId != null) {
            vo.setLiked(vLikeMapper.selectByMap(Map.of("id", currentUserId, "v_id", vId)).size() > 0);
            vo.setFavorited(vFavoriteMapper.selectByMap(Map.of("id", currentUserId, "v_id", vId)).size() > 0);
        } else {
            vo.setLiked(false);
            vo.setFavorited(false);
        }
        return vo;
    }

    @Override
    public String create(VideoUploadDTO dto, Integer userId) {
        Video video = new Video();
        BeanUtils.copyProperties(dto, video);
        // 生成视频id: v + UUID前8位(简化实训)
        video.setVId("v" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        video.setId(String.valueOf(userId));
        video.setPlayNum(0);
        video.setLikeNum(0);
        video.setShareNum(0);
        videoMapper.insert(video);
        log.info("视频上传成功: vId={}, author={}", video.getVId(), userId);
        return video.getVId();
    }

    @Override
    public void update(String vId, VideoUploadDTO dto, Integer userId) {
        Video video = checkOwner(vId, userId);
        BeanUtils.copyProperties(dto, video);
        video.setVId(vId);
        videoMapper.updateById(video);
    }

    @Override
    public void delete(String vId, Integer userId) {
        checkOwner(vId, userId);
        videoMapper.deleteById(vId);
        // 同时清理关联的点赞、收藏(弹幕、评论可保留作为历史)
        vLikeMapper.delete(new LambdaQueryWrapper<VLike>().eq(VLike::getVId, vId));
        vFavoriteMapper.delete(new LambdaQueryWrapper<VFavorite>().eq(VFavorite::getVId, vId));
    }

    @Override
    public void incrPlay(String vId) {
        // 播放量 +1,使用 update wrapper 避免查全表
        videoMapper.update(null,
                new LambdaUpdateWrapper<Video>()
                        .eq(Video::getVId, vId)
                        .setSql("play_num = play_num + 1"));
    }

    @Override
    @Transactional
    public boolean toggleLike(String vId, Integer userId) {
        // 查询是否已点赞
        VLike exist = vLikeMapper.selectOne(
                new LambdaQueryWrapper<VLike>()
                        .eq(VLike::getId, userId)
                        .eq(VLike::getVId, vId));
        if (exist != null) {
            // 已点赞 -> 取消点赞
            vLikeMapper.delete(new LambdaQueryWrapper<VLike>()
                    .eq(VLike::getId, userId)
                    .eq(VLike::getVId, vId));
            // 视频点赞数 -1
            videoMapper.update(null,
                    new LambdaUpdateWrapper<Video>()
                            .eq(Video::getVId, vId)
                            .setSql("like_num = GREATEST(like_num - 1, 0)"));
            return false;
        } else {
            // 未点赞 -> 点赞
            vLikeMapper.insert(new VLike(userId, vId));
            videoMapper.update(null,
                    new LambdaUpdateWrapper<Video>()
                            .eq(Video::getVId, vId)
                            .setSql("like_num = like_num + 1"));
            return true;
        }
    }

    @Override
    public boolean toggleFavorite(String vId, Integer userId) {
        VFavorite exist = vFavoriteMapper.selectOne(
                new LambdaQueryWrapper<VFavorite>()
                        .eq(VFavorite::getId, userId)
                        .eq(VFavorite::getVId, vId));
        if (exist != null) {
            vFavoriteMapper.delete(new LambdaQueryWrapper<VFavorite>()
                    .eq(VFavorite::getId, userId)
                    .eq(VFavorite::getVId, vId));
            return false;
        } else {
            vFavoriteMapper.insert(new VFavorite(userId, vId));
            return true;
        }
    }

    /**
     * 校验当前用户是否为该视频作者
     * 不是则抛出业务异常
     */
    private Video checkOwner(String vId, Integer userId) {
        Video video = videoMapper.selectById(vId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }
        if (!String.valueOf(userId).equals(video.getId())) {
            throw new BusinessException(403, "无权操作: 仅作者可修改/删除");
        }
        return video;
    }
}
