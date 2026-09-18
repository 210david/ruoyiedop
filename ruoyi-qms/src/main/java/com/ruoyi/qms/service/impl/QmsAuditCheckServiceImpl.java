package com.ruoyi.qms.service.impl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsAuditCheckItem;
import com.ruoyi.qms.domain.QmsAuditCheckTemplate;
import com.ruoyi.qms.domain.QmsAuditCheckTemplateItem;
import com.ruoyi.qms.domain.QmsAuditItem;
import com.ruoyi.qms.domain.QmsAuditPlan;
import com.ruoyi.qms.mapper.QmsAuditCheckMapper;
import com.ruoyi.qms.mapper.QmsAuditItemMapper;
import com.ruoyi.qms.mapper.QmsAuditPlanMapper;
import com.ruoyi.qms.service.IQmsAuditCheckService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;

@Service
public class QmsAuditCheckServiceImpl implements IQmsAuditCheckService {
    @Autowired
    private QmsAuditCheckMapper checkMapper;
    @Autowired
    private QmsAuditPlanMapper planMapper;
    @Autowired
    private QmsAuditItemMapper itemMapper;

    @Override
    public List<QmsAuditCheckTemplate> selectTemplateList(QmsAuditCheckTemplate template) {
        return checkMapper.selectTemplateList(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateCheckItemsFromTemplate(Long auditPlanId, Long templateId) {
        QmsAuditPlan plan = planMapper.selectAuditPlanById(auditPlanId);
        if (plan == null) throw new ServiceException("审核计划不存在");
        QmsAuditCheckTemplate template = checkMapper.selectTemplateById(templateId);
        if (template == null) throw new ServiceException("检查表模板不存在");
        List<QmsAuditCheckTemplateItem> tmplItems = checkMapper.selectTemplateItems(templateId);
        if (tmplItems == null || tmplItems.isEmpty()) throw new ServiceException("模板没有检查项");

        // 覆盖式生成：清空已有检查项后按模板重建
        checkMapper.deleteCheckItemsByPlanId(auditPlanId);
        String username = SecurityUtils.getUsername();
        int sort = 1;
        for (QmsAuditCheckTemplateItem t : tmplItems) {
            QmsAuditCheckItem item = new QmsAuditCheckItem();
            item.setAuditPlanId(auditPlanId);
            item.setClauseRef(t.getClauseRef());
            item.setCheckContent(t.getCheckContent());
            item.setCheckResult("0");
            item.setSortNo(sort++);
            item.setCreateBy(username);
            checkMapper.insertCheckItem(item);
        }
        return tmplItems.size();
    }

    @Override
    public List<QmsAuditCheckItem> selectCheckItemsByPlanId(Long auditPlanId) {
        return checkMapper.selectCheckItemsByPlanId(auditPlanId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveCheckItems(Long auditPlanId, List<QmsAuditCheckItem> items) {
        if (items == null) return 0;
        String username = SecurityUtils.getUsername();
        for (QmsAuditCheckItem item : items) {
            if (item.getCheckId() == null) continue;
            item.setAuditPlanId(auditPlanId);
            item.setUpdateBy(username);
            checkMapper.updateCheckItem(item);
        }
        return items.size();
    }

    @Override
    public byte[] generateAuditReport(Long auditPlanId) {
        QmsAuditPlan plan = planMapper.selectAuditPlanById(auditPlanId);
        if (plan == null) throw new ServiceException("审核计划不存在");
        List<QmsAuditCheckItem> checkItems = checkMapper.selectCheckItemsByPlanId(auditPlanId);
        List<QmsAuditItem> ncItems = itemMapper.selectByAuditPlanId(auditPlanId);

        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 标题
            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            var titleRun = title.createRun();
            titleRun.setText("内部审核报告");
            titleRun.setBold(true);
            titleRun.setFontSize(20);

            // 基本信息
            addInfoLine(doc, "审核编号：" + nvl(plan.getAuditPlanNo()));
            addInfoLine(doc, "审核标题：" + nvl(plan.getAuditTitle()));
            addInfoLine(doc, "审核年度：" + (plan.getAuditYear() != null ? String.valueOf(plan.getAuditYear()) : "-"));
            addInfoLine(doc, "审核类型：" + ("annual".equals(plan.getAuditType()) ? "年度计划" : "临时审核"));
            addInfoLine(doc, "主审人：" + nvl(plan.getLeadAuditor()));
            addInfoLine(doc, "审核期间：" + (plan.getAuditDateFrom() != null ? com.ruoyi.common.utils.DateUtils.parseDateToStr("yyyy-MM-dd", plan.getAuditDateFrom()) : "-")
                    + " 至 " + (plan.getAuditDateTo() != null ? com.ruoyi.common.utils.DateUtils.parseDateToStr("yyyy-MM-dd", plan.getAuditDateTo()) : "-"));
            addInfoLine(doc, "审核范围：" + nvl(plan.getAuditScope()));
            addInfoLine(doc, "");

            // 检查表汇总
            XWPFParagraph checkTitle = doc.createParagraph();
            var ctRun = checkTitle.createRun();
            ctRun.setText("一、检查表执行情况");
            ctRun.setBold(true);
            ctRun.setFontSize(14);
            if (checkItems.isEmpty()) {
                addInfoLine(doc, "（本次审核未执行检查表）");
            } else {
                XWPFTable table = doc.createTable(checkItems.size() + 1, 4);
                setHeaderRow(table.getRow(0), "条款", "检查内容", "结果", "客观证据");
                int conform = 0, nonConform = 0, na = 0;
                for (int i = 0; i < checkItems.size(); i++) {
                    QmsAuditCheckItem ci = checkItems.get(i);
                    XWPFTableRow row = table.getRow(i + 1);
                    row.getCell(0).setText(nvl(ci.getClauseRef()));
                    row.getCell(1).setText(nvl(ci.getCheckContent()));
                    String resultText;
                    if ("1".equals(ci.getCheckResult())) { resultText = "不符合"; nonConform++; }
                    else if ("2".equals(ci.getCheckResult())) { resultText = "不适用"; na++; }
                    else { resultText = "符合"; conform++; }
                    row.getCell(2).setText(resultText);
                    row.getCell(3).setText(nvl(ci.getEvidence()));
                }
                addInfoLine(doc, "");
                int total = checkItems.size();
                BigDecimal rate = BigDecimal.valueOf(conform)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(Math.max(1, conform + nonConform)), 1, RoundingMode.HALF_UP);
                addInfoLine(doc, "检查项合计 " + total + " 项：符合 " + conform + " 项、不符合 " + nonConform + " 项、不适用 " + na + " 项；符合率 " + rate.stripTrailingZeros().toPlainString() + "%。");
            }

            // 不符合项
            XWPFParagraph ncTitle = doc.createParagraph();
            var ntRun = ncTitle.createRun();
            ntRun.setText("二、不符合项及整改要求");
            ntRun.setBold(true);
            ntRun.setFontSize(14);
            if (ncItems.isEmpty()) {
                addInfoLine(doc, "本次审核未发现不符合项。");
            } else {
                int idx = 1;
                for (QmsAuditItem nc : ncItems) {
                    addInfoLine(doc, idx++ + ". 条款 " + nvl(nc.getClauseRef()) + "【" + ncTypeText(nc.getNcType()) + "】责任部门：" + nvl(nc.getDeptName()));
                    addInfoLine(doc, "   问题描述：" + nvl(nc.getNcDesc()));
                    if (StringUtils.isNotEmpty(nc.getCorrectiveAction())) {
                        addInfoLine(doc, "   整改措施：" + nc.getCorrectiveAction());
                    }
                }
            }

            // 结论
            XWPFParagraph conTitle = doc.createParagraph();
            var cnRun = conTitle.createRun();
            cnRun.setText("三、审核结论");
            cnRun.setBold(true);
            cnRun.setFontSize(14);
            addInfoLine(doc, StringUtils.isNotEmpty(plan.getAuditConclusion()) ? plan.getAuditConclusion() : "（待填写）");

            doc.write(out);
            return out.toByteArray();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("审核报告生成失败：" + e.getMessage());
        }
    }

    private void addInfoLine(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.createRun().setText(text);
    }

    private void setHeaderRow(XWPFTableRow row, String... cells) {
        for (int i = 0; i < cells.length; i++) {
            row.getCell(i).setText(cells[i]);
        }
    }

    private String ncTypeText(String ncType) {
        if ("1".equals(ncType)) return "严重不符合";
        if ("2".equals(ncType)) return "一般不符合";
        if ("3".equals(ncType)) return "观察项";
        return "不符合";
    }

    private String nvl(String s) { return s == null ? "-" : s; }
}
