package com.lvai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvai.dto.WxLoginDTO;
import com.lvai.entity.User;
import com.lvai.vo.LoginVO;

public interface IUserService extends IService<User> {
    LoginVO wxLogin(WxLoginDTO dto);
    User getCurrentUser();
    User updateUserInfo(User user);
    String generateInviteCode(Long userId);
    boolean bindCouple(Long userId, String inviteCode);
}
