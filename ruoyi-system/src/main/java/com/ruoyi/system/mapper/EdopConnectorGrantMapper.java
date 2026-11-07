package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.EdopConnectorGrant;

/**
 * 连接器逐用户授权 数据层
 *
 * @author edop
 */
public interface EdopConnectorGrantMapper
{
    /**
     * 管理员查询授权列表（关联用户/部门）
     */
    public List<EdopConnectorGrant> selectGrantList(EdopConnectorGrant grant);

    /**
     * 按用户查询其全部授权
     */
    public List<EdopConnectorGrant> selectGrantsByUserId(Long userId);

    /**
     * 按grant_token哈希查询
     */
    public EdopConnectorGrant selectGrantByTokenHash(String grantTokenHash);

    /**
     * 按ID查询
     */
    public EdopConnectorGrant selectGrantByGrantId(Long grantId);

    /**
     * 新增授权
     */
    public int insertGrant(EdopConnectorGrant grant);

    /**
     * 停用/启用授权
     */
    public int updateGrantStatus(@Param("grantId") Long grantId, @Param("status") String status);

    /**
     * 滚动续期：刷新最近使用时间与过期时间
     */
    public int touchGrant(@Param("grantId") Long grantId, @Param("expireTime") java.util.Date expireTime);
}
