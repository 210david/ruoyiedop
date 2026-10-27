package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsComplaint;

public interface IQmsComplaintService {
    public List<QmsComplaint> selectComplaintList(QmsComplaint complaint);
    public QmsComplaint selectComplaintById(Long complaintId);
    public int insertComplaint(QmsComplaint complaint);
    public int updateComplaint(QmsComplaint complaint);
    public int deleteComplaintByIds(Long[] complaintIds);
    /** 受理客诉：已登记(0) → 处理中(1) */
    public int acceptComplaint(Long complaintId);
    /** 处理完成：处理中(1) → 待确认(2)，同时保存处理描述、处理结果、退货金额、索赔金额等信息 */
    public int completeComplaint(QmsComplaint complaint);
    /** 关闭客诉：待确认(2) → 已关闭(3) */
    public int closeComplaint(Long complaintId);
}
