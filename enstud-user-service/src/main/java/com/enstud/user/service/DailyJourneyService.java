package com.enstud.user.service;

import com.enstud.user.dto.DailyJourneyDTO;

public interface DailyJourneyService {

    /**
     * 获取用户今日学习之旅任务状态
     */
    DailyJourneyDTO getDailyJourney(Long userId);
}
