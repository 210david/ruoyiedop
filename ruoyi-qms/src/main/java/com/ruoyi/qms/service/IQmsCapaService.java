package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsCapa;

public interface IQmsCapaService {
    public List<QmsCapa> selectCapaList(QmsCapa capa);
    public QmsCapa selectCapaById(Long capaId);
    public int insertCapa(QmsCapa capa);
    public int updateCapa(QmsCapa capa);
    public int deleteCapaByIds(Long[] capaIds);
    /** 草稿(0)/已驳回(4) → 进行中(1) */
    public int submitCapa(Long capaId);
    /** 进行中(1) → 验证中(2)，填写D6验证结果 */
    public int submitVerify(QmsCapa capa);
    /** 验证中(2) → 已关闭(3)，填写D7预防措施和D8关闭备注 */
    public int closeCapa(QmsCapa capa);
    /** 验证中(2) → 已驳回(4)，填写驳回原因 */
    public int rejectCapa(Long capaId, String rejectReason);

    /** 一键从NCR发起CAPA */
    public Long createCapaFromNcr(Long ncrId);

    /** 一键从客诉发起CAPA */
    public Long createCapaFromComplaint(Long complaintId);
}
