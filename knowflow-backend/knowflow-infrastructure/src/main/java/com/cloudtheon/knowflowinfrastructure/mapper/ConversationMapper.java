package com.cloudtheon.knowflowinfrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudtheon.knowflowinfrastructure.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话 Mapper
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
