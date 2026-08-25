package com.bili.demo.service;

import java.util.List;
import java.util.Map;

/**
 * 观看历史服务接口
 * 提供记录观看历史和查询观看历史列表功能
 */
public interface HistoryService {

    /**
     * 记录观看历史(去重: 重复观看更新时间)
     * @param userId 用户id
     * @param vId    视频id
     */
    void recordWatch(Integer userId, String vId);

    /**
     * 查询用户观看历史列表(含视频信息)
     * @param userId 用户id
     * @return 观看历史列表,每条含视频信息和观看时间
     */
    List<Map<String, Object>> getHistoryList(Integer userId);

    /**
     * 清空用户观看历史
     * @param userId 用户id
     */
    void clearHistory(Integer userId);
}
