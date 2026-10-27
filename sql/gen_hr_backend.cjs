// 批量生成HR模块后端Java代码（精简版）
const fs = require('fs');
const path = require('path');

const basePath = 'e:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-hr/src/main';
const pkgBase = 'com.ruoyi.hr';

// 读取实体配置
const config = JSON.parse(fs.readFileSync(path.join(__dirname, 'hr_entities.json'), 'utf8'));

function cap(s) { return s.charAt(0).toUpperCase() + s.slice(1); }
function uncap(s) { return s.charAt(0).toLowerCase() + s.slice(1); }

function genDomain(e) {
  let imports = ['com.fasterxml.jackson.annotation.JsonFormat', 'com.ruoyi.common.annotation.Excel', 'com.ruoyi.common.core.domain.BaseEntity'];
  let needNotBlank = false, needNotNull = false;
  if (e.fields.some(f => f[2] === 'Date')) imports.push('java.util.Date');
  if (e.fields.some(f => f[2] === 'BigDecimal')) imports.push('java.math.BigDecimal');
  if (e.fields.some(f => f[4] === true && f[2] === 'String')) { needNotBlank = true; }
  if (e.fields.some(f => f[4] === true && f[2] !== 'String')) { needNotNull = true; }
  if (needNotBlank) imports.push('jakarta.validation.constraints.NotBlank');
  if (needNotNull) imports.push('jakarta.validation.constraints.NotNull');
  let code = `package ${pkgBase}.domain;\n\n${[...new Set(imports)].map(i=>`import ${i};`).join('\n')}\n\npublic class ${e.cls} extends BaseEntity\n{\n    private static final long serialVersionUID = 1L;\n\n`;
  // PK
  code += `    private ${e.pkType} ${e.pk};\n`;
  for (const [jn, dc, jt, en, req] of e.fields) {
    let anns = [];
    if (req) {
      if (jt === 'String') anns.push(`    @NotBlank(message = "${en}不能为空")`);
      else anns.push(`    @NotNull(message = "${en}不能为空")`);
    }
    if (jt === 'Date') {
      let pattern = dc.includes('time') && !dc.includes('date') ? 'yyyy-MM-dd HH:mm:ss' : 'yyyy-MM-dd';
      anns.push(`    @JsonFormat(pattern = "${pattern}")`);
    }
    anns.push(`    @Excel(name = "${en}")`);
    code += `${anns.join('\n')}\n    private ${jt} ${jn};\n`;
  }
  // getters/setters
  code += `\n    public ${e.pkType} get${cap(e.pk)}() { return ${e.pk}; }\n    public void set${cap(e.pk)}(${e.pkType} ${e.pk}) { this.${e.pk} = ${e.pk}; }\n`;
  for (const [jn, dc, jt, en, req] of e.fields) {
    code += `    public ${jt} get${cap(jn)}() { return ${jn}; }\n    public void set${cap(jn)}(${jt} ${jn}) { this.${jn} = ${jn}; }\n`;
  }
  code += `}\n`;
  return code;
}

function genMapper(e) {
  let code = `package ${pkgBase}.mapper;\n\nimport java.util.List;\nimport ${pkgBase}.domain.${e.cls};\n\npublic interface ${e.cls}Mapper {\n`;
  code += `    public ${e.cls} select${e.cls}ById(${e.pkType} ${e.pk});\n`;
  code += `    public List<${e.cls}> select${e.cls}List(${e.cls} ${uncap(e.cls)});\n`;
  code += `    public int insert${e.cls}(${e.cls} ${uncap(e.cls)});\n`;
  code += `    public int update${e.cls}(${e.cls} ${uncap(e.cls)});\n`;
  code += `    public int delete${e.cls}ByIds(${e.pkType}[] ${e.pk}s);\n`;
  code += `    public int delete${e.cls}ById(${e.pkType} ${e.pk});\n`;
  code += `}\n`;
  return code;
}

function genService(e) {
  let code = `package ${pkgBase}.service;\n\nimport java.util.List;\nimport ${pkgBase}.domain.${e.cls};\n\npublic interface I${e.cls}Service {\n`;
  code += `    public ${e.cls} select${e.cls}ById(${e.pkType} ${e.pk});\n`;
  code += `    public List<${e.cls}> select${e.cls}List(${e.cls} ${uncap(e.cls)});\n`;
  code += `    public int insert${e.cls}(${e.cls} ${uncap(e.cls)});\n`;
  code += `    public int update${e.cls}(${e.cls} ${uncap(e.cls)});\n`;
  code += `    public int delete${e.cls}ByIds(${e.pkType}[] ${e.pk}s);\n`;
  code += `    public int delete${e.cls}ById(${e.pkType} ${e.pk});\n`;
  code += `}\n`;
  return code;
}

function genServiceImpl(e) {
  let code = `package ${pkgBase}.service.impl;\n\nimport java.util.List;\nimport org.springframework.beans.factory.annotation.Autowired;\nimport org.springframework.stereotype.Service;\nimport ${pkgBase}.domain.${e.cls};\nimport ${pkgBase}.mapper.${e.cls}Mapper;\nimport ${pkgBase}.service.I${e.cls}Service;\n\n@Service\npublic class ${e.cls}ServiceImpl implements I${e.cls}Service\n{\n    @Autowired\n    private ${e.cls}Mapper ${uncap(e.cls)}Mapper;\n\n`;
  code += `    @Override\n    public ${e.cls} select${e.cls}ById(${e.pkType} ${e.pk}) { return ${uncap(e.cls)}Mapper.select${e.cls}ById(${e.pk}); }\n\n`;
  code += `    @Override\n    public List<${e.cls}> select${e.cls}List(${e.cls} ${uncap(e.cls)}) { return ${uncap(e.cls)}Mapper.select${e.cls}List(${uncap(e.cls)}); }\n\n`;
  code += `    @Override\n    public int insert${e.cls}(${e.cls} ${uncap(e.cls)}) { return ${uncap(e.cls)}Mapper.insert${e.cls}(${uncap(e.cls)}); }\n\n`;
  code += `    @Override\n    public int update${e.cls}(${e.cls} ${uncap(e.cls)}) { return ${uncap(e.cls)}Mapper.update${e.cls}(${uncap(e.cls)}); }\n\n`;
  code += `    @Override\n    public int delete${e.cls}ByIds(${e.pkType}[] ${e.pk}s) { return ${uncap(e.cls)}Mapper.delete${e.cls}ByIds(${e.pk}s); }\n\n`;
  code += `    @Override\n    public int delete${e.cls}ById(${e.pkType} ${e.pk}) { return ${uncap(e.cls)}Mapper.delete${e.cls}ById(${e.pk}); }\n`;
  code += `}\n`;
  return code;
}

function genController(e) {
  let code = `package ${pkgBase}.controller;\n\nimport java.util.List;\nimport org.springframework.beans.factory.annotation.Autowired;\nimport org.springframework.security.access.prepost.PreAuthorize;\nimport org.springframework.validation.annotation.Validated;\nimport org.springframework.web.bind.annotation.*;\nimport com.ruoyi.common.annotation.Log;\nimport com.ruoyi.common.core.controller.BaseController;\nimport com.ruoyi.common.core.domain.AjaxResult;\nimport com.ruoyi.common.core.page.TableDataInfo;\nimport com.ruoyi.common.enums.BusinessType;\nimport com.ruoyi.common.utils.poi.ExcelUtil;\nimport ${pkgBase}.domain.${e.cls};\nimport ${pkgBase}.service.I${e.cls}Service;\nimport jakarta.servlet.http.HttpServletResponse;\n\n@RestController\n@RequestMapping("/hr/${e.path}")\npublic class ${e.cls}Controller extends BaseController\n{\n    @Autowired\n    private I${e.cls}Service ${uncap(e.cls)}Service;\n\n`;
  code += `    @PreAuthorize("@ss.hasPermi('${e.perms}:list')")\n`;
  code += `    @GetMapping("/list")\n`;
  code += `    public TableDataInfo list(${e.cls} ${uncap(e.cls)})\n    {\n        startPage();\n        List<${e.cls}> list = ${uncap(e.cls)}Service.select${e.cls}List(${uncap(e.cls)});\n        return getDataTable(list);\n    }\n\n`;
  code += `    @Log(title = "${e.title}", businessType = BusinessType.EXPORT)\n`;
  code += `    @PreAuthorize("@ss.hasPermi('${e.perms}:export')")\n`;
  code += `    @PostMapping("/export")\n`;
  code += `    public void export(HttpServletResponse response, ${e.cls} ${uncap(e.cls)})\n    {\n        List<${e.cls}> list = ${uncap(e.cls)}Service.select${e.cls}List(${uncap(e.cls)});\n        ExcelUtil<${e.cls}> util = new ExcelUtil<>(${e.cls}.class);\n        util.exportExcel(response, list, "${e.title}");\n    }\n\n`;
  code += `    @PreAuthorize("@ss.hasPermi('${e.perms}:query')")\n`;
  code += `    @GetMapping(value = "/{${e.pk}}")\n`;
  code += `    public AjaxResult getInfo(@PathVariable("${e.pk}") ${e.pkType} ${e.pk})\n    {\n        return AjaxResult.success(${uncap(e.cls)}Service.select${e.cls}ById(${e.pk}));\n    }\n\n`;
  code += `    @PreAuthorize("@ss.hasPermi('${e.perms}:add')")\n`;
  code += `    @Log(title = "${e.title}", businessType = BusinessType.INSERT)\n`;
  code += `    @PostMapping\n`;
  code += `    public AjaxResult add(@Validated @RequestBody ${e.cls} ${uncap(e.cls)})\n    {\n        return toAjax(${uncap(e.cls)}Service.insert${e.cls}(${uncap(e.cls)}));\n    }\n\n`;
  code += `    @PreAuthorize("@ss.hasPermi('${e.perms}:edit')")\n`;
  code += `    @Log(title = "${e.title}", businessType = BusinessType.UPDATE)\n`;
  code += `    @PutMapping\n`;
  code += `    public AjaxResult edit(@Validated @RequestBody ${e.cls} ${uncap(e.cls)})\n    {\n        return toAjax(${uncap(e.cls)}Service.update${e.cls}(${uncap(e.cls)}));\n    }\n\n`;
  code += `    @PreAuthorize("@ss.hasPermi('${e.perms}:remove')")\n`;
  code += `    @Log(title = "${e.title}", businessType = BusinessType.DELETE)\n`;
  code += `    @DeleteMapping("/{${e.pk}s}")\n`;
  code += `    public AjaxResult remove(@PathVariable ${e.pkType}[] ${e.pk}s)\n    {\n        return toAjax(${uncap(e.cls)}Service.delete${e.cls}ByIds(${e.pk}s));\n    }\n`;
  code += `}\n`;
  return code;
}

function genMapperXML(e) {
  let xml = `<?xml version="1.0" encoding="UTF-8" ?>\n<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">\n`;
  xml += `<mapper namespace="${pkgBase}.mapper.${e.cls}Mapper">\n\n`;
  xml += `    <resultMap type="${e.cls}" id="${e.cls}Result">\n`;
  xml += `        <result property="${e.pk}" column="${e.pkCol}"/>\n`;
  for (const [jn, dc, jt, en, req] of e.fields) {
    xml += `        <result property="${jn}" column="${dc}"/>\n`;
  }
  xml += `        <result property="createBy" column="create_by"/>\n`;
  xml += `        <result property="createTime" column="create_time"/>\n`;
  xml += `        <result property="updateBy" column="update_by"/>\n`;
  xml += `        <result property="updateTime" column="update_time"/>\n`;
  xml += `        <result property="remark" column="remark"/>\n`;
  xml += `    </resultMap>\n\n`;
  
  // selectVo
  let cols = [e.pkCol, ...e.fields.map(f => f[1]), 'create_by','create_time','update_by','update_time','remark'];
  xml += `    <sql id="selectVo">\n        SELECT ${cols.join(', ')}\n        FROM ${e.table}\n    </sql>\n\n`;
  
  // selectList
  xml += `    <select id="select${e.cls}List" parameterType="${e.cls}" resultMap="${e.cls}Result">\n`;
  xml += `        <include refid="selectVo"/>\n        <where>\n            del_flag = '0'\n`;
  for (const [jn, dc, jt, en, req] of e.fields) {
    if (jt === 'String') {
      xml += `            <if test="${jn} != null and ${jn} != ''"> AND ${dc} LIKE CONCAT('%', #{${jn}}, '%')</if>\n`;
    } else {
      xml += `            <if test="${jn} != null"> AND ${dc} = #{${jn}}</if>\n`;
    }
  }
  xml += `        </where>\n        ORDER BY create_time DESC\n    </select>\n\n`;
  
  // selectById
  xml += `    <select id="select${e.cls}ById" parameterType="${e.pkType}" resultMap="${e.cls}Result">\n`;
  xml += `        <include refid="selectVo"/> WHERE ${e.pkCol} = #{${e.pk}}\n    </select>\n\n`;
  
  // insert
  xml += `    <insert id="insert${e.cls}" parameterType="${e.cls}" useGeneratedKeys="true" keyProperty="${e.pk}">\n`;
  xml += `        INSERT INTO ${e.table} (\n`;
  let insertFields = e.fields.map(f => f[1]);
  xml += `            ${insertFields.join(', ')}, create_by, create_time, remark\n`;
  xml += `        ) VALUES (\n`;
  xml += `            ${e.fields.map(f => `#{${f[0]}}`).join(', ')}, #{createBy}, sysdate(), #{remark}\n`;
  xml += `        )\n    </insert>\n\n`;
  
  // update
  xml += `    <update id="update${e.cls}" parameterType="${e.cls}">\n        UPDATE ${e.table}\n        <set>\n`;
  for (const [jn, dc, jt, en, req] of e.fields) {
    if (jt === 'String') {
      xml += `            <if test="${jn} != null and ${jn} != ''">${dc} = #{${jn}},</if>\n`;
    } else {
      xml += `            <if test="${jn} != null">${dc} = #{${jn}},</if>\n`;
    }
  }
  xml += `            <if test="updateBy != null and updateBy != ''">update_by = #{updateBy},</if>\n`;
  xml += `            <if test="remark != null">remark = #{remark},</if>\n`;
  xml += `            update_time = sysdate()\n`;
  xml += `        </set>\n        WHERE ${e.pkCol} = #{${e.pk}}\n    </update>\n\n`;
  
  // delete
  xml += `    <delete id="delete${e.cls}ById" parameterType="${e.pkType}">\n        UPDATE ${e.table} SET del_flag = '2' WHERE ${e.pkCol} = #{${e.pk}}\n    </delete>\n\n`;
  xml += `    <delete id="delete${e.cls}ByIds" parameterType="${e.pkType}">\n        UPDATE ${e.table} SET del_flag = '2' WHERE ${e.pkCol} IN\n`;
  xml += `        <foreach item="${e.pk}" collection="array" open="(" separator="," close=")">#{${e.pk}}</foreach>\n`;
  xml += `    </delete>\n\n`;
  xml += `</mapper>\n`;
  return xml;
}

// 生成所有文件
config.entities.forEach(e => {
  e.pkType = e.pkType || 'Long';
  // Domain
  const domainPath = path.join(basePath, 'java', 'com/ruoyi/hr/domain');
  fs.mkdirSync(domainPath, { recursive: true });
  fs.writeFileSync(path.join(domainPath, `${e.cls}.java`), genDomain(e), 'utf8');
  
  // Mapper
  const mapperPath = path.join(basePath, 'java', 'com/ruoyi/hr/mapper');
  fs.mkdirSync(mapperPath, { recursive: true });
  fs.writeFileSync(path.join(mapperPath, `${e.cls}Mapper.java`), genMapper(e), 'utf8');
  
  // Service
  const servicePath = path.join(basePath, 'java', 'com/ruoyi/hr/service');
  fs.mkdirSync(servicePath, { recursive: true });
  fs.writeFileSync(path.join(servicePath, `I${e.cls}Service.java`), genService(e), 'utf8');
  
  // ServiceImpl
  const implPath = path.join(basePath, 'java', 'com/ruoyi/hr/service/impl');
  fs.mkdirSync(implPath, { recursive: true });
  fs.writeFileSync(path.join(implPath, `${e.cls}ServiceImpl.java`), genServiceImpl(e), 'utf8');
  
  // Controller
  const ctrlPath = path.join(basePath, 'java', 'com/ruoyi/hr/controller');
  fs.mkdirSync(ctrlPath, { recursive: true });
  fs.writeFileSync(path.join(ctrlPath, `${e.cls}Controller.java`), genController(e), 'utf8');
  
  // Mapper XML
  const xmlPath = path.join(basePath, 'resources/mapper/hr');
  fs.mkdirSync(xmlPath, { recursive: true });
  fs.writeFileSync(path.join(xmlPath, `${e.cls}Mapper.xml`), genMapperXML(e), 'utf8');
  
  console.log(`Generated: ${e.cls} (table: ${e.table})`);
});

console.log(`\nTotal: ${config.entities.length} entities generated.`);
