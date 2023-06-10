package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.BigFilesService;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class VideoTask {
    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;

    @Autowired
    private BigFilesService bigFilesService;

    @Autowired
    private MediaFileProcessService mediaFileProcessService;

    @XxlJob("videoJobHandler")
    public void shardingJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        System.out.println("分片参数：当前分片序号 = " + shardIndex + "总分片数 =" + shardTotal);

        //获取当前cpu核心数
        int processors = Runtime.getRuntime().availableProcessors();
        log.info("当前CPU核心数：{}",processors);
        //查询待处理任务
        List<MediaProcess> mediaProcessList = mediaProcessMapper.getMediaProcessList(shardIndex, shardTotal, processors);
        int size = mediaProcessList.size();
        if (size <= 0) {
            return;
        }
        //构建线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            threadPool.execute(() -> {
                try {
                    //获取文件id
                    Long id = mediaProcess.getId();
                    //获取文件fileId
                    String fileId = mediaProcess.getFileId();
                    //获取文件url
                    String url = mediaProcess.getUrl();
                    //存储桶名称
                    String bucket = mediaProcess.getBucket();
                    //源avi视频的路径
                    String filePath = mediaProcess.getFilePath();
                    //从minio下载到本地
                    File file = bigFilesService.downloadFileFromMinio(bucket, filePath);
                    if (file == null) {
                        log.error("文件下载失败，文件id:{}", fileId);
                        mediaFileProcessService.saveProcessFinishStatus(id, "3", fileId, url, "文件下载失败");
                        return;
                    }
                    //转换视频

                    File tempFile = null;
                    try {
                        tempFile = File.createTempFile("minio", ".mp4");
                    } catch (Exception e) {
                        log.error("临时文件创建失败");
                        e.printStackTrace();
                        mediaFileProcessService.saveProcessFinishStatus(id, "3", fileId, url, "临时文件创建失败");
                        return;
                    }

                    String result = "";
                    try {
                        //创建工具类对象
                        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, file.getAbsolutePath(), tempFile.getName(), tempFile.getAbsolutePath());
                        //开始视频转换，成功将返回success
                        result = videoUtil.generateMp4();
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("处理视频文件:{},出错:{}", mediaProcess.getFilePath(), e.getMessage());
                    }

                    if (!"success".equals(result)) {
                        //处理视频虽然未抛异常，但是不成功
                        mediaFileProcessService.saveProcessFinishStatus(id, "3", fileId, url, "视频处理失败");
                        log.error("处理视频失败,视频地址:{},错误信息:{}", bucket + filePath, result);
                        return;
                    }
                    //上传到minio
                    //获得存储后文件名称
                    String fileName = mediaProcess.getFilePath().substring(0,mediaProcess.getFilePath().lastIndexOf(".")) + ".mp4";
                    url = bucket+"/"+mediaProcess.getFilePath().substring(0,mediaProcess.getFilePath().lastIndexOf(".")) + ".mp4";
                    try {
                        bigFilesService.addMediaFilesToMinIO(tempFile.getAbsolutePath(), bucket, "video/mp4", fileName);
                        mediaFileProcessService.saveProcessFinishStatus(id, "2", fileId, url, null);
                    } catch (Exception e) {
                        log.error("上传处理后文件失败");
                        e.printStackTrace();
                        mediaFileProcessService.saveProcessFinishStatus(id, "3", fileId, url, "处理后文件上传失败或文件信息入库失败");
                    }
                } finally {
                    countDownLatch.countDown();
                }
            });
        });
        countDownLatch.await(30, TimeUnit.MINUTES);
    }
}
