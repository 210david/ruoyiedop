# EDOP × WorkBuddy 连接器 · 逐用户鉴权（OAuth）详细设计

> 版本：v1.0 ｜ 日期：2026-11-06 ｜ 状态：**待评审**
> 上游文档：`EDOP-WorkBuddy连接器接入方案.md` v1.2（2A/2B 节为本文的需求来源）
> 批次：Phase 2-2.2 ｜ 前置：2.1 已完成（HTTP 网关 + API Key 验收通过）

---

## 1. 目标与 Non-goals

**目标**：同一网关下，不同 WorkBuddy 用户查到各自数据权限范围内的数据——权限跟随 EDOP 用户身份（角色 + @DataScope），不新建独立权限体系。

**Non-goals（本期不做）**：
- 不改现有登录/JWT 体系，不动任何业务 Controller
- 不做写入类操作的逐用户授权（仍只读）
- stdio 模式不改造（个人本机用 profile 配自己账号，天然逐人）
- 不引入 spring-security-oauth 重框架（轻量自研 3 个端点）

## 2. 环境事实（已核实，设计依据）

| 事实 | 设计影响 |
|------|---------|
| 无短信服务（无任何 sms 依赖/配置） | 第一期登录凭证用 **EDOP 账号+密码**（授权页），短信验证码作为可选增强待客户有短信服务时启用 |
| 无 spring-security-oauth | 自研轻量授权端点，凭证自定义（grant_token） |
| RuoYi JWT 30 分钟滑动过期 | 网关不能直接持有用户 JWT 常驻 → 引入长效 grant_token 换发机制 |
| 2.1 已建成 HTTP 网关（x-api-key 服务级鉴权） | 网关双鉴权：x-api-key=服务账号（保留），Bearer grant_token=逐用户（新增） |

## 3. 总体流程

```
【首次授权】用户 → EDOP 授权页(账号密码登录) → 校验用户状态/密码
         → 生成 grant_token(24位随机) → 库存哈希，绑定 user_id
         → 展示 token + 使用说明（用户粘贴进 WorkBuddy 连接器 headers）

【每次调用】WorkBuddy → 网关(Bearer grant_token)
         → 网关缓存无此用户 JWT 或已过期 → 调 /edop/connector/auth/exchange 换 JWT
         → 以该 JWT 调业务接口（/wms/inventory/list 等）
         → @DataScope 按该用户部门/角色裁剪 → 返回

【吊销】用户"我的授权"自助撤销 / 管理员管理页强制吊销 / 用户停用自动断权
```

**核心设计决策：grant_token 与业务 JWT 分离**
- grant_token：长效（默认 90 天滚动续期）、可吊销、库存 SHA-256 哈希、绑定 user_id —— 这是"连接器世界的身份证"
- 业务 JWT：由 exchange 端点按 grant_token 换发，30 分钟过期，网关缓存自动重换 —— EDOP 现有鉴权零侵入
- 好处：改密码/停用立即断权（exchange 时校验用户状态）；token 泄露只影响 90 天窗口且可即时吊销；不碰 RuoYi 登录核心逻辑

## 4. 数据库设计

```sql
CREATE TABLE edop_connector_grant (
  grant_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '授权ID',
  user_id         BIGINT       NOT NULL                COMMENT 'EDOP用户ID',
  grant_token_hash CHAR(64)    NOT NULL                COMMENT 'grant_token的SHA-256哈希(唯一索引)',
  phone_snapshot  VARCHAR(20)  DEFAULT ''              COMMENT '授权时手机号快照(换绑检测用)',
  client_desc     VARCHAR(100) DEFAULT ''              COMMENT '授权来源描述(如:WorkBuddy-张三电脑)',
  status          CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '0正常 1停用',
  expire_time     DATETIME     DEFAULT NULL            COMMENT '过期时间(90天滚动)',
  last_used_time  DATETIME     DEFAULT NULL            COMMENT '最近使用时间(僵尸授权清理依据)',
  create_time     DATETIME     DEFAULT NULL,
  update_time     DATETIME     DEFAULT NULL,
  del_flag        CHAR(1)      DEFAULT '0',
  PRIMARY KEY (grant_id),
  UNIQUE KEY uk_token_hash (grant_token_hash),
  KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='连接器逐用户授权表';
```

> 遵循主规范 3.1/3.6（显式 COLLATE）、3.4（配套菜单 M/C/F SQL）。不声明数据权限分类（授权数据按 user_id 天然隔离，管理员查全量走管理页权限）。

## 5. 后端新增（EDOP，全部为新增文件，零侵入现有代码）

### 5.1 端点（新 Controller，匿名可访问的仅 login，其余需认证）

| 端点 | 方法 | 鉴权 | 说明 |
|------|------|------|------|
| `/edop/connector/auth/login` | POST | 匿名 | 账号+密码登录 → 校验通过创建 grant → 返回 grant_token（仅此一次明文返回） |
| `/edop/connector/auth/exchange` | POST | grant_token | 校验哈希/状态/过期/用户状态 → 返回该用户业务 JWT（网关专用，30min） |
| `/edop/connector/auth/my` | GET | 用户 JWT | 查"我的授权"列表（设备/时间/状态） |
| `/edop/connector/auth/revoke` | POST | 用户 JWT | 用户自助撤销某条授权 |
| `/edop/connector/auth/admin/list` | GET | `edop:grant:list` 权限 | 管理员查全部授权 |
| `/edop/connector/auth/admin/revoke` | POST | `edop:grant:revoke` 权限 | 管理员强制吊销 |

### 5.2 关键逻辑

- **login**：复用现有密码校验（与登录一致），校验 `del_flag/status`；一人允许多条授权（多设备）；同设备重复授权覆盖旧 token
- **exchange**：每调用刷新 `last_used_time` 与 `expire_time`（滚动 90 天）；用户 `status=1`/`del_flag=1` → 立即 401（停用即断权）；换绑检测：`sys_user.phonenumber ≠ phone_snapshot` 时记审计日志（可选配置"换绑强制重授权"开关，默认关）
- **防爆破**：login 端点 IP 维度简单限流（1 分钟 5 次，内存计数即可）；grant_token 128bit 随机，无爆破空间
- **审计**：所有授权/换发/吊销写 `audit.log`（网关侧已有，后端侧走 log）
- 菜单 SQL：系统管理下新增「连接器授权」目录 + 管理页 C/F 权限，放 `sql/` 目录

### 5.3 管理页（前端 Vue）

- 系统管理 → 连接器授权：列表（用户/手机号快照/来源/状态/最近使用/过期时间）+ 吊销按钮，复用现有列表页模板
- 用户侧入口本期从简：个人中心加"我的授权"仅查询+撤销（工作量小，可放二期）

## 6. 网关改造（gateway.mjs，约半天）

- **鉴权升级**（仅 HTTP 模式）：
  - `x-api-key` → 服务账号模式（现状，不变）
  - `Authorization: Bearer <grant_token>` → 用户模式（新增）
- **JWT 缓存**：`Map<grant_token_hash, {jwt, expireAt}>`；缺失/过期/401 时调 exchange 重换（复用现有 401 重试骨架）
- **审计**：记录 `mode=user|service`，逐用户调用可归因到人
- **工具定义不变**：5 个工具零改动——权限裁剪发生在后端 @DataScope

## 7. WorkBuddy 侧使用（一期形态）

用户在自定义连接器 headers 填自己的 grant_token：

```json
{
  "mcpServers": {
    "edop-assistant": {
      "type": "streamablehttp",
      "url": "http://<服务器IP>:3000/mcp",
      "headers": { "Authorization": "Bearer <我的grant_token>" }
    }
  }
}
```

- 一期：token 手动粘贴（授权页给出复制框）——每人一次，90 天滚动无需重填
- 二期（2.2b）：对接 WorkBuddy 企业控制台 OAuth 2.0 / MCP OAuth 2.1，授权动作内嵌到「启用连接器」流程，无感获取——**前置调研项：腾讯云企业连接器 OAuth 协议的确切要求（元数据端点/客户端注册方式），开工前核实**

## 8. 手机号生命周期场景覆盖（对应方案 2B 承诺）

| 场景 | 机制 |
|------|------|
| 换绑手机号 | grant 绑 user_id 不绑号，在线授权不受影响；phone_snapshot 记录偏差 + 可选强制重授权开关 |
| 修改密码 | 已发 grant 不失效（登录凭证与 grant 分离）；用户可自助撤销旧设备授权 |
| 停用/离职 | exchange 校验用户状态 → 立即断权 |
| 号码回收再分配 | 授权不随号转移（绑 user_id）；后续短信方案中验证码发当前绑号人 |
| 一号多人 | 建议给 sys_user.phonenumber 加唯一检查项（初始化检查清单） |
| 90 天未用 | last_used_time 超期自动置停（定时任务或 exchange 时惰性判断，选惰性——不加定时任务） |

## 9. 验收标准（对应方案 2H）

1. 两个测试账号（不同部门，数据权限分别设"本部门"）各走一遍授权流程拿到 grant_token
2. 两个 WorkBuddy 连接器分别配各自 token，同一工具 `query_inventory` 返回各自部门范围数据
3. 吊销其中一人授权 → 该用户立即 401，另一人不受影响
4. 停用测试账号 → exchange 返回 401（停用即断权实测）
5. 服务账号模式（x-api-key）回归不受影响

## 10. 工作量与风险

| 项 | 估算 |
|----|------|
| 后端（表 SQL + 6 端点 + 权限 + 管理页） | ~1 人天 |
| 网关双鉴权 + JWT 缓存 | ~0.5 人天 |
| 验收（双账号对比 + 吊销/停用实测） | ~0.5 人天 |

**风险**：①@DataScope 并非全部接口生效（统计类豁免）——验收时逐工具核对实际裁剪范围，不符的以"该工具走服务模式"临时兜底；②2.2b 企业控制台 OAuth 协议未核实——不影响本期交付，只影响"无感授权"体验的上线时间。
