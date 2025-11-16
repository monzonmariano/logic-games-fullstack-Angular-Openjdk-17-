package com.logicgames.api.game;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface GameMetricRepository extends JpaRepository<GameMetric, Long> {

    // "Cuenta cuántos eventos de un tipo ocurrieron DESPUÉS de una fecha"
    long countByEventTypeAndEventTimestampAfter(String eventType, LocalDateTime cutOffDate);
}
