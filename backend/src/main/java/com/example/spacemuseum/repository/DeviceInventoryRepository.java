package com.example.spacemuseum.repository;

import com.example.spacemuseum.entity.Device;
import com.example.spacemuseum.entity.DeviceInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceInventoryRepository extends JpaRepository<DeviceInventory, Long> {

    Optional<DeviceInventory> findByDeviceId(Long deviceId);

    @Query("SELECT i FROM DeviceInventory i JOIN FETCH i.device ORDER BY i.device.deviceCode")
    List<DeviceInventory> findAllWithDevice();

    @Query("SELECT d FROM Device d WHERE d NOT IN (SELECT i.device FROM DeviceInventory i)")
    List<Device> findDevicesWithoutInventory();
}
