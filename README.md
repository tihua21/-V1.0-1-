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

## 6. 环境要求

本节安装步骤以 **Windows 10/11 x64** 为准。在全新机器上运行本系统，需要先安装以下环境（首次启动需联网下载 Maven / npm 依赖，内网机器按 Q3、Q4 配置镜像）：

### 6.1 JDK 17

- 下载地址：https://adoptium.net/temurin/releases/?version=17
- 选择 Windows x64 `.msi` 安装包
- 安装时勾选 **Set JAVA_HOME environment variable**（Maven 依赖 `JAVA_HOME`，
  没设置时可能报 “JAVA_HOME not found”）；没勾选则手动设置
  `JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot`
- 安装后验证：`java -version` 应显示 `openjdk version "17.x.x"`，
  `mvn -version` 的 Java version 也应显示 17

### 6.2 Maven 3.9+

- 下载地址：https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip
- 解压到任意目录，如 `C:\apache-maven`
- 配置环境变量：
  - `MAVEN_HOME` = `C:\apache-maven`
  - `PATH` 追加 `%MAVEN_HOME%\bin`
- 验证：`mvn -version` 应显示 `Apache Maven 3.9.9`

### 6.3 MySQL 8.0 / 8.4

- 下载地址：https://dev.mysql.com/downloads/mysql/
- Windows 选择 x64 MSI 安装包；安装时保持默认英文安装路径与数据目录
  （MySQL 对中文数据目录支持不稳定，路径含中文时可能直接启动失败）
- 安装时设置 root 密码为 `123456`（与项目配置一致）
- 把 `C:\Program Files\MySQL\MySQL Server 8.4\bin` 加入系统 `Path`
  （安装器没自动加时手动加；否则会提示 `mysql` 不是内部或外部命令）
- 验证：新开终端执行

```bash
mysql --version
mysql -u root -p
```

- 服务名不一定是 `MySQL80`：8.0 安装器通常建 `MySQL80`，8.4 安装器通常建 `MySQL84`，
  以「步骤 2」查到的实际名称为准

### 6.4 Node.js 20 LTS

- 下载地址：https://nodejs.org/
- **选择 20.x LTS**（不要选 Current 尝鲜版）；npm 会随 Node 一起安装，
  全新机器上的 npm 10.x 即可运行本项目
- 验证：`node -v` 显示 `v20.x`，`npm -v` 显示 `10.x` 或更高

### 6.5 Git

- 下载地址：https://git-scm.com/downloads
- 用于克隆仓库

---

## 7. 启动步骤

以下步骤按顺序执行，每一步完成后验证再继续下一步。

### 步骤 0：环境自检

先在终端确认 5 个命令都能执行（没装或没配 PATH 的按第 6 节补）：

```bash
java -version
mvn -version
mysql --version
node -v
npm -v
```

### 步骤 1：克隆仓库

```bash
git clone https://github.com/tihua21/-V1.0-1-.git
# 仓库目录名以 - 开头，PowerShell 下必须加引号，否则会被当成参数
cd "-V1.0-1-"
```

> 如果是把整个项目目录拷贝到新电脑（不是 `git clone`），跳过本步，直接进入项目根目录，从步骤 0 继续。

### 步骤 2：启动 MySQL 服务

后端启动前必须保证 MySQL 服务正在运行。Windows 下两种方式任选其一：

**方式一：命令行（推荐，需以管理员身份运行终端）**

```powershell
# 第一步：查本机实际服务名（8.0 常见 MySQL80，8.4 常见 MySQL84，也可能被自定义）
Get-Service *mysql* | Select-Object Name, Status

# 第二步：启动，把 MySQL80 换成上一步查到的实际名称
net start MySQL80

# 如果提示"服务名无效"，用 CMD/PowerShell 再查一次：
sc.exe query state= all | findstr /I "MYSQL"
```

**方式二：图形界面**

1. 按 `Win + R`，输入 `services.msc` 回车
2. 在服务列表中找到 `MySQL80` 或 `MySQL84`（以实际名称为准）
3. 右键 → 启动；如需开机自启，右键 → 属性 → 启动类型选"自动"

**验证服务已启动：**

```bash
mysql -u root -p
```

能成功登录即表示 MySQL 服务正常。提示"mysql 不是内部或外部命令"时，说明 MySQL 的 `bin` 目录还没进 `Path`，见 6.3。

### 步骤 3：初始化数据库

在仓库根目录执行（`docs/week07-08/schema.sql` 是相对路径）。如果提示 `mysql` 不是内部或外部命令，先临时把 MySQL 的 `bin` 目录加入当前终端：

```powershell
$env:Path += ";C:\Program Files\MySQL\MySQL Server 8.4\bin"
```

```bash
# 方式一：登录 MySQL 后执行（必须加 --default-character-set=utf8mb4，否则中文数据会插入失败）
mysql -u root -p --default-character-set=utf8mb4

# 在 MySQL 中执行第 8 周定稿的建表脚本（路径用正斜杠 /）
source docs/week07-08/schema.sql

# 方式二：Windows PowerShell 一条命令直接执行
# 先设置管道编码为 UTF-8；Windows PowerShell 5 默认按 ASCII 传给 mysql，会把中文数据写成乱码
$OutputEncoding = [System.Text.Encoding]::UTF8
Get-Content docs/week07-08/schema.sql -Raw -Encoding UTF8 | mysql -u root -p --default-character-set=utf8mb4
```

预期结果：脚本末尾输出 8 张表的行数——role 2、user 2、meeting_room 3、facility 3、meeting_room_facility 6、time_slot 7、booking 6、booking_audit_log 13。脚本会删除并重建 meeting_room 库，第 6 周的临时表（users / meeting_rooms）已废弃。

> 种子数据的示例预约集中在 2026-11-02 至 2026-11-04。如果在新电脑上执行时当前日期已晚于这些日期，这些示例预约会变成历史数据，不影响启动、登录和查询；只需要演示时重新提交一条未来日期的预约即可。

### 步骤 4：检查后端数据库配置

打开 `backend/src/main/resources/application.yml`，确认数据库密码与你的 MySQL root 密码一致：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meeting_room?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8&allowPublicKeyRetrieval=true
    username: root
    password: 123456    # ← 改成你的 MySQL 密码
```

如果本机 root 密码不是 `123456`，两种做法任选其一：改这里的 `password`，或按 Q5 把 MySQL 密码改回 `123456`（推荐，和 README 保持一致）。

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
npm ci
```

仓库已带 `package-lock.json`，用 `npm ci` 能保证全新机器装出与开发机一致的依赖；首次约 20-30 秒。如果提示没有 lock 文件，再用 `npm install`。

> Windows 全新机器常见问题：PowerShell 默认禁止运行 `npm.ps1`，会报“无法加载文件 …npm.ps1，因为在此系统上禁止运行脚本”。此时把命令改成 `npm.cmd ci`（后面启动也同理用 `npm.cmd run dev`），或按 Q8 调整执行策略。

### 步骤 7：启动前端

```bash
npm run dev
```

PowerShell 被执行策略拦截时用：

```bash
npm.cmd run dev
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
├── docs/                     # 全部项目文档
│   ├── 01_启动阶段/          # 第1周启动材料
│   ├── week02/               # 第2周：现有系统走查
│   ├── week03/               # 第3周：方案决策
│   ├── week04/               # 第4周：需求分析
│   ├── week05/               # 第5周：需求清单+评审+DFD+用例规约
│   ├── week06/               # 第6周：技术选型+技术栈+README+阶段检查
│   ├── week07-08/            # 第7-8周：系统设计说明书+数据库与接口设计+schema.sql
│   ├── week09/               # 第9周：可运行基础页面 + 5 张页面截图（01-05）
│   └── week10/               # 第10周：九组冲突测试数据（.sql）+ 说明
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
