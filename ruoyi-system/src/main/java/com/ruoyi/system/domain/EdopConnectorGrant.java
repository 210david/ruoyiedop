package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 连接器逐用户授权对象 edop_connector_grant
 *
 * grant_token 明文只在 login 颁发时返回一次，库中仅存 SHA-256 哈希
 *
 * @author edop
 */
public class EdopConnectorGrant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 授权ID */
    private Long grantId;

    /** EDOP用户ID */
    private Long userId;

    /** grant_token的SHA-256哈希 */
    private String grantTokenHash;

    /** 授权时手机号快照（换绑检测） */
    private String phoneSnapshot;

    /** 授权来源描述 */
    private String clientDesc;

    /** 状态（0正常 1停用） */
    private String status;

    /** 过期时间（90天滚动续期） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /** 最近使用时间（僵尸授权清理依据） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUsedTime;

    /** 关联查询：用户账号 */
    private String userName;

    /** 关联查询：当前手机号 */
    private String phonenumber;

    /** 关联查询：部门名称 */
    private String deptName;

    /** 颁发时的明文 token（仅 createGrant 返回时临时携带，不落库） */
    private String grantToken;

    public String getGrantToken()
    {
        return grantToken;
    }

    public void setGrantToken(String grantToken)
    {
        this.grantToken = grantToken;
    }

    public Long getGrantId()
    {
        return grantId;
    }

    public void setGrantId(Long grantId)
    {
        this.grantId = grantId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getGrantTokenHash()
    {
        return grantTokenHash;
    }

    public void setGrantTokenHash(String grantTokenHash)
    {
        this.grantTokenHash = grantTokenHash;
    }

    public String getPhoneSnapshot()
    {
        return phoneSnapshot;
    }

    public void setPhoneSnapshot(String phoneSnapshot)
    {
        this.phoneSnapshot = phoneSnapshot;
    }

    public String getClientDesc()
    {
        return clientDesc;
    }

    public void setClientDesc(String clientDesc)
    {
        this.clientDesc = clientDesc;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Date expireTime)
    {
        this.expireTime = expireTime;
    }

    public Date getLastUsedTime()
    {
        return lastUsedTime;
    }

    public void setLastUsedTime(Date lastUsedTime)
    {
        this.lastUsedTime = lastUsedTime;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }
}
