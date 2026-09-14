package com.example.spacemuseum.service;

import com.example.spacemuseum.dto.AllocationDTO;
import com.example.spacemuseum.dto.AllocationResultDTO;
import com.example.spacemuseum.dto.GroupAllocationDTO;
import com.example.spacemuseum.entity.Allocation;
import com.example.spacemuseum.entity.Device;
import com.example.spacemuseum.entity.StudyGroup;
import com.example.spacemuseum.entity.TimeSlot;
import com.example.spacemuseum.repository.AllocationRepository;
import com.example.spacemuseum.repository.DeviceRepository;
import com.example.spacemuseum.repository.StudyGroupRepository;
import com.example.spacemuseum.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AllocationService {

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<AllocationResultDTO> autoAllocate(Long studyGroupId) {
        // 悲观锁串行化同一研学团的并发分配：连点或重复提交时，后到的请求等待前一事务提交，
        // 只会看到并替换已生效的分配，不会在台账留下重复记录
        StudyGroup group = studyGroupRepository.findByIdForUpdate(studyGroupId)
                .orElseThrow(() -> new RuntimeException("研学团不存在"));

        // 带队老师、联系电话为自动分配前置必填项，缺项时明确提示缺哪一项；
        // 校验先于任何写操作，且同事务回滚保证未补全的分配不会进入台账
        List<String> missingFields = new ArrayList<>();
        if (group.getContactPerson() == null || group.getContactPerson().isBlank()) {
            missingFields.add("带队老师");
        }
        if (group.getContactPhone() == null || group.getContactPhone().isBlank()) {
            missingFields.add("联系电话");
        }
        if (!missingFields.isEmpty()) {
            throw new RuntimeException("该研学团未填写" + String.join("和", missingFields) + "，请先补全后再提交自动分配");
        }

        cancelExistingAllocations(studyGroupId);

        List<Device> suitableDevices = deviceRepository.findSuitableDevices(group.getAverageAge());
        if (suitableDevices.isEmpty()) {
            throw new RuntimeException("没有适合该年龄段的设备");
        }

        int dayOfWeek = group.getVisitDate().getDayOfWeek().getValue();
        List<TimeSlot> availableSlots = timeSlotRepository.findAvailableSlotsByDevicesAndDay(
                suitableDevices.stream().map(Device::getId).collect(Collectors.toList()),
                dayOfWeek
        );

        Map<Device, List<TimeSlot>> deviceSlotsMap = availableSlots.stream()
                .collect(Collectors.groupingBy(TimeSlot::getDevice));

        List<AllocationResultDTO> results = new ArrayList<>();
        int remainingStudents = group.getTotalStudents();
        int deviceIndex = 0;

        String batchNumber = generateBatchNumber(group.getGroupCode());

        while (remainingStudents > 0 && deviceIndex < suitableDevices.size()) {
            Device device = suitableDevices.get(deviceIndex);
            List<TimeSlot> slots = deviceSlotsMap.getOrDefault(device, List.of());
            
            if (!slots.isEmpty()) {
                for (TimeSlot slot : slots) {
                    if (remainingStudents <= 0) break;
                    
                    Integer allocated = allocationRepository.sumStudentsByTimeSlot(slot.getId());
                    allocated = allocated == null ? 0 : allocated;
                    
                    int availableCapacity = device.getCapacity() - allocated;
                    if (availableCapacity > 0) {
                        int studentsToAllocate = Math.min(availableCapacity, remainingStudents);
                        
                        Allocation allocation = new Allocation();
                        allocation.setStudyGroup(group);
                        allocation.setDevice(device);
                        allocation.setTimeSlot(slot);
                        allocation.setStudentCount(studentsToAllocate);
                        allocation.setAllocationTime(LocalDateTime.now());
                        allocation.setStatus(1);
                        allocation.setBatchNumber(batchNumber);
                        
                        allocationRepository.save(allocation);
                        
                        results.add(toAllocationResultDTO(allocation));
                        remainingStudents -= studentsToAllocate;
                    }
                }
            }
            
            deviceIndex++;
        }

        if (remainingStudents > 0) {
            throw new RuntimeException("无法分配所有学生，剩余 " + remainingStudents + " 人");
        }

        return results;
    }

    @Transactional
    public Allocation manualAllocate(AllocationDTO dto) {
        StudyGroup group = studyGroupRepository.findById(dto.getStudyGroupId())
                .orElseThrow(() -> new RuntimeException("研学团不存在"));
        Device device = deviceRepository.findById(dto.getDeviceId())
                .orElseThrow(() -> new RuntimeException("设备不存在"));
        TimeSlot slot = timeSlotRepository.findById(dto.getTimeSlotId())
                .orElseThrow(() -> new RuntimeException("时段不存在"));

        Integer allocated = allocationRepository.sumStudentsByTimeSlot(dto.getTimeSlotId());
        allocated = allocated == null ? 0 : allocated;
        
        if (allocated + dto.getStudentCount() > device.getCapacity()) {
            throw new RuntimeException("该时段容量不足");
        }

        Allocation allocation = new Allocation();
        allocation.setStudyGroup(group);
        allocation.setDevice(device);
        allocation.setTimeSlot(slot);
        allocation.setStudentCount(dto.getStudentCount());
        allocation.setAllocationTime(LocalDateTime.now());
        allocation.setStatus(dto.getStatus());
        allocation.setBatchNumber(generateBatchNumber(group.getGroupCode()));
        
        return allocationRepository.save(allocation);
    }

    @Transactional
    public void cancelAllocation(Long allocationId) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new RuntimeException("分配记录不存在"));
        allocation.setStatus(0);
        allocationRepository.save(allocation);
    }

    @Transactional
    public void cancelExistingAllocations(Long studyGroupId) {
        List<Allocation> allocations = allocationRepository.findActiveAllocationsByGroup(studyGroupId);
        allocations.forEach(a -> a.setStatus(0));
        allocationRepository.saveAll(allocations);
    }

    @Transactional
    public Allocation updateAllocation(Long id, AllocationDTO dto) {
        Allocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分配记录不存在"));

        if (!allocation.getStudyGroup().getId().equals(dto.getStudyGroupId())) {
            StudyGroup group = studyGroupRepository.findById(dto.getStudyGroupId())
                    .orElseThrow(() -> new RuntimeException("研学团不存在"));
            allocation.setStudyGroup(group);
        }

        if (!allocation.getDevice().getId().equals(dto.getDeviceId())) {
            Device device = deviceRepository.findById(dto.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("设备不存在"));
            allocation.setDevice(device);
        }

        if (!allocation.getTimeSlot().getId().equals(dto.getTimeSlotId())) {
            TimeSlot slot = timeSlotRepository.findById(dto.getTimeSlotId())
                    .orElseThrow(() -> new RuntimeException("时段不存在"));
            allocation.setTimeSlot(slot);
        }

        allocation.setStudentCount(dto.getStudentCount());
        allocation.setStatus(dto.getStatus());
        
        return allocationRepository.save(allocation);
    }

    public List<Allocation> getAllAllocations() {
        return allocationRepository.findAll();
    }

    public List<Allocation> getAllocationsByGroup(Long studyGroupId) {
        return allocationRepository.findActiveAllocationsByGroup(studyGroupId);
    }

    public GroupAllocationDTO getGroupAllocationDetail(Long studyGroupId) {
        StudyGroup group = studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> new RuntimeException("研学团不存在"));

        List<Allocation> allocations = allocationRepository.findActiveAllocationsByGroup(studyGroupId);
        List<AllocationResultDTO> resultDTOs = allocations.stream()
                .map(this::toAllocationResultDTO)
                .collect(Collectors.toList());

        GroupAllocationDTO dto = new GroupAllocationDTO();
        dto.setGroupId(group.getId());
        dto.setGroupCode(group.getGroupCode());
        dto.setGroupName(group.getGroupName());
        dto.setSchoolName(group.getSchoolName());
        dto.setTotalStudents(group.getTotalStudents());
        dto.setAverageAge(group.getAverageAge());
        dto.setVisitDate(group.getVisitDate());
        dto.setAllocations(resultDTOs);

        return dto;
    }

    public List<GroupAllocationDTO> getAllGroupAllocations() {
        List<StudyGroup> groups = studyGroupRepository.findByStatus(1);
        return groups.stream()
                .map(g -> getGroupAllocationDetail(g.getId()))
                .collect(Collectors.toList());
    }

    public List<Allocation> getAllocationsByDate(java.time.LocalDate visitDate) {
        return allocationRepository.findActiveAllocationsByDate(visitDate);
    }

    public List<Allocation> getAllocationsByDateAndGroup(java.time.LocalDate visitDate, Long studyGroupId) {
        return allocationRepository.findActiveAllocationsByDateAndGroup(visitDate, studyGroupId);
    }

    private String generateBatchNumber(String groupCode) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "BATCH_" + groupCode + "_" + timestamp;
    }

    private AllocationResultDTO toAllocationResultDTO(Allocation allocation) {
        AllocationResultDTO dto = new AllocationResultDTO();
        dto.setAllocationId(allocation.getId());
        dto.setDeviceId(allocation.getDevice().getId());
        dto.setDeviceCode(allocation.getDevice().getDeviceCode());
        dto.setDeviceName(allocation.getDevice().getDeviceName());
        dto.setTimeSlotId(allocation.getTimeSlot().getId());
        dto.setStartTime(allocation.getTimeSlot().getStartTime());
        dto.setEndTime(allocation.getTimeSlot().getEndTime());
        dto.setStudentCount(allocation.getStudentCount());
        dto.setBatchNumber(allocation.getBatchNumber());
        return dto;
    }
}