package com.ruoyi.pms.service;

import com.ruoyi.pms.domain.dto.PmsEnterpriseInfoDTO;

/**
 * 企业信息查询 Service接口
 *
 * @author ruoyi
 */
public interface IPmsEnterpriseLookupService
{
    /**
     * 根据企业名称或统一信用代码查询企业信息
     *
     * @param keyword 企业名称或统一信用代码
     * @return 企业信息
     */
    PmsEnterpriseInfoDTO lookup(String keyword);
}
