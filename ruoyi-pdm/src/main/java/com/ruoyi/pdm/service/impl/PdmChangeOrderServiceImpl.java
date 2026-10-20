package com.ruoyi.pdm.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.pdm.domain.PdmChangeAuditLog;
import com.ruoyi.pdm.domain.PdmChangeOrder;
import com.ruoyi.pdm.domain.PdmChangeOrderItem;
import com.ruoyi.pdm.domain.PdmDesignBom;
import com.ruoyi.pdm.domain.PdmDesignFile;
import com.ruoyi.pdm.domain.PdmDocVersion;
import com.ruoyi.pdm.domain.PdmProduct;
import com.ruoyi.pdm.domain.PdmScheme;
import com.ruoyi.pdm.mapper.PdmChangeAuditLogMapper;
import com.ruoyi.pdm.mapper.PdmChangeOrderMapper;
import com.ruoyi.pdm.mapper.PdmDesignBomMapper;
import com.ruoyi.pdm.mapper.PdmDesignFileMapper;
import com.ruoyi.pdm.mapper.PdmProductMapper;
import com.ruoyi.pdm.mapper.PdmSchemeMapper;
import com.ruoyi.pdm.service.IPdmChangeOrderService;
import com.ruoyi.pdm.service.IPdmDesignBomService;
import com.ruoyi.pdm.service.IPdmDesignFileService;
import com.ruoyi.pdm.service.IPdmDocVersionService;
import com.ruoyi.pdm.service.IPdmSchemeService;
import com.ruoyi.system.utils.MessageHelper;

/**
 * 技术变更单 Service 实现
 *
 * 主流程（设计方案 十、技术变更主流程）：发起变更申请（勾选涉及资料与当前生效版本）
 * → 审批 → 按变更单修改资料上传新版本 → 明细升版生效（依据=变更单号，旧版本转历史）
 * → 全部生效后关闭 → 推送仓库/采购/质量/生产管控变更通知。
 *
 * @author ruoyi
 */
@Service
public class PdmChangeOrderServiceImpl implements IPdmChangeOrderService
{
    /** 变更通知接收岗位角色（D4口径：复用各模块既有角色权限字符） */
    private static final String[] NOTIFY_ROLES = {"wms:material:list", "pms:order:list", "qms:std:list", "mms:bom:list"};

    @Autowired
    private PdmChangeOrderMapper changeMapper;

    @Autowired
    private PdmChangeAuditLogMapper changeAuditLogMapper;

    @Autowired
    private PdmProductMapper productMapper;

    @Autowired
    private PdmSchemeMapper schemeMapper;

    @Autowired
    private PdmDesignFileMapper fileMapper;

    @Autowired
    private PdmDesignBomMapper bomMapper;

    @Autowired
    private IPdmDocVersionService docVersionService;

    @Autowired
    private IPdmSchemeService schemeService;

    @Autowired
    private IPdmDesignFileService fileService;

    @Autowired
    private IPdmDesignBomService bomService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private MessageHelper messageHelper;

    @Override
    @DataScope(deptAlias = "pdm_change_order")
    public List<PdmChangeOrder> selectChangeList(PdmChangeOrder change)
    {
        return changeMapper.selectChangeList(change);
    }

    @Override
    public PdmChangeOrder selectChangeById(Long changeId)
    {
        PdmChangeOrder change = changeMapper.selectChangeById(changeId);
        if (change != null)
        {
            change.setItemList(changeMapper.selectItemsByChangeId(changeId));
            change.setAuditLogList(changeAuditLogMapper.selectAuditLogByChangeId(changeId));
        }
        return change;
    }

    @Override
    public List<PdmChangeOrderItem> listEffectiveDocs(Long productId)
    {
        PdmProduct product = productMapper.selectProductById(productId);
        if (product == null)
        {
            throw new ServiceException("产品档案不存在或已删除");
        }
        List<PdmChangeOrderItem> docs = new java.util.ArrayList<>();
        // 设计方案（已定稿）
        PdmScheme sq = new PdmScheme();
        sq.setProductId(productId);
        sq.setSchemeStatus("1");
        for (PdmScheme s : schemeMapper.selectSchemeList(sq))
        {
            addItemDoc(docs, "1", s.getSchemeId(), s.getSchemeNo(), s.getSchemeName());
        }
        // 图纸技术文件（受控）
        PdmDesignFile fq = new PdmDesignFile();
        fq.setProductId(productId);
        fq.setFileStatus("1");
        for (PdmDesignFile f : fileMapper.selectFileList(fq))
        {
            addItemDoc(docs, "2", f.getFileId(), f.getFileNo(), f.getFileName());
        }
        // 设计BOM（已生效）
        PdmDesignBom bq = new PdmDesignBom();
        bq.setProductId(productId);
        bq.setBomStatus("1");
        for (PdmDesignBom b : bomMapper.selectBomList(bq))
        {
            addItemDoc(docs, "3", b.getBomId(), b.getBomNo(), b.getBomName());
        }
        return docs;
    }

    /** 组装生效资料勾选项（含当前生效版本号） */
    private void addItemDoc(List<PdmChangeOrderItem> docs, String docType, Long bizId, String bizNo, String bizName)
    {
        PdmDocVersion effective = docVersionService.selectEffectiveVersion(docType, bizId);
        if (effective == null)
        {
            return;
        }
        PdmChangeOrderItem item = new PdmChangeOrderItem();
        item.setDocType(docType);
        item.setBizId(bizId);
        item.setBizNo(bizNo);
        item.setBizName(bizName);
        item.setCurrentVersion(effective.getVersionNo());
        docs.add(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertChange(PdmChangeOrder change)
    {
        checkBase(change);
        PdmProduct product = checkProduct(change.getProductId());
        // 保存即取号，编号生成后不可修改
        change.setChangeNo(mkNumberRuleService.generateNumber("pdm_change_order"));
        change.setChangeStatus("0");
        change.setProductCode(product.getProductCode());
        change.setProductName(product.getMaterialName());
        // 记录创建人与创建时间
        change.setCreateBy(SecurityUtils.getUsername());
        change.setCreateTime(DateUtils.getNowDate());
        int rows = changeMapper.insertChange(change);
        saveItems(change);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateChange(PdmChangeOrder change)
    {
        PdmChangeOrder exist = changeMapper.selectChangeById(change.getChangeId());
        if (exist == null)
        {
            throw new ServiceException("变更单不存在或已删除");
        }
        if (!"0".equals(exist.getChangeStatus()) && !"5".equals(exist.getChangeStatus()))
        {
            throw new ServiceException("变更单【" + exist.getChangeNo() + "】已提交审批，不允许修改");
        }
        checkBase(change);
        // 编号与关联产品不允许修改
        change.setChangeNo(null);
        change.setProductId(null);
        change.setProductCode(null);
        change.setProductName(null);
        int rows = changeMapper.updateChange(change);
        if (change.getItemList() != null)
        {
            changeMapper.deleteItemsByChangeId(change.getChangeId());
            saveItems(change);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteChangeByIds(Long[] changeIds)
    {
        for (Long changeId : changeIds)
        {
            PdmChangeOrder exist = changeMapper.selectChangeById(changeId);
            if (exist != null && !"0".equals(exist.getChangeStatus()))
            {
                // 需求7.6：变更记录不可删除，仅未提交的草稿允许删除
                throw new ServiceException("变更单【" + exist.getChangeNo() + "】已进入审批/执行流程，记录不可删除");
            }
        }
        for (Long changeId : changeIds)
        {
            changeMapper.deleteItemsByChangeId(changeId);
        }
        return changeMapper.deleteChangeByIds(changeIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitChange(Long changeId)
    {
        PdmChangeOrder exist = changeMapper.selectChangeById(changeId);
        if (exist == null)
        {
            throw new ServiceException("变更单不存在或已删除");
        }
        if (!"0".equals(exist.getChangeStatus()) && !"5".equals(exist.getChangeStatus()))
        {
            throw new ServiceException("变更单【" + exist.getChangeNo() + "】当前状态不允许提交审批");
        }
        List<PdmChangeOrderItem> items = changeMapper.selectItemsByChangeId(changeId);
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("请先勾选变更涉及的资料清单");
        }
        PdmChangeOrder up = new PdmChangeOrder();
        up.setChangeId(changeId);
        up.setChangeStatus("1");
        up.setSubmitTime(new Date());
        up.setUpdateBy(SecurityUtils.getUsername());
        int rows = changeMapper.updateChange(up);
        // 推送审批人待办（设计负责人/部门负责人，权限字符 pdm:change:audit）
        messageHelper.sendMessage(
                "技术变更待审批：" + exist.getChangeNo(),
                "变更【" + exist.getChangeName() + "】（产品：" + exist.getProductName() + "）已提交审批，请及时处理。",
                "3", "2", "pdm", changeId, "/pdm/change", "pdm:change:audit", "审批中", "技术变更");
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditChange(Long changeId, boolean approved, String opinion)
    {
        PdmChangeOrder exist = changeMapper.selectChangeById(changeId);
        if (exist == null)
        {
            throw new ServiceException("变更单不存在或已删除");
        }
        if (!"1".equals(exist.getChangeStatus()))
        {
            throw new ServiceException("变更单【" + exist.getChangeNo() + "】不是审批中状态，无法审批");
        }
        PdmChangeOrder up = new PdmChangeOrder();
        up.setChangeId(changeId);
        up.setChangeStatus(approved ? "2" : "5");
        up.setAuditBy(SecurityUtils.getUsername());
        up.setAuditTime(new Date());
        up.setAuditOpinion(opinion);
        up.setUpdateBy(SecurityUtils.getUsername());
        int rows = changeMapper.updateChange(up);
        // 审批流水落库（支持审批历史时间轴）
        PdmChangeAuditLog auditLog = new PdmChangeAuditLog();
        auditLog.setChangeId(changeId);
        auditLog.setAuditAction(approved ? "1" : "2");
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditTime(new Date());
        auditLog.setAuditRemark(opinion);
        changeAuditLogMapper.insertAuditLog(auditLog);
        // 审批待办处理完毕
        messageHelper.markHandled("pdm", changeId);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int executeItem(Long changeId, Long itemId)
    {
        PdmChangeOrder exist = changeMapper.selectChangeById(changeId);
        if (exist == null)
        {
            throw new ServiceException("变更单不存在或已删除");
        }
        if (!"2".equals(exist.getChangeStatus()) && !"3".equals(exist.getChangeStatus()))
        {
            throw new ServiceException("变更单【" + exist.getChangeNo() + "】未在执行期（需审批通过后），无法执行资料升版");
        }
        PdmChangeOrderItem item = changeMapper.selectItemById(itemId);
        if (item == null || !changeId.equals(item.getChangeId()))
        {
            throw new ServiceException("变更明细不属于该变更单");
        }
        if ("1".equals(item.getItemStatus()))
        {
            throw new ServiceException("资料【" + item.getBizNo() + "】已按本变更单升版生效，无需重复执行");
        }
        // 按资料类型执行升版生效（依据=技术变更单号，旧版本自动转历史）
        String docType = item.getDocType();
        if ("1".equals(docType))
        {
            schemeService.effectiveForChange(item.getBizId(), exist.getChangeNo());
        }
        else if ("2".equals(docType))
        {
            fileService.effectiveForChange(item.getBizId(), exist.getChangeNo());
        }
        else if ("3".equals(docType))
        {
            bomService.effectiveForChange(item.getBizId(), exist.getChangeNo());
        }
        else
        {
            throw new ServiceException("变更明细资料类型无效");
        }
        // 回填升版后版本号
        PdmDocVersion effective = docVersionService.selectEffectiveVersion(docType, item.getBizId());
        PdmChangeOrderItem up = new PdmChangeOrderItem();
        up.setItemId(itemId);
        up.setNewVersion(effective != null ? effective.getVersionNo() : "");
        up.setItemStatus("1");
        up.setEffectTime(new Date());
        up.setEffectBy(SecurityUtils.getUsername());
        changeMapper.updateItem(up);
        // 变更单待执行 → 执行中
        if ("2".equals(exist.getChangeStatus()))
        {
            PdmChangeOrder cu = new PdmChangeOrder();
            cu.setChangeId(changeId);
            cu.setChangeStatus("3");
            cu.setUpdateBy(SecurityUtils.getUsername());
            changeMapper.updateChange(cu);
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeChange(Long changeId)
    {
        PdmChangeOrder exist = changeMapper.selectChangeById(changeId);
        if (exist == null)
        {
            throw new ServiceException("变更单不存在或已删除");
        }
        if (!"2".equals(exist.getChangeStatus()) && !"3".equals(exist.getChangeStatus()))
        {
            throw new ServiceException("变更单【" + exist.getChangeNo() + "】未在执行期，无法关闭");
        }
        // 需求7.6：所有涉及资料均已升版生效方可关闭
        List<PdmChangeOrderItem> items = changeMapper.selectItemsByChangeId(changeId);
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("变更单未勾选涉及资料，无法关闭");
        }
        for (PdmChangeOrderItem item : items)
        {
            if (!"1".equals(item.getItemStatus()))
            {
                throw new ServiceException("资料【" + item.getBizNo() + " " + item.getBizName() + "】尚未升版生效，不满足关闭条件");
            }
        }
        PdmChangeOrder up = new PdmChangeOrder();
        up.setChangeId(changeId);
        up.setChangeStatus("4");
        up.setCloseTime(new Date());
        up.setCloseBy(SecurityUtils.getUsername());
        up.setUpdateBy(SecurityUtils.getUsername());
        int rows = changeMapper.updateChange(up);
        // 变更生效通知：仓库/采购/质量/生产管控（变更产品、生效资料、生效时间）
        StringBuilder docDesc = new StringBuilder();
        for (PdmChangeOrderItem item : items)
        {
            if (docDesc.length() > 0)
            {
                docDesc.append("、");
            }
            docDesc.append(item.getBizNo()).append(" ").append(item.getBizName())
                    .append("（").append(item.getNewVersion()).append("）");
        }
        for (String roleKey : NOTIFY_ROLES)
        {
            messageHelper.sendMessage(
                    "技术变更生效：" + exist.getChangeNo(),
                    "产品【" + exist.getProductCode() + " " + exist.getProductName() + "】技术变更已生效，涉及资料："
                            + docDesc + "；生效时间：" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())
                            + "。请关注后续生产/采购/质量影响。",
                    "2", "2", "pdm", changeId, "/pdm/change", roleKey, "已关闭", "技术变更");
        }
        messageHelper.markHandled("pdm", changeId);
        return rows;
    }

    /** 基础校验 */
    private void checkBase(PdmChangeOrder change)
    {
        if (StringUtils.isEmpty(change.getChangeName()))
        {
            throw new ServiceException("请填写变更标题");
        }
        if (change.getItemList() == null || change.getItemList().isEmpty())
        {
            throw new ServiceException("请勾选变更涉及的资料清单");
        }
    }

    /** 校验产品档案 */
    private PdmProduct checkProduct(Long productId)
    {
        if (productId == null)
        {
            throw new ServiceException("请选择变更产品");
        }
        PdmProduct product = productMapper.selectProductById(productId);
        if (product == null)
        {
            throw new ServiceException("产品档案不存在或已删除");
        }
        return product;
    }

    /** 保存涉及资料明细（冗余资料编号/名称与当前生效版本号，校验资料归属与受控状态） */
    private void saveItems(PdmChangeOrder change)
    {
        List<PdmChangeOrderItem> items = change.getItemList();
        int order = 1;
        for (PdmChangeOrderItem item : items)
        {
            String docType = item.getDocType();
            Long bizId = item.getBizId();
            if (StringUtils.isEmpty(docType) || bizId == null)
            {
                throw new ServiceException("涉及资料明细存在无效行");
            }
            if ("1".equals(docType))
            {
                PdmScheme s = schemeMapper.selectSchemeById(bizId);
                checkDoc(s != null && change.getProductId().equals(s.getProductId()), "设计方案", bizId);
                if (!"1".equals(s.getSchemeStatus()))
                {
                    throw new ServiceException("设计方案【" + s.getSchemeNo() + "】未定稿，不在可变更范围");
                }
                item.setBizNo(s.getSchemeNo());
                item.setBizName(s.getSchemeName());
            }
            else if ("2".equals(docType))
            {
                PdmDesignFile f = fileMapper.selectFileById(bizId);
                checkDoc(f != null && change.getProductId().equals(f.getProductId()), "图纸技术文件", bizId);
                if (!"1".equals(f.getFileStatus()))
                {
                    throw new ServiceException("图纸文件【" + f.getFileNo() + "】未受控，不在可变更范围");
                }
                item.setBizNo(f.getFileNo());
                item.setBizName(f.getFileName());
            }
            else if ("3".equals(docType))
            {
                PdmDesignBom b = bomMapper.selectBomById(bizId);
                checkDoc(b != null && change.getProductId().equals(b.getProductId()), "设计BOM", bizId);
                if (!"1".equals(b.getBomStatus()))
                {
                    throw new ServiceException("设计BOM【" + b.getBomNo() + "】未生效，不在可变更范围");
                }
                item.setBizNo(b.getBomNo());
                item.setBizName(b.getBomName());
            }
            else
            {
                throw new ServiceException("涉及资料明细的资料类型无效");
            }
            PdmDocVersion effective = docVersionService.selectEffectiveVersion(docType, bizId);
            item.setCurrentVersion(effective != null ? effective.getVersionNo() : "");
            item.setChangeId(change.getChangeId());
            item.setItemStatus("0");
            item.setOrderNum(order++);
        }
        for (PdmChangeOrderItem item : items)
        {
            changeMapper.insertItem(item);
        }
    }

    /** 资料归属校验 */
    private void checkDoc(boolean pass, String label, Long bizId)
    {
        if (!pass)
        {
            throw new ServiceException(label + "[" + bizId + "]不存在或不属于该变更产品");
        }
    }
}
