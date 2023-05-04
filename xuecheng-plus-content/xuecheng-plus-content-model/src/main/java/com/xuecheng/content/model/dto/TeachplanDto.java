package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.*;

import java.util.List;

/**
 * @autuor 范大晨
 * @Date 2023/4/30 20:53
 * @description 课程计划Dto
 */

@Data
@ToString
public class TeachplanDto extends Teachplan {
    private TeachplanMedia teachplanMedia;
    private List<TeachplanDto> teachPlanTreeNodes;
}
