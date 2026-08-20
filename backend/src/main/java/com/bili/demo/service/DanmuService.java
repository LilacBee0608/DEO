package com.bili.demo.service;

import com.bili.demo.dto.DanmuSendDTO;
import com.bili.demo.entity.Danmu;

import java.util.List;

/**
 * 弹幕服务接口
 */
public interface DanmuService {

    /** 查询某视频的全部弹幕(按时间轴排序,游客可见) */
    List<Danmu> listByVideo(String vId);

    /** 发送弹幕(需登录) */
    Integer send(DanmuSendDTO dto, Integer userId);
}
