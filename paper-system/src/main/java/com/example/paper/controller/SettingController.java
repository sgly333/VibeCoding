package com.example.paper.controller;

import com.example.paper.dto.ResearchDirectionRequest;
import com.example.paper.dto.ResearchDirectionResponse;
import com.example.paper.service.SettingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping("/research-direction")
    public ResearchDirectionResponse getResearchDirection() {
        return new ResearchDirectionResponse(settingService.getResearchDirection());
    }

    @PutMapping("/research-direction")
    public ResearchDirectionResponse setResearchDirection(@RequestBody ResearchDirectionRequest request) {
        String saved = settingService.setResearchDirection(request == null ? "" : request.getResearchDirection());
        return new ResearchDirectionResponse(saved);
    }
}

