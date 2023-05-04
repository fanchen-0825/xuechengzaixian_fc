package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @autuor 范大晨
 * @Date 2023/5/1 21:45
 * @description 课程教师管理
 */
@RestController
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;
    @GetMapping("/courseTeacher/list/{id}")
    public List<CourseTeacher> selectTeacherList(@PathVariable Long id){
        return courseTeacherService.selectTeacherList(id);
    }

    @PostMapping("/courseTeacher")
    public void addOrUpdateTeacher(@RequestBody CourseTeacher courseTeacher){
        courseTeacherService.addOrUpdateTeacher(courseTeacher);
    }

    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteTeacher(@PathVariable("courseId") Long courseId,@PathVariable("teacherId") Long teacherId){
        courseTeacherService.deleteTeacher(courseId,teacherId);
    }
}
