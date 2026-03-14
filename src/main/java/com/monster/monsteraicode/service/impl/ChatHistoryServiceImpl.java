package com.monster.monsteraicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.monster.monsteraicode.constant.UserConstant;
import com.monster.monsteraicode.entity.App;
import com.monster.monsteraicode.entity.ChatHistory;
import com.monster.monsteraicode.entity.User;
import com.monster.monsteraicode.exception.BusinessException;
import com.monster.monsteraicode.exception.ErrorCode;
import com.monster.monsteraicode.exception.ThrowUtils;
import com.monster.monsteraicode.mapper.ChatHistoryMapper;
import com.monster.monsteraicode.model.dto.chathistory.ChatHistoryQueryRequest;
import com.monster.monsteraicode.model.dto.vo.ChatHistoryVO;
import com.monster.monsteraicode.model.dto.vo.UserVO;
import com.monster.monsteraicode.model.enums.MessageTypeEnum;
import com.monster.monsteraicode.service.AppService;
import com.monster.monsteraicode.service.ChatHistoryService;
import com.monster.monsteraicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/monster999">monster</a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Autowired
    private UserService userService;

    @Lazy
    @Autowired
    private AppService appService;

    @Override
    public long addUserMessage(Long appId, String message, Long userId) {
        return addMessage(appId, message, MessageTypeEnum.USER.getValue(), userId);
    }

    @Override
    public long addAiMessage(Long appId, String message, Long userId) {
        return addMessage(appId, message, MessageTypeEnum.AI.getValue(), userId);
    }

    @Override
    public List<ChatHistoryVO> listChatHistoryByAppId(Long appId, Long cursor, int size, User loginUser) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        }
        if (size <= 0 || size > 50) {
            size = 10;
        }
        // 校验权限：仅应用创建者和管理员可查看
        App app = appService.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()) && !app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的对话历史");
        }

        // 游标分页查询：按 id 倒序取 size 条，如果有 cursor 则取 id < cursor 的记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        if (cursor != null && cursor > 0) {
            queryWrapper.lt("id", cursor);
        }
        queryWrapper.orderBy("id", false);

        Page<ChatHistory> page = this.page(Page.of(1, size), queryWrapper);
        List<ChatHistory> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new ArrayList<>();
        }
        // 反转列表，使其按时间正序排列（便于前端展示）
        Collections.reverse(records);
        return getChatHistoryVOList(records);
    }

    @Override
    public Page<ChatHistory> listChatHistoryByPageAndAppId(Long appId, LocalDateTime lastCreateTime, int size, User loginUser) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        }
        if (size <= 0 || size > 50) {
            size = 10;
        }
        // 校验权限：仅应用创建者和管理员可查看
        App app = appService.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()) && !app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的对话历史");
        }
        ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setAppId(appId);
        chatHistoryQueryRequest.setLastCreateTime(lastCreateTime);
        chatHistoryQueryRequest.setPageSize(size);
        QueryWrapper queryWrapper = this.getQueryWrapper(chatHistoryQueryRequest);
        return this.page(Page.of(1, size), queryWrapper);

    }

    @Override
    public Page<ChatHistoryVO> listChatHistoryByPage(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        if (chatHistoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        QueryWrapper queryWrapper = getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> chatHistoryPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<ChatHistoryVO> chatHistoryVOPage = new Page<>(pageNum, pageSize, chatHistoryPage.getTotalRow());
        List<ChatHistoryVO> chatHistoryVOList = getChatHistoryVOList(chatHistoryPage.getRecords());
        chatHistoryVOPage.setRecords(chatHistoryVOList);
        return chatHistoryVOPage;
    }



    @Override
    public boolean deleteChatHistoryByAppId(Long appId) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        }
        QueryWrapper queryWrapper = QueryWrapper.create().eq("appId", appId);
        return this.remove(queryWrapper);
    }

    @Override
    public ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory) {
        if (chatHistory == null) {
            return null;
        }
        ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
        BeanUtil.copyProperties(chatHistory, chatHistoryVO);
        // 查询用户信息
        Long userId = chatHistory.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                UserVO userVO = userService.getUserVO(user);
                chatHistoryVO.setUser(userVO);
            }
        }
        return chatHistoryVO;
    }

    @Override
    public List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList) {
        if (CollUtil.isEmpty(chatHistoryList)) {
            return new ArrayList<>();
        }
        return chatHistoryList.stream().map(this::getChatHistoryVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        if (chatHistoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        String messageType = chatHistoryQueryRequest.getMessageType();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId)
                .eq("userId", userId)
                .eq("messageType", messageType);
        //游标查询逻辑 使用createTime字段作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 默认按创建时间降序
        if (sortField != null && !sortField.isEmpty()) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    /**
     * 保存一条消息
     */
    private long addMessage(Long appId, String message, String messageType, Long userId) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        }
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 ID 无效");
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        boolean saveResult = this.save(chatHistory);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存对话历史失败");
        }
        return chatHistory.getId();
    }
}
