package com.monster.monsteraicode.service;

import com.monster.monsteraicode.entity.ChatHistory;
import com.monster.monsteraicode.entity.User;
import com.monster.monsteraicode.model.dto.chathistory.ChatHistoryQueryRequest;
import com.monster.monsteraicode.model.dto.vo.ChatHistoryVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/monster999">monster</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存用户消息
     *
     * @param appId   应用 id
     * @param message 消息内容
     * @param userId  用户 id
     * @return 消息 id
     */
    long addUserMessage(Long appId, String message, Long userId);

    /**
     * 保存 AI 消息
     *
     * @param appId   应用 id
     * @param message 消息内容
     * @param userId  用户 id
     * @return 消息 id
     */
    long addAiMessage(Long appId, String message, Long userId);

    /**
     * 游标分页查询某个应用的对话历史（按时间倒序，支持向前加载）
     *
     * @param appId     应用 id
     * @param cursor    游标（上一页最小的消息 id，首次加载传 null）
     * @param size      每页大小
     * @param loginUser 当前登录用户
     * @return 对话历史列表（按时间正序排列，便于前端展示）
     */
    List<ChatHistoryVO> listChatHistoryByAppId(Long appId, Long cursor, int size, User loginUser);


    /**
     * 游标分页查询某个应用的对话历史（按时间倒序，支持向前加载）
     *
     * @param appId     应用 id
     * @param lastCreateTime    游标 根据最后的创建时间查询
     * @param size      每页大小
     * @param loginUser 当前登录用户
     * @return 对话历史列表（按时间正序排列，便于前端展示）
     */
    Page<ChatHistory> listChatHistoryByPageAndAppId(Long appId, LocalDateTime lastCreateTime, int size, User loginUser);
    /**
     * 管理员分页查询对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 分页结果
     */
    Page<ChatHistoryVO> listChatHistoryByPage(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 根据应用 id 删除所有对话历史（删除应用时级联调用）
     *
     * @param appId 应用 id
     * @return 是否删除成功
     */
    boolean deleteChatHistoryByAppId(Long appId);

    /**
     * 获取脱敏的对话历史信息
     *
     * @param chatHistory 对话历史实体
     * @return 脱敏后的对话历史信息
     */
    ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory);

    /**
     * 获取对话历史列表的脱敏信息
     *
     * @param chatHistoryList 对话历史列表
     * @return 脱敏后的对话历史列表
     */
    List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList);

    /**
     * 获取查询条件包装器（管理员查询）
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 查询条件包装器
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
