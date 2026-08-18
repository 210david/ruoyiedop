package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsKitCheck;
import com.ruoyi.mms.domain.MmsKitCheckDetail;

/**
 * 齐套检查 Service接口
 *
 * 状态机：
 * 0(待检查) → 1(已通过) / 2(缺料)
 *
 * @author ruoyi
 */
public interface IMmsKitCheckService
{
    /** 标准CRUD */
    public List<MmsKitCheck> selectKitCheckList(MmsKitCheck kitCheck);
    public MmsKitCheck selectKitCheckById(Long kitId);
    public int insertKitCheck(MmsKitCheck kitCheck);
    public int updateKitCheck(MmsKitCheck kitCheck);
    public int deleteKitCheckByIds(Long[] kitIds);

    /** 齐套检查明细 */
    public List<MmsKitCheckDetail> selectKitCheckDetailByKitId(Long kitId);

    /**
     * 执行齐套检查
     * 根据工单BOM展开物料需求，对比库存计算齐套率
     * 状态：0(待检查) → 1(已通过) 或 2(缺料)
     */
    public int executeKitCheck(Long kitId);
}
