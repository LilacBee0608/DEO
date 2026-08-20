package com.bili.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bili.demo.entity.VUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 * 继承 BaseMapper 即可拥有基础的 CRUD (insert/selectById/update/deleteById 等)
 * 复杂查询可通过 @Select 注解或 XML 实现
 */
@Mapper
public interface VUserMapper extends BaseMapper<VUser> {
}
