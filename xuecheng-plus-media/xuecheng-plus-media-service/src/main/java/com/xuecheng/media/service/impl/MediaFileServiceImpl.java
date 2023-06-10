package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Override
    @Transactional
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String absolutePath, String objectName) {

        //将文件存入minion
        //得到本地文件名称
        String localPath = absolutePath;
        //获取存储桶名称
        String bucket = bucket_files;
        //获取在文件系统中文件名称
        //获取年月日信息
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date()).replace("-", "/") + "/";

        //获取文件md5值
        String md5Hex = getFileMd5(localPath);
        //获取文件类型
        //获取文件后缀名
        String sourceFilename = uploadFileParamsDto.getFilename();
        String extensionName = sourceFilename.substring(sourceFilename.lastIndexOf(".") + 1);
        //拼接
        String fileName=null;
        if (objectName==null) {
            fileName = date + md5Hex + "." + extensionName;
        } else {
            fileName = objectName;
        }
        //获取类型
        String mimeType = getMimeType(extensionName);
        try {
            addMediaFilesToMinIO(localPath, bucket, mimeType, fileName);
            log.info("上传文件成功");
        } catch (Exception e) {
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}", bucket, fileName, e.getMessage());
            throw new XueChengPlusException("文件上传失败");
        }

        //将文件信息存入数据库 存的是文件在minion中的数据
        MediaFiles mediaFiles = addMediaFilesToDb(companyId, uploadFileParamsDto, bucket, md5Hex, extensionName, fileName);

        //返回结果
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    /**
     * 根据id查询媒资信息
     *
     * @param mediaId
     * @return
     */
    @Override
    public MediaFiles getFileById(String mediaId) {
        return mediaFilesMapper.selectById(mediaId);
    }


    public MediaFiles addMediaFilesToDb(Long companyId, UploadFileParamsDto uploadFileParamsDto, String bucket, String md5Hex, String extensionName, String fileName) {
        //先查存不存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(md5Hex);
        if (mediaFiles == null) {
            //将uploadFileParamsDto拷贝到mediaFiles
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            //为mediaFiles设置没有的属性
            mediaFiles.setId(md5Hex);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setFilename(md5Hex + "." + extensionName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(fileName);
            mediaFiles.setFileId(md5Hex);
            mediaFiles.setUrl(bucket + "/" + fileName);
            mediaFiles.setStatus("1");
            mediaFiles.setFileSize(uploadFileParamsDto.getFileSize());
            mediaFiles.setAuditStatus("202003");
            //入库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.error("保存文件信息到数据库失败,{}", mediaFiles.toString());
                throw new XueChengPlusException("文件信息入库失败");
            }
        }
        return mediaFiles;
    }

    private void addMediaFilesToMinIO(String localPath, String bucket, String mimeType, String fileName) throws Exception {
        UploadObjectArgs upload = UploadObjectArgs.builder()
                .filename(localPath)//本地文件
                .bucket(bucket)                             //存储桶的名称
                .object(fileName)                            //存储后文件名称
                .contentType(mimeType)                                  //文件类型
                .build();
        minioClient.uploadObject(upload);
    }

    private String getMimeType(String extensionName) {
        if (extensionName == null) {
            extensionName = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extensionName);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    private String getFileMd5(String localPath) {
        try {
            //拿到本地文件资源路径,获取输入流
            File file = new File(localPath);
            FileInputStream fileInputStream = new FileInputStream(file);
            //进行md5值获取
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
