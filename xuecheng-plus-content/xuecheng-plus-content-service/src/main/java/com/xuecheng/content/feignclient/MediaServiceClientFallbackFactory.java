package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @autuor 范大晨
 * @Date 2023/6/9 19:30
 * @description 熔断后降级处理
 */
@Component
@Slf4j
public class MediaServiceClientFallbackFactory implements FallbackFactory {
    @Override
    public Object create(Throwable throwable) {
        return new MediaServiceClient() {
            @Override
            public String upload(MultipartFile fileData, String objectName) throws IOException {
                log.info("发生熔断后降级处理，异常信息{}",throwable.toString(),throwable);
                return null;
            }
        };
    }
}
