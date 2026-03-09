package com.monster.monsteraicode.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.monster.monsteraicode.entity.ChatHistory;
import com.monster.monsteraicode.mapper.ChatHistoryMapper;
import com.monster.monsteraicode.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/monster999">monster</a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

}
