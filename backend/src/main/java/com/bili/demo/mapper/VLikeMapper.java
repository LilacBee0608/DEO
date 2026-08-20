package com.bili.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bili.demo.entity.VLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频点赞 Mapper 接口
 * 联合主键场景,主要使用 selectByMap / deleteByMap / insert
 */
@Mapper
public interface VLikeMapper extends BaseMapper<VLike> {
}
