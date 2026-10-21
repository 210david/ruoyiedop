package com.ruoyi.pdm.service;

import java.util.List;
import com.ruoyi.pdm.domain.PdmMbomRelease;

/**
 * BOM下发记录 Service 接口
 *
 * 业务规则（设计方案 7.4 / 协调方案衔接点一 D1/D3）：
 * 1. 仅"生效版本"的设计BOM允许下发，下发前校验物料申请全部确认、明细物料编码有效；
 * 2. 经 mms receiveMbom 落库为标准BOM（bomType=0,status=1），同产品旧已发布BOM置停用；
 * 3. 下发记录保存 EBOM 与 MBOM 的版本对应关系，重复下发旧记录置"已替代"。
 *
 * @author ruoyi
 */
public interface IPdmMbomReleaseService
{
    /** 查询下发记录列表 */
    public List<PdmMbomRelease> selectReleaseList(PdmMbomRelease release);

    /** 生效设计BOM一键下发转换为制造BOM（mms 接收），返回下发记录 */
    public PdmMbomRelease releaseMbom(Long bomId);
}
