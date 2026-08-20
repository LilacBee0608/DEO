package com.bili.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bili.demo.entity.VComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论 Mapper 接口
 * 自定义方法:
 *   - selectByVideo: 查询某视频的全部评论(按时间倒序)
 */
@Mapper
public interface VCommentMapper extends BaseMapper<VComment> {

    /**
     * 查询某视频的全部评论,按时间倒序(最新评论优先)
     */
    @Select("SELECT cid, id, v_id, comment_num, comment_content, comment_frame, like_num " +
            "FROM v_comment WHERE v_id = #{vId} ORDER BY comment_frame DESC")
    List<VComment> selectByVideo(@Param("vId") String vId);
}
