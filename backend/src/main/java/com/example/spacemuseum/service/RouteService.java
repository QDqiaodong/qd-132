package com.example.spacemuseum.service;

import com.example.spacemuseum.dto.RoutePlanDTO;
import com.example.spacemuseum.dto.RoutePlanRequest;
import com.example.spacemuseum.dto.RouteStopDTO;
import com.example.spacemuseum.entity.Device;
import com.example.spacemuseum.entity.RoutePlan;
import com.example.spacemuseum.entity.RouteStop;
import com.example.spacemuseum.entity.StudyGroup;
import com.example.spacemuseum.repository.DeviceRepository;
import com.example.spacemuseum.repository.RoutePlanRepository;
import com.example.spacemuseum.repository.RouteStopRepository;
import com.example.spacemuseum.repository.StudyGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 体验动线服务：馆务为每个研学团编排带站序的动线；
 * 放行严格按站序推进——上一站未完成时，下一站一律拦住不放行。
 */
@Service
public class RouteService {

    @Autowired
    private RoutePlanRepository routePlanRepository;

    @Autowired
    private RouteStopRepository routeStopRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    /** 全部研学团的动线（未编排的团也会返回，stops 为空），站点始终按站序排列。 */
    @Transactional(readOnly = true)
    public List<RoutePlanDTO> getAllRoutes() {
        Map<Long, RoutePlan> planByGroup = routePlanRepository.findAll().stream()
                .collect(Collectors.toMap(p -> p.getStudyGroup().getId(), Function.identity()));
        return studyGroupRepository.findByStatus(1).stream()
                .map(group -> toRoutePlanDTO(planByGroup.get(group.getId()), group))
                .collect(Collectors.toList());
    }

    /** 单个研学团的动线详情，站点按站序排列。 */
    @Transactional(readOnly = true)
    public RoutePlanDTO getGroupRoute(Long studyGroupId) {
        StudyGroup group = studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> new RuntimeException("研学团不存在"));
        RoutePlan plan = routePlanRepository.findByStudyGroupId(studyGroupId).orElse(null);
        return toRoutePlanDTO(plan, group);
    }

    /**
     * 编排（或重新编排）动线：按请求中的设备顺序写入站序。
     * 重新编排会清空原有站点，放行与完成进度随之重置。
     */
    @Transactional
    public RoutePlanDTO saveRoute(Long studyGroupId, RoutePlanRequest request) {
        StudyGroup group = studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> new RuntimeException("研学团不存在"));

        List<Long> deviceIds = request.getDeviceIds();
        Set<Long> uniqueIds = new HashSet<>(deviceIds);
        if (uniqueIds.size() != deviceIds.size()) {
            throw new RuntimeException("同一设备在动线中重复出现");
        }
        Map<Long, Device> deviceById = deviceRepository.findAllById(deviceIds).stream()
                .collect(Collectors.toMap(Device::getId, Function.identity()));
        if (deviceById.size() != uniqueIds.size()) {
            throw new RuntimeException("动线中包含不存在的设备");
        }

        RoutePlan plan = routePlanRepository.findByStudyGroupId(studyGroupId).orElseGet(() -> {
            RoutePlan p = new RoutePlan();
            p.setStudyGroup(group);
            p.setCreatedTime(LocalDateTime.now());
            return p;
        });
        plan.setRouteName(StringUtils.hasText(request.getRouteName()) ? request.getRouteName().trim() : "参观动线");
        plan.setUpdatedTime(LocalDateTime.now());

        plan.getStops().clear();
        int order = 1;
        for (Long deviceId : deviceIds) {
            RouteStop stop = new RouteStop();
            stop.setRoutePlan(plan);
            stop.setDevice(deviceById.get(deviceId));
            stop.setStopOrder(order++);
            stop.setReleased(false);
            stop.setCompleted(false);
            plan.getStops().add(stop);
        }

        return toRoutePlanDTO(routePlanRepository.save(plan), group);
    }

    /**
     * 放行进入某站。漏站或颠倒站序一律拦住：
     * 站序在前的站点必须全部完成，否则拒绝放行。
     */
    @Transactional
    public RouteStopDTO releaseStop(Long stopId) {
        RouteStop stop = routeStopRepository.findById(stopId)
                .orElseThrow(() -> new RuntimeException("动线站点不存在"));

        if (Boolean.TRUE.equals(stop.getCompleted())) {
            throw new RuntimeException("该站已完成体验，无需放行");
        }
        if (Boolean.TRUE.equals(stop.getReleased())) {
            throw new RuntimeException("该站已放行，请勿重复操作");
        }

        List<RouteStop> stops = routeStopRepository
                .findByRoutePlanIdOrderByStopOrderAsc(stop.getRoutePlan().getId());
        for (RouteStop previous : stops) {
            if (previous.getStopOrder() < stop.getStopOrder() && !Boolean.TRUE.equals(previous.getCompleted())) {
                throw new RuntimeException("未按站序完成第 " + previous.getStopOrder()
                        + " 站，不能放行进入第 " + stop.getStopOrder() + " 站");
            }
        }

        stop.setReleased(true);
        stop.setReleasedTime(LocalDateTime.now());
        return toRouteStopDTO(routeStopRepository.save(stop));
    }

    /** 删除研学团动线，站点随动线一并删除。 */
    @Transactional
    public void deleteRoute(Long studyGroupId) {
        RoutePlan plan = routePlanRepository.findByStudyGroupId(studyGroupId)
                .orElseThrow(() -> new RuntimeException("该研学团尚未编排动线"));
        routePlanRepository.delete(plan);
    }

    /** 标记某站体验完成；未放行的站点不能标记完成。 */
    @Transactional
    public RouteStopDTO completeStop(Long stopId) {
        RouteStop stop = routeStopRepository.findById(stopId)
                .orElseThrow(() -> new RuntimeException("动线站点不存在"));

        if (Boolean.TRUE.equals(stop.getCompleted())) {
            throw new RuntimeException("该站已完成，请勿重复操作");
        }
        if (!Boolean.TRUE.equals(stop.getReleased())) {
            throw new RuntimeException("该站尚未放行，不能标记完成");
        }

        stop.setCompleted(true);
        stop.setCompletedTime(LocalDateTime.now());
        return toRouteStopDTO(routeStopRepository.save(stop));
    }

    private RoutePlanDTO toRoutePlanDTO(RoutePlan plan, StudyGroup group) {
        RoutePlanDTO dto = new RoutePlanDTO();
        dto.setGroupId(group.getId());
        dto.setGroupCode(group.getGroupCode());
        dto.setGroupName(group.getGroupName());
        dto.setSchoolName(group.getSchoolName());
        dto.setVisitDate(group.getVisitDate());
        if (plan != null) {
            dto.setRouteId(plan.getId());
            dto.setRouteName(plan.getRouteName());
            dto.setStops(plan.getStops().stream()
                    .map(this::toRouteStopDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private RouteStopDTO toRouteStopDTO(RouteStop stop) {
        RouteStopDTO dto = new RouteStopDTO();
        dto.setStopId(stop.getId());
        dto.setDeviceId(stop.getDevice().getId());
        dto.setDeviceCode(stop.getDevice().getDeviceCode());
        dto.setDeviceName(stop.getDevice().getDeviceName());
        dto.setStopOrder(stop.getStopOrder());
        dto.setReleased(stop.getReleased());
        dto.setCompleted(stop.getCompleted());
        dto.setReleasedTime(stop.getReleasedTime());
        dto.setCompletedTime(stop.getCompletedTime());
        return dto;
    }
}
