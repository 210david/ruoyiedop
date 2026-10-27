package com.ruoyi.dms.mapper;

import java.util.List;
import com.ruoyi.dms.domain.DmsRepairFaq;

public interface DmsRepairFaqMapper
{
    public List<DmsRepairFaq> selectFaqList(DmsRepairFaq faq);
    public DmsRepairFaq selectFaqById(Long faqId);
    public int insertFaq(DmsRepairFaq faq);
    public int updateFaq(DmsRepairFaq faq);
    public int deleteFaqByIds(Long[] faqIds);
}
