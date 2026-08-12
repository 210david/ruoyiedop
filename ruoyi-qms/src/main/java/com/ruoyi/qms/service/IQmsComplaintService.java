package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsComplaint;

public interface IQmsComplaintService {
    public List<QmsComplaint> selectComplaintList(QmsComplaint complaint);
    public QmsComplaint selectComplaintById(Long complaintId);
    public int insertComplaint(QmsComplaint complaint);
    public int updateComplaint(QmsComplaint complaint);
    public int deleteComplaintByIds(Long[] complaintIds);
    public int closeComplaint(Long complaintId);
}
