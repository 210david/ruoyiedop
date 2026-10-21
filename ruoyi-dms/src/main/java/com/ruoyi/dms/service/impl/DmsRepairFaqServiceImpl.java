package com.ruoyi.dms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.dms.domain.DmsRepairFaq;
import com.ruoyi.dms.mapper.DmsRepairFaqMapper;
import com.ruoyi.dms.service.IDmsRepairFaqService;

@Service
public class DmsRepairFaqServiceImpl implements IDmsRepairFaqService
{
    @Autowired
    private DmsRepairFaqMapper mapper;

    @Override
    public List<DmsRepairFaq> selectFaqList(DmsRepairFaq faq) { return mapper.selectFaqList(faq); }
    @Override
    public DmsRepairFaq selectFaqById(Long faqId) { return mapper.selectFaqById(faqId); }
    @Override
    public int insertFaq(DmsRepairFaq faq) { faq.setDelFlag("0"); return mapper.insertFaq(faq); }
    @Override
    public int updateFaq(DmsRepairFaq faq) { return mapper.updateFaq(faq); }
    @Override
    public int deleteFaqByIds(Long[] faqIds) { return mapper.deleteFaqByIds(faqIds); }
}
