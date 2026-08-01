package com.cloudtheon.knowflowcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudtheon.knowflowcommon.exception.BusinessException;
import com.cloudtheon.knowflowcommon.result.ResultCode;
import com.cloudtheon.knowflowcore.service.AdminService;
import com.cloudtheon.knowflowcore.vo.AdminUserVO;
import com.cloudtheon.knowflowcore.vo.PageVO;
import com.cloudtheon.knowflowinfrastructure.entity.Conversation;
import com.cloudtheon.knowflowinfrastructure.entity.Feedback;
import com.cloudtheon.knowflowinfrastructure.entity.KnowledgeDoc;
import com.cloudtheon.knowflowinfrastructure.entity.User;
import com.cloudtheon.knowflowinfrastructure.mapper.ConversationMapper;
import com.cloudtheon.knowflowinfrastructure.mapper.FeedbackMapper;
import com.cloudtheon.knowflowinfrastructure.mapper.KnowledgeDocMapper;
import com.cloudtheon.knowflowinfrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 系统管理后台业务实现（仅管理员可用）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final String CONFIG_KEY = "admin:config";

    private final UserMapper userMapper;
    private final ConversationMapper conversationMapper;
    private final KnowledgeDocMapper knowledgeDocMapper;
    private final FeedbackMapper feedbackMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Map<String, Long> overview(Long adminId) {
        assertAdmin(adminId);
        Map<String, Long> stat = new LinkedHashMap<>();
        stat.put("userCount", userMapper.selectCount(null));
        stat.put("conversationCount", conversationMapper.selectCount(null));
        stat.put("documentCount", knowledgeDocMapper.selectCount(null));
        stat.put("feedbackCount", feedbackMapper.selectCount(null));
        return stat;
    }

    @Override
    public PageVO<AdminUserVO> listUsers(Long adminId, long page, long pageSize, String keyword) {
        assertAdmin(adminId);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword.trim());
        }
        Page<User> p = userMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<AdminUserVO> records = p.getRecords().stream()
                .map(u -> new AdminUserVO(u.getId(), u.getUsername(), u.getRole(), u.getStatus(), u.getCreatedAt()))
                .toList();
        return new PageVO<>(records, p.getTotal(), p.getCurrent(), p.getSize());
    }

    @Override
    public void updateUserStatus(Long adminId, Long userId, String status) {
        assertAdmin(adminId);
        if (Objects.equals(adminId, userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不能禁用或修改自己的状态");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus(status);
        userMapper.updateById(user);
        log.info("管理员 {} 将用户 {} 状态改为 {}", adminId, userId, status);
    }

    @Override
    public Map<String, Object> getConfig(Long adminId) {
        assertAdmin(adminId);
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(CONFIG_KEY);
        Map<String, Object> result = new LinkedHashMap<>();
        entries.forEach((k, v) -> result.put(String.valueOf(k), String.valueOf(v)));
        return result;
    }

    @Override
    public void updateConfig(Long adminId, Map<String, Object> config) {
        assertAdmin(adminId);
        if (config == null || config.isEmpty()) {
            return;
        }
        config.forEach((k, v) -> stringRedisTemplate.opsForHash().put(CONFIG_KEY, k, String.valueOf(v)));
        log.info("管理员 {} 更新系统配置: {}", adminId, config.keySet());
    }

    /** 校验当前用户是否为管理员 */
    private void assertAdmin(Long adminId) {
        User user = userMapper.selectById(adminId);
        if (user == null || !"admin".equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
