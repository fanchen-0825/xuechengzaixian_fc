package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.inner.ShardingTableInnerInterceptor;
import com.xuecheng.ucenter.mapper.XcRoleMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcRole;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.WxAuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @autuor 范大晨
 * @Date 2023/6/13 15:54
 * @description 微信扫码认证
 */
@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService, WxAuthService {

    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private XcUserRoleMapper xcRoleMapper;

    @Value("${weixin.appid}")
    private String appid;

    @Value("${weixin.secret}")
    private String secret;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WxAuthServiceImpl proxy;

    /**
     * 进行认证
     *
     * @param authParamsDto 认证参数
     */
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //账号
        String username = authParamsDto.getUsername();
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (user == null) {
            //返回空表示用户不存在
            throw new RuntimeException("账号不存在");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        return xcUserExt;
    }

    /**
     * @param code 授权码
     */
    @Override
    public XcUser wxAuth(String code) {
        //这里面需要执行微信扫码认证的逻辑
        //先拿到一个令牌
        Map<String, String> accessTokenObj = getAccessToken(code);
        String access_token = accessTokenObj.get("access_token");
        String openid = accessTokenObj.get("openid");
        //用这个令牌向微信申请用户数据
        Map<String, String> userInformation = getUserInformation(access_token, openid);
        if (userInformation==null) {
            throw new RuntimeException("微信扫码登录获取信息失败");
        }
        //将数据保存到本地数据库
        XcUser one = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid,userInformation.get("unionid")));
        if (one!= null) {
            return one;
        }
        XcUser user = new XcUser();
        XcUserRole userRole = new XcUserRole();
        //todo 添加数据
        String userId = UUID.randomUUID().toString();
        user.setId(userId);
        user.setUsername(userInformation.get("nickname"));
        user.setWxUnionid(userInformation.get("unionid"));
        user.setNickname(userInformation.get("nickname"));
        user.setSex(String.valueOf(userInformation.get("sex")));
        user.setUserpic(userInformation.get("headimgurl"));
        user.setName(userInformation.get("nickname"));
        user.setStatus("1");
        user.setUtype("101001");
        user.setCreateTime(LocalDateTime.now());

        userRole.setId(UUID.randomUUID().toString());
        userRole.setUserId(userId);
        userRole.setRoleId("17");
        userRole.setCreateTime(LocalDateTime.now());
        //非事务方法调用事务方法 会引起事务失效 我们采用代理对象的方法进行调用
        proxy.saveUser2DB(user,userRole);
        return user;
    }

    /**
     * 保存用户数据到本本地
     * @param user 入库用户数据
     */
    @Transactional
    public void saveUser2DB(XcUser user,XcUserRole userRole) {
        int insert = xcUserMapper.insert(user);
        if (insert<=0){
            throw new RuntimeException("保存用户数据失败");
        }
        int insert1 = xcRoleMapper.insert(userRole);
        if (insert1<=0) {
            throw new RuntimeException("保存用户数据失败");
        }
    }

    /**
     * 获取含token信息
     * @param code 授权码
     */
    private Map<String, String> getAccessToken(String code) {
        String url_temp = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String url = String.format(url_temp, appid, secret, code);
        ResponseEntity<String> accessTokenObj = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        String result=new String(accessTokenObj.getBody().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
        return JSON.parseObject(String.valueOf(accessTokenObj.getBody()), Map.class);
    }

    /**
     * 携带令牌获取用户信息
     * @param access_token 令牌
     * @param openid 标识
     */
    private Map<String,String> getUserInformation (String access_token,String openid){
        String url_temp="https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String url = String.format(url_temp, access_token, openid);
        ResponseEntity<String> informationEmpty = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        String result=new String(informationEmpty.getBody().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
        return JSON.parseObject(result, Map.class);
    }
}
