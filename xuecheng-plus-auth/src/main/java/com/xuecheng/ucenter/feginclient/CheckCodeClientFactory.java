package com.xuecheng.ucenter.feginclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @autuor 范大晨
 * @Date 2023/6/15 15:38
 * @description 熔断降级方案
 */
@Slf4j
@Component
public class CheckCodeClientFactory implements FallbackFactory {
    @Override
    public Object create(Throwable throwable) {
        return new CheckCodeClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.debug("调用验证码服务熔断异常:{}", throwable.getMessage());
                return null;
            }
        };
    }
}
