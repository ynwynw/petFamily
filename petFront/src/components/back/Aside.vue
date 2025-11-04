<script setup>
import '../../assets/css/iconfont.css'
</script>
<template>
  <div class="aside-container">
    <div class="aside-logo">
      <div class="logo-container">
        <div class="icon-wrapper">
          <i class="el-icon-star-on"></i>
          <i class="el-icon-pet main-icon"></i>
          <i class="el-icon-star-on"></i>
        </div>
        <div class="text-wrapper">
          <span class="logo-text">萌宠乐园</span>
          <span class="logo-subtitle">Pet Paradise</span>
        </div>
      </div>
    </div>

    <div class="menu-container">
      <el-menu
        style="width: 220px"
        default-active="dashboard"
        class="el-menu-vertical-demo"
        router
        background-color="white"
        text-color="rgb(77, 74, 74)"
      >
        <div v-for="item in userMenuList" :key="item.id">
          <div v-if="item.path">
            <div v-if="!item.pid">
              <el-menu-item :index="item.path">
                <span :class="item.icon" style="margin-right: 5px"></span>
                <span slot="title">{{ item.name }}</span>
              </el-menu-item>
            </div>
          </div>
          <div v-else>
            <el-submenu :index="item.id + ''" text-color="#fff">
              <template slot="title">
                <span :class="item.icon" style=""></span>
                <span slot="title">{{ item.name }}</span>
              </template>
              <div v-for="subItem in item.children" :key="subItem.id">
                {{ printMenu(subItem) }}
                <el-menu-item
                  :index="subItem.path"
                  style="display: flex; align-items: center"
                >
                  <template slot="title">
                    <span :class="subItem.icon"></span>
                    <span slot="title">{{ subItem.name }}</span>
                  </template>
                </el-menu-item>
              </div>
            </el-submenu>
          </div>
        </div>
      </el-menu>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Aside',
  components: {},
  created() {
    let userMenuListStr = sessionStorage.getItem('userMenuList')
    this.userMenuList = userMenuListStr ? JSON.parse(userMenuListStr) : []
  },
  data() {
    return {
      userMenuList: [],
      user: {},
      path: this.$route.path,
    }
  },
  methods: {
    printMenu(menu) {},
  },
}
</script>

<style scoped>
.aside-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.aside-logo {
  flex-shrink: 0;
  height: 65px;
  padding: 10px;
  background: linear-gradient(
    135deg,
    rgba(248, 187, 208, 0.8) 0%,
    #e4ecf7 100%
  );
  border-bottom: 1px solid rgba(58, 110, 165, 0.1);
  margin-bottom: 5px;
  position: relative;
  overflow: hidden;
}

.aside-logo::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(
      circle at 30% 50%,
      rgba(58, 110, 165, 0.05) 0%,
      transparent 50%
    ),
    radial-gradient(
      circle at 70% 50%,
      rgba(58, 110, 165, 0.05) 0%,
      transparent 50%
    );
  pointer-events: none;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.icon-wrapper {
  display: flex;
  align-items: center;
}

.icon-wrapper .el-icon-star-on {
  color: #ffd700;
  font-size: 14px;
  margin: 0 3px;
  animation: twinkle 1.5s infinite alternate;
  filter: drop-shadow(0 0 2px rgba(255, 215, 0, 0.3));
}

.main-icon:before {
  content: '\e6f3';
  font-size: 32px;
  background: linear-gradient(45deg, #3a6ea5, #304878);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(1px 1px 1px rgba(58, 110, 165, 0.3));
}

.text-wrapper {
  display: flex;
  flex-direction: column;
}

.logo-text {
  font-size: 22px;
  font-weight: bold;
  background: linear-gradient(45deg, #3a6ea5, #304878);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-family: '黑体', 'SimHei', sans-serif;
  letter-spacing: 1px;
  line-height: 1.2;
  text-shadow: 0 2px 4px rgba(58, 110, 165, 0.1);
}

.logo-subtitle {
  font-size: 12px;
  background: linear-gradient(45deg, #5d7b9e, #3a6ea5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-family: 'Arial', sans-serif;
  letter-spacing: 0.5px;
  font-style: italic;
  margin-top: 3px;
}

.menu-container {
  flex: 1;
  overflow-y: auto;
  background-color: #f5f7fa;
}

.el-menu-vertical-demo {
  height: 100%;
  overflow-y: auto;
  background-color: #f5f7fa !important;
}

@keyframes twinkle {
  from {
    opacity: 0.6;
    transform: scale(0.9);
    filter: brightness(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1.1);
    filter: brightness(1.2);
  }
}

.el-menu {
  background-color: rgba(248, 187, 208, 0.8) !important;
  border-right: none;
}

.el-menu-item {
  /* background-color: #f5f7fa !important; */
  background-color: rgba(248, 187, 208, 0.8) !important;
  height: 55px;
  line-height: 55px;
  font-size: 15px;
  min-width: 95% !important;
}

.el-menu-item:hover {
  background: #d4e6f8 !important;
  color: black;
}

.el-menu-item.is-active {
  color: white;
  background: #3a6ea5 !important;
  border-radius: 6px;
}

.el-submenu ::v-deep .el-submenu__title {
  font-size: 15px !important;
  line-height: 55px !important;
  /* background-color: #f5f7fa !important; */
  background-color: rgba(248, 187, 208, 0.8) !important;
  height: 55px;
  border-radius: 6px;
}

.el-submenu ::v-deep .el-submenu__title:hover {
  background: #d4e6f8 !important;
}

.el-submenu ::v-deep .el-menu--inline {
  background-color: #f5f7fa !important;
}

.el-submenu .el-menu--inline .el-menu-item {
  /* background-color: #f5f7fa !important; */
  background-color: rgba(248, 187, 208, 0.8) !important;
  margin: 0 !important;
}

.el-submenu .el-menu--inline .el-menu-item:hover {
  background: #d4e6f8 !important;
}

.el-submenu .el-menu--inline .el-menu-item.is-active {
  background: #3a6ea5 !important;
  margin: 0 !important;
}

.icon-hover {
  transition: transform 0.3s ease;
}

.el-menu-item:hover .icon-hover ::v-deep {
  transform: scale(2) !important;
}
</style>
