package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @autuor 范大晨
 * @Date 2023/6/9 16:31
 * @description 远程调用媒资服务接口
 */

@FeignClient(value = "media-api", configuration = {MultipartSupportConfig.class,MediaServiceClientFallbackFactory.class})
public interface MediaServiceClient {
    @PostMapping(value = "media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String upload(@RequestPart("filedata") MultipartFile fileData,
                  @RequestParam(value = "objectName", required = false) String objectName) throws IOException;
}
