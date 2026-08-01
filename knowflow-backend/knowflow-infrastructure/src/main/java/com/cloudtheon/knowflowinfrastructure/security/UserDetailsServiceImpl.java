package com.cloudtheon.knowflowinfrastructure.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudtheon.knowflowinfrastructure.entity.User;
import com.cloudtheon.knowflowinfrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 用户详情加载服务
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 账号被禁用时拒绝鉴权（管理员禁用后其旧 Token 立即失效）
        if ("disabled".equals(user.getStatus())) {
            throw new DisabledException("账号已被禁用: " + username);
        }

        return new LoginUser(user.getId(), user.getUsername(), user.getPassword());
    }
}
