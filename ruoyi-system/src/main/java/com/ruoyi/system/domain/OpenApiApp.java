package com.ruoyi.system.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 开放接口应用表 openapi_app
 *
 * @author ruoyi
 */
public class OpenApiApp extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 应用ID */
    private Long appId;

    /** 应用名称 */
    @Excel(name = "应用名称")
    @NotBlank(message = "应用名称不能为空")
    @Size(min = 0, max = 100, message = "应用名称长度不能超过100个字符")
    private String appName;

    /** API Key */
    @Excel(name = "API Key")
    @NotBlank(message = "API Key不能为空")
    @Size(min = 0, max = 128, message = "API Key长度不能超过128个字符")
    private String appKey;

    /** 允许访问的模块 */
    @Excel(name = "允许模块")
    private String allowedModules;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getAppId() { return appId; }
    public void setAppId(Long appId) { this.appId = appId; }
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getAllowedModules() { return allowedModules; }
    public void setAllowedModules(String allowedModules) { this.allowedModules = allowedModules; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("appId", getAppId())
            .append("appName", getAppName())
            .append("appKey", getAppKey())
            .append("allowedModules", getAllowedModules())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
