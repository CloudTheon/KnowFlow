package com.cloudtheon.knowflowcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudtheon.knowflowcore.dto.FeedbackRequest;
import com.cloudtheon.knowflowcore.service.FeedbackService;
import com.cloudtheon.knowflowcore.vo.FeedbackVO;
import com.cloudtheon.knowflowcore.vo.PageVO;
import com.cloudtheon.knowflowinfrastructure.entity.Feedback;
import com.cloudtheon.knowflowinfrastructure.mapper.FeedbackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 帮助与反馈业务实现
 */
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    @Override
    public FeedbackVO submit(Long userId, FeedbackRequest req) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(req.getType());
        feedback.setContent(req.getContent());
        feedback.setContact(req.getContact());
        feedback.setStatus("pending");
        feedbackMapper.insert(feedback);
        return toVO(feedback);
    }

    @Override
    public PageVO<FeedbackVO> listMine(Long userId, long page, long pageSize) {
        Page<Feedback> p = feedbackMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Feedback>()
                        .eq(Feedback::getUserId, userId)
                        .orderByDesc(Feedback::getCreatedAt));
        List<FeedbackVO> records = p.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(records, p.getTotal(), p.getCurrent(), p.getSize());
    }

    private FeedbackVO toVO(Feedback f) {
        return new FeedbackVO(f.getId(), f.getType(), f.getContent(), f.getContact(),
                f.getStatus(), f.getCreatedAt());
    }
}
