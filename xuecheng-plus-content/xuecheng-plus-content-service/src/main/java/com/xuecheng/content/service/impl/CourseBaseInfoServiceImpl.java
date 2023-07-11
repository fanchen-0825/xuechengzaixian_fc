package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.UpdateCourseDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @autuor 范大晨
 * @Date 2023/4/24 19:41
 * @description 课程信息管理业务接口实现类
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //构建查询条件，根据课程名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        //构建查询条件，根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        //构建查询条件，根据课程发布状态查询
        //根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());
        //细粒度 cpmpanyID
        queryWrapper.eq(CourseBase::getCompanyId,companyId);
        //分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<CourseBase> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    /**
     * 新增课程基本信息与销售信息
     * @param companyId 公司id
     * @param addCourseDto 新增信息
     * @return
     */
    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        //数据合法性校验
//        if (StringUtils.isEmpty(addCourseDto.getName())) {
//            throw new XueChengPlusException("课程名称不能为空");
//        }
//        if (StringUtils.isEmpty(addCourseDto.getMt())) {
//            throw new XueChengPlusException("课程分类不能为空");
//        }
//        if (StringUtils.isEmpty(addCourseDto.getSt())) {
//            throw new XueChengPlusException("课程分类不能为空");
//        }
//        if (StringUtils.isEmpty(addCourseDto.getGrade())) {
//            throw new XueChengPlusException("课程等级不能为空");
//        }
//        if (StringUtils.isEmpty(addCourseDto.getTeachmode())) {
//            throw new XueChengPlusException("教育模式不能为空");
//        }
//        if (StringUtils.isEmpty(addCourseDto.getUsers())) {
//            throw new XueChengPlusException("适用人群不能为空");
//        }
//        if (StringUtils.isEmpty(addCourseDto.getCharge())) {
//            throw new XueChengPlusException("收费规则不能为空");
//        }

        //校验通过 进行课程信息插入
        CourseBase courseBase = insertCourseBase(companyId, addCourseDto);
        // 进行课程销售信息插入
        insertIntoCourseMarket(addCourseDto, courseBase.getId());
        //两者都成功，进行查询返回
        CourseBaseInfoDto courseInfoDto = getCourseInfo(courseBase.getId());
        return courseInfoDto;
    }

    /**
     * 新增课程基本信息
     * @param companyId 公司id
     * @param addCourseDto 新增信息
     * @return 操作完成后查询出课程基本信息与销售信息返回
     */
    private CourseBase insertCourseBase(Long companyId, AddCourseDto addCourseDto) {
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBase);
        if (insert <= 0) {
            throw new XueChengPlusException("新增课程基本信息失败");//抛出RuntimeException事务仍然有效 Exception会导致事务失效
        }
        return courseBase;
    }

    /**
     * 课程销售信息插入
     *
     * @param addCourseDto 新增的dto类
     * @param id           要插入课程的id
     */
    private void insertIntoCourseMarket(AddCourseDto addCourseDto, Long id) {
        if ("201001".equals(addCourseDto.getCharge())) {
            if (addCourseDto.getPrice() == null || addCourseDto.getPrice().floatValue() <= 0) {
                throw new XueChengPlusException("收费课程价格不能为空且不能小于零");
            }
        }
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarket);

        courseMarket.setId(id);
        //进行新增操作
        int insert1 = courseMarketMapper.insert(courseMarket);
        if (insert1 <= 0) {
            throw new XueChengPlusException("新增课程销售信息失败");
        }
    }

    /**
     * 修改课程信息
     * @param companyId 公司id
     * @param updateCourseDto 修改信息
     * @return 操作完成后查询出课程基本信息与销售信息返回
     */
    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBase(Long companyId, UpdateCourseDto updateCourseDto) {
        Long courseId = updateCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase==null) {
            throw new XueChengPlusException("课程信息不存在");
        }
        if (!companyId.equals(courseBase.getCompanyId())) {
            throw new XueChengPlusException("本机构只能修改本机构的课程信息");
        }

        CourseBase courseBaseNew = new CourseBase();
        BeanUtils.copyProperties(updateCourseDto,courseBaseNew);
        courseBaseNew.setChangeDate(LocalDateTime.now());
        int i = courseBaseMapper.updateById(courseBaseNew);
        if (i<=0) {
            throw new XueChengPlusException("更新失败");
        }

        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(updateCourseDto,courseMarket);
        int i1 = courseMarketMapper.updateById(courseMarket);
        if (i1<=0) {
            throw new XueChengPlusException("更新失败");
        }

        //查询课程信息返回
        CourseBaseInfoDto courseInfo = getCourseInfo(updateCourseDto.getId());
        return courseInfo;
    }

    /**
     * 查询课程基本信息和营销信息进行包装为dto后返回
     *
     * @param id 要查询课程的id
     */
    @Override
    public CourseBaseInfoDto getCourseInfo(Long id) {
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if (courseBase==null) {
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);

        courseBaseInfoDto.setMtName(courseCategoryMapper.selectById(courseBase.getMt()).getName());
        courseBaseInfoDto.setStName(courseCategoryMapper.selectById(courseBase.getSt()).getName());
        return courseBaseInfoDto;
    }


}
