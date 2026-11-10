@echo off
rem ============================================================
rem  智能会议室管理系统 V1.0 一键启动（第11周 README 步骤3配套）
rem  前置条件：MySQL 服务已启动，且已执行步骤2的数据库初始化
rem  作用：开两个窗口分别启动后端(8080)与前端(5173)
rem ============================================================
cd /d %~dp0
start "meeting-room-backend" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"
start "meeting-room-frontend" cmd /k "cd /d %~dp0frontend && npm ci && npm run dev"
echo Backend  -^> http://localhost:8080
echo Frontend -^> http://localhost:5173
echo Two windows opened. Keep them running.
