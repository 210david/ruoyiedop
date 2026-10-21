package com.ruoyi.qms.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.qms.domain.QmsLotGenealogy;

/**
 * 批次谱系 Mapper
 *
 * @author ruoyi
 */
public interface QmsLotGenealogyMapper
{
    /** 查询谱系列表 */
    List<QmsLotGenealogy> selectGenealogyList(QmsLotGenealogy genealogy);

    /** 按ID查询 */
    QmsLotGenealogy selectGenealogyById(Long id);

    /** 按投入批次号查询（正向追溯：找下游产出） */
    List<QmsLotGenealogy> selectByParentBatchNo(String parentBatchNo);

    /** 按产出批次号查询（反向追溯：找上游投入） */
    List<QmsLotGenealogy> selectByChildBatchNo(String childBatchNo);

    /** 查询所有断点谱系（支持条件筛选） */
    List<QmsLotGenealogy> selectBreakList(QmsLotGenealogy genealogy);

    /** 统计已关联工单数 */
    int countLinkedOrders();

    /** 统计断点工单数 */
    int countBreakOrders();

    /** 新增 */
    int insertGenealogy(QmsLotGenealogy genealogy);

    /** 批量新增 */
    int batchInsertGenealogy(List<QmsLotGenealogy> list);

    /** 修改 */
    int updateGenealogy(QmsLotGenealogy genealogy);

    /** 删除 */
    int deleteGenealogyByIds(Long[] ids);

    /** 检测谱系环：查询某批次是否已经是某路径上的祖先 */
    int checkCycle(String parentBatchNo, String childBatchNo);

    /** 幂等检查：按来源类型+来源单据号统计未删除谱系数 */
    int countBySource(@Param("sourceType") String sourceType, @Param("sourceNo") String sourceNo);

    /** 按来源类型+来源单据号查询谱系记录（业务单据联动回填/撤销用；exact=1 精确匹配，否则含 单号-序号 前缀匹配） */
    List<QmsLotGenealogy> selectBySource(@Param("sourceType") String sourceType, @Param("sourceNo") String sourceNo, @Param("exact") Integer exact);
}
