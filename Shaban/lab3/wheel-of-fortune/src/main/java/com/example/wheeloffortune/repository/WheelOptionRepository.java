package com.example.wheeloffortune.repository;

import com.example.wheeloffortune.domain.WheelOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository  // Добавьте эту аннотацию
public interface WheelOptionRepository extends JpaRepository<WheelOption, Long> {
}