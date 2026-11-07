package com.ruoyi.web.controller.system;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.web.service.SysPermissionService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.domain.EdopConnectorGrant;
import com.ruoyi.system.service.IEdopConnectorGrantService;
import com.ruoyi.system.service.ISysUserService;

/**
 * EDOP 连接器逐用户授权
 *
 * 通道设计：
 *  - POST /login     匿名，账号密码 → 颁发 grant_token（明文仅此一次）
 *  - POST /exchange  匿名（凭证为 grant_token），校验并换发业务 JWT（30min，网关专用）
 *  - GET  /config    匿名，下发网关地址（系统内授权页生成 MCP 配置用）
 *  - 其余端点走现有 JWT 鉴权体系
 *
 * @author edop
 */
@RestController
@RequestMapping("/edop/connector/auth")
public class EdopConnectorAuthController extends BaseController
{
    /**
     * MCP 网关地址，来自 application.yml edop.connector.gateway-url。
     * 留空/未配置时按当前请求地址自动推导（见 deriveGatewayUrl）：
     * 用户经什么地址访问 EDOP，网关即在同主机同地址的网关端口上可达，生产环境零配置改动。
     */
    @Value("${edop.connector.gateway-url:}")
    private String gatewayUrl;

    /** MCP 网关端口（gateway-url 留空时自动推导用） */
    @Value("${edop.connector.gateway-port:3000}")
    private int gatewayPort;

    @Autowired
    private IEdopConnectorGrantService grantService;

    /**
     * 下发网关地址（匿名：系统内授权页据此生成客户端 MCP 配置 JSON）
     */
    @Anonymous
    @GetMapping("/config")
    public AjaxResult config(HttpServletRequest request)
    {
        String url = (gatewayUrl == null || gatewayUrl.trim().isEmpty())
                ? deriveGatewayUrl(request) : gatewayUrl.trim();
        AjaxResult ok = AjaxResult.success();
        ok.put("gatewayUrl", url);
        return ok;
    }

    /**
     * 网关地址自动推导（gateway-url 留空时）：用户经什么地址访问 EDOP，
     * 网关即在同主机同地址的网关端口上可达。scheme 取 X-Forwarded-Proto（反代场景，
     * 未转发时即直连协议），host 取 Host 头（仅保留主机名部分，兼容 IPv6 字面量）。
     */
    private String deriveGatewayUrl(HttpServletRequest request)
    {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.trim().isEmpty())
        {
            scheme = request.getScheme();
        }
        String host = request.getHeader("Host");
        if (host == null || host.trim().isEmpty())
        {
            host = request.getServerName();
        }
        else
        {
            host = host.trim();
            int colon = host.lastIndexOf(':');
            // 仅当末冒号在 ']' 之后（即 IPv6 字面量之后的端口段）且其内容为纯数字时才截断
            if (colon > host.lastIndexOf(']') && host.substring(colon + 1).matches("\\d+"))
            {
                host = host.substring(0, colon);
            }
        }
        return scheme + "://" + host + ":" + gatewayPort;
    }

    /**
     * 系统内一键授权：基于当前登录态直接颁发 grant_token（免二次输密码）
     */
    @PostMapping("/create")
    public AjaxResult create(@RequestBody(required = false) Map<String, String> body)
    {
        String clientDesc = body == null ? "" : body.getOrDefault("clientDesc", "");
        try
        {
            SysUser user = userService.selectUserById(SecurityUtils.getUserId());
            EdopConnectorGrant grant = grantService.createGrantForUser(user, clientDesc);
            AjaxResult ok = AjaxResult.success("授权成功，请立即保存 token（仅显示一次）");
            ok.put("grantToken", grant.getGrantToken());
            ok.put("expireTime", grant.getExpireTime());
            return ok;
        }
        catch (ServiceException e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysPermissionService permissionService;

    /**
     * 授权登录：账号密码换取 grant_token（明文仅此一次返回）
     */
    @Anonymous
    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, String> body)
    {
        String username = body.getOrDefault("username", "");
        String password = body.getOrDefault("password", "");
        String clientDesc = body.getOrDefault("clientDesc", "");
        try
        {
            EdopConnectorGrant grant = grantService.createGrant(username, password, clientDesc);
            AjaxResult ok = AjaxResult.success("授权成功，请立即保存 token（仅显示一次）");
            ok.put("grantToken", grant.getGrantToken());
            ok.put("expireTime", grant.getExpireTime());
            ok.put("usage", "WorkBuddy 连接器 headers 填: Authorization: Bearer <grantToken>");
            return ok;
        }
        catch (ServiceException e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 凭证换发：grant_token → 业务 JWT（网关每次调用前换发并缓存）
     */
    @Anonymous
    @PostMapping("/exchange")
    public AjaxResult exchange(@RequestHeader(value = "Authorization", required = false) String authorization)
    {
        String grantToken = authorization == null ? "" : authorization.replaceFirst("(?i)^Bearer\\s+", "");
        EdopConnectorGrant grant;
        try
        {
            grant = grantService.validateGrant(grantToken);
        }
        catch (ServiceException e)
        {
            Integer code = e.getCode();
            return AjaxResult.error(code != null ? code : 401, e.getMessage());
        }

        SysUser user = userService.selectUserByUserName(grant.getUserName());
        if (user == null || "2".equals(user.getDelFlag()) || !"0".equals(user.getStatus()))
        {
            return AjaxResult.error(401, "账号状态异常，授权失效");
        }
        LoginUser loginUser = new LoginUser(user.getUserId(), user.getDeptId(), user,
                permissionService.getMenuPermission(user));
        String jwt = tokenService.createToken(loginUser);
        AjaxResult ok = AjaxResult.success();
        ok.put("token", jwt);
        ok.put("expiresIn", 25 * 60);
        ok.put("userName", user.getUserName());
        ok.put("deptName", grant.getDeptName());
        return ok;
    }

    /**
     * 我的授权列表
     */
    @GetMapping("/my")
    public AjaxResult my()
    {
        return AjaxResult.success(grantService.selectGrantsByUserId(SecurityUtils.getUserId()));
    }

    /**
     * 本人撤销授权
     */
    @PostMapping("/revoke/{grantId}")
    public AjaxResult revoke(@PathVariable Long grantId)
    {
        try
        {
            return toAjax(grantService.revokeByOwner(grantId, SecurityUtils.getUserId()));
        }
        catch (ServiceException e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 管理员：授权列表
     */
    @PreAuthorize("@ss.hasPermi('edop:grant:list')")
    @GetMapping("/admin/list")
    public TableDataInfo adminList(EdopConnectorGrant query)
    {
        startPage();
        List<EdopConnectorGrant> list = grantService.selectGrantList(query);
        return getDataTable(list);
    }

    /**
     * 管理员：强制吊销
     */
    @PreAuthorize("@ss.hasPermi('edop:grant:revoke')")
    @PostMapping("/admin/revoke/{grantId}")
    public AjaxResult adminRevoke(@PathVariable Long grantId)
    {
        return toAjax(grantService.revokeByAdmin(grantId));
    }
}
