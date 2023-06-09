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
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = Long.valueOf(user.getCompanyId());
        return courseBaseInfoService.queryCourseBaseList(companyId,pageParams, queryCourseParamsDto);
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
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println(principal);
        SecurityUtil.XcUser xcUser = SecurityUtil.getUser();
        System.out.println(xcUser);
        return courseBaseInfoService.getCourseInfo(id);
    }

    @PutMapping("/course")
    @ApiOperation("修改课程基础信息")
    public CourseBaseInfoDto updateCourseBase(@RequestBody UpdateCourseDto updateCourseDto){
        Long companyId = 1232141425L;

        return courseBaseInfoService.updateCourseBase(companyId,updateCourseDto);
    }
}
