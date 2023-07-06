package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

/**
 * @autuor 范大晨
 * @Date 2023/6/28 15:01
 * @description 微信扫码接入
 */
public interface WxAuthService {
    /**
     * 微信扫码 申请令牌 携带令牌查询用户信息 保存用户信息到数据库
     * @param code 授权码
     */
    XcUser wxAuth(String code);
}
