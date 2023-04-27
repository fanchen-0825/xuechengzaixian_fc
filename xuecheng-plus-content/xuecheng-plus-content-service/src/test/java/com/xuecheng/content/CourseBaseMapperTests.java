package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @autuor 范大晨
 * @Date 2023/4/24 17:03
 * @description TODO
 */
@SpringBootTest
public class CourseBaseMapperTests {
    @Autowired
    CourseBaseMapper baseMapper;

    @Test
    void testCourseBaseMapper() {
//        CourseBase courseBase = baseMapper.selectById(18);
//        Assertions.assertNotNull(courseBase);

        PageParams pageParams = new PageParams(1L, 5L);
        IPage<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        QueryCourseParamsDto queryCourseParamsDto = new QueryCourseParamsDto();
        queryCourseParamsDto.setCourseName("java");
        queryCourseParamsDto.setAuditStatus("202004");
        queryCourseParamsDto.setPublishStatus("203001");
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                        CourseBase::getName, queryCourseParamsDto.getCourseName())
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                        CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        IPage<CourseBase> pageResult = baseMapper.selectPage(page, queryWrapper);
        List<CourseBase> baseList = pageResult.getRecords();
        long total = pageResult.getTotal();
        PageResult<CourseBase> result=new PageResult<>();
        result.setItems(baseList);
        result.setCounts(total);
        result.setPage(pageParams.getPageNo());
        result.setPageSize(pageParams.getPageSize());
        System.out.println(result);
    }
}
