package com.duo.relationships.internal.repository;

import com.duo.relationships.internal.model.Couple;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CoupleRepository extends JpaRepository<Couple, Long> {
}
