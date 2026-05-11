package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lvai.common.Result;
import com.lvai.dto.WxLoginDTO;
import com.lvai.entity.User;
import com.lvai.service.IUserService;
import com.lvai.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户模块")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PostMapping("/login")
    @Operation(summary = "微信一键登录")
    public Result<LoginVO> wxLogin(@Valid @RequestBody WxLoginDTO dto) {
        return Result.success(userService.wxLogin(dto));
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<User> getUserInfo() {
        return Result.success(userService.getCurrentUser());
    }

    @PutMapping("/info")
    @Operation(summary = "更新用户信息")
    public Result<User> updateUserInfo(@RequestBody User user) {
        return Result.success(userService.updateUserInfo(user));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }

    @GetMapping("/invite-code")
    @Operation(summary = "获取邀请码")
    public Result<String> getInviteCode() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userService.generateInviteCode(userId));
    }

    @PostMapping("/bind-couple")
    @Operation(summary = "绑定情侣(通过邀请码)")
    public Result<Void> bindCouple(@RequestParam String inviteCode) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.bindCouple(userId, inviteCode);
        return Result.success();
    }
}
