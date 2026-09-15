package com.example.spacemuseum.service;

import com.example.spacemuseum.dto.DeviceDTO;
import com.example.spacemuseum.dto.TimeSlotDTO;
import com.example.spacemuseum.entity.Device;
import com.example.spacemuseum.entity.DeviceInventory;
import com.example.spacemuseum.entity.TimeSlot;
import com.example.spacemuseum.repository.DeviceInventoryRepository;
import com.example.spacemuseum.repository.DeviceRepository;
import com.example.spacemuseum.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private static final String DEVICE_AGE_KEY = "device:age:";

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private DeviceInventoryRepository deviceInventoryRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public Device createDevice(DeviceDTO dto) {
        Device device = new Device();
        device.setDeviceCode(dto.getDeviceCode());
        device.setDeviceName(dto.getDeviceName());
        device.setDescription(dto.getDescription());
        device.setExperienceDuration(dto.getExperienceDuration());
        device.setMinAge(dto.getMinAge());
        device.setMaxAge(dto.getMaxAge());
        device.setCapacity(dto.getCapacity());
        device.setStatus(dto.getStatus());
        
        Device saved = deviceRepository.save(device);
        
        if (dto.getTimeSlots() != null && !dto.getTimeSlots().isEmpty()) {
            for (TimeSlotDTO slotDTO : dto.getTimeSlots()) {
                TimeSlot slot = new TimeSlot();
                slot.setDevice(saved);
                slot.setStartTime(slotDTO.getStartTime());
                slot.setEndTime(slotDTO.getEndTime());
                slot.setDayOfWeek(slotDTO.getDayOfWeek());
                slot.setAvailable(true);
                slot.setSortOrder(slotDTO.getSortOrder());
                timeSlotRepository.save(slot);
            }
        }

        cacheDeviceAge(saved);

        // 新设备同步开出盘点台账行：应出/实到初始为 0/空，不带差异标记
        DeviceInventory inventory = new DeviceInventory();
        inventory.setDevice(saved);
        inventory.setExpectedParts(0);
        inventory.setActualParts(null);
        inventory.setMismatch(false);
        inventory.setDifferenceCount(null);
        deviceInventoryRepository.save(inventory);

        return saved;
    }

    @Transactional
    public Device updateDevice(Long id, DeviceDTO dto) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备不存在"));
        
        device.setDeviceCode(dto.getDeviceCode());
        device.setDeviceName(dto.getDeviceName());
        device.setDescription(dto.getDescription());
        device.setExperienceDuration(dto.getExperienceDuration());
        device.setMinAge(dto.getMinAge());
        device.setMaxAge(dto.getMaxAge());
        device.setCapacity(dto.getCapacity());
        device.setStatus(dto.getStatus());
        
        Device saved = deviceRepository.save(device);
        cacheDeviceAge(saved);
        return saved;
    }

    @Transactional
    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备不存在"));
        
        List<TimeSlot> slots = timeSlotRepository.findByDeviceId(id);
        timeSlotRepository.deleteAll(slots);
        // 设备删除时一并清掉盘点台账行
        deviceInventoryRepository.findByDeviceId(id).ifPresent(deviceInventoryRepository::delete);
        deviceRepository.delete(device);
        
        clearDeviceCache(id);
    }

    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id);
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public List<Device> getActiveDevices() {
        return deviceRepository.findActiveDevices();
    }

    public List<Device> getSuitableDevices(Integer age) {
        List<Device> cached = getSuitableDevicesFromCache(age);
        if (!cached.isEmpty()) {
            return cached;
        }
        List<Device> devices = deviceRepository.findSuitableDevices(age);
        cacheSuitableDevices(age, devices);
        return devices;
    }

    @Transactional
    public void addTimeSlot(Long deviceId, TimeSlotDTO dto) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("设备不存在"));
        
        TimeSlot slot = new TimeSlot();
        slot.setDevice(device);
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setDayOfWeek(dto.getDayOfWeek());
        slot.setAvailable(true);
        slot.setSortOrder(dto.getSortOrder());
        timeSlotRepository.save(slot);
    }

    @Transactional
    public void updateTimeSlot(Long id, TimeSlotDTO dto) {
        TimeSlot slot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("时段不存在"));
        
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setDayOfWeek(dto.getDayOfWeek());
        slot.setAvailable(dto.getAvailable());
        slot.setSortOrder(dto.getSortOrder());
        timeSlotRepository.save(slot);
    }

    @Transactional
    public void deleteTimeSlot(Long id) {
        timeSlotRepository.deleteById(id);
    }

    public List<TimeSlot> getTimeSlotsByDevice(Long deviceId) {
        return timeSlotRepository.findByDeviceIdOrderBySortOrder(deviceId);
    }

    private void cacheDeviceAge(Device device) {
        for (int age = device.getMinAge(); age <= device.getMaxAge(); age++) {
            String key = DEVICE_AGE_KEY + age;
            redisTemplate.opsForSet().add(key, device.getId());
        }
    }

    private void clearDeviceCache(Long deviceId) {
        for (int age = 3; age <= 18; age++) {
            String key = DEVICE_AGE_KEY + age;
            redisTemplate.opsForSet().remove(key, deviceId);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Device> getSuitableDevicesFromCache(Integer age) {
        String key = DEVICE_AGE_KEY + age;
        List<Object> deviceIds = (List<Object>) redisTemplate.opsForSet().members(key);
        if (deviceIds == null || deviceIds.isEmpty()) {
            return List.of();
        }
        List<Long> ids = deviceIds.stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
        return deviceRepository.findAllById(ids).stream()
                .filter(d -> d.getStatus() == 1)
                .collect(Collectors.toList());
    }

    private void cacheSuitableDevices(Integer age, List<Device> devices) {
        String key = DEVICE_AGE_KEY + age;
        devices.forEach(d -> redisTemplate.opsForSet().add(key, d.getId()));
    }
}