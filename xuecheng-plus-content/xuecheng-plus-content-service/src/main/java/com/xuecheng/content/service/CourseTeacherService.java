package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @autuor 范大晨
 * @Date 2023/5/1 21:48
 * @description 课程师资管理接口
 */
public interface CourseTeacherService {
    List<CourseTeacher> selectTeacherList(Long id);

    void addOrUpdateTeacher(CourseTeacher courseTeacher);

    void deleteTeacher(Long courseId, Long teacherId);
}
