package com.example.spacemuseum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_group")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_code", unique = true, nullable = false, length = 50)
    private String groupCode;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "school_name", length = 100)
    private String schoolName;

    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "total_students", nullable = false)
    private Integer totalStudents;

    @Column(name = "average_age", nullable = false)
    private Integer averageAge;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "remark", length = 500)
    private String remark;

    @JsonIgnore
    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Allocation> allocations = new ArrayList<>();
}