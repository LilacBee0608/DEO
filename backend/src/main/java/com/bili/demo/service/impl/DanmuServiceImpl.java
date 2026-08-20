package com.bili.demo.service.impl;

import com.bili.demo.dto.DanmuSendDTO;
import com.bili.demo.entity.Danmu;
import com.bili.demo.mapper.DanmuMapper;
import com.bili.demo.service.DanmuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 弹幕服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DanmuServiceImpl implements DanmuService {

    private final DanmuMapper danmuMapper;

    @Override
    public List<Danmu> listByVideo(String vId) {
        return danmuMapper.selectByVideo(vId);
    }

    @Override
    public Integer send(DanmuSendDTO dto, Integer userId) {
        Danmu danmu = new Danmu();
        danmu.setId(String.valueOf(userId));
        danmu.setVId(dto.getVId());
        danmu.setDanmuContent(dto.getDanmuContent());
        danmu.setDanmuFrame(dto.getDanmuFrame());
        // 颜色为空则默认白色
        danmu.setColor(dto.getColor() != null ? dto.getColor() : "#FFFFFF");
        danmuMapper.insert(danmu);
        log.info("弹幕发送成功: did={}, vId={}, userId={}", danmu.getDid(), dto.getVId(), userId);
        return danmu.getDid();
    }
}
