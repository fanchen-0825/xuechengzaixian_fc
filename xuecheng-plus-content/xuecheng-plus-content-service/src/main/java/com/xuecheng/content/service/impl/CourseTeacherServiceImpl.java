package com.xuecheng.content.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.context.Theme;

import java.util.List;

/**
 * @autuor 范大晨
 * @Date 2023/5/1 21:49
 * @description 课程师资管理接口实现类
 */
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;
    @Override
    public List<CourseTeacher> selectTeacherList(Long id) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,id);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    @Override
    public void addOrUpdateTeacher(CourseTeacher courseTeacher) {
        Long id = courseTeacher.getId();
        if (id==null){
            //新增
            int insert = courseTeacherMapper.insert(courseTeacher);
            if (insert<=0){
                throw new XueChengPlusException("新增教师失败");
            }
        }else {
            //修改
            int update = courseTeacherMapper.updateById(courseTeacher);
            if (update<=0){
                throw new XueChengPlusException("修改教师失败");
            }
        }
    }

    @Override
    public void deleteTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId)
                        .eq(CourseTeacher::getId,teacherId);
        int delete = courseTeacherMapper.delete(queryWrapper);
        if (delete<=0) {
            throw new XueChengPlusException("删除教师失败");
        }
    }
}

