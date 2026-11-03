package com.ruoyi.mk.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mk.domain.MkDealerSellFlow;

/**
 * 经销商分销流向登记 数据层
 *
 * @author ruoyi
 */
public interface MkDealerSellFlowMapper
{
    /** 查询流向登记列表（关联经销商与物料名） */
    public List<MkDealerSellFlow> selectFlowList(MkDealerSellFlow flow);

    /** 按流向ID查询 */
    public MkDealerSellFlow selectFlowById(Long flowId);

    /** 按发货单+物料查重（唯一键 uk_shipment_material 防重） */
    public MkDealerSellFlow selectByShipmentAndMaterial(@Param("shipmentId") Long shipmentId, @Param("materialId") Long materialId);

    /** 新增流向 */
    public int insertFlow(MkDealerSellFlow flow);

    /** 修改流向 */
    public int updateFlow(MkDealerSellFlow flow);

    /** 批量删除流向 */
    public int deleteFlowByIds(Long[] flowIds);

    /** 按发货单生成草稿来源行（发货单明细→订单明细→物料） */
    public List<MkDealerSellFlow> selectDraftItemsByShipmentId(Long shipmentId);
}
