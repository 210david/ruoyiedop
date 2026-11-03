package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsSupplierRectify;

/**
 * 供应商整改单（客诉供应商责任处理）Mapper接口
 */
public interface QmsSupplierRectifyMapper {
    public List<QmsSupplierRectify> selectRectifyList(QmsSupplierRectify rectify);

    public QmsSupplierRectify selectRectifyById(Long rectifyId);

    public int insertRectify(QmsSupplierRectify rectify);

    public int updateRectify(QmsSupplierRectify rectify);

    public int deleteRectifyByIds(Long[] rectifyIds);

    /** KR-03：逾期未回复自动升级（reply_deadline < 今天 且状态为待回复） */
    public int markOverdue();
}
