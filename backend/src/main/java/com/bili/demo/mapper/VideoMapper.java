package com.bili.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bili.demo.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 视频 Mapper 接口
 * BaseMapper 提供基础 CRUD
 * 自定义方法:
 *   - selectVideoPage: 按标题/标签模糊分页查询视频列表(游客可见)
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {

    /**
     * 分页查询视频(支持按 title / tags 模糊搜索)
     * @param page   分页参数(Page对象,由 MyBatis-Plus 分页插件处理)
     * @param title  标题关键词(可空)
     * @param tags   标签关键词(可空)
     * @return 分页结果
     */
    @Select("""
        <script>
        SELECT v.v_id, v.id, v.title, v.tags, v.play_num, v.like_num, v.share_num,
               v.description, v.cover_url, v.video_url, v.create_time,
               u.user_name AS authorName
        FROM video v
        LEFT JOIN v_user u ON v.id = CAST(u.id AS CHAR)
        WHERE 1=1
          <if test="title != null and title != ''">AND v.title LIKE CONCAT('%', #{title}, '%')</if>
          <if test="tags  != null and tags  != ''">AND v.tags  LIKE CONCAT('%', #{tags},  '%')</if>
        ORDER BY v.play_num DESC, v.create_time DESC
        </script>
        """)
    IPage<Video> selectVideoPage(Page<Video> page,
                                @Param("title") String title,
                                @Param("tags") String tags);
}
