package com.example.wheeloffortune.controller;

import com.example.wheeloffortune.domain.WheelOption;
import com.example.wheeloffortune.service.WheelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wheel")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WheelController {
    private final WheelService wheelService;

    @GetMapping("/spin")
    public WheelOption spin() {
        return wheelService.spinWheel();
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return wheelService.getStats();
    }

    @GetMapping("/wheel-options")
    public List<Map<String, Object>> getWheelOptions() {
        return wheelService.getAllOptionsForWheel();
    }

    @GetMapping("/revealed")
    public List<WheelOption> getRevealedOptions() {
        return wheelService.getRevealedOptions();
    }

    @PostMapping("/complete/{id}")
    public WheelOption markAsCompleted(@PathVariable Long id) {
        return wheelService.markAsCompleted(id);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "OK",
                "service", "Anime Wheel of Fortune v2.0",
                "version", "2.0"
        );
    }
}