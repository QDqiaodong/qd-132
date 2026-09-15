package com.example.spacemuseum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 某研学团的实验顺序条：团信息 + 按时段先后排列的有效配对行。
 * 数据实时取自当前有效占用（status=1），占用取消后重新打开顺序条即看不到该行。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentOrderDTO {

    private Long groupId;
    private String groupCode;
    private String groupName;
    private String schoolName;
    private String contactPerson;
    private String contactPhone;
    private Integer totalStudents;
    private LocalDate visitDate;
    /** 顺序条各行人数合计；各行人数可能因手工调整与团总人数不一致 */
    private Integer allocatedStudents;
    private List<ExperimentOrderItemDTO> items;
}
