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

在干净机器上运行本系统前，需先装好以下 5 个软件（Java、Maven、MySQL 的版本为硬性要求；Node.js 20 及以上均可。首次启动需联网下载依赖，内网机器按 Q3、Q4 配置镜像）：

| 软件 | 版本要求 | 官方下载 | 验证命令 |
|------|----------|----------|----------|
| JDK | **17（LTS）** | https://adoptium.net/temurin/releases/?version=17 | `java -version` 显示 `17.x.x` |
| Maven | **3.9+** | https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/ | `mvn -version` 显示 `3.9.x` |
| MySQL | **8.0 / 8.4** | https://dev.mysql.com/downloads/mysql/ | `mysql --version` 显示 `8.x` |
| Node.js | **20 或更高**（推荐 20 LTS） | https://nodejs.org/ | `node -v` 显示 `v20.x` 及以上 |
| Git | 任意近期版本 | https://git-scm.com/downloads | `git --version` |

安装注意（Windows 10/11 x64）：

- JDK：安装时勾选 **Set JAVA_HOME environment variable**（Maven 依赖它）
- MySQL：安装路径保持默认英文路径；root 密码设为 `123456`（与 `backend/src/main/resources/application.yml` 一致；不一致则改 yml 或按 Q5 改密码）；服务启动方式见 Q10
- Node.js：选 20.x LTS 或更高版本均可（推荐 20 LTS，无需使用 Current 版）
- 五个验证命令全部通过后即可进入第 7 节，全程只需 3 步

> 可选：如需重跑第 11 周测试脚本，另装 Python 3.9+ 并执行 `pip install requests pymysql`；仅运行系统本身不需要 Python。

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
mysql -u root -p123456 --default-character-set=utf8mb4 -e "source docs/week07-08/schema.sql"
```

预期输出 8 张表的行数：role 2、user 2、meeting_room 3、facility 3、meeting_room_facility 6、time_slot 7、booking 6、booking_audit_log 13。
（脚本会删除并重建 meeting_room 库；若 root 密码不是 123456，把命令中的 `-p123456` 换成你的密码，并同步修改 `backend/src/main/resources/application.yml` 的 `password`。）

> 上面的 `source` 写法由 MySQL 客户端直接读取 UTF-8 文件，不经过 PowerShell 转码，不会出现中文乱码。若改用管道写法，需要先确认 `$OutputEncoding` 为 `utf-8`（Windows PowerShell 5.1 默认不是），否则中文可能变成 `?`：
>
> ```powershell
> $OutputEncoding = [System.Text.UTF8Encoding]::new()
> Get-Content docs/week07-08/schema.sql -Raw -Encoding UTF8 | mysql -u root -p123456 --default-character-set=utf8mb4
> ```

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
路径参数类型不合法（如 `/api/bookings/1 OR 1=1`）统一返回 400「请求参数格式不正确」，不会被当作 id 执行。

第 12 周行为补充（不改变接口路径与参数）：

- `POST /api/bookings`：开始时间不晚于当前时间时返回 400「预约开始时间已过，请选择未来时段」；
- `PUT /api/bookings/{id}`：新时段已过时返回 400「新时段已过，请选择未来时段」；
- 数据库不可用时返回 500「数据库连接失败，请检查数据库服务」（连接超时已调整为 3 秒）。

---

## 10. 质量保障：第 11 周测试结论与缺陷修复

第 11 周功能冻结后完成系统测试与缺陷修复。测试脚本与原始证据随仓库提供，位于 `docs/week11/scripts/` 与 `docs/week11/evidence/`，可直接重跑复核。

### 10.1 测试用例执行结果

| 类别 | 用例数 | 覆盖内容 | 结果 |
|------|--------|----------|------|
| 功能 | 3 | 登录、提交预约落库、修改与取消 | 全部通过 |
| 异常 | 2 | 密码错误、重复时段 409、非法时段 400 | 全部通过 |
| 权限 | 3 | 未登录 401、普通用户访问管理员接口 403、管理员取消必填原因 | 全部通过 |
| 适配 | 2 | 1366×768 桌面、390×844 手机访问会议室列表 | 全部通过 |
| 部署 | 2 | 干净机器按本文 3 步启动、环境版本核对 | 全部通过 |

### 10.2 非功能与破坏性测试

| 项目 | 方法 | 结果 |
|------|------|------|
| 并发 | 50 线程同一秒抢订同一会议室同一时段 | 1 个成功、49 个返回 409，库内恰好 1 条，无超卖 |
| 性能 | 灌入 1000 条预约后两个查询接口各请求 20 次取 P95 | /api/rooms 18.8ms、/api/bookings/mine 164.1ms |
| 越权 | 取消他人预约、访问管理员接口 | 403，库内数据无变化，无数据泄漏 |
| 时间边界 | 首尾相接、重复提交、改期撞单 | 首尾相接放行，重复与撞单返回 409 |
| SQL 注入 | 登录、筛选、用途字段、路径参数 | 参数化与类型校验拦截，无数据破坏 |

> 性能数字随机器配置与灌入数据量波动，达标线为两个接口 P95 均 < 500ms。

### 10.3 已修复缺陷

| 编号 | 现象 | 修复方式 | 回归结果 |
|------|------|----------|----------|
| BUG-W11-01 | 50 并发同秒抢订出现多条重复占用 | 冲突检测与写入改为按「会议室+日期」分片的分段锁 + 编程式事务，锁内完成检测与提交 | 1 成功 / 49 个 409 / 库内 1 条 |
| BUG-W11-02 | `DELETE /api/bookings/1 OR 1=1` 返回 500 | 全局异常处理新增参数类型不匹配处理器，统一返回 400 | 返回 400「请求参数格式不正确」 |
| BUG-W11-03 | 390×844 视口会议室列表横向溢出 | 增加移动端媒体查询与导航菜单弹性布局 | 桌面与手机视口均无溢出，按钮可交互 |
| BUG-W12-01 | 可对已开始的时段建单，建完马上无法修改/取消 | 创建与修改预约增加开始时间校验 | 过去时间返回 400「预约开始时间已过，请选择未来时段」/「新时段已过，请选择未来时段」 |
| BUG-W12-02 | 停库后页面等待较久才报错 | Hikari `connection-timeout=3000`、`validation-timeout=1000`，并统一处理数据访问异常 | 约 3-4 秒返回 500「数据库连接失败，请检查数据库服务」 |
| BUG-W12-03 | 详情页默认今天，预置模板数据都在过去 | 详情页与预约页默认明天 | 打开即显示明天的占用情况 |
| BUG-W12-04 | 当天已结束时段仍可选，提交后才报错 | 已结束时段显示「已过」并禁用，过去日期不可选 | 页面不可选，接口兜底返回 400 |
| BUG-W12-05 | 成功/冲突提示 3 秒消失，现场看不清 | 预约页改为常驻绿色/红色提示，提交成功不自动跳转 | 可连续演示「成功 → 冲突」 |
| BUG-W12-06 | 停库后列表仍显示上一次数据 | 列表失败清空数据并常驻报错；点击当前菜单强制刷新 | 停库后列表为空并显示错误，恢复后数据回来 |
| BUG-W12-07 | 登录页与其他页面底色不一致 | 登录页统一为蓝色 | 视觉一致 |

重跑测试脚本前置条件：按第 7 节启动后端（8080）与数据库，Python 环境安装 `requests`、`pymysql`；脚本默认访问 `http://localhost:8080`，数据库连接参数见 `docs/week11/scripts/api_common.py`。

---

## 11. 第 12 周：预答辩演示与验收

第 12 周在功能冻结基础上只做演示可靠性修复与验收材料整理，不改变接口路径、参数和数据库结构。演示数据准备脚本、九组冲突现场脚本与手动验收流程文档当前仅保留在本地，未纳入仓库；本节记录复现口径，便于在没有脚本时手工准备。

### 11.1 演示数据准备

日期按答辩当天计算，跨天后必须重新准备：

| 用途 | 会议室 | 日期 | 时段 | 准备方式 |
|---|---|---|---|---|
| 占用演示 | A101 | 当天 +1 | 09:00-10:00 | 用 user1 提交一条预约 |
| 异常预约 | C301 | 当天 +5 | 14:00-15:00 | 用 user1 提交一条预约，供管理员强制取消 |
| 主演示 | A101 | 当天 +1 | 10:00-11:00 | 保持空闲 |

准备后自查：A101 当天 +1 的 09:00 在详情页显示「已被占用」，10:00 显示「可预订」。

### 11.2 演示顺序

1. user1 登录 → 会议室查询（3 间）
2. A101 详情 → 默认明天 → 09:00 已被占用、10:00 可预订
3. 立即预约 → 选 10:00-11:00 → 提交成功（常驻绿色提示）
4. 同表单再提交一次 → 常驻红色「该时段已被占用」
5. 我的预约 → 修改用途 → 取消预约
6. 回 A101 详情 → 10:00 恢复「可预订」
7. admin 登录 → 预约审核 → 强制取消 C301 异常预约（必填原因）

### 11.3 验收自查项

- [ ] 停库后 3-4 秒内显示「数据库连接失败，请检查数据库服务」，恢复后数据正常
- [ ] 九组冲突：5 组拒绝（409）、4 组放行（200）
- [ ] 50 并发：1 个成功、49 个 409、库内恰好 1 条
- [ ] 两个查询接口 P95 < 500ms
- [ ] 未登录 401、普通用户访问管理员接口 403、非法路径参数 400
- [ ] 新建/取消/强制取消在 `booking_audit_log` 中均有记录

---

## 12. 常见问题

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

## 13. 目录结构

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
│   ├── package-lock.json     # npm 依赖锁定（start.bat 的 npm ci 依赖此文件）
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

## 14. Git 协作规范

1. main 分支：保存稳定版本；功能改动走 `feature/xxx` 分支，文档改动走 `docs/xxx` 分支，合并后进入 main
2. 开发分支规范：`feature/xxx` 功能分支；`docs/xxx` 文档分支
3. 提交信息格式：`[类型] 简短描述`，类型：`feat` 新功能、`docs` 文档更新、`fix` 修复、`refactor` 重构
4. 每周至少一次小组会议，重要决策、任务变更记录在文档中
5. 所有文档、代码提交均体现实际贡献人

### 版本标签

| 标签 | 对应内容 |
|---|---|
| week09-final | 第 9 周可运行基础页面与页面截图 |
| week10-final | 第 10 周冲突检测与九组测试数据 |
| week11-final | 第 11 周测试报告、脚本与原始证据 |

查看与切换：`git tag -l`、`git checkout week11-final`（只读复核用，复核后切回 `main`）。

---

## 15. 附：停止 MySQL 服务

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
