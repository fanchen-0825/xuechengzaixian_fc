package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * @autuor 范大晨
 * @Date 2023/5/14 16:23
 * @description 媒资文件处理业务方法
 */
public interface MediaFileProcessService {
    List<MediaProcess> getMediaProcessList(int shardIndex,int shardTotal,int count);

    boolean startTask(Long id);

    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);
}
