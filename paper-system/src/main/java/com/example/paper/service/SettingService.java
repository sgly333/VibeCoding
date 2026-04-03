package com.example.paper.service;

import com.example.paper.entity.AppSetting;
import com.example.paper.repository.AppSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingService {

    private static final String KEY_RESEARCH_DIRECTION = "research_direction";

    private final AppSettingRepository appSettingRepository;

    public SettingService(AppSettingRepository appSettingRepository) {
        this.appSettingRepository = appSettingRepository;
    }

    @Transactional(readOnly = true)
    public String getResearchDirection() {
        return appSettingRepository.findBySettingKey(KEY_RESEARCH_DIRECTION)
                .map(AppSetting::getSettingValue)
                .orElse("");
    }

    @Transactional
    public String setResearchDirection(String researchDirection) {
        String value = researchDirection == null ? "" : researchDirection.trim();
        AppSetting setting = appSettingRepository.findBySettingKey(KEY_RESEARCH_DIRECTION)
                .orElseGet(() -> {
                    AppSetting s = new AppSetting();
                    s.setSettingKey(KEY_RESEARCH_DIRECTION);
                    return s;
                });
        setting.setSettingValue(value);
        appSettingRepository.save(setting);
        return value;
    }
}

