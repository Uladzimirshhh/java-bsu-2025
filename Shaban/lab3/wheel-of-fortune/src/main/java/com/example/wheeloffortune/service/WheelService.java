package com.example.wheeloffortune.service;

import com.example.wheeloffortune.domain.WheelOption;
import com.example.wheeloffortune.repository.WheelOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WheelService {
    private final WheelOptionRepository repository;
    private final Random random = new Random();

    @Transactional
    public WheelOption spinWheel() {
        List<WheelOption> options = repository.findAll();
        if (options.isEmpty()) {
            return createDefaultOption();
        }

        // Получаем случайный вариант с учетом веса
        WheelOption selected = getRandomOptionWeighted(options);

        // Открываем задание
        selected.setSecret(false);
        repository.save(selected);

        return selected;
    }

    @Transactional
    public WheelOption markAsCompleted(Long id) {
        WheelOption option = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задание не найдено"));

        option.setCompleted(true);
        return repository.save(option);
    }

    // Получить статистику
    public Map<String, Object> getStats() {
        List<WheelOption> allOptions = repository.findAll();
        long total = allOptions.size();
        long completed = allOptions.stream()
                .filter(WheelOption::getCompleted)
                .count();
        long revealed = allOptions.stream()
                .filter(opt -> !opt.getSecret())
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("completed", completed);
        stats.put("revealed", revealed);
        stats.put("completionPercentage", total > 0 ? (completed * 100) / total : 0);
        stats.put("revealedPercentage", total > 0 ? (revealed * 100) / total : 0);

        return stats;
    }

    // Получить все задания (для отображения колеса)
    public List<Map<String, Object>> getAllOptionsForWheel() {
        return repository.findAll().stream()
                .map(opt -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", opt.getId());
                    map.put("displayText", opt.getDisplayText());
                    map.put("displayColor", opt.getDisplayColor());
                    map.put("text", opt.getText());
                    map.put("color", opt.getColor());
                    map.put("icon", opt.getIcon());
                    map.put("secret", opt.getSecret());
                    map.put("completed", opt.getCompleted());
                    map.put("emoji", opt.getEmoji());
                    map.put("weight", opt.getWeight());
                    map.put("animeCharacter", opt.getAnimeCharacter());
                    map.put("genre", opt.getGenre());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Получить открытые задания для галереи
    public List<WheelOption> getRevealedOptions() {
        return repository.findAll().stream()
                .filter(opt -> !opt.getSecret())
                .collect(Collectors.toList());
    }

    private WheelOption getRandomOptionWeighted(List<WheelOption> options) {
        // Учитываем вес для вероятности выпадения
        int totalWeight = options.stream()
                .mapToInt(WheelOption::getWeight)
                .sum();
        int randomWeight = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (WheelOption option : options) {
            currentWeight += option.getWeight();
            if (randomWeight < currentWeight) {
                return option;
            }
        }
        return options.get(0);
    }

    private WheelOption createDefaultOption() {
        WheelOption option = new WheelOption();
        option.setText("Колесо пустое!");
        option.setColor("#808080");
        option.setIcon("❓");
        option.setSecret(false);
        return option;
    }
}