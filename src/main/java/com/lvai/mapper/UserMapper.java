package com.lvai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lvai.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
