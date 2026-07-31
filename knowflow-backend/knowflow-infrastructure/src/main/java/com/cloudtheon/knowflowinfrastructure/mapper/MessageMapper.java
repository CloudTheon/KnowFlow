package com.cloudtheon.knowflowinfrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudtheon.knowflowinfrastructure.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息 Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
