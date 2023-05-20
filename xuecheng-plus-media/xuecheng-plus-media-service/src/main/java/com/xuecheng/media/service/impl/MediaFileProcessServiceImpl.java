package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @autuor 范大晨
 * @Date 2023/5/14 16:24
 * @description 媒资文件处理业务方法实现类
 */
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {
    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    /**
     * 查看任务
     *
     * @param shardIndex 执行器id
     * @param shardTotal 执行器总数
     * @param count      查询个数
     * @return 返回查询到的任务列表
     */
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.getMediaProcessList(shardIndex, shardTotal, count);
    }

    /**
     * 开启任务
     *
     * @param id 任务id
     * @return 返回一个布尔值 表示任务开启是否成功
     */
    @Override
    public boolean startTask(Long id) {
        int i = mediaProcessMapper.startTask(id);
        return i > 0;
    }

    /**
     * 任务执行完后保存任务处理状态
     *
     * @param taskId   任务id
     * @param status   状态（成功 失败）
     * @param fileId   文件id
     * @param url      处理后文件的url
     * @param errorMsg 错误信息
     */
    @Override
    @Transactional
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (Objects.isNull(mediaProcess)) {
            return;
        }
        //任务执行失败
        if ("3".equals(status)) {
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setErrormsg(errorMsg);
            mediaProcess.setStatus("3");
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }
        //任务执行成功
        if ("2".equals(status)){
            //修改文件表
            MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
            //修改任务表
            mediaProcess.setStatus("2");
            mediaProcess.setUrl(url);
            mediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcessMapper.updateById(mediaProcess);
            //插入历史表
            MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
            BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
            mediaProcessHistoryMapper.insert(mediaProcessHistory);
            //删除任务表
            mediaProcessMapper.deleteById(taskId);
        }
    }
}
