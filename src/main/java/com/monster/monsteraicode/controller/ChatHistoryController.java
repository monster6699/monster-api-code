package com.monster.monsteraicode.controller;

import com.monster.monsteraicode.annotation.AuthCheck;
import com.monster.monsteraicode.common.BaseResponse;
import com.monster.monsteraicode.common.ResultUtils;
import com.monster.monsteraicode.constant.UserConstant;
import com.monster.monsteraicode.entity.ChatHistory;
import com.monster.monsteraicode.entity.User;
import com.monster.monsteraicode.exception.ErrorCode;
import com.monster.monsteraicode.exception.ThrowUtils;
import com.monster.monsteraicode.model.dto.chathistory.ChatHistoryQueryRequest;
import com.monster.monsteraicode.model.dto.vo.ChatHistoryVO;
import com.monster.monsteraicode.service.ChatHistoryService;
import com.monster.monsteraicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 控制层。
 *
 * @author <a href="https://github.com/monster999">monster</a>
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;
    @Autowired
    private UserService userService;

    // ==================== 用户接口 ====================

    /**
     * 游标分页查询某个应用的对话历史（仅应用创建者和管理员可见）
     * 首次加载不传 cursor，加载更多传上一页最小的消息 id
     */
    @GetMapping("/list")
    @Operation(summary = "游标分页查询应用的对话历史")
    public BaseResponse<List<ChatHistoryVO>> listChatHistoryByAppId(
            @RequestParam Long appId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        User loginUser = userService.getLoginUser(request);
        List<ChatHistoryVO> chatHistoryVOList = chatHistoryService.listChatHistoryByAppId(appId, cursor, size, loginUser);
        return ResultUtils.success(chatHistoryVOList);
    }

    @GetMapping("/list/page")
    @Operation(summary = "游标根据创建时间分页查询应用的对话历史")
    public BaseResponse<Page<ChatHistory>> listChatHistoryByPage(
            @RequestParam Long appId,
            @RequestParam(required = false) LocalDateTime lastCreateTime,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> chatHistoryPage = chatHistoryService.listChatHistoryByPageAndAppId(appId, lastCreateTime, size, loginUser);
        return ResultUtils.success(chatHistoryPage);
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员分页查询所有对话历史（按时间降序排序）
     */
    @PostMapping("/list/page/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "分页查询对话历史列表（管理员）")
    public BaseResponse<Page<ChatHistoryVO>> listChatHistoryByPage(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<ChatHistoryVO> chatHistoryVOPage = chatHistoryService.listChatHistoryByPage(chatHistoryQueryRequest);
        return ResultUtils.success(chatHistoryVOPage);
    }
}
