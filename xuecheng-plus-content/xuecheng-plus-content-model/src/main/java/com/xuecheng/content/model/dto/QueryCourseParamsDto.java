package com.xuecheng.content.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @autuor 范大晨
 * @Date 2023/4/20 16:39
 * @description 内容管理查询参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class QueryCourseParamsDto {
    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;

}
