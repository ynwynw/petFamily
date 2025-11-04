<template>
  <div class="el-sidebar">
    <div class="el-sidebar-logo">
      <img src="@/assets/logo.png" alt="logo">
    </div>
    <el-menu
      default-active="1-1"
      class="el-menu-vertical-demo"
      background-color="#001529" 
      text-color="#fff"
      active-text-color="#ffd04b"  

    >
      <el-submenu index="1">
        <template slot="title">
          <i class="el-icon-location"></i>
          <span>导航一</span>
        </template>
        <el-menu-item index="1-1">选项 1</el-menu-item>
        <el-menu-item index="1-2">选项 2</el-menu-item>
      </el-submenu>
      <el-submenu index="2">
        <template slot="title">
          <i class="el-icon-menu"></i>
          <span>导航二</span>
        </template>
        <el-menu-item index="2-1">选项 3</el-menu-item>
        <el-menu-item index="2-2">选项 4</el-menu-item>
      </el-submenu>
      <el-submenu index="3">
        <template slot="title">
          <i class="el-icon-setting"></i>
          <span>导航三</span>
        </template>
        <el-menu-item index="3-1">选项 5</el-menu-item>
        <el-menu-item index="3-2">选项 6</el-menu-item>
      </el-submenu>
    </el-menu>
  </div>
</template>

<script>
export default {
  data() {
    return {};
  },
  methods: {
    handleOpen(key, keyPath) {
      console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      console.log(key, keyPath);
    },
  },
};
</script>

<style lang="less">
.el-sidebar {
  width: 200px; /* 侧边栏宽度 */
  height: 100vh; /* 全视口高度 */
  overflow: hidden; /* 隐藏溢出的内容 */
  .el-sidebar-logo {
    height: 60px; /* Logo 区域高度 */
    display: flex;
    align-items: center;
    justify-content: center;
    img {
      height: 30px; /* Logo 图片高度 */
    }
  }
  .el-menu-vertical-demo {
    border-right: none; /* 移除默认边框 */
    .el-menu-item {
      font-size: 14px; /* 菜单项文字大小 */
      &:hover {
        background-color: #409EFF; /* 鼠标悬浮背景色 */
      }
    }
    .el-submenu__title {
      font-size: 14px; /* 子菜单标题文字大小 */
      &:hover {
        background-color: #409EFF; /* 鼠标悬浮背景色 */
      }
    }
  }
}
</style>