package com.monster.monsteraicode.service;

import com.monster.monsteraicode.model.dto.app.AppAddRequest;
import com.monster.monsteraicode.model.dto.app.AppAdminUpdateRequest;
import com.monster.monsteraicode.model.dto.app.AppQueryRequest;
import com.monster.monsteraicode.model.dto.app.AppUpdateRequest;
import com.monster.monsteraicode.model.dto.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.monster.monsteraicode.entity.App;
import com.monster.monsteraicode.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/monster999">monster</a>
 */
public interface AppService extends IService<App> {

    /**
     * 创建应用
     *
     * @param appAddRequest 创建请求
     * @param loginUser     登录用户
     * @return 新应用 id
     */
    long addApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 更新应用（用户只能更新自己的应用名称）
     *
     * @param appUpdateRequest 更新请求
     * @param loginUser        登录用户
     * @return 是否更新成功
     */
    boolean updateApp(AppUpdateRequest appUpdateRequest, User loginUser);

    /**
     * 更新应用（管理员可以更新任意应用）
     *
     * @param appUpdateRequest 更新请求
     * @param loginUser        登录用户
     * @return 是否更新成功
     */
    boolean updateAppAdmin(AppAdminUpdateRequest appUpdateRequest, User loginUser);

    /**
     * 删除应用（用户只能删除自己的应用）
     *
     * @param id        应用 id
     * @param loginUser 登录用户
     * @return 是否删除成功
     */
    boolean deleteApp(long id, User loginUser);

    /**
     * 获取应用详情（用户只能查看自己的应用）
     *
     * @param id        应用 id
     * @param loginUser 登录用户
     * @return 应用详情
     */
    App getApp(long id, User loginUser);

    /**
     * 分页查询当前用户自己的应用列表
     *
     * @param appQueryRequest 查询请求
     * @param loginUser       登录用户
     * @return 分页结果
     */
    com.mybatisflex.core.paginate.Page<App> listMyAppsByPage(AppQueryRequest appQueryRequest, User loginUser);

    /**
     * 分页查询精选的应用列表（优先级为 99）
     *
     * @param appQueryRequest 查询请求
     * @return 分页结果
     */
    com.mybatisflex.core.paginate.Page<App> listFeaturedAppsByPage(AppQueryRequest appQueryRequest);

    /**
     * 获取脱敏的应用信息
     *
     * @param app 应用实体
     * @return 脱敏后的应用信息
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用列表的脱敏信息
     *
     * @param appList 应用列表
     * @return 脱敏后的应用列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取查询条件包装器
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件包装器
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 聊天生成代码
     *
     * @param appId     应用 id
     * @param message   聊天消息
     * @param loginUser 登录用户
     * @return 代码
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    String deployApp(Long appId, User loginUser);
}
