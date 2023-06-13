package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

/**
 * @autuor 范大晨
 * @Date 2023/6/11 19:24
 * @description 用户详细信息实现类
 */
@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto=null;
        try {
            authParamsDto=JSON.parseObject(s,AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}",s);
            throw new RuntimeException("认证请求数据格式错误");
        }

        //策略模式
        String authType = authParamsDto.getAuthType();
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        XcUser xcUser = new XcUser();
        BeanUtils.copyProperties(xcUserExt,xcUser);

        return getUserDetails(xcUser);
    }

    private static UserDetails getUserDetails(XcUser xcUser) {
        String[] authorities={"test"};
        //进行用户信息扩展 将查到的信息除敏感信息外全部转为json存入
        String password = xcUser.getPassword();
        xcUser.setPassword(null);
        return User.withUsername(JSON.toJSONString(xcUser)).password(password).authorities(authorities).build();
    }
}
