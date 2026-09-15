package com.example.spacemuseum.repository;

import com.example.spacemuseum.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    List<Allocation> findByStudyGroupId(Long studyGroupId);

    List<Allocation> findByDeviceId(Long deviceId);

    List<Allocation> findByTimeSlotId(Long timeSlotId);

    @Query("SELECT a FROM Allocation a WHERE a.timeSlot.id = :timeSlotId AND a.status = 1")
    List<Allocation> findActiveAllocationsByTimeSlot(@Param("timeSlotId") Long timeSlotId);

    @Query("SELECT a FROM Allocation a WHERE a.studyGroup.id = :studyGroupId AND a.status = 1 ORDER BY a.timeSlot.sortOrder")
    List<Allocation> findActiveAllocationsByGroup(@Param("studyGroupId") Long studyGroupId);

    @Query("SELECT a FROM Allocation a WHERE a.studyGroup.id = :studyGroupId AND a.status = 1 ORDER BY a.timeSlot.startTime, a.device.deviceCode, a.id")
    List<Allocation> findActiveAllocationsByGroupOrderByTime(@Param("studyGroupId") Long studyGroupId);

    @Query("SELECT a FROM Allocation a WHERE a.studyGroup.visitDate = :visitDate AND a.status = 1")
    List<Allocation> findActiveAllocationsByDate(@Param("visitDate") java.time.LocalDate visitDate);

    @Query("SELECT a FROM Allocation a WHERE a.studyGroup.visitDate = :visitDate AND a.studyGroup.id = :studyGroupId AND a.status = 1")
    List<Allocation> findActiveAllocationsByDateAndGroup(@Param("visitDate") java.time.LocalDate visitDate,
                                                         @Param("studyGroupId") Long studyGroupId);

    @Query("SELECT SUM(a.studentCount) FROM Allocation a WHERE a.timeSlot.id = :timeSlotId AND a.status = 1")
    Integer sumStudentsByTimeSlot(@Param("timeSlotId") Long timeSlotId);

    @Query("SELECT a FROM Allocation a WHERE a.batchNumber = :batchNumber ORDER BY a.timeSlot.sortOrder")
    List<Allocation> findByBatchNumber(@Param("batchNumber") String batchNumber);
}