package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    List<TeachplanDto> selectTreeNodes(Long id);

    /**
     * 查询课程计划排序字段最大值
     * @param courseId 课程id
     * @param parentId 课程计划父id
     */
    @Select("select max(orderby) from teachplan where course_id=#{courseId} and parentid=#{parentId}")
    int selectMaxOrderBy( @Param("courseId") Long courseId,@Param("parentId") Long parentId);

    /**
     * 查询课程计划分支的数量 为0时排序字段置为0
     * @param courseId 课程id
     * @param parentId 课程计划父id
     */
    @Select("select count(*) from teachplan where course_id=#{courseId} and parentid=#{parentId}")
    int selectSize(@Param("courseId") Long courseId,@Param("parentId") Long parentId);
}
