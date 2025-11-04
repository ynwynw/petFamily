<template>
  
  <el-dropdown class="user-avatar-wrapper" @command="handleCommand">
    <div class="avatar-box">
      <el-avatar size="small" :src="this.HOST+'/'+this.userInfo.avatar" />
      <span class="username">{{userInfo.username}}</span>
      <i class="el-icon-caret-bottom" />
    
    </div>
 
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item command="userCenter">个人中心</el-dropdown-item>
      <el-dropdown-item command="loginOut">退出登录</el-dropdown-item>
    </el-dropdown-menu>
    
  </el-dropdown>
</template>

<script>

export default {
  inject: ['userInfo'],
  created() {
    // 添加全局样式修复确认对话框问题
   
  },
  name: 'UserAvatar',
  data() {
    return {
      // userInfo: {},
      imgBaseUrl: this.HOST,
    }
  },
  methods: {
    handleCommand(command) {
      if (command === 'userCenter') {
          this.$router.push({ path: '/personInfo' })
      }
      if (command === 'loginOut') {
          this.loginOut()
      }
    },
    loginOut() {
      this.$confirm('确定注销并退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        sessionStorage.removeItem("user")
        this.$router.push({ path: '/login'});
        //   removeToken()
        //   location.reload()
      })
    },

  }
}
</script>

<style lang="less">
.user-avatar-wrapper {
  float: left;
  // width: 120px;
  padding: 3px 0 3px 20px;
  margin-left: 20px;
  border-left: solid 1px #ddd;
  cursor: pointer;

  .avatar-box {
    outline: none;
    display: flex;
  align-items: center; /* 垂直居中对齐 */
  justify-content: space-between; /* 头像和下拉箭头之间留有空间 */
  }

  .username {
    font-size: 14px;
    /* 根据需要调整字体大小 */
    color: #333;
    /* 字体颜色 */
    margin-left: 5px;
    /* 与头像之间的间距 */
  }

  .el-avatar--small {
    display: inline-block;
    vertical-align: middle;
    width: 32px;
    height: 32px;
    line-height: 32px;
  }

  i {
    display: inline-block;
    vertical-align: middle;
    margin-left: 2px;
  }
}

/* 修复确认对话框样式 */
.el-message-box {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.2);
}

.el-message-box__title {
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}

.el-message-box__content {
  padding: 20px;
  font-size: 15px;
}

.el-message-box__btns {
  padding: 10px 20px 15px;
}

.el-message-box__btns .el-button {
  padding: 9px 20px;
  font-size: 14px;
}
</style>