package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import sun.util.resources.cldr.mg.LocaleNames_mg;

import java.io.File;

/**
 * @autuor 范大晨
 * @Date 2023/5/24 15:03
 * @description 课程预览、发布接口
 */
public interface CoursePublishService {
    CoursePreviewDto getCoursePreviewInfo(Long courseId);
    /**
     * @description 提交审核
     * @param courseId  课程id
     * @return void
     * @author Mr.M
     * @date 2022/9/18 10:31
     */
     void commitAudit(Long companyId,Long courseId);

    void coursePublish(Long companyId, Long courseId);

    File generateCourseHtml(Long courseId);
    void  uploadCourseHtml(File file, Long courseId);

    CoursePublish getCoursePublish(Long courseId);
}
