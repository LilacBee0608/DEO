package com.bili.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bili.demo.entity.History;
import com.bili.demo.mapper.HistoryMapper;
import com.bili.demo.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 观看历史服务实现
 * - recordWatch: 利用 ON DUPLICATE KEY UPDATE 去重,重复观看更新时间
 * - getHistoryList: 联表查询视频信息和作者名
 * - clearHistory: 清空用户观看历史
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryMapper historyMapper;

    @Override
    public void recordWatch(Integer userId, String vId) {
        // upsertWatch 内部使用 INSERT ... ON DUPLICATE KEY UPDATE
        // 依赖 history 表的 UNIQUE 约束 (user_id, v_id) 保证去重
        historyMapper.upsertWatch(userId, vId);
        log.info("观看历史已记录: userId={}, vId={}", userId, vId);
    }

    @Override
    public List<Map<String, Object>> getHistoryList(Integer userId) {
        // 联表查询返回视频信息 + 观看时间,按观看时间倒序
        return historyMapper.selectHistoryList(userId);
    }

    @Override
    public void clearHistory(Integer userId) {
        historyMapper.delete(new LambdaQueryWrapper<History>()
                .eq(History::getUserId, userId));
        log.info("观看历史已清空: userId={}", userId);
    }
}
