# xxt-sc

校享团校园供应链平台 V2 · 供应链域工程

- 对外产品名：校享团校园供应链平台 V2
- 工程代号：`xxt-campus-platform`
- 本仓库：供应链（Supply Chain）相关模块
- 需求基线：[docs/PRD.md](./docs/PRD.md)
- 开发约定：[AGENTS.md](./AGENTS.md)（**提交前必读**）
- 技术补充规范：[docs/技术规范补充.md](./docs/技术规范补充.md)

## 项目定位

「学生消费 + 楼长经营 + 平台运营/财务 + 供应链自助 + ERP/支付协同」的校园供应链平台，
不是单一商城。

一期必须先跑通一条可核对的资金闭环：

```
学生登录 → 浏览学校/店铺/SKU → 下单并支付 → 楼长接单发货
→ 学生确认收货 → 清分/入账 → 财务查询导出 → 部分退款时冲正原流水
```

三条交易链分开核算：

1. 供应商供货 → 平台采购与入库
2. 平台供货 → 楼长采购、调拨与库存
3. 楼长销售 → 学生订单、收款、售后和分润

## 开发红线（违反一律打回）

- 客户端下单**只提交 SKU 和数量**，金额/优惠/手续费/分润全部服务端计算并保存快照
- 订单、支付、履约、售后、清分、ERP 同步**各自独立状态**，不得合并成一个字段
- 支付成功以**渠道回调或后端查单**为准，页面提示不改变事实
- 回调必须校验签名、商户、金额、订单号和幂等键，重复通知不得重复入账
- 清分后退款**生成冲正记录，不删除原流水**
- 供应商 `supplierId` 从**登录身份推导**，绝不信任请求参数
- 金额使用**分**，时间使用 **Asia/Shanghai**
- 待决策项（D-001～D-010）未签字前，不得把临时值写成正式业务规则

## 技术约定

- API 统一前缀 `/api/v1/...`，领域划分 `/ma/` `/leader/` `/admin/` `/supplier/` `/webhooks/` `/exports/`
- 所有写操作支持幂等键
- 错误响应：业务错误码 + 可读消息 + 追踪标识 + 是否可重试
- 第三方（汇付 / OCR / 管家婆 ERP）必须有失败队列、重试和人工处理入口

## 协作方式

| 账号 | 职责 |
|---|---|
| `aiyangjan` | 日常开发、提交分支、发起 PR |
| `aiyangdie` | 代码审核、合并 PR |

所有改动通过 Pull Request 进入 `main`，保留完整评审记录。

## 技术栈

- **Java 8 + Spring Boot 2.7.x（2.7.18）+ Maven**（与本机 JDK 1.8 及一期 `xxt-boot` 对齐）
- 接口统一前缀 `/api/v1`，统一响应结构（`code` / `message` / `traceId` / `retryable` / `data`）
- 金额一律以「分」为单位的 `long` 存储与传输，禁止浮点数参与金额计算

## 目录结构

```
src/main/java/com/xxt/sc/
├── XxtScApplication.java     启动类
├── common/
│   ├── Amount.java           金额工具（元分转换、按比例分摊）
│   ├── exception/BizException.java
│   └── result/
│       ├── ApiResponse.java  统一响应
│       └── ErrorCode.java    错误码（含 retryable 标记）
└── supplier/
    ├── SupplierIdentityProvider.java  供应商身份来源（必须从登录身份推导）
    └── SupplierScope.java             数据范围校验（对应 AC-102）
```

## 本地开发

```bash
mvn -q spring-boot:run
# 或打包后运行
mvn clean package && java -jar target/xxt-sc-0.1.0-SNAPSHOT.jar
```

服务默认端口 `8081`，健康检查：`http://localhost:8081/actuator/health`

> `application.yml` 中的 `xxt.sc.order.*` 为**临时占位值**（对应 PRD D-001 / D-005），
> 业务负责人签字确认前不得当作正式业务规则。
