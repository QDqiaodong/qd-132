package com.example.spacemuseum.repository;

import com.example.spacemuseum.entity.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    Optional<StudyGroup> findByGroupCode(String groupCode);

    List<StudyGroup> findByStatus(Integer status);

    List<StudyGroup> findByVisitDate(LocalDate visitDate);

    List<StudyGroup> findByVisitDateBetween(LocalDate startDate, LocalDate endDate);
}