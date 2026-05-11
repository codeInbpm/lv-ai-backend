package com.lvai.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.common.BusinessException;
import com.lvai.dto.WxLoginDTO;
import com.lvai.entity.User;
import com.lvai.mapper.UserMapper;
import com.lvai.service.IUserService;
import com.lvai.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Value("${wechat.mini.app-id}")
    private String appId;

    @Value("${wechat.mini.app-secret}")
    private String appSecret;

    @Value("${wechat.mini.login-url}")
    private String loginUrl;

    @Override
    public LoginVO wxLogin(WxLoginDTO dto) {
        // 1. 调用微信接口换取openid
        String url = loginUrl + "?appid=" + appId + "&secret=" + appSecret
                + "&js_code=" + dto.getCode() + "&grant_type=authorization_code";
        String response = HttpUtil.get(url);
        log.info("微信登录响应: {}", response);

        JSONObject wxResult = JSON.parseObject(response);
        if (wxResult.containsKey("errcode") && wxResult.getIntValue("errcode") != 0) {
            throw new BusinessException("微信登录失败: " + wxResult.getString("errmsg"));
        }

        String openid = wxResult.getString("openid");
        String unionid = wxResult.getString("unionid");
        if (StrUtil.isBlank(openid)) {
            throw new BusinessException("获取openid失败");
        }

        // 2. 查询或创建用户
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        boolean isNew = false;
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(unionid);
            user.setNickname(StrUtil.isNotBlank(dto.getNickname()) ? dto.getNickname() : "旅行者" + RandomUtil.randomNumbers(6));
            user.setAvatar(dto.getAvatar());
            user.setGender(dto.getGender());
            user.setStatus(1);
            user.setPoints(0);
            user.setInviteCode(RandomUtil.randomString(8).toUpperCase());
            user.setLastLoginTime(LocalDateTime.now());
            save(user);
            isNew = true;
        } else {
            // 更新登录信息
            user.setLastLoginTime(LocalDateTime.now());
            if (StrUtil.isNotBlank(dto.getNickname())) user.setNickname(dto.getNickname());
            if (StrUtil.isNotBlank(dto.getAvatar())) user.setAvatar(dto.getAvatar());
            updateById(user);
        }

        // 3. Sa-Token 登录
        StpUtil.login(user.getId());

        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setTokenTimeout(StpUtil.getTokenTimeout());
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setIsNew(isNew);
        return vo;
    }

    @Override
    public User getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return getById(userId);
    }

    @Override
    public User updateUserInfo(User user) {
        Long userId = StpUtil.getLoginIdAsLong();
        user.setId(userId);
        updateById(user);
        return getById(userId);
    }

    @Override
    public String generateInviteCode(Long userId) {
        User user = getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (StrUtil.isBlank(user.getInviteCode())) {
            user.setInviteCode(RandomUtil.randomString(8).toUpperCase());
            updateById(user);
        }
        return user.getInviteCode();
    }

    @Override
    public boolean bindCouple(Long userId, String inviteCode) {
        User target = getOne(new LambdaQueryWrapper<User>().eq(User::getInviteCode, inviteCode));
        if (target == null) throw new BusinessException("邀请码无效");
        if (target.getId().equals(userId)) throw new BusinessException("不能绑定自己");

        User user = getById(userId);
        user.setCoupleId(target.getId());
        target.setCoupleId(userId);
        updateById(user);
        updateById(target);
        return true;
    }
}
