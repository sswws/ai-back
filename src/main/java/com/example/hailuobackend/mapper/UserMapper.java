package com.example.hailuobackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hailuobackend.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}