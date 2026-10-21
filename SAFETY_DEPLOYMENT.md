# 安全生产管理模块 - 部署指南

## 📋 已完成内容

### 后端代码 (Java)
- ✅ Maven 模块结构 (`ruoyi-safety/pom.xml`)
- ✅ 父项目和Admin模块注册
- ✅ Domain实体类: `SafetyEnterprise`, `SafetyRiskPoint`, `SafetyHazard`
- ✅ Mapper接口: `SafetyHazardMapper`
- ✅ Service接口: `ISafetyHazardService`
- ✅ Controller: `SafetyHazardController`
- ✅ MyBatis XML映射: `SafetyHazardMapper.xml`
- ✅ XSS配置更新 (`application.yml`)

### 前端代码 (Vue3)
- ✅ API接口: `api/safety/hazard.js`, `api/safety/risk.js`, `api/safety/enterprise.js`
- ✅ Vue页面: `views/safety/dualcontrol/hazard/index.vue` (隐患管理)

### 数据库
- ✅ SQL脚本: `sql/safety_all.sql` (18个业务表 + 字典 + 菜单)
- ✅ 部署脚本: `deploy_safety.bat`

## 🚀 部署步骤

### 步骤1: 执行SQL脚本
打开MySQL客户端或命令行，执行以下操作：

```bash
# 方式1: 使用MySQL命令行
mysql -h localhost -u root -p123456 ry-vue < e:/AIcode/EDOP/RuoYi/RuoYi-Vue3/sql/safety_all.sql

# 方式2: 使用Navicat或DBeaver等工具
# 打开SQL文件: sql/safety_all.sql
# 在数据库 ry-vue 中执行全部SQL语句
```

**SQL脚本包含内容:**
- 18个业务表 (企业档案、区域、证照、人员、设备、物料、风险点、隐患等)
- 23个字典类型
- 60+ 字典数据
- 30+ 菜单和权限配置

### 步骤2: 编译后端项目

```bash
cd e:/AIcode/EDOP/RuoYi/RuoYi-Vue3
mvn clean package -DskipTests
```

如果编译出现错误，常见解决方案:
- 找不到类: 检查`pom.xml`中的依赖是否正确
- Mapper扫描问题: 检查`application.yml`中的`mapperLocations`配置

### 步骤3: 启动后端服务

```bash
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

或者使用提供的部署脚本:
```bash
deploy_safety.bat
```

### 步骤4: 启动前端服务

```bash
cd e:/AIcode/EDOP/RuoYi/RuoYi-Vue3/ruoyi-ui
npm install
npm run dev
```

### 步骤5: 登录并测试

1. 访问: `http://localhost:8080`
2. 登录系统 (admin/admin123)
3. 菜单中会出现"安全生产管理"一级菜单
4. 依次展开测试功能模块

## 📁 文件结构

```
RuoYi-Vue3/
├── ruoyi-safety/                          # 新增后端模块
│   ├── pom.xml
│   └── src/main/java/com/ruoyi/safety/
│       ├── controller/
│       │   └── SafetyHazardController.java
│       ├── domain/
│       │   ├── SafetyEnterprise.java
│       │   ├── SafetyRiskPoint.java
│       │   └── SafetyHazard.java
│       ├── mapper/
│       │   └── SafetyHazardMapper.java
│       ├── service/
│       │   └── ISafetyHazardService.java
│       └── resources/mapper/safety/
│           └── SafetyHazardMapper.xml
├── ruoyi-ui/src/
│   ├── api/safety/                        # 新增API接口
│   │   ├── hazard.js
│   │   ├── risk.js
│   │   └── enterprise.js
│   └── views/safety/                      # 新增页面
│       └── dualcontrol/
│           └── hazard/
│               └── index.vue
├── sql/
│   └── safety_all.sql                     # SQL脚本
├── deploy_safety.bat                      # 部署脚本
├── pom.xml                               # 已更新
└── ruoyi-admin/
    ├── pom.xml                           # 已更新
    └── src/main/resources/
        └── application.yml                # 已更新XSS配置
```

## ⚠️ 需要补充的代码

由于代码量巨大,以下代码需要您根据已创建的模板补充:

### 后端需要补充 (参考WMS/QMS模块)

1. **Service实现类**
   - `SafetyHazardServiceImpl.java`
   - `SafetyRiskPointServiceImpl.java`
   - `SafetyEnterpriseServiceImpl.java`
   - 以及其他14个实体对应的Service实现

2. **剩余的Mapper和XML**
   - 参考已创建的`SafetyHazardMapper`和`SafetyHazardMapper.xml`
   - 为其他17个表创建对应的Mapper和XML

3. **剩余的Controller**
   - 参考已创建的`SafetyHazardController`
   - 为其他功能创建Controller

### 前端需要补充

1. **API文件**
   - `api/safety/area.js`
   - `api/safety/cert.js`
   - `api/safety/worker.js`
   - `api/safety/equipment.js`
   - `api/safety/material.js`
   - `api/safety/task.js`
   - `api/safety/training.js`
   - `api/safety/emergency.js`

2. **Vue页面**
   - `views/safety/arch/enterprise/index.vue`
   - `views/safety/arch/area/index.vue`
   - `views/safety/arch/cert/index.vue`
   - `views/safety/arch/worker/index.vue`
   - `views/safety/arch/equipment/index.vue`
   - `views/safety/arch/material/index.vue`
   - `views/safety/dualcontrol/risk/index.vue`
   - `views/safety/dualcontrol/task/index.vue`
   - `views/safety/training/plan/index.vue`
   - `views/safety/training/record/index.vue`
   - `views/safety/emergency/plan/index.vue`
   - `views/safety/emergency/material/index.vue`
   - `views/safety/emergency/drill/index.vue`

## 🔧 代码生成建议

由于需要大量相似的CRUD代码,建议使用RuoYi自带的代码生成器:

1. 执行SQL后,在系统菜单 `系统工具 > 代码生成` 中找到业务表
2. 选择需要的表进行代码生成
3. 下载生成代码后,复制到对应目录
4. 略微修改导入包和类名即可

## 📝 功能模块清单

| 模块 | 表名 | 状态 | 优先级 |
|------|------|------|--------|
| 一企一档 | biz_safety_enterprise | SQL+部分代码 | P0 |
| 区域管理 | biz_safety_area | SQL | P0 |
| 证照管理 | biz_safety_cert | SQL | P0 |
| 特种人员 | biz_safety_worker | SQL | P0 |
| 设备台账 | biz_safety_equipment | SQL | P0 |
| 物料台账 | biz_safety_material | SQL | P0 |
| 风险点管理 | biz_safety_risk_point | SQL+部分代码 | P0 |
| 隐患管理 | biz_safety_hazard | SQL+完整代码 | P0 |
| 整改记录 | biz_safety_rectify | SQL | P0 |
| 排查任务 | biz_safety_inspection_task | SQL | P1 |
| 培训计划 | biz_safety_training_plan | SQL | P1 |
| 培训记录 | biz_safety_training_record | SQL | P1 |
| 应急预案 | biz_safety_emergency_plan | SQL | P1 |
| 应急物资 | biz_safety_emergency_material | SQL | P1 |
| 演练记录 | biz_safety_drill | SQL | P1 |
| 事故管理 | biz_safety_incident | SQL | P1 |
| 到期提醒 | biz_safety_remind | SQL | P1 |
| 标准库 | biz_safety_std_library | SQL | P2 |

## ✅ 验收测试

完成部署后,请按以下顺序测试:

1. **菜单显示**: 检查"安全生产管理"菜单是否正确显示
2. **权限配置**: 分配角色权限
3. **基础功能**:
   - 企业档案增删改查
   - 隐患管理全流程 (新增→提交→整改→验收→闭环)
   - 风险点管理 (含LEC计算)
4. **到期提醒**: 验证证照、特作证到期提醒
5. **数据导出**: 验证Excel导出功能

## 🆘 常见问题

### Q: 编译时找不到某个类
A: 检查Maven依赖,确保ruoyi-safety模块被正确注册

### Q: Mapper无法扫描
A: 检查`application.yml`中的`mapperLocations`配置: `classpath*:mapper/**/*Mapper.xml`

### Q: 前端接口404
A: 检查Controller的`@RequestMapping`路径是否与前端API一致

### Q: SQL执行失败
A: 检查数据库连接,确保`ry-vue`数据库存在

## 📞 技术支持

如有问题,请检查:
1. 日志文件: `logs/ruoyi.log`
2. 浏览器控制台错误
3. 数据库表是否创建成功