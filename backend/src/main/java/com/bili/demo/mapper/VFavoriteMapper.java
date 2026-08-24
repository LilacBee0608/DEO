package com.bili.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bili.demo.entity.VFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 视频收藏 Mapper 接口
 * 联合主键场景,主要使用 selectByMap / deleteByMap / insert
 * v_favorite 表联合主键 (id, v_id) 已天然保证同一用户对同一视频不重复收藏
 */
@Mapper
public interface VFavoriteMapper extends BaseMapper<VFavorite> {

    /**
     * 查询用户收藏的视频列表(联表 video + v_user,用于个人中心收藏夹展示)
     * 按视频播放量倒序
     * SQL 别名使用 camelCase(authorName),确保 MyBatis 非数据库字段正确映射
     *
     * @param userId 用户id(对应 v_favorite.id)
     * @return 收藏视频列表(每条含标题、封面、作者名等展示字段)
     */
    @Select("""
            SELECT f.id, f.v_id,
                   v.title, v.tags, v.play_num, v.like_num,
                   v.description, v.cover_url, v.video_url, v.create_time,
                   u.user_name AS authorName
            FROM v_favorite f
            INNER JOIN video v ON f.v_id = v.v_id
            LEFT JOIN v_user u ON v.id = CAST(u.id AS CHAR)
            WHERE f.id = #{userId}
            ORDER BY v.play_num DESC
            """)
    List<Map<String, Object>> selectFavoriteList(@Param("userId") Integer userId);
}
