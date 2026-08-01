package com.cloudtheon.knowflowcore.service;

import com.cloudtheon.knowflowcore.dto.FeedbackRequest;
import com.cloudtheon.knowflowcore.vo.FeedbackVO;
import com.cloudtheon.knowflowcore.vo.PageVO;

/**
 * 帮助与反馈业务接口
 */
public interface FeedbackService {

    /**
     * 提交反馈
     *
     * @param userId 当前用户 ID
     * @param req    反馈请求
     * @return 提交后的反馈信息
     */
    FeedbackVO submit(Long userId, FeedbackRequest req);

    /**
     * 查询我的反馈列表（分页，按提交时间倒序）
     *
     * @param userId   当前用户 ID
     * @param page     页码
     * @param pageSize 每页数量
     * @return 分页数据
     */
    PageVO<FeedbackVO> listMine(Long userId, long page, long pageSize);
}
