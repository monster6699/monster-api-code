package com.monster.monsteraicode.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.monster.monsteraicode.annotation.AuthCheck;
import com.monster.monsteraicode.common.BaseResponse;
import com.monster.monsteraicode.common.DeleteRequest;
import com.monster.monsteraicode.common.ResultUtils;
import com.monster.monsteraicode.constant.UserConstant;
import com.monster.monsteraicode.exception.BusinessException;
import com.monster.monsteraicode.exception.ErrorCode;
import com.monster.monsteraicode.exception.ThrowUtils;
import com.monster.monsteraicode.model.dto.app.*;
import com.monster.monsteraicode.model.dto.vo.AppVO;
import com.monster.monsteraicode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.monster.monsteraicode.entity.App;
import com.monster.monsteraicode.entity.User;
import com.monster.monsteraicode.service.AppService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 应用 控制层。
 *
 * @author <a href="https://github.com/monster999">monster</a>
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Autowired
    private AppService appService;
    @Autowired
    private UserService userService;

    // ==================== 用户接口 ====================

    /**
     * 创建应用（须填写 initPrompt）
     */
    @PostMapping("/add")
    @Operation(summary = "创建应用")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = appService.getLoginUser(request);
        long result = appService.addApp(appAddRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 修改自己的应用（目前只支持修改应用名称）
     */
    @PostMapping("/update")
    @Operation(summary = "更新应用（用户只能更新自己的应用）")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = appService.getLoginUser(request);
        boolean result = appService.updateApp(appUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 删除自己的应用
     */
    @PostMapping("/delete")
    @Operation(summary = "删除应用（用户只能删除自己的应用）")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = appService.getLoginUser(request);
        boolean result = appService.deleteApp(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 查看应用详情
     */
    @GetMapping("/get")
    @Operation(summary = "根据 id 获取应用详情（用户只能查看自己的应用）")
    public BaseResponse<AppVO> getApp(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = appService.getLoginUser(request);
        App app = appService.getApp(id, loginUser);
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 分页查询自己的应用列表（支持根据名称查询，每页最多 20 个）
     */
    @PostMapping("/list/my/page")
    @Operation(summary = "分页查询当前用户自己的应用列表")
    public BaseResponse<Page<AppVO>> listMyAppsByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = appService.getLoginUser(request);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        // 每页最多 20 个
        if (pageSize > 20) {
            pageSize = 20;
        }
        Page<App> appPage = appService.listMyAppsByPage(appQueryRequest, loginUser);
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页查询精选的应用列表（支持根据名称查询，每页最多 20 个）
     */
    @PostMapping("/list/featured/page")
    @Operation(summary = "分页查询精选的应用列表")
    public BaseResponse<Page<AppVO>> listFeaturedAppsByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        // 每页最多 20 个
        if (pageSize > 20) {
            pageSize = 20;
        }
        Page<App> appPage = appService.listFeaturedAppsByPage(appQueryRequest);
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员根据 id 删除任意应用
     */
    @PostMapping("/delete/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "删除应用（管理员可以删除任意应用）")
    public BaseResponse<Boolean> deleteAppAdmin(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = appService.getLoginUser(request);
        boolean result = appService.deleteApp(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 管理员根据 id 更新任意应用（支持更新应用名称、应用封面、优先级）
     */
    @PostMapping("/update/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "更新应用（管理员可以更新任意应用）")
    public BaseResponse<Boolean> updateAppAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = appService.getLoginUser(request);
        boolean result = appService.updateAppAdmin(appAdminUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 管理员根据 id 查看应用详情
     */
    @GetMapping("/get/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "根据 id 获取应用详情（管理员可以查看任意应用）")
    public BaseResponse<AppVO> getAppAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 管理员分页查询应用列表（支持根据除时间外的任何字段查询，每页数量不限）
     */
    @PostMapping("/list/page/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "分页查询应用列表（管理员）")
    public BaseResponse<Page<AppVO>> listAppsByPageAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        // 管理员每页数量不限
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                appService.getQueryWrapper(appQueryRequest));
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 应用聊天生成代码（流式 SSE）
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @param request 请求对象
     * @return 生成结果流
     */
    @Operation(summary = "应用聊天生成代码（流式 SSE）")
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务生成代码（流式）
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
        // 转换为 ServerSentEvent 格式
        return contentFlux
                .map(chunk -> {
                    // 将内容包装成JSON对象
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                }).concatWith(Mono.just(
                        //发送消息
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }


    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @Operation(summary = "应用部署")
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }




    // ==================== 基础接口（保留） ====================

    /**
     * 保存应用。
     *
     * @param app 应用
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody App app) {
        return appService.save(app);
    }

    /**
     * 根据主键删除应用。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return appService.removeById(id);
    }

    /**
     * 根据主键更新应用。
     *
     * @param app 应用
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody App app) {
        return appService.updateById(app);
    }

    /**
     * 查询所有应用。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<App> list() {
        return appService.list();
    }

    /**
     * 根据主键获取应用。
     *
     * @param id 应用主键
     * @return 应用详情
     */
    @GetMapping("getInfo/{id}")
    public App getInfo(@PathVariable Long id) {
        return appService.getById(id);
    }

    /**
     * 分页查询应用。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<App> page(Page<App> page) {
        return appService.page(page);
    }

}
