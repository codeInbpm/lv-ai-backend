package com.lvai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvai.entity.UserBrowsingHistory;
import com.lvai.mapper.UserBrowsingHistoryMapper;
import com.lvai.service.IUserBrowsingHistoryService;
import org.springframework.stereotype.Service;

@Service
public class UserBrowsingHistoryServiceImpl extends ServiceImpl<UserBrowsingHistoryMapper, UserBrowsingHistory> implements IUserBrowsingHistoryService {
}
