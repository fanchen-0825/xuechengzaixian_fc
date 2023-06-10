package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @autuor 范大晨
 * @Date 2023/4/30 20:56
 * @description 课程计划管理接口
 */
@RestController
@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
public class TeachplanController {
    @Autowired
    private TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId", name = "课程 Id", required = true, dataType = "Long", paramType = "path")
    @GetMapping("teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.selectTreeNodes(courseId);
    }

    @ApiOperation("新增或修改课程计划树形结构")
    @PostMapping("/teachplan")
    public List<TeachplanDto> addOrUpdateTeachPlan(@RequestBody Teachplan teachplan) {
        return teachplanService.addOrUpdateTeachPlan(teachplan);
    }

    @ApiOperation("上移")
    @PostMapping("/teachplan/moveup/{id}")
    public void moveUp(@PathVariable Long id) {
        String flag = "moveUp";
        teachplanService.move(flag, id);
    }

    @ApiOperation("下移")
    @PostMapping("/teachplan/movedown/{id}")
    public void moveDown(@PathVariable Long id) {
        String flag = "moveDown";
        teachplanService.move(flag, id);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachPlan(@PathVariable Long id) {
        teachplanService.deleteTeachPlan(id);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }
}
