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
- 登录，看到与普通用户不同的菜单
- 查看全部预约，可按会议室 / 日期 / 状态筛选
- 取消异常预约并填写原因（状态与取消原因可在列表中看到）

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
| 数据库 | MySQL | 8.0 / 8.4 均可 |
| 包管理 | npm | 10+（随 Node 安装） |

---

## 6. 环境要求（版本要求）

在干净机器上运行本系统前，需先装好以下 5 个软件（版本号为硬性要求，首次启动需联网下载依赖，内网机器按 Q3、Q4 配置镜像）：

| 软件 | 版本要求 | 官方下载 | 验证命令 |
|------|----------|----------|----------|
| JDK | **17（LTS）** | https://adoptium.net/temurin/releases/?version=17 | `java -version` 显示 `17.x.x` |
| Maven | **3.9+** | https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/ | `mvn -version` 显示 `3.9.x` |
| MySQL | **8.0 / 8.4** | https://dev.mysql.com/downloads/mysql/ | `mysql --version` 显示 `8.x` |
| Node.js | **20（LTS）** | https://nodejs.org/ | `node -v` 显示 `v20.x` |
| Git | 任意近期版本 | https://git-scm.com/downloads | `git --version` |

安装注意（Windows 10/11 x64）：

- JDK：安装时勾选 **Set JAVA_HOME environment variable**（Maven 依赖它）
- MySQL：安装路径保持默认英文路径；root 密码设为 `123456`（与 `backend/src/main/resources/application.yml` 一致；不一致则改 yml 或按 Q5 改密码）；服务启动方式见 Q10
- Node.js：选 20.x LTS，不要选 Current 版；npm 随 Node 一起安装
- 五个验证命令全部通过后即可进入第 7 节，全程只需 3 步

---

## 7. 启动步骤（干净机器 3 步跑通）

前置：第 6 节 5 个软件已装好，MySQL 服务已启动（启动方式见 Q10）。以下每步只有一条命令，依次执行。

### 步骤 1：克隆仓库并进入目录

```powershell
git clone https://github.com/tihua21/-V1.0-1-.git; cd "-V1.0-1-"
```

> 目录名以 `-` 开头，PowerShell 下 `cd` 必须加引号。若是直接拷贝的项目目录，跳过克隆、直接进入项目根目录即可。

### 步骤 2：初始化数据库（建库建表 + 种子数据）

```powershell
Get-Content docs/week07-08/schema.sql -Raw -Encoding UTF8 | mysql -u root -p123456 --default-character-set=utf8mb4
```

预期输出 8 张表的行数：role 2、user 2、meeting_room 3、facility 3、meeting_room_facility 6、time_slot 7、booking 6、booking_audit_log 13。
（脚本会删除并重建 meeting_room 库；若 root 密码不是 123456，把命令中的 `-p123456` 换成你的密码，并同步修改 `backend/src/main/resources/application.yml` 的 `password`。）

### 步骤 3：一键启动后端与前端

```powershell
.\start.bat
```

脚本会打开两个新窗口：后端窗口（Maven 首次下载依赖约 2-5 分钟，看到 `Tomcat started on port 8080` 即成功）和前端窗口（首次自动执行 `npm ci` 安装依赖，看到 `Local: http://localhost:5173/` 即成功）。

**完成**：浏览器打开 **http://localhost:5173** ，用第 8 节默认账号登录。

> Linux/macOS 手动等价命令：终端 A 执行 `cd backend && mvn spring-boot:run`；终端 B 执行 `cd frontend && npm ci && npm run dev`。

---

## 8. 默认账号

| 角色 | 用户名 | 密码 | 权限说明 |
|------|--------|------|----------|
| 管理员 | admin | 123456 | 查看全部预约、按会议室/日期筛选、取消异常预约 |
| 普通用户 | user1 | 123456 | 查看会议室、提交预约、修改/取消自己的预约 |

> 数据库中的用户登录口令只存 BCrypt 哈希，不明文存储；测试口令仅用于本地演示。

---

## 9. 接口说明

第 9 周已实现（接口边界按第 7—8 周定稿清单）：

| 接口 | 方法 | 请求参数 | 响应 |
|------|------|---------|------|
| /api/login | POST | `{"username":"user1","password":"123456"}` | `data: { token, role, realName, menus }`；401 账号或密码错误 / 403 账号已停用 |
| /api/rooms | GET | 可选：name、minCapacity、location、equipment | 会议室数组（含设备列表） |
| /api/time-slots | GET | 无 | 启用中的预置时段数组 |
| /api/bookings | POST | `{"roomId":1,"date":"2026-11-06","timeSlotId":5,"purpose":"..."}` | `data: { bookingId, bookingNo, status:"已预约" }`；400 参数或时段不合法 / 409 会议室停用或时段冲突 |
| /api/bookings/mine | GET | 可选 status、roomId、date | 本人预约数组 |

第 10 周新增（普通用户修改/取消 + 管理员处理）：

| 接口 | 方法 | 请求参数 | 响应 / 说明 |
|------|------|---------|------------|
| /api/rooms/{id}/occupied | GET | 必填 date=yyyy-MM-dd | 该会议室该日占用中的预约数组，供详情页展示占用时段 |
| /api/bookings/{id} | PUT | `{"purpose":"...","timeSlotId":6}`，两者至少给一个 | 修改本人未开始的预约；换时段会重新做冲突检测，409 表示新时段冲突 |
| /api/bookings/{id} | DELETE | `{"reason":"..."}`（可选） | 取消本人未开始的预约；取消后时段立即释放 |
| /api/admin/bookings | GET | 可选 roomId、date、status | 管理员查看全部预约 |
| /api/admin/bookings/{id}/cancel | PUT | `{"reason":"..."}`（必填） | 管理员取消异常预约，必须填写原因 |

冲突判定规则：半开区间 `[start, end)`，`new.start < old.end AND new.end > old.start` 才算冲突；
首尾相接（前一段结束时间 = 后一段开始时间）放行。提交预约经冲突判定通过后直接为「已预约」(status=1)。

除 /api/login 外，所有接口需在请求头携带 `Authorization: <token>`，否则 401。
`/api/admin/**` 还需管理员角色，普通用户访问返回 403。
响应格式说明：`code=0` 成功；失败时 code 为 HTTP 状态码（400/401/403/409/500）。

---

## 10. 常见问题

### Q1：后端启动报错 "Communications link failure"

MySQL 未启动、端口不是 3306，或密码不匹配。

```bash
# 先按「步骤 2」启动 MySQL 服务，再验证能登录
mysql -u root -p

# 如果密码不是 123456，修改 application.yml 中的 password
# 如果 MySQL 端口不是 3306，同时改 application.yml 里的 jdbc url
```

### Q2：前端页面打开后提示"网络错误，请检查后端是否启动"

后端未启动或端口被占用。

```bash
# 检查 8080 端口是否被占用
netstat -ano | findstr :8080

# 如果被占用，杀掉占用进程或修改 application.yml 中的 server.port
# 注意：前端代理固定转发到 8080，改后端端口时必须同步改 frontend/vite.config.js 的 target
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

### Q4：npm 安装依赖很慢或失败

配置国内源后重试：

```bash
npm config set registry https://registry.npmmirror.com
npm ci
```

### Q5：MySQL 报 "Access denied for user 'root'"

密码不对。重新设置密码：

```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';
FLUSH PRIVILEGES;
```

### Q6：前端登录后跳转白页

检查浏览器控制台是否有报错。确认后端接口 `/api/rooms` 能正常返回数据。

### Q7：前端 5173 端口被占用

```bash
netstat -ano | findstr :5173
```

关闭占用进程，或修改 `frontend/vite.config.js` 中的 `server.port`。

### Q8：提示 mysql 不是内部或外部命令

MySQL 的 `bin` 目录没进 `Path`。当前终端临时加：

```powershell
$env:Path += ";C:\Program Files\MySQL\MySQL Server 8.4\bin"
```

永久生效则把该目录加到系统环境变量 `Path`，然后新开终端验证 `mysql --version`。

### Q9：PowerShell 报"无法加载文件 npm.ps1，因为在此系统上禁止运行脚本"

把 `npm` 换成 `npm.cmd`：

```bash
npm.cmd ci
npm.cmd run dev
```

或在确认允许的前提下调整当前用户执行策略：

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

### Q10：net start 提示"服务名无效"或本机没有 MySQL 服务

先用 `Get-Service *mysql*` 查实际服务名再启动。若确实没有服务（例如使用 ZIP/绿色版 MySQL），需要手动启动：

```powershell
# 数据目录必须是纯英文路径，否则 MySQL 可能报 errno 2
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --console
```

手动启动的窗口不能关，关掉即停止数据库；正式使用建议用 MSI 安装器安装为 Windows 服务。

---

## 11. 目录结构

```
meeting-room-v1-group01/
├── README.md                 # 本文件
├── start.bat                 # 第11周新增：一键启动后端+前端（README 步骤3）
├── docs/                     # 全部项目文档
│   ├── 01_启动阶段/          # 第1周启动材料
│   ├── week02/               # 第2周：现有系统走查
│   ├── week03/               # 第3周：方案决策
│   ├── week04/               # 第4周：需求分析
│   ├── week05/               # 第5周：需求清单+评审+DFD+用例规约
│   ├── week06/               # 第6周：技术选型+技术栈+README+阶段检查
│   ├── week07-08/            # 第7-8周：系统设计说明书+数据库与接口设计+schema.sql
│   ├── week09/               # 第9周：可运行基础页面 + 5 张页面截图（01-05）
│   ├── week10/               # 第10周：九组冲突测试数据（.sql）+ 说明
│   └── week11/               # 第11周：测试报告+测试脚本(scripts/)+原始证据(evidence/)
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
│           ├── RoomDetail.vue  # 会议室详情页（预置时段 + 当日占用）
│           ├── BookingForm.vue # 预约表单页
│           ├── MyBookings.vue  # 我的预约页（修改 / 取消）
│           └── AdminBooking.vue # 管理员全部预约页（筛选 / 强制取消）
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

```powershell
# 先查实际服务名，再停止；需以管理员身份运行终端
Get-Service *mysql* | Select-Object Name, Status
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
