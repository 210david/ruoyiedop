package com.ruoyi.pdm.service;

import java.util.List;
import com.ruoyi.pdm.domain.PdmChangeOrder;
import com.ruoyi.pdm.domain.PdmChangeOrderItem;

/**
 * 技术变更单 Service 接口
 *
 * 业务规则（设计方案 7.6）：
 * 1. 变更申请自动列出该产品当前生效版本清单供勾选；
 * 2. 审批通过后进入"待执行"，设计员按变更单修改资料并上传新版本，关联变更单生效新版本；
 * 3. 变更执行完毕前原生效版本保持有效；关闭必须满足所有涉及资料均已升版生效；
 * 4. 变更记录不可删除（进入流程后），变更台账完整留痕。
 *
 * @author ruoyi
 */
public interface IPdmChangeOrderService
{
    /** 查询变更单列表（数据权限） */
    public List<PdmChangeOrder> selectChangeList(PdmChangeOrder change);

    /** 查询变更单详情（含涉及资料明细） */
    public PdmChangeOrder selectChangeById(Long changeId);

    /** 列出产品当前生效资料清单（供变更单勾选涉及资料） */
    public List<PdmChangeOrderItem> listEffectiveDocs(Long productId);

    /** 新增变更单（保存即取号，仅草稿） */
    public int insertChange(PdmChangeOrder change);

    /** 修改变更单（仅草稿/已驳回；涉及资料全量替换） */
    public int updateChange(PdmChangeOrder change);

    /** 批量删除（仅草稿；进入流程后变更记录不可删除） */
    public int deleteChangeByIds(Long[] changeIds);

    /** 提交审批（草稿/已驳回 → 审批中，推送审批人待办） */
    public int submitChange(Long changeId);

    /** 审批（审批中 → 通过=待执行 / 驳回=已驳回，意见留痕） */
    public int auditChange(Long changeId, boolean approved, String opinion);

    /** 执行明细：按变更单对资料升版生效（依据=技术变更单号） */
    public int executeItem(Long changeId, Long itemId);

    /** 关闭变更单（校验涉及资料全部升版生效；推送仓库/采购/质量/生产管控变更通知） */
    public int closeChange(Long changeId);
}
