package com.ruoyi.wms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.wms.domain.vo.WmsStatisticsVO;
import com.ruoyi.wms.mapper.WmsStatisticsMapper;
import com.ruoyi.wms.service.IWmsStatisticsService;

/**
 * 仓库管理统计报表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsStatisticsServiceImpl implements IWmsStatisticsService
{
    @Autowired
    private WmsStatisticsMapper wmsStatisticsMapper;

    @Override
    public WmsStatisticsVO selectOverview()
    {
        return wmsStatisticsMapper.selectOverview();
    }

    @Override
    public List<WmsStatisticsVO> selectInventoryByWarehouse()
    {
        return wmsStatisticsMapper.selectInventoryByWarehouse();
    }

    @Override
    public List<WmsStatisticsVO> selectInventoryByMaterialType()
    {
        return wmsStatisticsMapper.selectInventoryByMaterialType();
    }

    @Override
    public List<WmsStatisticsVO> selectInboundOutboundTrend(WmsStatisticsVO query)
    {
        return wmsStatisticsMapper.selectInboundOutboundTrend(query);
    }

    @Override
    public List<WmsStatisticsVO> selectInboundByType(WmsStatisticsVO query)
    {
        return wmsStatisticsMapper.selectInboundByType(query);
    }

    @Override
    public List<WmsStatisticsVO> selectOutboundByType(WmsStatisticsVO query)
    {
        return wmsStatisticsMapper.selectOutboundByType(query);
    }

    @Override
    public List<WmsStatisticsVO> selectMaterialTop(WmsStatisticsVO query)
    {
        if (query.getCount() == null || query.getCount() <= 0)
        {
            query.setCount(10);
        }
        return wmsStatisticsMapper.selectMaterialTop(query);
    }

    @Override
    public List<WmsStatisticsVO> selectStockAlert()
    {
        return wmsStatisticsMapper.selectStockAlert();
    }

    @Override
    public List<WmsStatisticsVO> selectStockAlertList(WmsStatisticsVO query)
    {
        return wmsStatisticsMapper.selectStockAlertList(query);
    }

    @Override
    public List<WmsStatisticsVO> selectStockAlertSummary()
    {
        return wmsStatisticsMapper.selectStockAlertSummary();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteStockAlert(WmsStatisticsVO alert)
    {
        if (alert.getAlertType() == null)
        {
            throw new ServiceException("预警类型不能为空");
        }
        if (alert.getMaterialId() == null)
        {
            throw new ServiceException("物料ID不能为空");
        }
        String alertType = alert.getAlertType();
        if ("near_expiry".equals(alertType) || "expired".equals(alertType))
        {
            // 临期/过期预警：软删除对应的库存记录
            return wmsStatisticsMapper.deleteInventoryByAlert(alert.getMaterialId(), alert.getWarehouseId(),
                    alert.getProductionDate(), alert.getExpiryDate());
        }
        else if ("low_stock".equals(alertType) || "overstock".equals(alertType))
        {
            // 库存不足/积压预警：清除物料的安全库存上下限设置
            return wmsStatisticsMapper.clearSafetyStock(alert.getMaterialId());
        }
        else
        {
            throw new ServiceException("不支持的预警类型：" + alertType);
        }
    }
}
