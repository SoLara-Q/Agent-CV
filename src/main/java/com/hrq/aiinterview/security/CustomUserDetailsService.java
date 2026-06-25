package com.hrq.aiinterview.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrq.aiinterview.entity.SysUser;
import com.hrq.aiinterview.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }
        return new User(
                sysUser.getUsername(),
                sysUser.getPassword(),
                sysUser.getEnabled() != null && sysUser.getEnabled() == 1,
                true,
                true,
                true,
                AuthorityUtils.createAuthorityList("ROLE_" + sysUser.getRole())
        );
    }
}
