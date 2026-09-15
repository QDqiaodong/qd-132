package com.example.spacemuseum.config;

import com.example.spacemuseum.entity.Device;
import com.example.spacemuseum.entity.DeviceInventory;
import com.example.spacemuseum.entity.StudyGroup;
import com.example.spacemuseum.entity.TimeSlot;
import com.example.spacemuseum.repository.DeviceInventoryRepository;
import com.example.spacemuseum.repository.DeviceRepository;
import com.example.spacemuseum.repository.StudyGroupRepository;
import com.example.spacemuseum.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private DeviceInventoryRepository deviceInventoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (deviceRepository.count() == 0) {
            Device device1 = new Device();
            device1.setDeviceCode("DEV001");
            device1.setDeviceName("航天模拟器");
            device1.setDescription("模拟太空飞行体验设备");
            device1.setExperienceDuration(30);
            device1.setMinAge(10);
            device1.setMaxAge(18);
            device1.setCapacity(4);
            device1.setStatus(1);
            deviceRepository.save(device1);

            Device device2 = new Device();
            device2.setDeviceCode("DEV002");
            device2.setDeviceName("星球模型展示");
            device2.setDescription("太阳系行星模型互动展示");
            device2.setExperienceDuration(20);
            device2.setMinAge(6);
            device2.setMaxAge(12);
            device2.setCapacity(10);
            device2.setStatus(1);
            deviceRepository.save(device2);

            Device device3 = new Device();
            device3.setDeviceCode("DEV003");
            device3.setDeviceName("火箭发射体验");
            device3.setDescription("模拟火箭发射过程");
            device3.setExperienceDuration(25);
            device3.setMinAge(8);
            device3.setMaxAge(16);
            device3.setCapacity(6);
            device3.setStatus(1);
            deviceRepository.save(device3);

            Device device4 = new Device();
            device4.setDeviceCode("DEV004");
            device4.setDeviceName("空间站模型");
            device4.setDescription("国际空间站缩比模型展示");
            device4.setExperienceDuration(15);
            device4.setMinAge(5);
            device4.setMaxAge(14);
            device4.setCapacity(8);
            device4.setStatus(1);
            deviceRepository.save(device4);

            Device device5 = new Device();
            device5.setDeviceCode("DEV005");
            device5.setDeviceName("太空行走体验");
            device5.setDescription("模拟宇航员太空行走");
            device5.setExperienceDuration(20);
            device5.setMinAge(12);
            device5.setMaxAge(18);
            device5.setCapacity(2);
            device5.setStatus(1);
            deviceRepository.save(device5);

            // 盘点台账演示数据：每台设备一行，包含相符与盘点不符两种情形
            int[][] counts = {
                    {8, 8},   // DEV001 应出 8 实到 8，相符
                    {12, 10}, // DEV002 应出 12 实到 10，少 2 件，盘点不符
                    {6, 6},   // DEV003 相符
                    {10, 11}, // DEV004 多 1 件，盘点不符
                    {4, 4}    // DEV005 相符
            };
            int idx = 0;
            List<Device> seededDevices = deviceRepository.findAll(
                    org.springframework.data.domain.Sort.by("deviceCode"));
            for (Device device : seededDevices) {
                int expected = counts[idx][0];
                int actual = counts[idx][1];
                DeviceInventory inventory = new DeviceInventory();
                inventory.setDevice(device);
                inventory.setExpectedParts(expected);
                inventory.setActualParts(actual);
                inventory.setMismatch(expected != actual);
                inventory.setDifferenceCount(actual - expected);
                inventory.setInventoryTime(LocalDateTime.now());
                inventory.setRemark(expected != actual ? "盘点时发现配件件数与出库登记不一致" : "配件齐套");
                deviceInventoryRepository.save(inventory);
                idx++;
            }
        }

        if (timeSlotRepository.count() == 0) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            deviceRepository.findAll().forEach(device -> {
                String[] times = {"09:00", "10:00", "11:00", "14:00", "15:00", "16:00"};
                for (int i = 0; i < times.length; i++) {
                    TimeSlot slot = new TimeSlot();
                    slot.setDevice(device);
                    slot.setStartTime(LocalTime.parse(times[i], formatter));
                    slot.setEndTime(LocalTime.parse(times[i].replace("00", "30"), formatter));
                    slot.setDayOfWeek(1);
                    slot.setAvailable(true);
                    slot.setSortOrder(i + 1);
                    timeSlotRepository.save(slot);
                }
            });
        }

        if (studyGroupRepository.count() == 0) {
            StudyGroup group1 = new StudyGroup();
            group1.setGroupCode("GRP001");
            group1.setGroupName("阳光小学五年级");
            group1.setSchoolName("阳光小学");
            group1.setContactPerson("张老师");
            group1.setContactPhone("13800138001");
            group1.setTotalStudents(45);
            group1.setAverageAge(11);
            group1.setVisitDate(LocalDate.now());
            group1.setStatus(1);
            group1.setRemark("希望体验航天模拟器");
            studyGroupRepository.save(group1);

            StudyGroup group2 = new StudyGroup();
            group2.setGroupCode("GRP002");
            group2.setGroupName("明德中学初二");
            group2.setSchoolName("明德中学");
            group2.setContactPerson("李老师");
            group2.setContactPhone("13800138002");
            group2.setTotalStudents(30);
            group2.setAverageAge(14);
            group2.setVisitDate(LocalDate.now().plusDays(1));
            group2.setStatus(1);
            group2.setRemark("集体预约");
            studyGroupRepository.save(group2);
        }
    }
}