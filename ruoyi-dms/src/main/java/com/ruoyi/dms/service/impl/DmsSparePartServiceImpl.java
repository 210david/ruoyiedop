package com.ruoyi.dms.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.dms.domain.DmsSparePart;
import com.ruoyi.dms.domain.DmsSparePartRecord;
import com.ruoyi.dms.mapper.DmsSparePartMapper;
import com.ruoyi.dms.mapper.DmsSparePartRecordMapper;
import com.ruoyi.dms.service.IDmsSparePartService;

@Service
public class DmsSparePartServiceImpl implements IDmsSparePartService
{
    private static final String CODE_PREFIX = "BJ";

    @Autowired
    private DmsSparePartMapper dmsSparePartMapper;

    @Autowired
    private DmsSparePartRecordMapper dmsSparePartRecordMapper;

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
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = CODE_PREFIX + dateStr;
        String lastCode = dmsSparePartMapper.selectLastPartCode(prefix);
        int seq = 1;
        if (lastCode != null && lastCode.length() > prefix.length())
        {
            try
            {
                seq = Integer.parseInt(lastCode.substring(prefix.length())) + 1;
            }
            catch (NumberFormatException ignored)
            {
                seq = 1;
            }
        }
        return prefix + String.format("%03d", seq);
    }

    @Override
    public String generateInCode()
    {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = "BJIN" + dateStr;
        String lastCode = dmsSparePartRecordMapper.selectLastDocumentCode(prefix);
        int seq = 1;
        if (lastCode != null && lastCode.length() > prefix.length())
        {
            try
            {
                seq = Integer.parseInt(lastCode.substring(prefix.length())) + 1;
            }
            catch (NumberFormatException ignored)
            {
                seq = 1;
            }
        }
        return prefix + String.format("%03d", seq);
    }

    @Override
    public String generateOutCode()
    {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = "BJOUT" + dateStr;
        String lastCode = dmsSparePartRecordMapper.selectLastDocumentCode(prefix);
        int seq = 1;
        if (lastCode != null && lastCode.length() > prefix.length())
        {
            try
            {
                seq = Integer.parseInt(lastCode.substring(prefix.length())) + 1;
            }
            catch (NumberFormatException ignored)
            {
                seq = 1;
            }
        }
        return prefix + String.format("%03d", seq);
    }

    @Override
    @Transactional
    public int stockMove(DmsSparePartRecord record)
    {
        DmsSparePart part = dmsSparePartMapper.selectSparePartById(record.getPartId());
        if (part == null) throw new ServiceException("备件不存在");

        BigDecimal before = part.getCurrentStock() != null ? part.getCurrentStock() : BigDecimal.ZERO;
        BigDecimal qty = record.getQuantity();
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) throw new ServiceException("数量必须大于0");

        BigDecimal after;
        // 0入库增加，其他出库减少
        if ("0".equals(record.getMoveType()))
        {
            after = before.add(qty);
        }
        else
        {
            after = before.subtract(qty);
            if (after.compareTo(BigDecimal.ZERO) < 0) throw new ServiceException("库存不足，当前库存：" + before + part.getUnit());
        }

        // 更新库存
        DmsSparePart update = new DmsSparePart();
        update.setPartId(part.getPartId());
        update.setCurrentStock(after);
        // 入库时同步更新存放位置
        if ("0".equals(record.getMoveType()) && record.getStorageLocation() != null && !record.getStorageLocation().isEmpty())
        {
            update.setStorageLocation(record.getStorageLocation());
        }
        dmsSparePartMapper.updateSparePart(update);

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

        // 写入记录
        record.setPartCode(part.getPartCode());
        record.setPartName(part.getPartName());
        record.setBeforeStock(before);
        record.setAfterStock(after);
        return dmsSparePartRecordMapper.insertRecord(record);
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
