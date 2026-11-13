# 金蝶云星辰对接 — 数据库升级脚本修复任务书

> 交付对象：金蝶集成模块开发同事（可整体交给 AI 执行）
> 仓库：`edop_ruoyi3`（yhny 分支）
> 涉及文件：`deploy/config/sql/add_kingdee_full_upgrade_20261111.sql`、`deploy/config/sql/add_kingdee_menu.sql`、`ruoyi-kingdee/lib/`、`.gitignore`
> 范围约束：**只改 SQL 脚本与 lib 卫生，不改动任何 Java 代码**

---

## 一、背景

- 模块功能本身（ruoyi-kingdee：SDK 适配层、签名、客户/物料/供应商同步、前端配置页）无代码问题，无需改动。
- 问题是配套的数据库升级脚本在**生产环境（MySQL 8.0.28）实测无法执行**，且与仓库内既有升级脚本存在同名表结构冲突。
- 仓库中已有两份符合生产规范的幂等脚本范本，可直接参考写法：
  - `sql/upgrade_20261110_all.sql`
  - `sql/upgrade_20261111_dept_leader_id.sql`

---

## 二、问题 1（阻断级）：SQL 语法不兼容 MySQL 8.0

### 现象

脚本第二、三、四节使用了 `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` 语法。该语法是 MariaDB 专有的，MySQL 8.0 实测报错：

```
ERROR 1064 (42000): You have an error in your SQL syntax ...
near 'IF NOT EXISTS c1 INT' at line 1
```

导致脚本执行到 sys_user 就中断，后续 mk_customer 补列、新表、菜单**全部不会执行**。

### 涉及语句（全部需要重写）

- `sys_user`：leader_id / leader_name / superior_leader_id / superior_leader_name / charge_leader_id / charge_leader_name（6 列）
- `sys_logininfor`：`MODIFY COLUMN msg VARCHAR(500)`（此句无 IF NOT EXISTS，可直接保留）
- `mk_customer`：credit_days / credit_limit / customer_type / parent_customer_id / contact_phone（5 列）

### 改法

所有 `ADD COLUMN IF NOT EXISTS` 统一改为 information_schema 判断 + prepare/execute 的幂等模式。参考同仓库 `sql/upgrade_20261111_dept_leader_id.sql` 的现成写法：

```sql
-- 幂等加列模板（照此模式逐列改写）
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column leader_id bigint default null comment ''直接领导ID''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
```

注意：

- `MODIFY COLUMN msg VARCHAR(500)` 无需判断，直接执行即可（重复执行无副作用）
- 列定义、注释、默认值保持原脚本不变，只改"判断存在性"的实现方式

---

## 三、问题 2（冲突级）：第五节与既有脚本的同名表定义冲突

### 现象

脚本第五节创建的 `integration_user_binding` 和 `integration_sync_log` 与同仓库 `sql/integration_attendance.sql`（已于 2026-11-10 在生产执行）中的同名表**结构完全不同**：

| 项 | 既有表（生产已存在） | 你的脚本定义 |
|---|---|---|
| 主键 | binding_id | id |
| 关键列 | connector_type, external_userid, external_name, mobile, external_job_no, employee_no, employee_id, match_type, sync_time, remark | user_id, account_type, external_id, external_name, ext_json, bind_time, unbind_time, status |
| 唯一键 | uk_connector_user (connector_type, external_userid) | uk_user_type (user_id, account_type, status) |

生产库已按**既有表**结构建好。你的脚本用 `CREATE TABLE IF NOT EXISTS` 会静默跳过建表。

### 关键事实

已核实 **ruoyi-kingdee 模块代码没有引用这两张表**（全模块零引用；`integration_user_binding` 仅原 ruoyi-integration 模块的钉钉绑定功能在用）。因此第五节当前是"多余的死 SQL"，但留着会在**新环境首装**时埋雷：谁先执行谁的表结构生效，另一个模块可能挂。

### 改法（二选一）

- **改法 A（推荐）**：直接删除第五节整段
- **改法 B**：若金蝶后续确需绑定/日志表，将表改名为 `kingdee_binding` / `kingdee_sync_log`，并同步提供对应 mapper 与建表语句

---

## 四、问题 3（卫生级）：lib 目录无效二进制

- `ruoyi-kingdee/lib/` 下 4 个 rar/zip 压缩包（各约 1.9MB）不应入库
- `kingdee-sdk.jar` 实测内容是一段 JSON 错误文本（`{"code":1000800,"msg":"资源不存在！"}`），**不是有效 jar**
- `pom.xml` 未引用它们（ApigwClient 等类已以源码形式内嵌在 `ruoyi-kingdee/src/main/java/com/kingdee/apigw/`，构建不受影响）

### 改法

- `git rm` 移除 lib 目录全部文件
- `.gitignore` 追加 `ruoyi-kingdee/lib/`
- 如需保留 SDK 原始包，放公司网盘并在 README 记录下载地址

---

## 五、不需要动的部分

- ruoyi-kingdee 模块全部 Java 代码（与既有模块无任何冲突）
- 菜单 SQL（`add_kingdee_menu.sql` 及完整脚本中的菜单段）：幂等写法正确（NOT EXISTS），保留
- `sys_user` 列与既有 PART 0 重叠部分（superior_leader_id / charge_leader_id 已存在）：幂等跳过，无冲突
- 密钥方案：`ENC:` AES 加密 + 前端「金蝶配置」页录入，正确，保留

---

## 六、约束

1. 只改 `deploy/config/sql/` 下两个 SQL 文件、`ruoyi-kingdee/lib/`、`.gitignore`
2. 保持脚本幂等：重复执行无副作用、无报错
3. 菜单 SQL 保持 NOT EXISTS 写法不变
4. 不得把明文密钥写进脚本（保持 `ENC:` 加密方案，首次部署通过前端配置页录入）

---

## 七、验收标准（生产环境 MySQL 8.0.28 实测）

1. 脚本从头到尾**连续执行两遍**，两遍均无 ERROR，第二遍为全部幂等跳过
2. 执行后核验：
   - `sys_user` 新增 6 列（leader_id / leader_name / superior_leader_id / superior_leader_name / charge_leader_id / charge_leader_name）
   - `mk_customer` 新增 5 列（credit_days / credit_limit / customer_type / parent_customer_id / contact_phone）
   - `sys_logininfor.msg` 为 VARCHAR(500)
   - 金蝶菜单树完整且无重复（「金蝶对接」一级菜单 + 配置页 + 按钮权限）
   - 金蝶相关 sys_config 配置项存在（kingdee.enabled / timeout / retry_times / secret_refresh_cron / sync_cron）
3. `integration_user_binding` / `integration_sync_log` 在生产库中的结构保持与 `sql/integration_attendance.sql` 一致（即新脚本不再触碰这两张表）
4. `ruoyi-kingdee` 模块 `mvn clean package -DskipTests` 构建通过

---

## 八、部署提醒（改完合并后）

1. 需要重新打包后端 jar（新增 ruoyi-kingdee 模块）+ 前端 dist（新增金蝶配置页）
2. 首次部署后在「金蝶配置」页录入 app_key / client_id / app_secret / client_secret 等凭据（`ENC:` 加密落库），并确认服务端 `ruoyi.fileEncryptionKey` 与加密时一致，否则解密失败
3. 金蝶同步默认每 30 分钟自动执行一次，可在配置页手动触发验证连通性
