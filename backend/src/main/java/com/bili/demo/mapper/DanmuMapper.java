package com.bili.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bili.demo.entity.Danmu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 弹幕 Mapper 接口
 * 自定义方法:
 *   - selectByVideo: 查询某视频的全部弹幕(按时间轴排序)
 */
@Mapper
public interface DanmuMapper extends BaseMapper<Danmu> {

    /**
     * 查询某视频的全部弹幕,按时间轴(秒)升序排序
     * 前端播放器会根据 danmuFrame 在对应时间点渲染弹幕
     */
    @Select("SELECT did, id, v_id, danmu_num, danmu_content, danmu_frame, color, create_time " +
            "FROM danmu WHERE v_id = #{vId} ORDER BY danmu_frame ASC")
    List<Danmu> selectByVideo(@Param("vId") String vId);
}
