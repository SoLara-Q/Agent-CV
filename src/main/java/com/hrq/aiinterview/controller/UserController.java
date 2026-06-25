package com.hrq.aiinterview.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrq.aiinterview.entity.SysUser;
import com.hrq.aiinterview.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") long page,
                       @RequestParam(defaultValue = "") String keyword,
                       Model model) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysUser::getUsername, keyword).or().like(SysUser::getNickname, keyword);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = sysUserMapper.selectPage(new Page<>(page, 8), wrapper);
        model.addAttribute("page", result);
        model.addAttribute("keyword", keyword);
        return "user/list";
    }

    @GetMapping("/form")
    public String form(@RequestParam(required = false) Long id, Model model) {
        SysUser user = id == null ? new SysUser() : sysUserMapper.selectById(id);
        model.addAttribute("user", user == null ? new SysUser() : user);
        return "user/form";
    }

    @PostMapping("/save")
    public String save(SysUser user, @RequestParam(required = false) String rawPassword) {
        if (user.getEnabled() == null) {
            user.setEnabled(1);
        }
        if (!StringUtils.hasText(user.getRole())) {
            user.setRole("USER");
        }
        if (user.getId() == null) {
            user.setPassword(passwordEncoder.encode(StringUtils.hasText(rawPassword) ? rawPassword : "123456"));
            user.setCreateTime(LocalDateTime.now());
            sysUserMapper.insert(user);
        } else {
            SysUser old = sysUserMapper.selectById(user.getId());
            if (StringUtils.hasText(rawPassword)) {
                user.setPassword(passwordEncoder.encode(rawPassword));
            } else if (old != null) {
                user.setPassword(old.getPassword());
            }
            if (old != null) {
                user.setCreateTime(old.getCreateTime());
            }
            sysUserMapper.updateById(user);
        }
        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user != null && !"admin".equalsIgnoreCase(user.getUsername())) {
            sysUserMapper.deleteById(id);
        }
        return "redirect:/users";
    }
}
