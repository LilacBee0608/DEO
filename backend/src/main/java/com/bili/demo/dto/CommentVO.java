package com.bili.demo.dto;

import com.bili.demo.entity.VComment;
import lombok.Data;

/**
 * 评论返回 VO
 * 在原 VComment 基础上扩展:
 *   - userName: 评论者昵称(关联 v_user 查询)
 */
@Data
public class CommentVO extends VComment {
    /** 评论者昵称 */
    private String userName;
}
