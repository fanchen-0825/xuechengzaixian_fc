package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;

import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.UpdateCourseDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @autuor 范大晨
 * @Date 2023/4/20 17:03
 * @description 课程信息编辑接口
 */
@RestController
@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
public class CourseBaseInfoController {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;


    @PostMapping("/course/list")
    @ApiOperation("分页课程信息查询")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {
        return courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDto);
    }

    @PostMapping("/course")
    @ApiOperation("新增课程基础信息")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(value = {ValidationGroups.Insert.class}) AddCourseDto addCourseDto) {
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }

    @GetMapping("course/{id}")
    @ApiOperation("根据课程id查询")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long id) {
        return courseBaseInfoService.getCourseInfo(id);
    }

    @PutMapping("/course")
    @ApiOperation("修改课程基础信息")
    public CourseBaseInfoDto updateCourseBase(@RequestBody UpdateCourseDto updateCourseDto){
        Long companyId = 1232141425L;

        return courseBaseInfoService.updateCourseBase(companyId,updateCourseDto);
    }
}
