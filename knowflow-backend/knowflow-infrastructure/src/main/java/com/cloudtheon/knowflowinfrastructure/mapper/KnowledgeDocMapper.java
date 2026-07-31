package com.cloudtheon.knowflowinfrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudtheon.knowflowinfrastructure.entity.KnowledgeDoc;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文档 Mapper
 */
@Mapper
public interface KnowledgeDocMapper extends BaseMapper<KnowledgeDoc> {
}
