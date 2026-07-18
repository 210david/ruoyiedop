package com.ruoyi.dms.service;

import java.util.List;
import com.ruoyi.dms.domain.DmsRepairFaq;

public interface IDmsRepairFaqService
{
    public List<DmsRepairFaq> selectFaqList(DmsRepairFaq faq);
    public DmsRepairFaq selectFaqById(Long faqId);
    public int insertFaq(DmsRepairFaq faq);
    public int updateFaq(DmsRepairFaq faq);
    public int deleteFaqByIds(Long[] faqIds);
}
