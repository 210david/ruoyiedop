package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsBom;
import com.ruoyi.mms.domain.MmsBomDetail;
import com.ruoyi.mms.domain.MmsBomImport;
import com.ruoyi.mms.mapper.MmsBomMapper;
import com.ruoyi.mms.service.IMmsBomService;

/**
 * BOM Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsBomServiceImpl implements IMmsBomService
{
    @Autowired
    private MmsBomMapper bomMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsBom> selectBomList(MmsBom bom)
    {
        return bomMapper.selectBomList(bom);
    }

    @Override
    public MmsBom selectBomById(Long BomId)
    {
        MmsBom bom = bomMapper.selectBomById(BomId);
        if (bom != null)
        {
            bom.setDetailList(bomMapper.selectBomDetailByBomId(BomId));
        }
        return bom;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBom(MmsBom bom)
    {
        // 自动生成BOM编号
        if (StringUtils.isEmpty(bom.getBomNo()))
        {
            bom.setBomNo(mkNumberRuleService.generateNumber("mms_bom"));
        }
        // 初始化默认值
        bom.setDelFlag("0");
        bom.setCreateBy(SecurityUtils.getUsername());
        bom.setCreateTime(DateUtils.getNowDate());
        // 强制设置为草稿状态，新增时状态不可由前端指定
        bom.setStatus("0"); // 草稿
        // 校验明细不能为空
        if (bom.getDetailList() == null || bom.getDetailList().isEmpty())
        {
            throw new ServiceException("BOM明细不能为空，请至少添加一行物料明细");
        }
        // 校验明细行物料不能为空
        for (MmsBomDetail d : bom.getDetailList())
        {
            if (d.getMaterialId() == null)
            {
                throw new ServiceException("BOM明细中存在未选择物料的行，请选择物料或删除该行");
            }
        }
        if (bom.getBaseQty() == null)
        {
            bom.setBaseQty(BigDecimal.ONE);
        }
        // 插入主表
        int rows = bomMapper.insertBom(bom);
        // 插入明细
        insertBomDetails(bom);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBom(MmsBom bom)
    {
        // 已发布的BOM不允许修改
        MmsBom existing = bomMapper.selectBomById(bom.getBomId());
        if (existing != null && "1".equals(existing.getStatus()))
        {
            throw new ServiceException("已发布的BOM不允许修改，请复制新版本后编辑");
        }
        // 状态不允许通过修改接口变更，保持原有状态（草稿或已停用）
        bom.setStatus(existing != null ? existing.getStatus() : "0");
        // 校验明细不能为空
        if (bom.getDetailList() == null || bom.getDetailList().isEmpty())
        {
            throw new ServiceException("BOM明细不能为空，请至少添加一行物料明细");
        }
        // 校验明细行物料不能为空
        for (MmsBomDetail d : bom.getDetailList())
        {
            if (d.getMaterialId() == null)
            {
                throw new ServiceException("BOM明细中存在未选择物料的行，请选择物料或删除该行");
            }
        }
        // 删除旧明细，重新插入
        bomMapper.deleteBomDetailByBomId(bom.getBomId());
        // 更新主表
        int rows = bomMapper.updateBom(bom);
        // 插入新明细
        insertBomDetails(bom);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBomByIds(Long[] BomIds)
    {
        for (Long bomId : BomIds)
        {
            MmsBom bom = bomMapper.selectBomById(bomId);
            if (bom != null && "1".equals(bom.getStatus()))
            {
                throw new ServiceException("BOM[" + bom.getBomNo() + "]已发布，不允许删除");
            }
        }
        // 删除明细
        for (Long bomId : BomIds)
        {
            bomMapper.deleteBomDetailByBomId(bomId);
        }
        return bomMapper.deleteBomByIds(BomIds);
    }

    @Override
    public List<MmsBomDetail> selectBomDetailByBomId(Long BomId)
    {
        return bomMapper.selectBomDetailByBomId(BomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int publishBom(Long bomId)
    {
        MmsBom bom = bomMapper.selectBomById(bomId);
        if (bom == null)
        {
            throw new ServiceException("BOM不存在");
        }
        if (!"0".equals(bom.getStatus()))
        {
            throw new ServiceException("只有草稿状态的BOM才能发布");
        }
        // 校验明细不能为空
        List<MmsBomDetail> details = bomMapper.selectBomDetailByBomId(bomId);
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("BOM明细不能为空，无法发布");
        }
        // 校验同物料同时仅一个已发布版本
        int count = bomMapper.countPublishedByProductId(bom.getProductId());
        if (count > 0)
        {
            throw new ServiceException("产品[" + bom.getProductCode() + "]已存在已发布版本的BOM，请先停用旧版本");
        }
        MmsBom update = new MmsBom();
        update.setBomId(bomId);
        update.setStatus("1"); // 已发布
        if (bom.getEffectiveDate() == null)
        {
            update.setEffectiveDate(DateUtils.getNowDate());
        }
        else
        {
            update.setEffectiveDate(bom.getEffectiveDate());
        }
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        return bomMapper.updateBomStatus(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int disableBom(Long bomId)
    {
        MmsBom bom = bomMapper.selectBomById(bomId);
        if (bom == null)
        {
            throw new ServiceException("BOM不存在");
        }
        if (!"1".equals(bom.getStatus()))
        {
            throw new ServiceException("只有已发布状态的BOM才能停用");
        }
        MmsBom update = new MmsBom();
        update.setBomId(bomId);
        update.setStatus("2"); // 已停用
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        return bomMapper.updateBomStatus(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int enableBom(Long bomId)
    {
        MmsBom bom = bomMapper.selectBomById(bomId);
        if (bom == null)
        {
            throw new ServiceException("BOM不存在");
        }
        if (!"2".equals(bom.getStatus()))
        {
            throw new ServiceException("只有已停用状态的BOM才能启用");
        }
        // 校验同物料同时仅一个已发布版本
        int count = bomMapper.countPublishedByProductId(bom.getProductId());
        if (count > 0)
        {
            throw new ServiceException("产品[" + bom.getProductCode() + "]已存在已发布版本的BOM，请先停用旧版本");
        }
        MmsBom update = new MmsBom();
        update.setBomId(bomId);
        update.setStatus("1"); // 已发布
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        return bomMapper.updateBomStatus(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MmsBom copyBom(Long bomId)
    {
        MmsBom source = bomMapper.selectBomById(bomId);
        if (source == null)
        {
            throw new ServiceException("源BOM不存在");
        }
        // 复制主表
        MmsBom newBom = new MmsBom();
        newBom.setBomNo(mkNumberRuleService.generateNumber("mms_bom"));
        newBom.setBomName(source.getBomName());
        newBom.setProductId(source.getProductId());
        newBom.setProductCode(source.getProductCode());
        newBom.setProductName(source.getProductName());
        newBom.setBomType(source.getBomType());
        // 版本号递增
        newBom.setVersion(incrementVersion(source.getVersion()));
        newBom.setBaseQty(source.getBaseQty());
        newBom.setBaseUnit(source.getBaseUnit());
        newBom.setStatus("0"); // 草稿
        newBom.setDelFlag("0");
        newBom.setCreateBy(SecurityUtils.getUsername());
        newBom.setCreateTime(DateUtils.getNowDate());
        newBom.setRemark(source.getRemark());
        bomMapper.insertBom(newBom);
        // 复制明细
        List<MmsBomDetail> details = bomMapper.selectBomDetailByBomId(bomId);
        if (details != null)
        {
            for (MmsBomDetail d : details)
            {
                d.setDetailId(null); // 清空ID，走自增
                d.setBomId(newBom.getBomId());
                d.setDelFlag("0");
                d.setCreateBy(SecurityUtils.getUsername());
                d.setCreateTime(DateUtils.getNowDate());
                bomMapper.insertBomDetail(d);
            }
        }
        newBom.setDetailList(details);
        return newBom;
    }

    @Override
    public List<MmsBomDetail> selectBomTreeByBomId(Long bomId)
    {
        return collectBomTree(bomId, BigDecimal.ONE, 0);
    }

    /**
     * 递归构建BOM树形结构（多层级展开，最深5层）
     */
    private List<MmsBomDetail> collectBomTree(Long bomId, BigDecimal parentQty, int currentLevel)
    {
        if (currentLevel >= 5)
        {
            return new ArrayList<>();
        }
        List<MmsBomDetail> details = bomMapper.selectBomDetailByBomId(bomId);
        if (details == null)
        {
            return new ArrayList<>();
        }
        for (MmsBomDetail d : details)
        {
            d.setTreeLevel(currentLevel);
            BigDecimal actualQty = d.getUsageQty() != null ? d.getUsageQty().multiply(parentQty) : BigDecimal.ZERO;
            // 如果子项有引用BOM（半成品），递归构建子节点
            if (d.getBomRefId() != null && d.getBomRefId() > 0)
            {
                List<MmsBomDetail> children = collectBomTree(d.getBomRefId(), actualQty, currentLevel + 1);
                d.setChildren(children);
            }
        }
        return details;
    }

    /**
     * 批量插入BOM明细
     */
    private void insertBomDetails(MmsBom bom)
    {
        if (bom.getDetailList() != null && !bom.getDetailList().isEmpty())
        {
            int seq = 10;
            for (MmsBomDetail d : bom.getDetailList())
            {
                d.setBomId(bom.getBomId());
                d.setDelFlag("0");
                if (d.getSeq() == null)
                {
                    d.setSeq(seq);
                    seq += 10;
                }
                if (d.getSupplyType() == null)
                {
                    d.setSupplyType("1"); // 默认直接领料
                }
                if (d.getIsKeyMaterial() == null)
                {
                    d.setIsKeyMaterial("0");
                }
                if (d.getIsPhantom() == null)
                {
                    d.setIsPhantom("0");
                }
                d.setCreateBy(SecurityUtils.getUsername());
                d.setCreateTime(DateUtils.getNowDate());
                bomMapper.insertBomDetail(d);
            }
        }
    }

    /**
     * 版本号递增（如 1.0 -> 2.0, 1.2 -> 1.3）
     */
    private String incrementVersion(String version)
    {
        if (version == null || version.isEmpty())
        {
            return "1.0";
        }
        try
        {
            String[] parts = version.split("\\.");
            if (parts.length >= 2)
            {
                int major = Integer.parseInt(parts[0]);
                return (major + 1) + ".0";
            }
            return version + "-COPY";
        }
        catch (NumberFormatException e)
        {
            return version + "-COPY";
        }
    }

    /**
     * BOM批量导入
     * Excel每行包含BOM主表字段和明细字段，按"产品编码+版本号"分组构建BOM
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult importBom(List<MmsBomImport> importList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(importList) || importList.isEmpty())
        {
            return AjaxResult.error("导入BOM数据不能为空！");
        }

        // 1. 按"产品编码+版本号"分组
        Map<String, List<MmsBomImport>> groupedMap = new LinkedHashMap<>();
        for (MmsBomImport row : importList)
        {
            String productCode = StringUtils.trimToEmpty(row.getProductCode());
            String version = StringUtils.trimToEmpty(row.getVersion());
            if (productCode.isEmpty() || version.isEmpty())
            {
                continue;
            }
            String groupKey = productCode + "||" + version;
            groupedMap.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(row);
        }

        if (groupedMap.isEmpty())
        {
            return AjaxResult.error("未检测到有效的BOM数据，请确保产品编码和版本号不为空");
        }

        int successNum = 0;
        int failureNum = 0;
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();

        for (Map.Entry<String, List<MmsBomImport>> entry : groupedMap.entrySet())
        {
            String groupKey = entry.getKey();
            List<MmsBomImport> rows = entry.getValue();
            String productCode = groupKey.split("\\|\\|")[0];
            String version = groupKey.split("\\|\\|")[1];

            try
            {
                // 取第一行作为BOM主表信息
                MmsBomImport firstRow = rows.get(0);

                // 校验BOM名称
                if (StringUtils.isEmpty(firstRow.getBomName()))
                {
                    throw new RuntimeException("BOM名称不能为空");
                }

                // 字典类字段合法性校验（遍历所有行）
                for (MmsBomImport row : rows)
                {
                    validateDictFields(row);
                }

                // 校验产品编码
                if (StringUtils.isEmpty(firstRow.getProductCode()))
                {
                    throw new RuntimeException("产品编码不能为空");
                }

                // 通过产品编码查询产品物料信息
                Map<String, Object> productInfo = bomMapper.selectMaterialByCode(firstRow.getProductCode());
                if (productInfo == null)
                {
                    throw new RuntimeException("产品编码[" + firstRow.getProductCode() + "]在物料库中不存在");
                }

                // 检查是否已存在相同产品编码+版本号的BOM
                MmsBom existingBom = bomMapper.selectBomByProductCodeAndVersion(firstRow.getProductCode(), firstRow.getVersion());

                if (existingBom != null)
                {
                    if (!isUpdateSupport)
                    {
                        failureNum++;
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("bomName", firstRow.getBomName() != null ? firstRow.getBomName() : "");
                        item.put("productCode", productCode);
                        item.put("version", version);
                        item.put("errorReason", "BOM已存在，未勾选更新选项");
                        failureList.add(item);
                        continue;
                    }
                    // 更新模式：删除旧BOM后重新创建
                    bomMapper.deleteBomDetailByBomId(existingBom.getBomId());
                    bomMapper.deleteBomByIds(new Long[]{existingBom.getBomId()});
                }

                // 构建BOM主表对象
                MmsBom bom = new MmsBom();
                bom.setBomNo(mkNumberRuleService.generateNumber("mms_bom"));
                bom.setBomName(firstRow.getBomName());
                bom.setProductId((Long) productInfo.get("materialId"));
                bom.setProductCode(firstRow.getProductCode());
                bom.setProductName(firstRow.getProductName() != null ? firstRow.getProductName() : (String) productInfo.get("materialName"));
                bom.setBomType(StringUtils.isNotEmpty(firstRow.getBomType()) ? firstRow.getBomType() : "0");
                bom.setVersion(firstRow.getVersion());
                bom.setBaseQty(firstRow.getBaseQty() != null ? firstRow.getBaseQty() : BigDecimal.ONE);
                bom.setBaseUnit(StringUtils.isNotEmpty(firstRow.getBaseUnit()) ? firstRow.getBaseUnit() : (String) productInfo.get("unit"));
                bom.setStatus("0"); // 草稿
                bom.setDelFlag("0");
                bom.setCreateBy(operName);
                bom.setCreateTime(DateUtils.getNowDate());
                bom.setRemark(firstRow.getRemark());

                // 构建明细列表
                List<MmsBomDetail> detailList = new ArrayList<>();
                int seq = 10;
                for (MmsBomImport row : rows)
                {
                    if (StringUtils.isEmpty(row.getMaterialCode()))
                    {
                        // 跳过没有物料编码的行
                        continue;
                    }
                    // 通过物料编码查询物料信息
                    Map<String, Object> materialInfo = bomMapper.selectMaterialByCode(row.getMaterialCode());
                    if (materialInfo == null)
                    {
                        throw new RuntimeException("物料编码[" + row.getMaterialCode() + "]在物料库中不存在");
                    }
                    MmsBomDetail detail = new MmsBomDetail();
                    detail.setSeq(seq);
                    detail.setMaterialId((Long) materialInfo.get("materialId"));
                    detail.setMaterialCode(row.getMaterialCode());
                    detail.setMaterialName((String) materialInfo.get("materialName"));
                    detail.setSpecModel((String) materialInfo.get("specModel"));
                    detail.setUnit((String) materialInfo.get("unit"));
                    detail.setUsageQty(row.getUsageQty() != null ? row.getUsageQty() : BigDecimal.ONE);
                    detail.setLossRate(row.getLossRate() != null ? row.getLossRate() : BigDecimal.ZERO);
                    detail.setIsKeyMaterial(StringUtils.isNotEmpty(row.getIsKeyMaterial()) ? row.getIsKeyMaterial() : "0");
                    detail.setSupplyType(StringUtils.isNotEmpty(row.getSupplyType()) ? row.getSupplyType() : "1");
                    detail.setIsPhantom(StringUtils.isNotEmpty(row.getIsPhantom()) ? row.getIsPhantom() : "0");
                    detail.setDelFlag("0");
                    detail.setCreateBy(operName);
                    detail.setCreateTime(DateUtils.getNowDate());
                    detailList.add(detail);
                    seq += 10;
                }

                if (detailList.isEmpty())
                {
                    throw new RuntimeException("BOM明细不能为空，至少需要一行物料数据");
                }

                bom.setDetailList(detailList);

                // 插入主表
                bomMapper.insertBom(bom);
                // 插入明细
                insertBomDetails(bom);

                successNum++;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("bomName", bom.getBomName());
                item.put("productCode", bom.getProductCode());
                item.put("productName", bom.getProductName() != null ? bom.getProductName() : "");
                item.put("version", bom.getVersion());
                item.put("result", "导入成功，含" + detailList.size() + "条明细");
                successList.add(item);
            }
            catch (Exception e)
            {
                failureNum++;
                String errMsg = e.getMessage();
                if (errMsg != null && errMsg.length() > 200) { errMsg = errMsg.substring(0, 200) + "..."; }
                MmsBomImport firstRow = rows.get(0);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("bomName", firstRow.getBomName() != null ? firstRow.getBomName() : "");
                item.put("productCode", productCode);
                item.put("productName", firstRow.getProductName() != null ? firstRow.getProductName() : "");
                item.put("version", version);
                item.put("errorReason", errMsg != null ? errMsg : "未知错误");
                failureList.add(item);
            }
        }

        // 构建返回结果（结构化明细）
        AjaxResult ajax = AjaxResult.success();
        ajax.put("successNum", successNum);
        ajax.put("failureNum", failureNum);
        ajax.put("successList", successList);
        ajax.put("failureList", failureList);
        if (failureNum > 0 && successNum == 0)
        {
            ajax.put("code", 500);
            ajax.put("msg", "导入失败，共 " + failureNum + " 条数据异常");
        }
        else if (failureNum > 0)
        {
            ajax.put("msg", "部分导入成功：成功 " + successNum + " 条，失败 " + failureNum + " 条");
        }
        else
        {
            ajax.put("msg", "导入成功，共 " + successNum + " 条");
        }
        return ajax;
    }

    /**
     * 接收产品设计模块下发的制造BOM（跨模块衔接点一，纯新增不改既有逻辑）
     * 事务内完成：校验物料 → 停用旧发布BOM（D3）→ 落库主表+明细，失败整体回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long receiveMbom(MmsBom bom)
    {
        if (bom.getProductId() == null)
        {
            throw new ServiceException("MBOM接收失败：产品物料ID不能为空");
        }
        if (bom.getDetailList() == null || bom.getDetailList().isEmpty())
        {
            throw new ServiceException("MBOM接收失败：BOM明细不能为空");
        }
        for (MmsBomDetail d : bom.getDetailList())
        {
            if (d.getMaterialId() == null)
            {
                throw new ServiceException("MBOM接收失败：明细存在未确认编码的物料行，禁止下发");
            }
        }
        // 校验产品物料在物料库中有效
        Map<String, Object> material = bomMapper.selectMaterialById(bom.getProductId());
        if (material == null)
        {
            throw new ServiceException("MBOM接收失败：产品物料在物料库中不存在或已删除");
        }
        bom.setProductName(String.valueOf(material.get("materialName")));
        if (StringUtils.isEmpty(bom.getBaseUnit()))
        {
            bom.setBaseUnit(String.valueOf(material.get("unit")));
        }
        // D3：同产品旧已发布BOM置停用并记录失效日期（在制工单靠 mms_wo_bom_snapshot 快照兼容）
        List<MmsBom> publishedList = bomMapper.selectPublishedListByProductId(bom.getProductId());
        for (MmsBom old : publishedList)
        {
            MmsBom disable = new MmsBom();
            disable.setBomId(old.getBomId());
            disable.setStatus("2"); // 停用
            disable.setExpireDate(DateUtils.getNowDate());
            disable.setUpdateBy(SecurityUtils.getUsername());
            disable.setUpdateTime(DateUtils.getNowDate());
            bomMapper.disableBomWithExpire(disable);
        }
        // 落库：bomType=0标准BOM、status=1已发布（mms齐套按productId自动找已发布BOM）
        bom.setBomNo(mkNumberRuleService.generateNumber("mms_bom"));
        bom.setBomType("0");
        bom.setStatus("1");
        bom.setDelFlag("0");
        bom.setEffectiveDate(DateUtils.getNowDate());
        bom.setCreateBy(SecurityUtils.getUsername());
        bom.setCreateTime(DateUtils.getNowDate());
        bomMapper.insertBom(bom);
        insertBomDetails(bom);
        return bom.getBomId();
    }

    /**
     * 校验字典类字段和数字字段
     */
    private void validateDictFields(MmsBomImport row)
    {
        validateConverterField("BOM类型", row.getBomType(), "0=标准BOM,1=替代BOM,2=配方,3=临时BOM");
        validateConverterField("是否关键料", row.getIsKeyMaterial(), "0=否,1=是");
        validateConverterField("供应方式", row.getSupplyType(), "1=直接领料,2=倒冲,3=车间库存");
        validateConverterField("是否虚拟件", row.getIsPhantom(), "0=否,1=是");
        // 基准数量校验
        if (row.getBaseQty() != null && row.getBaseQty().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new RuntimeException("基准数量必须为正数");
        }
        // 单件用量校验
        if (row.getUsageQty() != null && row.getUsageQty().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new RuntimeException("单件用量必须为正数");
        }
        // 损耗率校验
        if (row.getLossRate() != null && (row.getLossRate().compareTo(BigDecimal.ZERO) < 0 || row.getLossRate().compareTo(new BigDecimal("100")) > 0))
        {
            throw new RuntimeException("损耗率必须在0-100之间");
        }
    }

    private void validateConverterField(String fieldLabel, String value, String converterExp)
    {
        if (StringUtils.isNotEmpty(value))
        {
            String label = ExcelUtil.convertByExp(value, converterExp, ",");
            if (StringUtils.isEmpty(label))
            {
                throw new RuntimeException(fieldLabel + "值不合法，请填写文字描述");
            }
        }
    }
}
