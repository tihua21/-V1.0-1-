<template>
  <router-view />
</template>

<script setup>
</script>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
  font-family: -apple-system, "Microsoft YaHei", sans-serif;
  background-color: #409EFF;
}

/* 第11周 Bug 修复（BUG-W11-03）：390px 手机视口下页面横向溢出。
   定位：诊断脚本测得 document.scrollWidth=562，溢出源为 NavBar 的
   .brand/.right（white-space:nowrap 的 min-content 撑破视口）。
   修复：窄屏下导航栏允许换行、压缩间距，菜单独占一行；页面容器减小边距。
   表格列宽超出部分由 el-table 内部横向滚动承载，不影响文档宽度。 */
@media (max-width: 768px) {
  .navbar {
    flex-wrap: wrap;
    padding: 6px 10px;
    row-gap: 4px;
  }
  .navbar .brand {
    margin-right: 10px;
    font-size: 14px;
  }
  .navbar .menu {
    flex: 1 1 100%;
    order: 3;
    min-width: 0;
    overflow-x: auto;
  }
  .navbar .right {
    margin-left: auto;
  }
  .navbar .right .user {
    font-size: 12px;
  }
  .room-list {
    margin: 10px auto;
    padding: 0 8px;
  }
}
</style>
