package com.example.spacemuseum.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyGroupDTO {

    private Long id;

    @NotBlank(message = "团号不能为空")
    @Size(max = 50, message = "团号长度不能超过50")
    private String groupCode;

    @NotBlank(message = "团名不能为空")
    @Size(max = 100, message = "团名长度不能超过100")
    private String groupName;

    @Size(max = 100, message = "学校名称长度不能超过100")
    private String schoolName;

    @Size(max = 50, message = "联系人长度不能超过50")
    private String contactPerson;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String contactPhone;

    @NotNull(message = "学生总数不能为空")
    @Min(value = 1, message = "学生总数不能小于1")
    private Integer totalStudents;

    @NotNull(message = "平均年龄不能为空")
    @Min(value = 3, message = "平均年龄不能小于3岁")
    @Max(value = 18, message = "平均年龄不能大于18岁")
    private Integer averageAge;

    @NotNull(message = "参观日期不能为空")
    private LocalDate visitDate;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}