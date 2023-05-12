package com.xuecheng.media.service;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;

/**
 * @autuor 范大晨
 * @Date 2023/5/11 13:24
 * @description 大数据上传接口
 */
public interface BigFilesService {
    RestResponse<Boolean> checkfile(String fileMd5);

    RestResponse<Boolean> checkchunk(String fileMd5, int chunk);

    RestResponse uploadchunk(String fileMd5, int chunk, String absolutePath);

    RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);
}
