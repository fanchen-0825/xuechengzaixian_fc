package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @autuor 范大晨
 * @Date 2023/5/1 12:40
 * @description 课程计划信息接口实现类
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Override
    public List<TeachplanDto> selectTreeNodes(Long id) {
        return teachplanMapper.selectTreeNodes(id);
    }

    @Override
    public List<TeachplanDto> addOrUpdateTeachPlan(Teachplan teachplan) {
        Long id = teachplan.getId();
        Long courseId = teachplan.getCourseId();
        Long parentId = teachplan.getParentid();
        if (id == null) {
            //新增
            //设置排序字段
            //先查一下是不是第一个 如果是第一个就要置为0
            //设置创建时间
            int size = teachplanMapper.selectSize(courseId, parentId);
            if (size == 0) {
                teachplan.setOrderby(0);
            } else {
                int maxOrderBy = teachplanMapper.selectMaxOrderBy(courseId, parentId);
                teachplan.setOrderby(++maxOrderBy);
            }
            int insert = teachplanMapper.insert(teachplan);
            if (insert <= 0) {
                XueChengPlusException.cast("新增课程计划失败");
            }
        } else {
            //修改
            int update = teachplanMapper.updateById(teachplan);
            if (update <= 0) {
                XueChengPlusException.cast("修改课程计划失败");
            }
        }
        return this.selectTreeNodes(courseId);
    }

    @Override
    public void move(String flag, Long id) {
        //先查出待操作记录
        Teachplan teachplan = teachplanMapper.selectById(id);
        Long parentid = teachplan.getParentid();
        Long courseId = teachplan.getCourseId();
        //获得上一条记录
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId)
                .eq(Teachplan::getParentid, parentid);
        queryWrapper=flag.equals("moveUp")?queryWrapper.orderByAsc(Teachplan::getOrderby):queryWrapper.orderByDesc(Teachplan::getOrderby);
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);

        //第一条记录 然后变成前一条记录
        Teachplan teachplanFront = teachplans.stream().findFirst().get();
        List<Teachplan> teachplanList = teachplans.stream().skip(1).collect(Collectors.toList());
        for (Teachplan item : teachplanList) {
            if (Objects.equals(item.getId(),id)) {
                exchangeOrderBy(teachplan, teachplanFront);
                break;
            }else {
                teachplanFront=item;
            }
        }
    }

    @Override
    public void deleteTeachPlan(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        teachplan.setStatus(0);
        int i = teachplanMapper.updateById(teachplan);
        if (i<=0){
            throw new XueChengPlusException("删除失败，请稍后重试");
        }
    }
//    /**
//     * 上移
//     *
//     * @param flag
//     * @param id   待操作记录id
//     */
//    @Override
//    public void moveUp(String flag, Long id) {
//        //先查出待操作记录
//        Teachplan teachplan = teachplanMapper.selectById(id);
//        Long parentid = teachplan.getParentid();
//        Long courseId = teachplan.getCourseId();
//        //获得上一条记录
//        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Teachplan::getCourseId, courseId)
//                .eq(Teachplan::getParentid, parentid)
//                .orderByAsc(Teachplan::getOrderby);
//        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
//
//        //第一条记录 然后变成前一条记录
//        Teachplan teachplanFront = teachplans.stream().findFirst().get();
//        List<Teachplan> teachplanList = teachplans.stream().skip(1).collect(Collectors.toList());
//        for (Teachplan item : teachplanList) {
//            if (Objects.equals(item.getId(),id)) {
//                exchangeOrderBy(teachplan, teachplanFront);
//                break;
//            }else {
//                teachplanFront=item;
//            }
//        }
//    }
//
//
//    /**
//     * 下移
//     *
//     * @param flag
//     * @param id   待操作记录id
//     */
//    @Override
//    public void moveDown(String flag, Long id) {
//        //先查出待操作记录
//        Teachplan teachplan = teachplanMapper.selectById(id);
//        Long parentid = teachplan.getParentid();
//        Long courseId = teachplan.getCourseId();
//        //获得上一条记录
//        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Teachplan::getCourseId, courseId)
//                .eq(Teachplan::getParentid, parentid)
//                .orderByDesc(Teachplan::getOrderby);
//        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
//
//        //第一条记录 然后变成前一条记录
//        Teachplan teachplanFront = teachplans.stream().findFirst().get();
//        List<Teachplan> teachplanList = teachplans.stream().skip(1).collect(Collectors.toList());
//        for (Teachplan item : teachplanList) {
//            if (Objects.equals(item.getId(),id)) {
//                exchangeOrderBy(teachplan, teachplanFront);
//                break;
//            }else {
//                teachplanFront=item;
//            }
//        }
//    }
//

    /**
     * 交换orderBy的值
     * @param teachplan 当前记录
     * @param teachplanCandidate 待交换记录
     */
    private void exchangeOrderBy(Teachplan teachplan, Teachplan teachplanCandidate) {
        Integer teachplanFrontOrderby = teachplanCandidate.getOrderby();
        Integer teachplanOrderby = teachplan.getOrderby();

        teachplan.setOrderby(teachplanFrontOrderby);
        teachplanCandidate.setOrderby(teachplanOrderby);

        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(teachplanCandidate);
    }
}
