# 智能会议室管理系统 V1.0——基础预约与管理平台

## 1. 小组信息

组号：第一组
仓库地址：https://github.com/tihua21/-V1.0-1-
项目负责人：汤有林
联系方式：1269434008@qq.com

## 2. 项目简介

本项目面向校园/单位会议室预约场景，开发智能会议室基础预约与管理平台 V1.0。本阶段完成软件完整软件工程过程，实现会议室预约的基础闭环，物联网、人脸识别等智能扩展功能不在本阶段实现。

## 3. V1.0 要实现什么

### 普通用户
- 登录
- 查看会议室
- 选择日期和时间
- 提交预约
- 系统自动判断预约时间冲突，返回预约成功或失败结果
- 查看、修改、取消自己的预约

### 管理员
- 登录
- 管理用户
- 管理会议室
- 查看和处理全部预约

### 暂不实现
- 物联网设备接入、人脸识别、3D 场景、复杂智能算法、消息推送、门禁联动

## 4. 小组成员与分工

| 角色 | 姓名 | 联系方式 | 主要职责 |
|------|------|----------|----------|
| 项目负责人 | 汤有林 | 18936527961 | 项目计划、任务拆分、版本集成、前端开发 |
| 需求与建模 | 查元昊 | 18980286385 | 调研、需求、用例、DFD、需求评审 |
| 技术与后端 | 李玉玺 | 13195542730 | 仓库管理、技术栈、数据库、后端接口 |
| 测试与文档 | 秦瑞祥 | 19305525889 | 测试用例、Bug 日志、文档整理、答辩材料 |

---

## 5. 技术栈

| 层 | 技术 | 版本 |
|----|------|------|
| 前端框架 | Vue 3 (JavaScript) | 3.4 |
| 前端 UI | Element Plus | 2.6 |
| 前端构建 | Vite | 5.4 |
| 后端语言 | Java | 17 (LTS) |
| 后端框架 | Spring Boot | 3.2 |
| ORM | MyBatis-Plus | 3.5.5 |
| 构建工具 | Maven | 3.9.9 |
| 数据库 | MySQL | 8.4 |
| 包管理 | npm | 11+ |

---

## 6. 环境要求

在全新机器上运行本系统，需要先安装以下环境：

### 6.1 JDK 17

- 下载地址：https://adoptium.net/temurin/releases/?version=17
- 选择 Windows x64 `.msi` 安装包
- 安装后验证：`java -version` 应显示 `openjdk version "17.x.x"`

### 6.2 Maven 3.9+

- 下载地址：https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip
- 解压到任意目录，如 `C:\apache-maven`
- 配置环境变量：
  - `MAVEN_HOME` = `C:\apache-maven`
  - `PATH` 追加 `%MAVEN_HOME%\bin`
- 验证：`mvn -version` 应显示 `Apache Maven 3.9.9`

### 6.3 MySQL 8.4

- 下载地址：https://dev.mysql.com/downloads/mysql/
- 选择 Windows x64 MSI 安装包
- 安装时设置 root 密码为 `123456`（与项目配置一致）
- 验证：`mysql -u root -p` 能登录

### 6.4 Node.js 20+

- 下载地址：https://nodejs.org/
- 选择 LTS 版本安装
- 验证：`node -v` 应显示 `v20.x` 或更高

### 6.5 Git

- 下载地址：https://git-scm.com/downloads
- 用于克隆仓库

---

## 7. 启动步骤

以下步骤按顺序执行，每一步完成后验证再继续下一步。

### 步骤 1：克隆仓库

```bash
git clone https://github.com/tihua21/-V1.0-1-.git
cd -V1.0-1-
```

### 步骤 2：启动 MySQL 服务

后端启动前必须保证 MySQL 服务正在运行。Windows 下两种方式任选其一：

**方式一：命令行（推荐，需以管理员身份运行终端）**

```bash
# 启动 MySQL 服务（服务名默认为 MySQL80，安装 8.4 时可能是 MySQL84）
net start MySQL80

# 如果提示"服务名无效"，先查看本机实际服务名
sc query state= all | findstr /I "MYSQL"
```

**方式二：图形界面**

1. 按 `Win + R`，输入 `services.msc` 回车
2. 在服务列表中找到 `MySQL80`（或 `MySQL84`）
3. 右键 → 启动；如需开机自启，右键 → 属性 → 启动类型选"自动"

**验证服务已启动：**

```bash
mysql -u root -p
```

能成功登录即表示 MySQL 服务正常。

### 步骤 3：初始化数据库

```bash
# 方式一：登录 MySQL 后执行（必须加 --default-character-set=utf8mb4，否则中文数据会插入失败）
mysql -u root -p --default-character-set=utf8mb4

# 在 MySQL 中执行第 8 周定稿的建表脚本（路径用正斜杠 /）
source docs/week07-08/schema.sql

# 方式二：Windows PowerShell 一条命令直接执行
Get-Content docs/week07-08/schema.sql -Raw -Encoding UTF8 | mysql -u root -p --default-character-set=utf8mb4
```

预期结果：脚本末尾输出 8 张表的行数——role 2、user 2、meeting_room 3、facility 3、meeting_room_facility 6、time_slot 7、booking 6、booking_audit_log 13。脚本会删除并重建 meeting_room 库，第 6 周的临时表（users / meeting_rooms）已废弃。

### 步骤 4：检查后端数据库配置

打开 `backend/src/main/resources/application.yml`，确认数据库密码与你的 MySQL root 密码一致：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meeting_room?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8&allowPublicKeyRetrieval=true
    username: root
    password: 123456    # ← 改成你的 MySQL 密码
```

### 步骤 5：启动后端

```bash
cd backend
mvn spring-boot:run
```

首次运行会下载 Maven 依赖，约 2-5 分钟。看到以下日志表示启动成功：

```
Tomcat started on port 8080 (http)
Started MeetingRoomApplication in x.xxx seconds
```

验证：浏览器打开 http://localhost:8080/api/rooms ，应返回 `{"code":401,"msg":"未登录或登录已失效"}`——该接口第 9 周起需登录后访问，401 即表示后端正常。

### 步骤 6：安装前端依赖

```bash
cd frontend
npm install
```

首次运行约 20-30 秒。

### 步骤 7：启动前端

```bash
npm run dev
```

看到以下输出表示启动成功：

```
VITE v5.x  ready in xxx ms
➜  Local:   http://localhost:5173/
```

### 步骤 8：访问系统

浏览器打开 **http://localhost:5173** ，将看到登录页面。

---

## 8. 默认账号

| 角色 | 用户名 | 密码 | 权限说明 |
|------|--------|------|----------|
| 管理员 | admin | 123456 | 可管理用户、会议室、查看全部预约 |
| 普通用户 | user1 | 123456 | 可查看会议室、提交预约 |

> 数据库中只存 BCrypt 哈希，不明文存储；测试口令仅此文档可见，仓库中不出现数据库明文密码。

---

## 9. 接口说明

第 9 周已实现（接口边界按第 7—8 周定稿清单）：

| 接口 | 方法 | 请求参数 | 响应 |
|------|------|---------|------|
| /api/login | POST | `{"username":"user1","password":"123456"}` | `data: { token, role, realName, menus }`；401 账号或密码错误 / 403 账号已停用 |
| /api/rooms | GET | 可选：name、minCapacity、location、equipment | 会议室数组（含设备列表） |
| /api/time-slots | GET | 无 | 启用中的预置时段数组 |
| /api/bookings | POST | `{"roomId":1,"date":"2026-11-06","timeSlotId":5,"purpose":"..."}` | `data: { bookingId, status:"待审核" }`；400 参数或时段不合法 / 409 会议室停用 |
| /api/bookings/mine | GET | 可选 status、roomId、date | 本人预约数组 |

除 /api/login 外，所有接口需在请求头携带 `Authorization: <token>`，否则 401。
响应格式说明：`code=0` 成功；失败时 code 为 HTTP 状态码（400/401/403/409/500）。

---

## 10. 常见问题

### Q1：后端启动报错 "Communications link failure"

MySQL 未启动或密码不匹配。

```bash
# 先按「步骤 2」启动 MySQL 服务，再验证能登录
mysql -u root -p

# 如果密码不是 123456，修改 application.yml 中的 password
```

### Q2：前端页面打开后提示"网络错误，请检查后端是否启动"

后端未启动或端口被占用。

```bash
# 检查 8080 端口是否被占用
netstat -ano | findstr :8080

# 如果被占用，杀掉占用进程或修改 application.yml 中的 server.port
```

### Q3：Maven 下载依赖很慢

配置国内镜像。编辑 `C:\Users\你的用户名\.m2\settings.xml`：

```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

### Q4：npm install 很慢或失败

配置国内源：

```bash
npm config set registry https://registry.npmmirror.com
npm install
```

### Q5：MySQL 报 "Access denied for user 'root'"

密码不对。重新设置密码：

```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';
FLUSH PRIVILEGES;
```

### Q6：前端登录后跳转白页

检查浏览器控制台是否有报错。确认后端接口 `/api/rooms` 能正常返回数据。

---

## 11. 目录结构

```
meeting-room-v1-group01/
├── README.md                 # 本文件
├── docs/                     # 全部项目文档
│   ├── 01_启动阶段/          # 第1周启动材料
│   ├── week02/               # 第2周：现有系统走查
│   ├── week03/               # 第3周：方案决策
│   ├── week04/               # 第4周：需求分析
│   ├── week05/               # 第5周：需求清单+评审+DFD+用例规约
│   ├── week06/               # 第6周：技术选型+技术栈+README+阶段检查
│   ├── week07-08/            # 第7-8周：系统设计说明书+数据库与接口设计+schema.sql
│   └── week09/               # 第9周：联调与阶段检查记录
├── backend/                  # 后端代码（Spring Boot）
│   ├── pom.xml               # Maven 依赖
│   ├── src/main/resources/
│   │   └── application.yml   # 后端配置
│   └── src/main/java/com/meetingroom/
│       ├── MeetingRoomApplication.java   # 启动类
│       ├── common/                        # 统一响应、全局异常处理
│       ├── config/                        # Web 配置（跨域+登录拦截器）
│       ├── entity/                        # 实体类（8 张表）
│       ├── mapper/                        # 数据访问层
│       ├── service/                       # 业务逻辑层
│       └── controller/                    # 接口控制器
├── frontend/                 # 前端代码（Vue 3）
│   ├── package.json          # npm 依赖
│   ├── vite.config.js        # Vite 配置 + API 代理
│   ├── index.html
│   └── src/
│       ├── main.js           # 入口
│       ├── App.vue           # 根组件
│       ├── api/index.js      # Axios 封装（token 拦截、401 跳转）
│       ├── router/index.js   # 路由配置（未登录守卫）
│       ├── components/NavBar.vue  # 角色菜单（普通用户无管理员入口）
│       └── views/            # 页面组件
│           ├── Login.vue       # 登录页
│           ├── RoomList.vue    # 会议室列表页（条件筛选）
│           ├── RoomDetail.vue  # 会议室详情页（预置时段）
│           ├── BookingForm.vue # 预约表单页
│           └── MyBookings.vue  # 我的预约页
├── sql/
│   └── init.sql              # 第6周临时脚本（已废弃，正式脚本见 docs/week07-08/schema.sql）
└── assets/                   # 图片、截图、原型图
```

---

## 12. Git 协作规范

1. main 分支：保存稳定版本，不直接提交代码；所有修改通过分支合并
2. 开发分支规范：`feature/xxx` 功能分支；`docs/xxx` 文档分支
3. 提交信息格式：`[类型] 简短描述`，类型：`feat` 新功能、`docs` 文档更新、`fix` 修复、`refactor` 重构
4. 每周至少一次小组会议，重要决策、任务变更记录在文档中
5. 所有文档、代码提交均体现实际贡献人

---

## 13. 附：停止 MySQL 服务

根据「步骤 2」中使用的启动方式，选择对应的停止方法。

**方式一：MySQL 以 Windows 服务运行（net start / services.msc 启动的）**

```bash
# 需以管理员身份运行终端
net stop MySQL80
```

**方式二：MySQL 是用 mysqld 命令手动启动的**

- 前台启动的（窗口一直显示日志）：直接在运行 mysqld 的终端窗口按 `Ctrl + C`
- 后台启动的：执行以下命令，输入 root 密码（123456）后即停止

```bash
mysqladmin -u root -p shutdown
```

**验证已停止：**

```bash
mysql -u root -p
```

提示 `ERROR 2003 ... Can't connect to MySQL server` 即表示服务已停止。
