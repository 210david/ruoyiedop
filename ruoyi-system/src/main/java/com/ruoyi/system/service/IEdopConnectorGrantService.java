package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.EdopConnectorGrant;

/**
 * 连接器逐用户授权 服务接口
 *
 * @author edop
 */
public interface IEdopConnectorGrantService
{
    /**
     * 登录并创建授权，返回授权记录（grantToken 明文挂在 remark 临时携带，仅此一次返回）
     *
     * @param username   EDOP 用户名
     * @param password   密码
     * @param clientDesc 授权来源描述（如 WorkBuddy-张三）
     * @return 含明文 token 的授权记录
     */
    public EdopConnectorGrant createGrant(String username, String password, String clientDesc);

    /**
     * 为已登录用户直接创建授权（免二次输密码，登录态即身份凭证）
     *
     * @param user       当前登录用户（含最新状态）
     * @param clientDesc 授权来源描述（如 WorkBuddy-张三）
     * @return 含明文 token 的授权记录（grantToken 明文仅此一次返回）
     */
    public EdopConnectorGrant createGrantForUser(SysUser user, String clientDesc);

    /**
     * 校验 grant_token 并滚动续期（90 天），停用/过期/用户被停用均立即失效
     *
     * @param grantToken 明文 token
     * @return 授权记录（含 userId，供换取业务 JWT）
     */
    public EdopConnectorGrant validateGrant(String grantToken);

    /**
     * 管理员查询授权列表
     */
    public List<EdopConnectorGrant> selectGrantList(EdopConnectorGrant grant);

    /**
     * 查询我的授权
     */
    public List<EdopConnectorGrant> selectGrantsByUserId(Long userId);

    /**
     * 管理员吊销
     */
    public int revokeByAdmin(Long grantId);

    /**
     * 本人吊销（校验归属）
     */
    public int revokeByOwner(Long grantId, Long userId);
}
