package com.monster.monsteraicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.monster.monsteraicode.ai.model.enums.CodeGenTypeEnum;
import com.monster.monsteraicode.constant.AppConstant;
import com.monster.monsteraicode.constant.UserConstant;
import com.monster.monsteraicode.core.AiCodeGeneratorFacade;
import com.monster.monsteraicode.exception.BusinessException;
import com.monster.monsteraicode.exception.ErrorCode;
import com.monster.monsteraicode.exception.ThrowUtils;
import com.monster.monsteraicode.model.dto.app.AppAddRequest;
import com.monster.monsteraicode.model.dto.app.AppAdminUpdateRequest;
import com.monster.monsteraicode.model.dto.app.AppQueryRequest;
import com.monster.monsteraicode.model.dto.app.AppUpdateRequest;
import com.monster.monsteraicode.model.dto.vo.AppVO;
import com.monster.monsteraicode.model.dto.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.monster.monsteraicode.entity.App;
import com.monster.monsteraicode.entity.User;
import com.monster.monsteraicode.mapper.AppMapper;
import com.monster.monsteraicode.service.AppService;
import com.monster.monsteraicode.service.ChatHistoryService;
import com.monster.monsteraicode.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/monster999">monster</a>
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Autowired
    private UserService userService;
    @Autowired
    private ChatHistoryService chatHistoryService;
    @Autowired
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Override
    public long addApp(AppAddRequest appAddRequest, User loginUser) {
        if (appAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // initPrompt 必填
        if (appAddRequest.getInitPrompt() == null || appAddRequest.getInitPrompt().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用初始化的 prompt 不能为空");
        }
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        // 设置创建用户id
        app.setUserId(loginUser.getId());
        // 默认优先级为 0
        if (app.getPriority() == null) {
            app.setPriority(AppConstant.DEFAULT_APP_PRIORITY);
        }
        boolean saveResult = this.save(app);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建应用失败");
        }
        return app.getId();
    }

    @Override
    public boolean updateApp(AppUpdateRequest appUpdateRequest, User loginUser) {
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = appUpdateRequest.getId();
        // 查询应用是否存在
        App oldApp = this.getById(id);
        if (oldApp == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 校验是否为创建者（普通用户只能更新自己的应用）
        if (!oldApp.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限更新他人的应用");
        }
        // 用户只能更新应用名称
        String appName = appUpdateRequest.getAppName();
        if (appName != null) {
            oldApp.setAppName(appName);
        }
        // 设置编辑时间
        oldApp.setEditTime(LocalDateTime.now());
        return this.updateById(oldApp);
    }

    @Override
    public boolean updateAppAdmin(AppAdminUpdateRequest appUpdateRequest, User loginUser) {
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = appUpdateRequest.getId();
        // 查询应用是否存在
        App oldApp = this.getById(id);
        if (oldApp == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 管理员可以更新应用名称
        String appName = appUpdateRequest.getAppName();
        if (appName != null) {
            oldApp.setAppName(appName);
        }
        // 管理员可以更新封面
        String cover = appUpdateRequest.getCover();
        if (cover != null) {
            oldApp.setCover(cover);
        }
        // 管理员可以更新优先级
        Integer priority = appUpdateRequest.getPriority();
        if (priority != null) {
            oldApp.setPriority(priority);
        }
        // 设置编辑时间
        oldApp.setEditTime(LocalDateTime.now());
        return this.updateById(oldApp);
    }

    @Override
    public boolean deleteApp(long id, User loginUser) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询应用是否存在
        App oldApp = this.getById(id);
        if (oldApp == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 校验是否为创建者（管理员可以删除任意应用，普通用户只能删除自己的应用）
        if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()) && !oldApp.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除他人的应用");
        }
        // 级联删除该应用的所有对话历史
        chatHistoryService.deleteChatHistoryByAppId(id);
        return this.removeById(id);
    }

    @Override
    public App getApp(long id, User loginUser) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 校验是否为创建者（管理员可以查看任意应用，普通用户只能查看自己的应用）
        if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()) && !app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看他人的应用");
        }
        return app;
    }

    @Override
    public Page<App> listMyAppsByPage(AppQueryRequest appQueryRequest, User loginUser) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 每页最多 20 个
        int pageSize = Math.min(appQueryRequest.getPageSize(), 20);
        appQueryRequest.setPageSize(pageSize);
        // 设置用户id查询条件
        appQueryRequest.setUserId(loginUser.getId());
        // 构建查询条件
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);
        long pageNum = appQueryRequest.getPageNum();
        return this.page(Page.of(pageNum, pageSize), queryWrapper);
    }

    @Override
    public Page<App> listFeaturedAppsByPage(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 每页最多 20 个
        int pageSize = Math.min(appQueryRequest.getPageSize(), 20);
        appQueryRequest.setPageSize(pageSize);
        // 构建查询条件，筛选优先级为 99 的精选应用
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);
        queryWrapper.eq("priority", AppConstant.GOOD_APP_PRIORITY);
        long pageNum = appQueryRequest.getPageNum();
        return this.page(Page.of(pageNum, pageSize), queryWrapper);
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                UserVO userVO = userService.getUserVO(user);
                appVO.setUser(userVO);
            }
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        return appList.stream().map(this::getAppVO).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return userService.getLoginUser(request);
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 保存用户消息
        chatHistoryService.addUserMessage(appId, message, loginUser.getId());
        // 6. 调用 AI 生成代码，收集完整响应后保存 AI 消息
        StringBuilder aiResponse = new StringBuilder();
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId)
                .doOnNext(aiResponse::append)
                .doOnComplete(() -> {
                    String fullResponse = aiResponse.toString();
                    if (StrUtil.isNotBlank(fullResponse)) {
                        chatHistoryService.addAiMessage(appId, fullResponse, loginUser.getId());
                    }
                })
                .doOnError(e -> {
                    String errorMsg = "AI 回复失败：" + e.getMessage();
                    log.error("应用 {} 的 AI 生成代码失败", appId, e);
                    chatHistoryService.addAiMessage(appId, errorMsg, loginUser.getId());
                });
    }


    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 8. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 9. 返回可访问的 URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }


}
