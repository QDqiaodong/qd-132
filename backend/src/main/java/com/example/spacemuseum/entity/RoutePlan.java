package com.example.spacemuseum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 研学团体验动线：每个研学团最多一条动线，
 * 动线下的站点按站序（stopOrder）严格依次放行。
 */
@Entity
@Table(name = "route_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id", nullable = false, unique = true)
    private StudyGroup studyGroup;

    @Column(name = "route_name", nullable = false, length = 100)
    private String routeName;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @OneToMany(mappedBy = "routePlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stopOrder ASC")
    private List<RouteStop> stops = new ArrayList<>();
}
