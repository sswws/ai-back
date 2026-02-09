package com.example.hailuobackend.controller;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hailuobackend.entity.User;
import com.example.hailuobackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*") // 允许跨域
public class UserController {

    @Autowired
    private UserMapper userMapper;

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        // 1. 校验手机号是否已存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, user.getPhone()));
        if (count > 0) {
            result.put("code", 400);
            result.put("message", "该手机号已注册");
            return result;
        }

        // 2. 密码加密 (MD5)
        user.setPassword(SecureUtil.md5(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 3. 插入数据库
        userMapper.insert(user);

        result.put("code", 200);
        result.put("message", "注册成功");
        return result;
    }

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User loginUser) {
        Map<String, Object> result = new HashMap<>();

        // 1. 根据手机号查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, loginUser.getPhone()));

        if (user == null) {
            result.put("code", 400);
            result.put("message", "用户不存在");
            return result;
        }

        // 2. 校验密码
        if (!user.getPassword().equals(SecureUtil.md5(loginUser.getPassword()))) {
            result.put("code", 400);
            result.put("message", "密码错误");
            return result;
        }

        // 3. 登录成功
        result.put("code", 200);
        result.put("message", "登录成功");
        result.put("data", user); // 返回用户信息
        return result;
    }
}