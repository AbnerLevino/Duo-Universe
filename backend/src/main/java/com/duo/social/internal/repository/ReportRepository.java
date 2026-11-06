package com.duo.social.internal.repository;

import com.duo.social.internal.model.Report;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
