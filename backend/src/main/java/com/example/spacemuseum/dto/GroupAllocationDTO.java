package com.example.spacemuseum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupAllocationDTO {

    private Long groupId;
    private String groupCode;
    private String groupName;
    private String schoolName;
    private Integer totalStudents;
    private Integer averageAge;
    private LocalDate visitDate;
    private List<AllocationResultDTO> allocations;
}