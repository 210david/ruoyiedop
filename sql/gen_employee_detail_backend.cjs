/**
 * 批量生成4个员工明细子表的后端Java代码
 */
const fs = require('fs');
const path = require('path');

const entities = [
  {
    className: 'HrEmployeeFamily',
    tableName: 'biz_hr_employee_family',
    cnName: '员工家庭成员',
    idField: 'familyId',
    idColumn: 'family_id',
    fields: [
      { name: 'employeeId', type: 'Long', column: 'employee_id', excelName: '员工ID', notNull: true, notNullMsg: '员工ID不能为空' },
      { name: 'memberName', type: 'String', column: 'member_name', excelName: '姓名' },
      { name: 'relationship', type: 'String', column: 'relationship', excelName: '关系' },
      { name: 'gender', type: 'String', column: 'gender', excelName: '性别' },
      { name: 'birthday', type: 'Date', column: 'birthday', excelName: '出生日期', jsonFormat: true },
      { name: 'phone', type: 'String', column: 'phone', excelName: '联系电话' },
      { name: 'occupation', type: 'String', column: 'occupation', excelName: '工作单位/职业' },
      { name: 'status', type: 'String', column: 'status', excelName: '状态' },
      { name: 'delFlag', type: 'String', column: 'del_flag', excelName: '删除标志' }
    ]
  },
  {
    className: 'HrEmployeeEducation',
    tableName: 'biz_hr_employee_education',
    cnName: '员工教育背景',
    idField: 'educationId',
    idColumn: 'education_id',
    fields: [
      { name: 'employeeId', type: 'Long', column: 'employee_id', excelName: '员工ID', notNull: true, notNullMsg: '员工ID不能为空' },
      { name: 'startDate', type: 'Date', column: 'start_date', excelName: '开始日期', jsonFormat: true },
      { name: 'endDate', type: 'Date', column: 'end_date', excelName: '结束日期', jsonFormat: true },
      { name: 'schoolName', type: 'String', column: 'school_name', excelName: '学校名称' },
      { name: 'major', type: 'String', column: 'major', excelName: '专业' },
      { name: 'eduLevel', type: 'String', column: 'edu_level', excelName: '学历' },
      { name: 'degree', type: 'String', column: 'degree', excelName: '学位' },
      { name: 'isHighest', type: 'String', column: 'is_highest', excelName: '是否最高学历' },
      { name: 'status', type: 'String', column: 'status', excelName: '状态' },
      { name: 'delFlag', type: 'String', column: 'del_flag', excelName: '删除标志' }
    ]
  },
  {
    className: 'HrEmployeeWorkExp',
    tableName: 'biz_hr_employee_work_exp',
    cnName: '员工工作经历',
    idField: 'workExpId',
    idColumn: 'work_exp_id',
    fields: [
      { name: 'employeeId', type: 'Long', column: 'employee_id', excelName: '员工ID', notNull: true, notNullMsg: '员工ID不能为空' },
      { name: 'startDate', type: 'Date', column: 'start_date', excelName: '开始日期', jsonFormat: true },
      { name: 'endDate', type: 'Date', column: 'end_date', excelName: '结束日期', jsonFormat: true },
      { name: 'companyName', type: 'String', column: 'company_name', excelName: '工作单位' },
      { name: 'position', type: 'String', column: 'position', excelName: '职位' },
      { name: 'workContent', type: 'String', column: 'work_content', excelName: '工作内容' },
      { name: 'leaveReason', type: 'String', column: 'leave_reason', excelName: '离职原因' },
      { name: 'status', type: 'String', column: 'status', excelName: '状态' },
      { name: 'delFlag', type: 'String', column: 'del_flag', excelName: '删除标志' }
    ]
  },
  {
    className: 'HrEmployeeQualification',
    tableName: 'biz_hr_employee_qualification',
    cnName: '员工培训/职称/资格认证',
    idField: 'qualificationId',
    idColumn: 'qualification_id',
    fields: [
      { name: 'employeeId', type: 'Long', column: 'employee_id', excelName: '员工ID', notNull: true, notNullMsg: '员工ID不能为空' },
      { name: 'qualType', type: 'String', column: 'qual_type', excelName: '类型' },
      { name: 'qualName', type: 'String', column: 'qual_name', excelName: '名称' },
      { name: 'qualNo', type: 'String', column: 'qual_no', excelName: '证书编号' },
      { name: 'issueOrg', type: 'String', column: 'issue_org', excelName: '发证机构' },
      { name: 'issueDate', type: 'Date', column: 'issue_date', excelName: '发证日期', jsonFormat: true },
      { name: 'validFrom', type: 'Date', column: 'valid_from', excelName: '有效起始日', jsonFormat: true },
      { name: 'validTo', type: 'Date', column: 'valid_to', excelName: '有效期至', jsonFormat: true },
      { name: 'score', type: 'String', column: 'score', excelName: '成绩/等级' },
      { name: 'status', type: 'String', column: 'status', excelName: '状态' },
      { name: 'delFlag', type: 'String', column: 'del_flag', excelName: '删除标志' }
    ]
  }
];

const baseDir = 'e:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-hr/src/main/java/com/ruoyi/hr';
const mapperDir = 'e:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-hr/src/main/resources/mapper/hr';

function genDomain(entity) {
  const { className, tableName, cnName, idField, fields } = entity;
  const imports = ['com.fasterxml.jackson.annotation.JsonFormat', 'com.ruoyi.common.annotation.Excel', 'com.ruoyi.common.core.domain.BaseEntity'];
  if (fields.some(f => f.type === 'Date')) imports.push('java.util.Date');
  if (fields.some(f => f.notNull)) { imports.push('jakarta.validation.constraints.NotBlank'); imports.push('jakarta.validation.constraints.NotNull'); }
  
  let fieldLines = '';
  for (const f of fields) {
    const annotations = [];
    if (f.notNull && f.type === 'String') annotations.push(`@NotBlank(message = "${f.notNullMsg}")`);
    else if (f.notNull && f.type !== 'String') annotations.push(`@NotNull(message = "${f.notNullMsg}")`);
    if (f.jsonFormat) annotations.push('@JsonFormat(pattern = "yyyy-MM-dd")');
    annotations.push(`@Excel(name = "${f.excelName}")`);
    fieldLines += `    ${annotations.join(' ')}\n    private ${f.type} ${f.name};\n\n`;
  }
  
  let getterSetter = '';
  for (const f of fields) {
    const capName = f.name.charAt(0).toUpperCase() + f.name.slice(1);
    getterSetter += `    public ${f.type} get${capName}() { return ${f.name}; }\n`;
    getterSetter += `    public void set${capName}(${f.type} ${f.name}) { this.${f.name} = ${f.name}; }\n`;
  }
  
  return `package com.ruoyi.hr.domain;

${[...new Set(imports)].map(i => `import ${i};`).join('\n')}

public class ${className} extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long ${idField};
${fieldLines}
    public Long get${idField.charAt(0).toUpperCase() + idField.slice(1)}() { return ${idField}; }
    public void set${idField.charAt(0).toUpperCase() + idField.slice(1)}(Long ${idField}) { this.${idField} = ${idField}; }
${getterSetter}}
`;
}

function genMapper(entity) {
  const { className, tableName, idField, idColumn, fields } = entity;
  const allFields = [{ name: idField, column: idColumn, type: 'Long' }, ...fields];
  const baseFields = ['create_by', 'create_time', 'update_by', 'update_time', 'remark'];
  
  let resultMap = '';
  for (const f of allFields) {
    resultMap += `        <result property="${f.name}" column="${f.column}"/>\n`;
  }
  for (const bf of baseFields) {
    const prop = bf.split('_').map((w, i) => i === 0 ? w : w.charAt(0).toUpperCase() + w.slice(1)).join('');
    resultMap += `        <result property="${prop}" column="${bf}"/>\n`;
  }
  
  const selectCols = allFields.map(f => f.column).join(', ') + ', create_by, create_time, update_by, update_time, remark';
  const queryFields = fields.filter(f => f.name !== 'status' && f.name !== 'delFlag');
  
  let whereConditions = `            del_flag = '0'\n`;
  for (const f of queryFields) {
    if (f.type === 'String') {
      whereConditions += `            <if test="${f.name} != null and ${f.name} != ''"> AND ${f.column} LIKE CONCAT('%', #{${f.name}}, '%')</if>\n`;
    } else if (f.type === 'Date') {
      whereConditions += `            <if test="${f.name} != null"> AND ${f.column} = #{${f.name}}</if>\n`;
    } else {
      whereConditions += `            <if test="${f.name} != null"> AND ${f.column} = #{${f.name}}</if>\n`;
    }
  }
  whereConditions += `            <if test="employeeId != null"> AND employee_id = #{employeeId}</if>\n`;
  
  const insertCols = fields.filter(f => f.name !== 'status' && f.name !== 'delFlag').map(f => f.column).join(', ') + ', status, del_flag, create_by, create_time, remark';
  const insertVals = fields.filter(f => f.name !== 'status' && f.name !== 'delFlag').map(f => `#{${f.name}}`).join(', ') + ", #{status}, #{delFlag}, #{createBy}, sysdate(), #{remark}";
  
  let updateSet = '';
  for (const f of fields) {
    if (f.name === 'status' || f.name === 'delFlag') continue;
    if (f.type === 'String') {
      updateSet += `            <if test="${f.name} != null and ${f.name} != ''">${f.column} = #{${f.name}},</if>\n`;
    } else {
      updateSet += `            <if test="${f.name} != null">${f.column} = #{${f.name}},</if>\n`;
    }
  }
  updateSet += `            <if test="status != null and status != ''">status = #{status},</if>\n`;
  updateSet += `            <if test="updateBy != null and updateBy != ''">update_by = #{updateBy},</if>\n`;
  updateSet += `            <if test="remark != null">remark = #{remark},</if>\n`;
  updateSet += `            update_time = sysdate()`;
  
  const capId = idField.charAt(0).toUpperCase() + idField.slice(1);
  
  return `<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.hr.mapper.${className}Mapper">

    <resultMap type="${className}" id="${className}Result">
${resultMap}    </resultMap>

    <sql id="selectVo">
        SELECT ${selectCols}
        FROM ${tableName}
    </sql>

    <select id="select${className}List" parameterType="${className}" resultMap="${className}Result">
        <include refid="selectVo"/>
        <where>
${whereConditions}        </where>
        ORDER BY create_time DESC
    </select>

    <select id="select${className}By${capId}" parameterType="Long" resultMap="${className}Result">
        <include refid="selectVo"/> WHERE ${idColumn} = #{${idField}} AND del_flag = '0'
    </select>

    <insert id="insert${className}" parameterType="${className}" useGeneratedKeys="true" keyProperty="${idField}">
        INSERT INTO ${tableName} (
            ${insertCols}
        ) VALUES (
            ${insertVals}
        )
    </insert>

    <update id="update${className}" parameterType="${className}">
        UPDATE ${tableName}
        <set>
${updateSet}
        </set>
        WHERE ${idColumn} = #{${idField}}
    </update>

    <delete id="delete${className}By${capId}" parameterType="Long">
        UPDATE ${tableName} SET del_flag = '2' WHERE ${idColumn} = #{${idField}}
    </delete>

    <delete id="delete${className}ByIds" parameterType="Long">
        UPDATE ${tableName} SET del_flag = '2' WHERE ${idColumn} IN
        <foreach item="${idField}" collection="array" open="(" separator="," close=")">#{${idField}}</foreach>
    </delete>

</mapper>
`;
}

function genMapperInterface(entity) {
  const { className, idField } = entity;
  const capId = idField.charAt(0).toUpperCase() + idField.slice(1);
  return `package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.${className};

public interface ${className}Mapper {
    public ${className} select${className}By${capId}(Long ${idField});
    public List<${className}> select${className}List(${className} ${className.charAt(0).toLowerCase() + className.slice(1)});
    public int insert${className}(${className} ${className.charAt(0).toLowerCase() + className.slice(1)});
    public int update${className}(${className} ${className.charAt(0).toLowerCase() + className.slice(1)});
    public int delete${className}By${capId}(Long ${idField});
    public int delete${className}ByIds(Long[] ${idField}s);
}
`;
}

function genServiceInterface(entity) {
  const { className, idField } = entity;
  const capId = idField.charAt(0).toUpperCase() + idField.slice(1);
  const varName = className.charAt(0).toLowerCase() + className.slice(1);
  return `package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.${className};

public interface I${className}Service {
    public ${className} select${className}By${capId}(Long ${idField});
    public List<${className}> select${className}List(${className} ${varName});
    public int insert${className}(${className} ${varName});
    public int update${className}(${className} ${varName});
    public int delete${className}By${capId}(Long ${idField});
    public int delete${className}ByIds(Long[] ${idField}s);
}
`;
}

function genServiceImpl(entity) {
  const { className, idField } = entity;
  const capId = idField.charAt(0).toUpperCase() + idField.slice(1);
  const varName = className.charAt(0).toLowerCase() + className.slice(1);
  return `package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.mapper.${className}Mapper;
import com.ruoyi.hr.domain.${className};
import com.ruoyi.hr.service.I${className}Service;

@Service
public class ${className}ServiceImpl implements I${className}Service {
    @Autowired
    private ${className}Mapper ${varName}Mapper;

    @Override
    public ${className} select${className}By${capId}(Long ${idField}) {
        return ${varName}Mapper.select${className}By${capId}(${idField});
    }

    @Override
    public List<${className}> select${className}List(${className} ${varName}) {
        return ${varName}Mapper.select${className}List(${varName});
    }

    @Override
    public int insert${className}(${className} ${varName}) {
        return ${varName}Mapper.insert${className}(${varName});
    }

    @Override
    public int update${className}(${className} ${varName}) {
        return ${varName}Mapper.update${className}(${varName});
    }

    @Override
    public int delete${className}By${capId}(Long ${idField}) {
        return ${varName}Mapper.delete${className}By${capId}(${idField});
    }

    @Override
    public int delete${className}ByIds(Long[] ${idField}s) {
        return ${varName}Mapper.delete${className}ByIds(${idField}s);
    }
}
`;
}

function genController(entity) {
  const { className, cnName, idField } = entity;
  const capId = idField.charAt(0).toUpperCase() + idField.slice(1);
  const varName = className.charAt(0).toLowerCase() + className.slice(1);
  const permissionPrefix = `hr:${className.replace('HrEmployee', 'hrEmployee').replace(/^Hr/, 'hr:').toLowerCase()}`
    .replace(/([A-Z])/g, (m, i) => i === 0 ? '' : ':' + m.toLowerCase())
    .replace(/^hr:hr/, 'hr');
  // Simpler approach
  const permName = className.replace(/^Hr/, '').replace(/([A-Z])/g, (m, i) => i === 0 ? m.toLowerCase() : m);
  const perm = `hr:${permName.charAt(0).toLowerCase() + permName.slice(1)}`;
  
  return `package com.ruoyi.hr.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.hr.domain.${className};
import com.ruoyi.hr.service.I${className}Service;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/${permName.split(/(?=[A-Z])|(?<=[a-z])(?=[A-Z])/).join('').toLowerCase()}")
public class ${className}Controller extends BaseController {
    @Autowired
    private I${className}Service ${varName}Service;

    @PreAuthorize("@ss.hasPermi('${perm}:list')")
    @GetMapping("/list")
    public TableDataInfo list(${className} ${varName}) {
        startPage();
        List<${className}> list = ${varName}Service.select${className}List(${varName});
        return getDataTable(list);
    }

    @Log(title = "${cnName}", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('${perm}:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ${className} ${varName}) {
        List<${className}> list = ${varName}Service.select${className}List(${varName});
        ExcelUtil<${className}> util = new ExcelUtil<>(${className}.class);
        util.exportExcel(response, list, "${cnName}");
    }

    @PreAuthorize("@ss.hasPermi('${perm}:query')")
    @GetMapping(value = "/{${idField}}")
    public AjaxResult getInfo(@PathVariable("${idField}") Long ${idField}) {
        return AjaxResult.success(${varName}Service.select${className}By${capId}(${idField}));
    }

    @PreAuthorize("@ss.hasPermi('${perm}:add')")
    @Log(title = "${cnName}", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ${className} ${varName}) {
        return toAjax(${varName}Service.insert${className}(${varName}));
    }

    @PreAuthorize("@ss.hasPermi('${perm}:edit')")
    @Log(title = "${cnName}", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ${className} ${varName}) {
        return toAjax(${varName}Service.update${className}(${varName}));
    }

    @PreAuthorize("@ss.hasPermi('${perm}:remove')")
    @Log(title = "${cnName}", businessType = BusinessType.DELETE)
    @DeleteMapping("/{${idField}s}")
    public AjaxResult remove(@PathVariable Long[] ${idField}s) {
        return toAjax(${varName}Service.delete${className}ByIds(${idField}s));
    }
}
`;
}

// Generate all files
for (const entity of entities) {
  // Domain
  const domainPath = path.join(baseDir, 'domain', `${entity.className}.java`);
  fs.writeFileSync(domainPath, genDomain(entity), 'utf8');
  
  // Mapper interface
  const mapperInterfacePath = path.join(baseDir, 'mapper', `${entity.className}Mapper.java`);
  fs.writeFileSync(mapperInterfacePath, genMapperInterface(entity), 'utf8');
  
  // Mapper XML
  const mapperXmlPath = path.join(mapperDir, `${entity.className}Mapper.xml`);
  fs.writeFileSync(mapperXmlPath, genMapper(entity), 'utf8');
  
  // Service interface
  const servicePath = path.join(baseDir, 'service', `I${entity.className}Service.java`);
  fs.writeFileSync(servicePath, genServiceInterface(entity), 'utf8');
  
  // Service impl
  const serviceImplPath = path.join(baseDir, 'service/impl', `${entity.className}ServiceImpl.java`);
  fs.writeFileSync(serviceImplPath, genServiceImpl(entity), 'utf8');
  
  // Controller
  const controllerPath = path.join(baseDir, 'controller', `${entity.className}Controller.java`);
  fs.writeFileSync(controllerPath, genController(entity), 'utf8');
  
  console.log(`Generated: ${entity.className}`);
}

console.log('All backend files generated successfully!');
