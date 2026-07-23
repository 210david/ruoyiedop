package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.*;
import com.ruoyi.mk.mapper.*;
import com.ruoyi.mk.service.IMkOpportunityService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 商机 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkOpportunityServiceImpl implements IMkOpportunityService
{
    @Autowired
    private MkOpportunityMapper mkOpportunityMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private MkOpportunityStageMapper mkOpportunityStageMapper;

    @Autowired
    private MkOpportunityStageLogMapper mkOpportunityStageLogMapper;

    @Autowired
    private MkInteractionMapper mkInteractionMapper;

    @Autowired
    private MkContractMapper mkContractMapper;

    @Autowired
    private MkOrderMapper mkOrderMapper;

    @Override
    public List<MkOpportunity> selectOpportunityList(MkOpportunity opportunity)
    {
        return mkOpportunityMapper.selectOpportunityList(opportunity);
    }

    @Override
    public MkOpportunity selectOpportunityById(Long opportunityId)
    {
        return mkOpportunityMapper.selectOpportunityById(opportunityId);
    }

    @Override
    public int insertOpportunity(MkOpportunity opportunity)
    {
        opportunity.setDelFlag("0");
        if (opportunity.getOpportunityStatus() == null)
        {
            opportunity.setOpportunityStatus("0");
        }
        if (opportunity.getStageCode() == null)
        {
            opportunity.setStageCode("stage1");
            opportunity.setStageName("初步接触");
            opportunity.setWinRate(10);
        }
        // 自动生成商机编号
        if (StringUtils.isEmpty(opportunity.getOpportunityNo()))
        {
            opportunity.setOpportunityNo(mkNumberRuleService.generateNumber("opportunity"));
        }
        // 设置进入阶段时间
        opportunity.setStageEnterTime(new Date());
        // 计算加权金额
        calcWeightedAmount(opportunity);
        return mkOpportunityMapper.insertOpportunity(opportunity);
    }

    @Override
    public int updateOpportunity(MkOpportunity opportunity)
    {
        // 重新计算加权金额
        calcWeightedAmount(opportunity);
        return mkOpportunityMapper.updateOpportunity(opportunity);
    }

    @Override
    public int deleteOpportunityByIds(Long[] opportunityIds)
    {
        return mkOpportunityMapper.deleteOpportunityByIds(opportunityIds);
    }

    @Override
    public List<MkOpportunity> selectOpportunityByStage(String stageCode)
    {
        return mkOpportunityMapper.selectOpportunityByStage(stageCode);
    }

    // ==================== 阶段推进引擎 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int advanceStage(Long opportunityId, String toStageCode, String remark)
    {
        MkOpportunity opp = mkOpportunityMapper.selectOpportunityById(opportunityId);
        if (opp == null)
        {
            throw new ServiceException("商机不存在");
        }
        if (!"0".equals(opp.getOpportunityStatus()))
        {
            throw new ServiceException("非进行中状态的商机无法推进阶段");
        }

        // 获取目标阶段
        MkOpportunityStage toStage = getStageByCode(toStageCode);
        if (toStage == null)
        {
            throw new ServiceException("目标阶段不存在: " + toStageCode);
        }

        // 校验阶段顺序：不能跳到更靠前的阶段（用advance的话）
        MkOpportunityStage fromStage = getStageByCode(opp.getStageCode());
        if (fromStage != null && toStage.getSort() <= fromStage.getSort())
        {
            throw new ServiceException("推进目标阶段必须在当前阶段之后，如需退回请使用退回功能");
        }

        // 校验必填字段
        validateRequiredFields(opp, toStage);

        // 记录旧阶段
        String oldStageCode = opp.getStageCode();
        String oldStageName = opp.getStageName();

        // 更新商机阶段
        opp.setStageCode(toStage.getStageCode());
        opp.setStageName(toStage.getStageName());
        opp.setWinRate(toStage.getWinRate());
        opp.setStageEnterTime(new Date());
        opp.setUpdateBy(SecurityUtils.getUsername());
        calcWeightedAmount(opp);
        mkOpportunityMapper.updateOpportunityStage(opp);

        // 记录阶段变更日志
        insertStageLog(opportunityId, oldStageCode, oldStageName, toStage.getStageCode(), toStage.getStageName(), "advance", remark);

        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int retreatStage(Long opportunityId, String toStageCode, String remark)
    {
        MkOpportunity opp = mkOpportunityMapper.selectOpportunityById(opportunityId);
        if (opp == null)
        {
            throw new ServiceException("商机不存在");
        }
        if (!"0".equals(opp.getOpportunityStatus()))
        {
            throw new ServiceException("非进行中状态的商机无法退回阶段");
        }

        MkOpportunityStage toStage = getStageByCode(toStageCode);
        if (toStage == null)
        {
            throw new ServiceException("目标阶段不存在: " + toStageCode);
        }

        // 校验阶段顺序：退回目标必须在当前阶段之前
        MkOpportunityStage fromStage = getStageByCode(opp.getStageCode());
        if (fromStage != null && toStage.getSort() >= fromStage.getSort())
        {
            throw new ServiceException("退回目标阶段必须在当前阶段之前，如需推进请使用推进功能");
        }

        String oldStageCode = opp.getStageCode();
        String oldStageName = opp.getStageName();

        opp.setStageCode(toStage.getStageCode());
        opp.setStageName(toStage.getStageName());
        opp.setWinRate(toStage.getWinRate());
        opp.setStageEnterTime(new Date());
        opp.setUpdateBy(SecurityUtils.getUsername());
        calcWeightedAmount(opp);
        mkOpportunityMapper.updateOpportunityStage(opp);

        insertStageLog(opportunityId, oldStageCode, oldStageName, toStage.getStageCode(), toStage.getStageName(), "retreat", remark);

        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int winOpportunity(Long opportunityId, MkOpportunity param)
    {
        MkOpportunity opp = mkOpportunityMapper.selectOpportunityById(opportunityId);
        if (opp == null)
        {
            throw new ServiceException("商机不存在");
        }
        if (!"0".equals(opp.getOpportunityStatus()))
        {
            throw new ServiceException("非进行中状态的商机无法赢单");
        }
        if (param.getActualAmount() == null)
        {
            throw new ServiceException("赢单必须填写实际成交金额");
        }
        if (param.getActualDate() == null)
        {
            throw new ServiceException("赢单必须填写实际成交日期");
        }

        String oldStageCode = opp.getStageCode();
        String oldStageName = opp.getStageName();

        // 更新为赢单状态
        opp.setOpportunityStatus("1");
        opp.setActualAmount(param.getActualAmount());
        opp.setActualDate(param.getActualDate());
        opp.setUpdateBy(SecurityUtils.getUsername());

        // 推进到最终阶段
        MkOpportunityStage lastStage = getLastStage();
        if (lastStage != null)
        {
            opp.setStageCode(lastStage.getStageCode());
            opp.setStageName(lastStage.getStageName());
            opp.setWinRate(100);
            opp.setWeightedAmount(param.getActualAmount());
        }
        opp.setStageEnterTime(new Date());
        mkOpportunityMapper.updateOpportunityStage(opp);

        insertStageLog(opportunityId, oldStageCode, oldStageName, opp.getStageCode(), opp.getStageName(), "win", param.getRemark());

        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int loseOpportunity(Long opportunityId, MkOpportunity param)
    {
        MkOpportunity opp = mkOpportunityMapper.selectOpportunityById(opportunityId);
        if (opp == null)
        {
            throw new ServiceException("商机不存在");
        }
        if (!"0".equals(opp.getOpportunityStatus()))
        {
            throw new ServiceException("非进行中状态的商机无法输单");
        }
        if (StringUtils.isEmpty(param.getLostReason()))
        {
            throw new ServiceException("输单必须填写输单原因");
        }

        String oldStageCode = opp.getStageCode();
        String oldStageName = opp.getStageName();

        opp.setOpportunityStatus("2");
        opp.setLostReason(param.getLostReason());
        opp.setLostRemark(param.getLostRemark());
        opp.setWinRate(0);
        opp.setWeightedAmount(BigDecimal.ZERO);
        opp.setUpdateBy(SecurityUtils.getUsername());
        mkOpportunityMapper.updateOpportunityStage(opp);

        insertStageLog(opportunityId, oldStageCode, oldStageName, null, null, "lose", param.getLostReason() + (StringUtils.isNotEmpty(param.getLostRemark()) ? " - " + param.getLostRemark() : ""));

        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reopenOpportunity(Long opportunityId)
    {
        MkOpportunity opp = mkOpportunityMapper.selectOpportunityById(opportunityId);
        if (opp == null)
        {
            throw new ServiceException("商机不存在");
        }
        if ("0".equals(opp.getOpportunityStatus()))
        {
            throw new ServiceException("进行中的商机无需重开");
        }

        opp.setOpportunityStatus("0");
        opp.setActualAmount(null);
        opp.setActualDate(null);
        opp.setLostReason(null);
        opp.setLostRemark(null);
        opp.setStageEnterTime(new Date());
        opp.setUpdateBy(SecurityUtils.getUsername());
        mkOpportunityMapper.updateOpportunityStage(opp);

        insertStageLog(opportunityId, null, null, opp.getStageCode(), opp.getStageName(), "reopen", "重新打开商机");

        return 1;
    }

    @Override
    public List<MkOpportunityStageLog> selectStageLogList(Long opportunityId)
    {
        return mkOpportunityStageLogMapper.selectStageLogList(opportunityId);
    }

    @Override
    public Map<String, Object> selectOpportunityRelations(Long opportunityId)
    {
        Map<String, Object> result = new HashMap<>();

        // 互动记录
        MkInteraction interactionQuery = new MkInteraction();
        interactionQuery.setOpportunityId(opportunityId);
        result.put("interactions", mkInteractionMapper.selectInteractionList(interactionQuery));

        // 阶段变更日志
        result.put("stageLogs", mkOpportunityStageLogMapper.selectStageLogList(opportunityId));

        // 关联合同
        MkContract contractQuery = new MkContract();
        contractQuery.setOpportunityId(opportunityId);
        result.put("contracts", mkContractMapper.selectContractList(contractQuery));

        // 关联订单 - 通过合同关联
        List<MkContract> contracts = mkContractMapper.selectContractList(contractQuery);
        List<MkOrder> orders = new ArrayList<>();
        for (MkContract contract : contracts)
        {
            MkOrder orderQuery = new MkOrder();
            orderQuery.setContractId(contract.getContractId());
            orders.addAll(mkOrderMapper.selectOrderList(orderQuery));
        }
        result.put("orders", orders);

        return result;
    }

    @Override
    public List<MkOpportunity> selectStagnantOpportunities()
    {
        return mkOpportunityMapper.selectStagnantOpportunities();
    }

    // ==================== 私有辅助方法 ====================

    private void calcWeightedAmount(MkOpportunity opp)
    {
        if (opp.getExpectedAmount() != null && opp.getWinRate() != null)
        {
            opp.setWeightedAmount(
                opp.getExpectedAmount().multiply(new BigDecimal(opp.getWinRate())).divide(new BigDecimal(100)));
        }
    }

    private MkOpportunityStage getStageByCode(String stageCode)
    {
        MkOpportunityStage query = new MkOpportunityStage();
        query.setStatus("0");
        List<MkOpportunityStage> stages = mkOpportunityStageMapper.selectStageList(query);
        for (MkOpportunityStage s : stages)
        {
            if (s.getStageCode().equals(stageCode))
            {
                return s;
            }
        }
        return null;
    }

    private MkOpportunityStage getLastStage()
    {
        MkOpportunityStage query = new MkOpportunityStage();
        query.setStatus("0");
        List<MkOpportunityStage> stages = mkOpportunityStageMapper.selectStageList(query);
        if (stages != null && !stages.isEmpty())
        {
            return stages.get(stages.size() - 1);
        }
        return null;
    }

    private void validateRequiredFields(MkOpportunity opp, MkOpportunityStage toStage)
    {
        String requiredFields = toStage.getRequiredFields();
        if (StringUtils.isEmpty(requiredFields))
        {
            return;
        }
        String[] fields = requiredFields.split(",");
        StringBuilder missing = new StringBuilder();
        for (String field : fields)
        {
            String f = field.trim();
            switch (f)
            {
                case "expectedAmount":
                    if (opp.getExpectedAmount() == null) missing.append("预计金额、");
                    break;
                case "expectedDate":
                    if (opp.getExpectedDate() == null) missing.append("预计成交日期、");
                    break;
                case "painPoint":
                    if (StringUtils.isEmpty(opp.getPainPoint())) missing.append("客户痛点、");
                    break;
                case "solution":
                    if (StringUtils.isEmpty(opp.getSolution())) missing.append("解决方案、");
                    break;
                case "competitor":
                    if (StringUtils.isEmpty(opp.getCompetitor())) missing.append("竞争对手、");
                    break;
                case "contactId":
                    if (opp.getContactId() == null) missing.append("主要联系人、");
                    break;
            }
        }
        if (missing.length() > 0)
        {
            throw new ServiceException("推进到此阶段需要先完善: " + missing.substring(0, missing.length() - 1));
        }
    }

    private void insertStageLog(Long opportunityId, String fromCode, String fromName, String toCode, String toName, String actionType, String remark)
    {
        MkOpportunityStageLog log = new MkOpportunityStageLog();
        log.setOpportunityId(opportunityId);
        log.setFromStageCode(fromCode);
        log.setFromStageName(fromName);
        log.setToStageCode(toCode);
        log.setToStageName(toName);
        log.setActionType(actionType);
        log.setOperatorId(SecurityUtils.getUserId());
        log.setOperatorName(SecurityUtils.getUsername());
        log.setRemark(remark);
        mkOpportunityStageLogMapper.insertStageLog(log);
    }
}
