package com.ruoyi.dms.service.impl;
import java.util.HashMap;
import java.util.Map;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.dms.domain.DmsSparePart;
import com.ruoyi.dms.domain.DmsSparePartRecord;
import com.ruoyi.dms.domain.DmsSparePartStock;
import com.ruoyi.dms.mapper.DmsSparePartMapper;
import com.ruoyi.dms.mapper.DmsSparePartRecordMapper;
import com.ruoyi.dms.mapper.DmsSparePartStockMapper;
import com.ruoyi.dms.service.IDmsSparePartService;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.system.utils.MessageHelper;

@Service
public class DmsSparePartServiceImpl implements IDmsSparePartService
{
    private static final String CODE_PREFIX = "BJ";

    @Autowired
    private DmsSparePartMapper dmsSparePartMapper;

    @Autowired
    private DmsSparePartRecordMapper dmsSparePartRecordMapper;

    @Autowired
    private DmsSparePartStockMapper dmsSparePartStockMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private MessageHelper messageHelper;

    @Override
    public List<DmsSparePart> selectSparePartList(DmsSparePart sparePart)
    {
        return dmsSparePartMapper.selectSparePartList(sparePart);
    }

    @Override
    public DmsSparePart selectSparePartById(Long partId)
    {
        return dmsSparePartMapper.selectSparePartById(partId);
    }

    @Override
    public int insertSparePart(DmsSparePart sparePart)
    {
        sparePart.setDelFlag("0");
        if (StringUtils.isEmpty(sparePart.getPartCode()))
        {
            Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(sparePart.getPartType())) { params.put("partType", sparePart.getPartType()); }
        sparePart.setPartCode(mkNumberRuleService.generateNumber("dms_spare_part", params));
        }
        return dmsSparePartMapper.insertSparePart(sparePart);
    }

    @Override
    public int updateSparePart(DmsSparePart sparePart)
    {
        return dmsSparePartMapper.updateSparePart(sparePart);
    }

    @Override
    public int deleteSparePartByIds(Long[] partIds)
    {
        return dmsSparePartMapper.deleteSparePartByIds(partIds);
    }

    @Override
    public String generatePartCode()
    {
        return mkNumberRuleService.generateNumber("dms_spare_part");
    }

    @Override
    public String generateInCode()
    {
        return mkNumberRuleService.generateNumber("dms_spare_in");
    }

    @Override
    public String generateOutCode()
    {
        return mkNumberRuleService.generateNumber("dms_spare_out");
    }

    @Override
    @Transactional
    public int stockMove(DmsSparePartRecord record)
    {
        // 检查备件是否存在
        DmsSparePart part = dmsSparePartMapper.selectSparePartById(record.getPartId());
        if (part == null) throw new ServiceException("备件不存在");
        if ("2".equals(part.getDelFlag())) throw new ServiceException("备件已删除，无法操作");

        // 0入库增加，其他出库减少
        boolean isInbound = "0".equals(record.getMoveType());
        BigDecimal qty = record.getQuantity();
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0)
            throw new ServiceException("数量必须大于0");

        // 确定仓库名称和库存记录
        DmsSparePartStock stock = null;
        String warehouseName;

        if (isInbound)
        {
            // 入库：从表单获取仓库名称，默认"备件库"
            warehouseName = (record.getWarehouseName() != null && !record.getWarehouseName().isEmpty())
                ? record.getWarehouseName() : "备件库";
            stock = dmsSparePartStockMapper.selectByPartAndWarehouse(record.getPartId(), warehouseName);
        }
        else
        {
            // 出库：通过 stockId 直接定位库存记录
            if (record.getStockId() != null)
            {
                stock = dmsSparePartStockMapper.selectStockById(record.getStockId());
            }
            if (stock == null && record.getWarehouseName() != null)
            {
                stock = dmsSparePartStockMapper.selectByPartAndWarehouse(record.getPartId(), record.getWarehouseName());
            }
            if (stock == null)
            {
                stock = dmsSparePartStockMapper.selectByPartAndWarehouse(record.getPartId(), "备件库");
            }
            warehouseName = (stock != null) ? stock.getWarehouseName() : "备件库";
        }
        BigDecimal before = (stock != null) ? stock.getCurrentStock() : BigDecimal.ZERO;

        BigDecimal after;
        if (isInbound)
        {
            after = before.add(qty);
        }
        else
        {
            after = before.subtract(qty);
            if (after.compareTo(BigDecimal.ZERO) < 0)
                throw new ServiceException("库存不足，当前库存：" + before + "，尝试出库：" + qty + "，仓库名称：" + warehouseName);
        }

        if (stock == null)
        {
            // 首次在仓库有库存，创建记录
            stock = new DmsSparePartStock();
            stock.setPartId(record.getPartId());
            stock.setWarehouseName(warehouseName);
            stock.setStorageLocation(record.getStorageLocation());
            stock.setCurrentStock(after);
            stock.setLockQty(BigDecimal.ZERO);
            dmsSparePartStockMapper.insertStock(stock);
            record.setStockId(stock.getStockId());
        }
        else
        {
            // 更新库存
            stock.setCurrentStock(after);
            dmsSparePartStockMapper.updateStock(stock);
            record.setStockId(stock.getStockId());
        }

        // 自动填充操作人（当前登录用户昵称）
        if (record.getOperatorName() == null || record.getOperatorName().isEmpty())
        {
            try
            {
                record.setOperatorName(SecurityUtils.getLoginUser().getUser().getNickName());
            }
            catch (Exception ignored) {}
        }

        // 自动填充操作日期（默认当天）
        if (record.getOperateDate() == null)
        {
            record.setOperateDate(new Date());
        }

        // 写入出入库记录
        record.setPartCode(part.getPartCode());
        record.setPartName(part.getPartName());
        record.setBeforeStock(before);
        record.setAfterStock(after);

        // 自动生成单据号
        if (StringUtils.isEmpty(record.getDocumentCode()))
        {
            if (isInbound)
            {
                record.setDocumentCode(mkNumberRuleService.generateNumber("dms_spare_in"));
            }
            else
            {
                record.setDocumentCode(mkNumberRuleService.generateNumber("dms_spare_out"));
            }
        }

        int rows = dmsSparePartRecordMapper.insertRecord(record);
        if (rows > 0)
        {
            // 发送出入库消息提醒（DEF-03）
            sendStockMoveMessage(record, part, isInbound, after);
        }
        return rows;
    }

    /**
     * 发送备件出入库消息提醒（DEF-03）
     * 消息规范（docs/消息提醒方案设计.md §2.7）：
     *   备件入库通知：类型1-系统通知，级别1-普通，接收角色 dms:partin:list
     *   备件出库通知：类型1-系统通知，级别1-普通，接收角色 dms:partout:list
     * 出库后库存低于下限时额外发送库存不足预警：类型2-业务预警，级别2-重要，接收角色 dms:partstock:list
     */
    private void sendStockMoveMessage(DmsSparePartRecord record, DmsSparePart part, boolean isInbound, BigDecimal after)
    {
        try
        {
            String timeText = record.getOperateDate() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(record.getOperateDate()) : "";
            if (isInbound)
            {
                messageHelper.sendMessage(
                    "备件入库通知",
                    "备件[" + part.getPartName() + "]（" + part.getPartCode() + "）入库 " + record.getQuantity()
                            + "，仓库：" + (record.getWarehouseName() != null ? record.getWarehouseName() : "备件库")
                            + "，入库后库存：" + after + "，单据号：" + record.getDocumentCode() + "，时间：" + timeText,
                    "1",   // 系统通知
                    "1",   // 普通
                    "dms",
                    record.getRecordId(),
                    "/dms/sparepart/partin",
                    "dms:partin:list",
                    null,
                    "备件入库"
                );
            }
            else
            {
                messageHelper.sendMessage(
                    "备件出库通知",
                    "备件[" + part.getPartName() + "]（" + part.getPartCode() + "）出库 " + record.getQuantity()
                            + "，仓库：" + (record.getWarehouseName() != null ? record.getWarehouseName() : "备件库")
                            + "，出库后库存：" + after + "，单据号：" + record.getDocumentCode() + "，时间：" + timeText,
                    "1",   // 系统通知
                    "1",   // 普通
                    "dms",
                    record.getRecordId(),
                    "/dms/sparepart/partout",
                    "dms:partout:list",
                    null,
                    "备件出库"
                );
            }

            // 库存低于安全下限预警
            if (!isInbound && part.getStockMin() != null && after.compareTo(part.getStockMin()) < 0)
            {
                messageHelper.sendMessage(
                    "备件库存不足预警",
                    "备件[" + part.getPartName() + "]（" + part.getPartCode() + "）当前库存 " + after
                            + " 已低于安全库存下限 " + part.getStockMin() + "，请及时补货。",
                    "2",   // 业务预警
                    "2",   // 重要
                    "dms",
                    part.getPartId(),
                    "/dms/dashboard/partflow",
                    "dms:partstock:list",
                    null,
                    "库存流水报表"
                );
            }
        }
        catch (Exception e)
        {
            // 消息发送失败不影响主业务流程
        }
    }

    @Override
    public List<DmsSparePartRecord> selectRecordList(DmsSparePartRecord record)
    {
        return dmsSparePartRecordMapper.selectRecordList(record);
    }

    @Override
    public int deleteRecordByIds(Long[] recordIds)
    {
        return dmsSparePartRecordMapper.deleteRecordByIds(recordIds);
    }

    @Override
    public DmsSparePartRecord selectRecordById(Long recordId)
    {
        return dmsSparePartRecordMapper.selectRecordById(recordId);
    }

    @Override
    @Transactional
    public int updateRecord(DmsSparePartRecord record)
    {
        DmsSparePartRecord old = dmsSparePartRecordMapper.selectRecordById(record.getRecordId());
        if (old == null) throw new ServiceException("记录不存在");
        BigDecimal newQty = record.getQuantity();
        if (newQty == null || newQty.compareTo(BigDecimal.ZERO) <= 0)
            throw new ServiceException("数量必须大于0");
        BigDecimal oldQty = old.getQuantity();
        Long stockId = old.getStockId();
        // 数量变化时重算库存
        if (oldQty.compareTo(newQty) != 0 && stockId != null)
        {
            DmsSparePartStock stock = dmsSparePartStockMapper.selectStockById(stockId);
            if (stock != null)
            {
                BigDecimal current = stock.getCurrentStock() != null ? stock.getCurrentStock() : BigDecimal.ZERO;
                boolean isInbound = "0".equals(old.getMoveType());
                BigDecimal afterRevert = isInbound ? current.subtract(oldQty) : current.add(oldQty);
                BigDecimal afterApply = isInbound ? afterRevert.add(newQty) : afterRevert.subtract(newQty);
                if (afterApply.compareTo(BigDecimal.ZERO) < 0)
                    throw new ServiceException("库存不足，修改后库存为：" + afterApply);
                stock.setCurrentStock(afterApply);
                dmsSparePartStockMapper.updateStock(stock);
                record.setBeforeStock(afterRevert);
                record.setAfterStock(afterApply);
            }
        }
        else
        {
            record.setBeforeStock(old.getBeforeStock());
            record.setAfterStock(old.getAfterStock());
        }
        return dmsSparePartRecordMapper.updateRecord(record);
    }

    @Override
    public int clearSparePartAlert(Long partId)
    {
        return dmsSparePartMapper.clearStockAlert(partId);
    }

    @Override
    public List<DmsSparePart> selectStockAlertList(DmsSparePart sparePart)
    {
        return dmsSparePartMapper.selectStockAlertList(sparePart);
    }
}