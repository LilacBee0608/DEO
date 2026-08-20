package com.bili.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bili.demo.dto.CommentSendDTO;
import com.bili.demo.dto.CommentVO;
import com.bili.demo.entity.VComment;
import com.bili.demo.entity.VUser;
import com.bili.demo.mapper.VCommentMapper;
import com.bili.demo.mapper.VUserMapper;
import com.bili.demo.service.VCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 * - listByVideo: 查询评论后批量查询用户昵称(避免 N+1 查询)
 * - send: 插入评论
 * - like: 评论点赞数 +1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VCommentServiceImpl implements VCommentService {

    private final VCommentMapper vCommentMapper;
    private final VUserMapper vUserMapper;

    @Override
    public List<CommentVO> listByVideo(String vId) {
        List<VComment> comments = vCommentMapper.selectByVideo(vId);
        if (comments.isEmpty()) {
            return List.of();
        }
        // 收集所有用户id,批量查询昵称(避免 N+1)
        Set<Integer> userIds = comments.stream()
                .map(c -> {
                    try {
                        return Integer.parseInt(c.getId());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, String> nameMap = Map.of();
        if (!userIds.isEmpty()) {
            List<VUser> users = vUserMapper.selectBatchIds(userIds);
            nameMap = users.stream()
                    .collect(Collectors.toMap(VUser::getId, VUser::getUserName, (a, b) -> a));
        }

        // 组装 VO
        final Map<Integer, String> finalNameMap = nameMap;
        return comments.stream().map(c -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(c, vo);
            try {
                Integer uid = Integer.parseInt(c.getId());
                vo.setUserName(finalNameMap.getOrDefault(uid, "匿名用户"));
            } catch (NumberFormatException e) {
                vo.setUserName("匿名用户");
            }
            return vo;
        }).toList();
    }

    @Override
    public Integer send(CommentSendDTO dto, Integer userId) {
        VComment comment = new VComment();
        comment.setId(String.valueOf(userId));
        comment.setVId(dto.getVId());
        comment.setCommentContent(dto.getCommentContent());
        comment.setCommentFrame(LocalDateTime.now());
        comment.setLikeNum(0);
        vCommentMapper.insert(comment);
        log.info("评论发送成功: cid={}, vId={}, userId={}", comment.getCid(), dto.getVId(), userId);
        return comment.getCid();
    }

    @Override
    public void like(Integer cid) {
        // 评论点赞数 +1 (简化:不记录谁点过赞)
        vCommentMapper.update(null,
                new LambdaUpdateWrapper<VComment>()
                        .eq(VComment::getCid, cid)
                        .setSql("like_num = like_num + 1"));
    }
}
