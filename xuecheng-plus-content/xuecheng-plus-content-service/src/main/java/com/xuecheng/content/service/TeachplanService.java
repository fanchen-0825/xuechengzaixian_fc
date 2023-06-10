package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * @autuor 范大晨
 * @Date 2023/5/1 12:39
 * @description 课程计划信息接口
 */
public interface TeachplanService {
    List<TeachplanDto> selectTreeNodes(Long id);

    List<TeachplanDto> addOrUpdateTeachPlan(Teachplan teachplan);

//    void moveUp(String flag, Long id);
//
//    void moveDown(String flag, Long id);

    void move(String flag, Long id);

    void deleteTeachPlan(Long id);

    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);
}
