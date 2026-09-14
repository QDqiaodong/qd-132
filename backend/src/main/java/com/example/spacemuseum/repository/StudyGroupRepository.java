package com.example.spacemuseum.repository;

import com.example.spacemuseum.entity.StudyGroup;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    Optional<StudyGroup> findByGroupCode(String groupCode);

    /** 悲观写锁读取，用于自动分配时串行化同一研学团的并发提交 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM StudyGroup g WHERE g.id = :id")
    Optional<StudyGroup> findByIdForUpdate(@Param("id") Long id);

    List<StudyGroup> findByStatus(Integer status);

    List<StudyGroup> findByVisitDate(LocalDate visitDate);

    List<StudyGroup> findByVisitDateBetween(LocalDate startDate, LocalDate endDate);

    List<StudyGroup> findByVisitDateAndStatus(LocalDate visitDate, Integer status);
}