package com.monster.monsteraicode.model.dto.chathistory;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 对话历史创建请求
 *
 * @author <a href="https://github.com/monster999">monster</a>
 */
@Data
public class ChatHistoryAddRequest implements Serializable {

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息类型（user/ai）
     */
    private String messageType;

    /**
     * 应用 id
     */
    private Long appId;

    @Serial
    private static final long serialVersionUID = 1L;
}
