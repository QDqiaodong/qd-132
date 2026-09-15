package com.example.spacemuseum.repository;

import com.example.spacemuseum.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByDeviceId(Long deviceId);

    List<TimeSlot> findByDeviceIdOrderBySortOrder(Long deviceId);

    List<TimeSlot> findByDeviceIdAndAvailable(Long deviceId, Boolean available);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.device.id = :deviceId AND ts.dayOfWeek = :dayOfWeek AND ts.available = true ORDER BY ts.sortOrder")
    List<TimeSlot> findAvailableSlotsByDeviceAndDay(@Param("deviceId") Long deviceId, @Param("dayOfWeek") Integer dayOfWeek);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.device.id IN :deviceIds AND ts.dayOfWeek = :dayOfWeek AND ts.available = true ORDER BY ts.device.id, ts.sortOrder")
    List<TimeSlot> findAvailableSlotsByDevicesAndDay(@Param("deviceIds") List<Long> deviceIds, @Param("dayOfWeek") Integer dayOfWeek);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.available = true ORDER BY ts.device.id, ts.sortOrder")
    List<TimeSlot> findAllAvailableSlots();

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.dayOfWeek = :dayOfWeek AND ts.available = true ORDER BY ts.device.id, ts.sortOrder")
    List<TimeSlot> findAvailableSlotsByDay(@Param("dayOfWeek") Integer dayOfWeek);
}