package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsSupplierRectify;

/**
 * 供应商整改单（客诉供应商责任处理）Service接口
 */
public interface IQmsSupplierRectifyService {
    public List<QmsSupplierRectify> selectRectifyList(QmsSupplierRectify rectify);

    public QmsSupplierRectify selectRectifyById(Long rectifyId);

    public int insertRectify(QmsSupplierRectify rectify);

    public int updateRectify(QmsSupplierRectify rectify);

    public int deleteRectifyByIds(Long[] rectifyIds);

    /** 关闭整改单（状态→已关闭） */
    public int closeRectify(Long rectifyId);

    /** KR-03：逾期未回复自动升级提醒 */
    public int handleOverdue();
}
