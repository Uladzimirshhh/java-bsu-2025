package com.example.wheeloffortune.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpinResultDto {
    private String text;
    private String color;
    private String icon;
    private String animeCharacter;
    private String genre;
    private String message;

    public SpinResultDto(String text, String color, String icon,
                         String animeCharacter, String genre) {
        this.text = text;
        this.color = color;
        this.icon = icon;
        this.animeCharacter = animeCharacter;
        this.genre = genre;
        this.message = "Удачи!";
    }
}