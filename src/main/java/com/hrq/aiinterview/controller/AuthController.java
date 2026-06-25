package com.hrq.aiinterview.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrq.aiinterview.entity.SysUser;
import com.hrq.aiinterview.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam(required = false) String nickname) {

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return "redirect:/register?error=empty";
        }

        if (!password.equals(confirmPassword)) {
            return "redirect:/register?error=confirm";
        }

        SysUser existUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );

        if (existUser != null) {
            return "redirect:/register?error=exists";
        }

        SysUser user = new SysUser();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(StringUtils.hasText(nickname) ? nickname.trim() : username.trim());
        user.setRole("USER");
        user.setEnabled(1);
        user.setCreateTime(LocalDateTime.now());

        sysUserMapper.insert(user);

        return "redirect:/login?registered";
    }
}