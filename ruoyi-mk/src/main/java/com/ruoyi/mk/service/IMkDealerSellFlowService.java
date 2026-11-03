package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkDealerSellFlow;

/**
 * 经销商分销流向登记 Service接口
 *
 * @author ruoyi
 */
public interface IMkDealerSellFlowService
{
    /** 查询流向登记列表 */
    public List<MkDealerSellFlow> selectFlowList(MkDealerSellFlow flow);

    /** 按流向ID查询 */
    public MkDealerSellFlow selectFlowById(Long flowId);

    /** 新增流向（含防重校验） */
    public int insertFlow(MkDealerSellFlow flow);

    /** 修改流向 */
    public int updateFlow(MkDealerSellFlow flow);

    /** 批量删除流向 */
    public int deleteFlowByIds(Long[] flowIds);

    /** 按发货单生成流向草稿（不入库，前端确认后提交批量保存） */
    public List<MkDealerSellFlow> generateDraftByShipment(Long shipmentId);

    /** 批量保存草稿（按发货单+物料防重，已登记的跳过），返回保存条数 */
    public int batchSaveFlows(List<MkDealerSellFlow> flows, String operName);
}
