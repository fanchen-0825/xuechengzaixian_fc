package com.xuecheng.content.jobhandler;

import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @autuor 范大晨
 * @Date 2023/6/8 19:44
 * @description 课程发布任务处理
 */
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler(){
        int shardTotal = XxlJobHelper.getShardTotal();
        int shardIndex = XxlJobHelper.getShardIndex();
        process(shardIndex,shardTotal,"course_publish",30,60);
    }

    @Override
    public boolean execute(MqMessage mqMessage) {
        //从mqMessage拿到课程id
        Long courseId = Long.valueOf(mqMessage.getBusinessKey1());
        //向ES写入索引
        saveCourseIndex(mqMessage,courseId);
        //向redis写缓存
        saveCourse2Redis(mqMessage,courseId);
        //课程静态化上传到minion
        generateCourseHtml(mqMessage,courseId);
        //返回true表示任务完成
        return true;
    }
    //课程静态化
    private void generateCourseHtml(MqMessage mqMessage,Long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne>0) {
            log.info("课程静态化任务处理完成 无需处理");
            return;
        }
        //int a=1/0;
        // 实现课程静态化
        File file = coursePublishService.generateCourseHtml(courseId);
        coursePublishService.uploadCourseHtml(file,courseId);
        mqMessageService.completedStageOne(taskId);
    }

    //向ES写入索引
    private void saveCourseIndex(MqMessage mqMessage,Long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo>0) {
            log.info("课程索引已写入 无需处理");
            return;
        }
        //todo 实现课程索引写入
        mqMessageService.completedStageTwo(taskId);
    }

    //向redis写缓存
    private void saveCourse2Redis(MqMessage mqMessage,Long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageThree = mqMessageService.getStageThree(taskId);
        if (stageThree>0) {
            log.info("课程索引已写入 无需处理");
            return;
        }
        //todo 实现课程写入redis
        mqMessageService.completedStageThree(taskId);
    }
}
