package com.xuecheng.media.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {
    @Autowired
    MediaFileService mediaFileService;
    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiels(companyId, pageParams, queryMediaParamsDto);

    }

    @PostMapping(value = "/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)//,consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile fileData,
                                      @RequestParam(value= "objectName",required=false) String objectName) throws IOException {
        //设置公司id 这里先直接写死 后期做单点登录会修改
        Long companyId = 1232141425L;

        //设置上传文件入库的信息 同样该信息在文件存入minion中也会用到
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileData.getOriginalFilename());
        uploadFileParamsDto.setFileSize(fileData.getSize());
        uploadFileParamsDto.setFileType("001001");

        File tempFile = File.createTempFile("minio", "temp");
        fileData.transferTo(tempFile);
        String absolutePath = tempFile.getAbsolutePath();
        return mediaFileService.uploadFile(companyId,uploadFileParamsDto,absolutePath,objectName);
    }
}
