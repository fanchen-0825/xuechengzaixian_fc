package com.xuecheng.media.service.impl;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.BigFilesService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @autuor 范大晨
 * @Date 2023/5/11 13:25
 * @description 大文件上传接口实现类
 */
@Service
@Slf4j
public class BigFilesServiceImpl implements BigFilesService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;

    /**
     * 查看上传文件是否已经存在
     *
     * @param fileMd5 上传文件的md5值
     * @return 返回统一结果包装类
     */
    @Override
    public RestResponse<Boolean> checkfile(String fileMd5) {
        //先查看数据库是否存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            //数据库存在信息 再查看minion是否存在
            GetObjectArgs getObiectArgs = GetObjectArgs.builder()
                    .bucket(mediaFiles.getBucket())
                    .object(mediaFiles.getFilePath())
                    .build();
            try {
                GetObjectResponse response = minioClient.getObject(getObiectArgs);
                if (response != null) {
                    return RestResponse.success(true, "文件已存在");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false, "文件不存在");
    }

    /**
     * 判断分块是否存在
     *
     * @param fileMd5 完整文件的md5值 用于获得文件路径
     * @param chunk   分块文件编号
     * @return 统一返回值
     */
    @Override
    public RestResponse<Boolean> checkchunk(String fileMd5, int chunk) {
        //获得分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //获得分块文件路径
        String chunkFilePath = chunkFileFolderPath + chunk;
        //查询分块文件是否存在
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_videofiles)
                .object(chunkFilePath)
                .build();
        try {
            GetObjectResponse response = minioClient.getObject(getObjectArgs);
            if (response != null) {
                return RestResponse.success(true, "文件已存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(false, "文件不存在");
    }

    /**
     * 上传具有指定MD5值和块编号的给定文件的一个块。
     *
     * @param fileMd5      文件切片的MD5值。
     * @param chunk        文件上传的切片编号。
     * @param absolutePath 文件的绝对路径。
     * @return 带有上传响应的RestResponse对象。
     */
    @Override
    public RestResponse uploadchunk(String fileMd5, int chunk, String absolutePath) {

        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String chunkFilePath = chunkFileFolderPath + chunk;
        String mimeType = getMimeType(null);
        try {
            addMediaFilesToMinIO(absolutePath, bucket_videofiles, mimeType, chunkFilePath);
            log.info("上传文件成功");
            return RestResponse.success("上传文件成功");
        } catch (Exception e) {
            log.error("上传文件到minio出错,bucket:{},fileMd5:{},错误原因:{}", bucket_videofiles, fileMd5, e.getMessage());
            throw new XueChengPlusException("上传分块失败");
        }
    }

    /**
     * 合并分块后文件
     *
     * @param companyId           公司id 后期做单点登录实现
     * @param fileMd5             原始文件的md5值
     * @param chunkTotal          文件分块个数
     * @param uploadFileParamsDto 文件入库的信息
     * @return 统一返回值
     */
    @Override
    @Transactional
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //合并文件
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i).limit(chunkTotal).map(i -> ComposeSource.builder().bucket(bucket_videofiles).object(chunkFileFolderPath + i).build()).collect(Collectors.toList());

        //获取文件名
        String filename = uploadFileParamsDto.getFilename();
        //获取文件后缀名
        String extensionName = filename.substring(filename.lastIndexOf("."));
        //获取合并后文件路径
        String mergeFilePath = getMergeFilePath(fileMd5, extensionName);

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_videofiles)//存储桶名称
                .sources(sources)//待合并文件资源
                .object(mergeFilePath)//合并后文件路径
                .build();
        try {
            ObjectWriteResponse response = minioClient.composeObject(composeObjectArgs);
            log.info("文件合并成功，{}", mergeFilePath);
        } catch (Exception e) {
            log.error("合并文件出错，bucket:{},filename:{},message:{}", bucket_videofiles, filename, e.getMessage());
            e.printStackTrace();
        }

        //下载文件
        File downFile = downloadFileFromMinio(bucket_videofiles, mergeFilePath);
        uploadFileParamsDto.setFileSize(downFile.length());
        //比较文件完整性 正确性
        try (FileInputStream fileInputStream = new FileInputStream(downFile)) {
            String downloadMd5Hex = DigestUtils.md5Hex(fileInputStream);
            if (!fileMd5.equals(downloadMd5Hex)) {
                log.error("校验后原始文件与合并后文件不同，原始文件md5:{},合并文件md5:{}",fileMd5,downloadMd5Hex);
                return RestResponse.validfail("文件校验失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //文件入库
        addMediaFilesToDb(companyId, uploadFileParamsDto, bucket_videofiles, fileMd5, extensionName, uploadFileParamsDto.getFilename());

        //清除分块
        clearChunk(bucket_videofiles,fileMd5,chunkTotal);
        return RestResponse.success(true);
    }

    private void clearChunk(String bucket, String fileMd5, int chunkTotal) {
        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> new DeleteObject(getChunkFileFolderPath(fileMd5) + i))
                .collect(Collectors.toList());

        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                .bucket(bucket)
                .objects(deleteObjects)
                .build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(r->{
            DeleteError deleteError=null;
            try {
                deleteError=r.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 媒资文件信息入库
     * @param companyId 公司id
     * @param uploadFileParamsDto 文件信息
     * @param bucket 存储桶名称
     * @param md5Hex 原始文件md5值
     * @param extensionName 文件扩展名
     * @param fileName 文件名称
     * @return 返回媒资文件信息
     */
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
            mediaFiles.setFilename(fileName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(getMergeFilePath(md5Hex,extensionName));
            mediaFiles.setFileId(md5Hex);
            mediaFiles.setUrl(bucket+"/"+getMergeFilePath(md5Hex,extensionName));
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

    /**
     * 从minion下载文件
     *
     * @param bucketVideofiles 存储桶的名称
     * @param mergeFilePath    合并后文件路径
     * @return 返回下载的文件
     */
    @NotNull
    private File downloadFileFromMinio(String bucketVideofiles, String mergeFilePath) {
        File tempFile = null;
        FileOutputStream fileOutputStream = null;
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket_videofiles)
                    .object(mergeFilePath)
                    .build();
            InputStream inputStream = minioClient.getObject(getObjectArgs);
            tempFile = File.createTempFile("minio", "temp");
            fileOutputStream = new FileOutputStream(tempFile);
            IOUtils.copy(inputStream, fileOutputStream);
            return tempFile;
        } catch (Exception e) {
            log.error("下载文件失败，bucket:{},mergeFilePath:{},message:{}", bucket_videofiles, mergeFilePath, e.getMessage());
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 获取合并后文件路径
     *
     * @param fileMd5       完整文件md5值
     * @param extensionName 文件扩展名
     * @return 返回合并后文件路径
     */
    @NotNull
    private static String getMergeFilePath(String fileMd5, String extensionName) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + extensionName;
    }

    /**
     * 获得分块文件目录
     *
     * @param fileMd5 完整文件的md5值
     * @return 返回分块文件目录
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk/";
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
}
