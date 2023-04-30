package com.xuecheng.content.model.dto;

import com.xuecheng.base.exception.ValidationGroups;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @autuor 范大晨
 * @Date 2023/4/30 17:32
 * @description 修改课程dto类
 */
@Data
public class UpdateCourseDto extends AddCourseDto{
    @NotEmpty(message ="修改课程id不能为空",groups = {ValidationGroups.Insert.class,ValidationGroups.Update.class})
    private Long id;
}
