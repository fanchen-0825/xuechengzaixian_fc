package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feginclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @autuor 范大晨
 * @Date 2023/6/13 15:54
 * @description 账号名密码方式认证
 */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {
    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();

        if (StringUtils.isBlank(checkcode)||StringUtils.isBlank(checkcodekey)) {
            throw new RuntimeException("验证码为空");
        }
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (verify==null||!verify) {
            throw new RuntimeException("验证码错误");
        }

        String username = authParamsDto.getUsername();

        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (xcUser == null) {
            throw new RuntimeException("账号不存在");
        }
        String passwordDb = xcUser.getPassword();
        String passwordFrom = authParamsDto.getPassword();
        boolean matches = passwordEncoder.matches(passwordFrom, passwordDb);
        if (!matches) {
            throw new RuntimeException("账号或密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }
}
