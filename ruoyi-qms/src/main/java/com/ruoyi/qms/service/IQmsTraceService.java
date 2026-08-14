package com.ruoyi.qms.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.qms.domain.QmsLotGenealogy;
import com.ruoyi.qms.domain.vo.TraceFallbackVO;

/**
 * 质量追溯 Service接口
 *
 * @author ruoyi
 */
public interface IQmsTraceService
{
    /** 谱系列表查询 */
    List<QmsLotGenealogy> selectGenealogyList(QmsLotGenealogy genealogy);

    /** 按ID查询谱系 */
    QmsLotGenealogy selectGenealogyById(Long id);

    /** 新增谱系 */
    int insertGenealogy(QmsLotGenealogy genealogy);

    /** 修改谱系 */
    int updateGenealogy(QmsLotGenealogy genealogy);

    /** 删除谱系 */
    int deleteGenealogyByIds(Long[] ids);

    /**
     * 正向追溯：原料/半成品批次 → 成品批次 → 客户/订单
     * 递归查找 parent_batch_no = X 的所有子节点，直至叶子节点
     */
    QmsLotGenealogy forwardTrace(String batchNo);

    /**
     * 反向追溯：成品批次 → 原料批次 → 供应商 → 检验记录/NCR
     * 递归查找 child_batch_no = X 的所有父节点，直至根节点
     */
    QmsLotGenealogy backwardTrace(String batchNo);

    /** 谱系完整度统计（已关联工单数/断点工单数/完整率） */
    Map<String, Object> traceCompleteness();

    /** 查询断点清单（支持条件筛选+分页） */
    List<QmsLotGenealogy> selectBreakList(QmsLotGenealogy genealogy);

    /** 批量补录谱系 */
    int batchInsertGenealogy(List<QmsLotGenealogy> list);

    /**
     * 降级追溯：当谱系表无数据时，利用已有业务单据中的 batch_no 做辅助关联追溯
     * @param batchNo 批次号
     * @param direction 追溯方向（forward=正向 / backward=反向）
     * @return 降级追溯结果（检验记录/NCR/仓库流转/采购收货/客诉等）
     */
    TraceFallbackVO fallbackTrace(String batchNo, String direction);
}
