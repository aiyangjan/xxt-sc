# AGENTS.md

本文件是给 AI 与开发者共同遵守的项目约定。修改本项目前请先读完。

## 1. 项目

- 工程：`xxt-sc`（校享团校园供应链平台 V2 · 供应链域）
- 工程代号：`xxt-campus-platform`
- 需求基线：[docs/PRD.md](./docs/PRD.md)（业务规则以此为准）
- 技术补充规范：[docs/技术规范补充.md](./docs/技术规范补充.md)

## 2. 环境（已确认，禁止擅自升级）

| 项 | 版本 |
|---|---|
| JDK | **1.8** |
| Maven | 3.9+（直接使用，仓库无 wrapper，不要臆造 `mvnw` 命令） |
| Spring Boot | **2.7.18** |

常用命令：

```bash
mvn -B clean compile     # 编译
mvn -B test              # 运行全部测试
mvn -B spring-boot:run   # 本地启动（端口 8081）
```

## 3. 硬性约束（违反一律打回）

### 语言
- **Java 8 语法**。禁止 `var`、`List.of`、`Map.of`、`String.isBlank`、文本块、Records。
- 金额一律 `long`（单位：**分**）。禁止 `double` / `float` 参与金额计算，禁止用 `BigDecimal` 做无谓转换。
- 时间明确时区 **Asia/Shanghai**，统一 `yyyy-MM-dd HH:mm:ss` 序列化。

### 业务红线（来自 PRD）
- 客户端下单**只提交 SKU 和数量**；金额、优惠分摊、手续费、分润全部服务端计算并存快照。
- 订单、支付、履约、售后、清分、ERP 同步**各自独立状态**，禁止合并成单一字段。
- 支付成功以渠道回调或后端查单为准；回调必须校验签名、商户、金额、订单号、幂等键。
- 清分后退款**生成冲正记录，不删除原流水**。
- `supplierId` 从**登录身份推导**，绝不信任请求参数（见 `SupplierScope`）。
- 待决策 D-001～D-010 未签字前，临时值（如 30 分钟关单）**不得写成正式规则**。

### 安全
- 外部输入一律视为不可信：请求头、参数、上传文件都必须校验或清洗（参考 `TraceId.sanitize`）。
- 密钥、证书、商户号不写入源码、日志、测试快照和示例配置。
- 日志禁止输出完整手机号、身份证、银行卡。

## 4. 目录约定

```
src/main/java/com/xxt/sc/
├── common/      通用能力（result / exception / trace / web），只放跨领域复用代码
└── supplier/    供应链域
```

- 新增业务能力**按领域建包**，不要都塞进 `common`。
- Controller 保持薄：只做参数接收与结果封装，业务判断放应用/领域层。
- 构造器注入优先，禁止字段注入 `@Autowired`。

## 5. 异常与响应

- 可预期的业务拒绝抛 `BizException(ErrorCode.XXX)`，由 `GlobalExceptionHandler` 统一转 `ApiResponse`。
- **业务异常按 HTTP 200 返回**，前端依据响应体 `code` 判断，不是依据 HTTP 状态码。
- `ErrorCode.retryable = true` 的才允许上游/任务队列自动重试（当前仅第三方超时、不可用、系统错误）。

## 6. 测试

- 新增业务逻辑**必须带单元测试**。
- 涉及金额的分摊/计算，必须验证**各行之和恒等于总额**（一分不丢）。
- 外部输入清洗类逻辑，必须覆盖换行、CRLF、超长、空值等注入场景。
- 提交前本地跑通 `mvn -B test`。

## 7. Git 工作流

- 分支：`feat/xxx`、`fix/xxx`、`docs/xxx`、`chore/xxx`。
- 提交信息：`type: 简述`，type ∈ `feat` / `fix` / `docs` / `refactor` / `test` / `chore`。
- 所有改动走 **Pull Request**，禁止直接 push `main`。
- PR 需说明：改了什么、为什么、如何验证。

## 8. 当前状态与阻塞

- **PR #1 未合并**：远端 `main` 目前只有 README.md，代码全在 `feat/init`。
- **本地 git 历史与远端已分叉**（曾因网络问题改用 GitHub API 提交）。PR 合并后需 `git reset --hard origin/main` 对齐，不要直接 `git pull`。
- **ORM 选型未定**（MyBatis-Plus / JPA）——未定前不写数据访问层。
- **是否升级 Java 17 未定**——未定前不得擅自升级 Spring Boot 大版本。

## 9. 禁止事项

- 不为「更现代」擅自升级 JDK、Spring Boot、依赖大版本。
- 不为套设计模式引入无必要的接口、工厂、事件层。
- 不在未确认业务语义时更改公开 API、字段含义或错误码。
- 不伪造测试结果：没跑过的测试不得称为通过。
