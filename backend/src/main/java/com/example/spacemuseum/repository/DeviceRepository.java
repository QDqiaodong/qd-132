package com.example.spacemuseum.repository;

import com.example.spacemuseum.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceCode(String deviceCode);

    List<Device> findByStatus(Integer status);

    @Query("SELECT d FROM Device d WHERE d.minAge <= :age AND d.maxAge >= :age AND d.status = 1")
    List<Device> findSuitableDevices(@Param("age") Integer age);

    @Query("SELECT d FROM Device d WHERE d.status = 1")
    List<Device> findActiveDevices();
}