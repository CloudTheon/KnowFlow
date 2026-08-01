package com.cloudtheon.knowflowcore.service;

import com.cloudtheon.knowflowcore.vo.AdminUserVO;
import com.cloudtheon.knowflowcore.vo.PageVO;

import java.util.Map;

/**
 * 系统管理后台业务接口（仅管理员可用）
 */
public interface AdminService {

    /**
     * 平台数据概览统计
     *
     * @param adminId 管理员用户 ID
     * @return 统计项（用户数/对话数/文档数/反馈数）
     */
    Map<String, Long> overview(Long adminId);

    /**
     * 分页查询用户列表
     *
     * @param adminId  管理员用户 ID
     * @param page     页码
     * @param pageSize 每页数量
     * @param keyword  用户名模糊搜索（可选）
     * @return 分页数据
     */
    PageVO<AdminUserVO> listUsers(Long adminId, long page, long pageSize, String keyword);

    /**
     * 启用/禁用用户
     *
     * @param adminId 管理员用户 ID
     * @param userId  目标用户 ID
     * @param status  目标状态
     */
    void updateUserStatus(Long adminId, Long userId, String status);

    /**
     * 获取系统配置（存于 Redis）
     *
     * @param adminId 管理员用户 ID
     * @return 配置键值
     */
    Map<String, Object> getConfig(Long adminId);

    /**
     * 更新系统配置
     *
     * @param adminId 管理员用户 ID
     * @param config  配置键值
     */
    void updateConfig(Long adminId, Map<String, Object> config);
}
