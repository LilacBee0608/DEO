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
        SELECT v_id, id, title, tags, play_num, like_num, share_num, description, cover_url, video_url, create_time
        FROM video
        WHERE 1=1
          <if test="title != null and title != ''">AND title LIKE CONCAT('%', #{title}, '%')</if>
          <if test="tags  != null and tags  != ''">AND tags  LIKE CONCAT('%', #{tags},  '%')</if>
        ORDER BY play_num DESC, create_time DESC
        </script>
        """)
    IPage<Video> selectVideoPage(Page<Video> page,
                                @Param("title") String title,
                                @Param("tags") String tags);
}
