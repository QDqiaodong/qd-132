package com.example.spacemuseum.repository;

import com.example.spacemuseum.entity.MaintenanceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceOrderRepository extends JpaRepository<MaintenanceOrder, Long> {

    /** 该设备当前的未完成工单（active_flag=1）；没有则为空 */
    Optional<MaintenanceOrder> findFirstByDeviceIdAndActiveFlag(Long deviceId, Integer activeFlag);

    boolean existsByDeviceIdAndActiveFlag(Long deviceId, Integer activeFlag);

    /** 未完成工单在前（早开的在前），已完成工单在后（刚完成的在前） */
    @Query("select o from MaintenanceOrder o order by o.activeFlag desc, o.createdAt asc, o.completedAt desc")
    List<MaintenanceOrder> findAllOrdered();
}
