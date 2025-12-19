package com.example.wheeloffortune.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wheel_options")
@Data
@NoArgsConstructor
public class WheelOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;           // Текст задания
    private String color;          // Цвет в колесе
    private String icon;           // Иконка emoji
    private Integer weight = 1;    // Вес/редкость (1-5)
    private String animeCharacter; // Аниме персонаж
    private String genre;          // Жанр
    private String emoji;          // Уникальный emoji для идентификации

    // Новые поля
    private Boolean secret = true;    // Секретное ли задание
    private Boolean completed = false; // Выполнено ли

    public WheelOption(String text, String color, String icon, String emoji,
                       Integer weight, String animeCharacter, String genre,
                       Boolean secret) {
        this.text = text;
        this.color = color;
        this.icon = icon;
        this.emoji = emoji;
        this.weight = weight;
        this.animeCharacter = animeCharacter;
        this.genre = genre;
        this.secret = secret;
        this.completed = false;
    }

    // Метод для получения отображаемого текста
    public String getDisplayText() {
        return secret ? "??? " + icon : text;
    }

    // Метод для получения цвета для отображения
    public String getDisplayColor() {
        return secret ? "#2d3748" : color; // Темный цвет для секретных
    }
}