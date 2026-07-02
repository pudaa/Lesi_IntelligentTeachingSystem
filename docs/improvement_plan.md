# 乐思智能刷题助手 —— 项目分析与改进方向

> 分析日期：2026-07-02 | 项目版本：1.0-SNAPSHOT | Java 17 + Swing + Maven

---

## 一、项目概述

本项目是一个基于 Java Swing 的 CS 架构刷题工具，包含**学生端**（增强练习、模拟考试、错题集）和**教师端**（试题管理、测后分析、答题记录、学生分析），采用 MySQL 远程数据库 + SQLite 本地缓存的混合存储方案，并通过 FlatLaf 实现现代化 UI 主题。

---

## 二、现存问题分析

### 2.1 架构设计问题（严重程度：🔴 高）

| 问题 | 位置 | 说明 |
|------|------|------|
| **无分层架构** | 全局 | UI 层、业务逻辑层、数据访问层完全混杂，`ConnectionUtil` 既是数据库门面又被直接传入各个 UI 组件 |
| **ConnectionUtil 职责过重** | `JDBC/ConnectionUtil.java` | 同时承担数据库连接管理、数据同步、登录验证等多个职责，违反单一职责原则 |
| **存在冗余副本类** | `JDBC/ConnectionUtilCopy.java` | 与 `OriConnectionUtil` 功能高度重叠，仅 PreparedStatement 使用方式不同，增加维护成本 |
| **组件紧耦合** | `ui/*` | 所有 UI 组件直接依赖 `ConnectionUtil`，无法独立测试或替换数据源 |
| **默认包中的 Main 类** | `Main.java` | `Main` 类位于默认包（无名包），不符合 Java 规范，且混入了数据同步和登录状态判断逻辑 |

### 2.2 安全性问题（严重程度：🔴 高）

| 问题 | 位置 | 风险 |
|------|------|------|
| **数据库密码硬编码** | `OriConnectionUtil.java:23`, `ConnectionUtilCopy.java:20` | MySQL root 密码 `001978` 直接写在源码中，可被反编译获取 |
| **AES 密钥硬编码且过短** | `EncryptionUtil.java:13` | 密钥 `"LesiSecretKey123"` 仅 16 字节且硬编码，不符合 AES-128 安全标准 |
| **密码可逆加密存储** | `SQLiteConnectionUtil.java:138` | 使用 AES 加密而非哈希（BCrypt/PBKDF2），密码可被解密还原 |
| **SQL 注入风险** | `OriConnectionUtil.java:53` | `login_jdbc` 方法使用字符串拼接构造 SQL，攻击者可通过手机号输入注入恶意 SQL |
| **敏感信息写入配置文件** | `config.properties` | 登录状态（用户ID、姓名、权限等级）以明文存储在用户目录下 |
| **未加密的网络传输** | `OriConnectionUtil.java:27` | MySQL 连接使用 `useSSL=false`，数据在公网明文传输（通过 frp-bar.com 穿透） |

### 2.3 代码质量问题（严重程度：🟡 中）

| 问题 | 位置 | 说明 |
|------|------|------|
| **异常处理不当** | 全局 61+ 处 | 大量使用 `e.printStackTrace()` 吞掉异常，无统一错误处理机制 |
| **魔法字符串** | 全局 | `"1"`/`"2"` 表示学生/教师，`"OK"`/`"password error"` 作为登录返回值，应用枚举替代 |
| **List\<String\> 作为返回契约** | `ConnectionUtil.login_jdbc()` | 用列表传回登录结果，索引依赖隐式约定，极易出错 |
| **无日志框架** | 全局 | 使用 `System.out.println` 而非 SLF4J/Log4j，已有 `commons-logging` 依赖但仅 `StudentBackendFrame` 中象征性使用 |
| **无单元测试** | `test/` 目录为空 | 没有任何单元测试或集成测试 |
| **颜色解析代码重复** | `BaseFrame.java`, `BasePanel.java` | `hexStringToColor` 方法完全相同但未抽取为工具方法 |
| **Windows 深色模式检测低效** | `BaseFrame.java:68-79` | 每次创建 Frame 都执行 `reg query` 外部进程 |
| **线程安全问题** | `Main.java:57-73` | `SwingUtilities.invokeLater` 内创建 `ConnectionUtil` 并赋给局部变量 `_connUtil`，但外层直接使用该变量可能为 null |

### 2.4 数据库问题（严重程度：🟡 中）

| 问题 | 位置 | 说明 |
|------|------|------|
| **全量同步策略粗暴** | `SQLiteConnectionUtil.syncTikuData()` | 每次同步都 `DELETE` 全表再重新插入，数据量大时性能差 |
| **无数据库迁移机制** | 全局 | 表结构通过 `CREATE TABLE IF NOT EXISTS` 创建，无版本化迁移方案 |
| **无连接池** | `OriConnectionUtil`, `SQLiteConnectionUtil` | 每次创建新连接，高并发下资源浪费 |
| **SQLite 未使用 WAL 模式** | `SQLiteConnectionUtil` | 默认 journal 模式在并发读写时性能较差 |

### 2.5 UI/UX 问题（严重程度：🟡 中）

| 问题 | 位置 | 说明 |
|------|------|------|
| **绝对定位布局** | `LoginFrame.java:56-83` | 使用 `setBounds()` 而非布局管理器，不同分辨率下显示异常 |
| **缺少国际化(i18n)** | 全局 | 所有文字硬编码为中文，无法支持多语言 |
| **固定窗口尺寸** | 多个 Frame | 未适配不同屏幕分辨率 |
| **BaseLabel 配色错误** | `BaseLabel.java:15-21` | 标签使用了 `button.background`/`button.foreground` 配置项，语义不一致 |
| **密码框类型错误** | `LoginFrame.java:77` | `JPasswordField` 赋值给 `JTextField` 变量，`getText()` 已弃用 |
| **add 重复调用** | `TestManagePanel.java:44-46`, `AnsSituationPanel.java` | `searchLabel` 和 `searchField` 被重复 `add` 到 `headPanel` |

### 2.6 功能完整性问题（严重程度：🟢 低）

| 问题 | 说明 |
|------|------|
| **错题集功能未完全实现** | `ErrorTrainingPanel` 与 `StrengthenExercisePanel` 代码高度相似，未体现错题复习的差异性 |
| **模拟考试缺少计时功能** | `MockExamPanel` 没有倒计时或时间限制 |
| **教师端缺少数据导出** | 无 Excel/CSV 导出功能 |
| **无账号注册流程** | 账号需通过教师端或数据库直接创建 |
| **无题目导入导出** | 题目只能逐条手动添加 |

---

## 三、改进方向与路线图

### 第一阶段：安全加固（优先级：🔴 紧急）

1. **移除硬编码密码**：将数据库密码和 AES 密钥移至外部配置文件，至少使用环境变量
2. **修复 SQL 注入**：`OriConnectionUtil.login_jdbc()` 改用 `PreparedStatement`
3. **密码哈希化**：使用 BCrypt 或 PBKDF2 替代 AES 可逆加密存储密码
4. **启用 SSL/TLS**：MySQL 连接启用 `useSSL=true` 并配置证书
5. **删除冗余文件**：移除 `ConnectionUtilCopy.java`

### 第二阶段：架构重构（优先级：🟡 建议）

1. **引入分层架构**：
   ```
   src/main/java/
   ├── LesiApplication.java          (应用入口)
   ├── config/                        (配置管理)
   ├── model/                         (数据模型/实体类)
   ├── dao/                           (数据访问层)
   ├── service/                       (业务逻辑层)
   ├── ui/
   │   ├── common/                    (公共UI组件)
   │   ├── login/                     (登录模块)
   │   ├── student/                   (学生端)
   │   └── teacher/                   (教师端)
   └── util/                          (工具类)
   ```

2. **引入依赖注入**：使用 Dagger2 或手动 DI 管理 `ConnectionUtil` 等依赖
3. **抽取业务逻辑**：将 `Calculator` 中的算法逻辑与数据访问分离
4. **引入 Result/Optional 模式**：替代 `List<String>` 作为返回值

### 第三阶段：数据库优化（优先级：🟡 建议）

1. **增量同步策略**：基于时间戳的增量同步替代全量 DELETE + INSERT
2. **引入 Flyway/Liquibase**：管理数据库版本迁移
3. **连接池化**：使用 HikariCP 管理 MySQL 连接池
4. **SQLite WAL 模式**：启用 Write-Ahead Logging 提升并发性能

### 第四阶段：UI/UX 升级（优先级：🟢 可选）

1. **布局管理器重构**：用 `GridBagLayout`/`MigLayout` 替代 `setBounds` 绝对定位
2. **响应式设计**：支持窗口缩放和不同分辨率
3. **国际化(i18n)**：抽取所有中文字符串到 `ResourceBundle`
4. **修复密码输入框**：使用 `JPasswordField` 的正确 API
5. **深色模式检测缓存**：避免每次创建 Frame 时执行外部进程

### 第五阶段：功能完善（优先级：🟢 可选）

1. **模拟考试计时**：添加倒计时组件
2. **数据导出**：教师端支持 Excel/CSV 导出
3. **题目批量导入**：支持 Excel/JSON 格式批量导入题目
4. **账号自助注册**：学生可自行注册账号
5. **通知系统**：教师可向学生推送练习任务

### 第六阶段：工程化提升（优先级：🟢 可选）

1. **单元测试覆盖**：至少覆盖 service 层和 util 层
2. **集成测试**：使用 TestContainers 进行数据库集成测试
3. **CI/CD**：GitHub Actions 自动化构建与测试
4. **代码规范**：引入 Checkstyle / SpotBugs 静态分析
5. **日志框架**：统一使用 SLF4J + Logback
6. **打包优化**：使用 `jpackage` 生成原生安装包替代 launch4j

---

## 四、快速修复清单（可立即执行）

| 序号 | 修复项 | 涉及文件 | 工作量 |
|------|--------|----------|--------|
| 1 | `OriConnectionUtil` SQL 注入修复 | `OriConnectionUtil.java` | 5 分钟 |
| 2 | 删除 `ConnectionUtilCopy.java` | `ConnectionUtilCopy.java` | 1 分钟 |
| 3 | 密码框类型修正 | `LoginFrame.java:77` | 1 分钟 |
| 4 | `searchLabel`/`searchField` 重复 add 修复 | `TestManagePanel.java`, `AnsSituationPanel.java` | 2 分钟 |
| 5 | AES 密钥外部化 | `EncryptionUtil.java` | 10 分钟 |
| 6 | 数据库密码外部化 | `OriConnectionUtil.java` | 5 分钟 |
| 7 | `hexStringToColor` 抽取为公共方法 | `BaseFrame.java`, `BasePanel.java` | 5 分钟 |
| 8 | 添加 `.gitignore` | 项目根目录 | 5 分钟 |

---

## 五、技术债务总结

| 类别 | 数量 | 严重程度 |
|------|------|----------|
| 安全漏洞 | 5 处 | 🔴 高 |
| 架构缺陷 | 4 处 | 🔴 高 |
| 代码质量 | 8 处 | 🟡 中 |
| 数据库问题 | 4 处 | 🟡 中 |
| UI/UX 问题 | 6 处 | 🟡 中 |
| 功能缺失 | 5 处 | 🟢 低 |
| 总计 | **32 处** | — |

---

> **备注**：作为大学期间的个人项目，整体完成度较高，核心刷题功能完整可用。主要问题集中在安全性和代码组织层面，属于学生项目的常见通病。建议优先完成"第一阶段：安全加固"，再逐步推进架构重构。
