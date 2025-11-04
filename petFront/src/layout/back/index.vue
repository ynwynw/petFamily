<template>
  <div class="app-wrapper" >
    <div class="side-container" >

      <SideMenu />
    </div>
    <div class="main-container">
      <div class="main-header" style="margin-bottom: 20px" >
        <HeaderBar />

      </div>
      <div class="main-content">
        <el-scrollbar wrap-class="scrollbar">
          <router-view @update:user="updateUser" style="flex: 1;" />
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>
<script>
import HeaderBar from '../../components/back/MyHeader.vue'
import SideMenu from '../../components/back/Aside.vue'
import Logo from '../../assets/logo.png'

export default {
  name: 'Layout',
  data() {
    return {
      userInfo: JSON.parse(sessionStorage.getItem("user") || {}),
      logoUrl: Logo
    };
  },
  created() {


  },
  provide() {
    return {
      userInfo: this.userInfo
    };
  },
  components: { HeaderBar, SideMenu },
  computed: {

  },
  methods: {
    updateUser(user) {
      this.userInfo = user;
    }

  }
}
</script>

<style lang="less">
@import "../../assets/css/scrollbar.css";

.app-wrapper {
  width: 100%;
  height: 100vh;
  overflow-y:hidden;
  overflow-x: hidden;
  .side-container {
    background-color: #f5f7fa;
    float: left;
    width: 220px;
    height: 100vh;
    img {
      display: block;
      width: 100%;
      height: 100%;
    }
  }

  .main-container {
    float: left;
    height: 100%;
    transition: width 0.5s;

    width: calc(100% - 220px);


    .main-content {

      .el-scrollbar {
        height: calc(100vh - 64px - 40px);
       

        .scrollbar {
          height: 100%;
          overflow-x: hidden;
        }
      }
    }
  }
}
</style>