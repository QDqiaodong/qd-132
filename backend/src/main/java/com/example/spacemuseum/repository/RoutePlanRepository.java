package com.example.spacemuseum.repository;

import com.example.spacemuseum.entity.RoutePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoutePlanRepository extends JpaRepository<RoutePlan, Long> {

    Optional<RoutePlan> findByStudyGroupId(Long studyGroupId);
}
