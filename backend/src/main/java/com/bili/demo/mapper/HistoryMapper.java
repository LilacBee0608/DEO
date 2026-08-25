package com.bili.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bili.demo.entity.History;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 观看历史 Mapper 接口
 * BaseMapper 提供基础 CRUD
 * 自定义方法:
 *   - selectHistoryList: 联表查询用户观看历史(含视频信息和作者名)
 *   - upsertWatch: 重复观看时更新时间,不存在则插入(去重)
 */
@Mapper
public interface HistoryMapper extends BaseMapper<History> {

    /**
     * 查询用户观看历史(联表 video + v_user)
     * 按观看时间倒序,返回视频信息用于前端展示
     * SQL 别名使用 camelCase 确保非数据库字段正确映射
     *
     * @param userId 用户id
     * @return 观看历史列表(每条含视频信息)
     */
    @Select("""
            SELECT h.hid, h.user_id, h.v_id, h.watch_time,
                   v.title, v.tags, v.play_num, v.like_num,
                   v.description, v.cover_url, v.video_url, v.create_time,
                   u.user_name AS authorName
            FROM history h
            INNER JOIN video v ON h.v_id = v.v_id
            LEFT JOIN v_user u ON v.id = CAST(u.id AS CHAR)
            WHERE h.user_id = #{userId}
            ORDER BY h.watch_time DESC
            """)
    List<Map<String, Object>> selectHistoryList(@Param("userId") Integer userId);

    /**
     * 记录或更新观看历史(去重)
     * 利用 UNIQUE 约束 (user_id, v_id):
     *   - 不存在则插入新记录
     *   - 已存在则更新 watch_time 为当前时间
     *
     * @param userId 用户id
     * @param vId    视频id
     * @return 影响行数
     */
    @org.apache.ibatis.annotations.Insert("""
            INSERT INTO history (user_id, v_id, watch_time)
            VALUES (#{userId}, #{vId}, NOW())
            ON DUPLICATE KEY UPDATE watch_time = NOW()
            """)
    int upsertWatch(@Param("userId") Integer userId, @Param("vId") String vId);
}
