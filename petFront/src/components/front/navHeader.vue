<template>
  <div class="header">
    <div class="logo">
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
    
    <el-menu
      :default-active="activeIndex"
      class="el-menu-demo"
      mode="horizontal"
      background-color="#ffffff"
      text-color="#2c3e50"
      active-text-color="#e91e63"
      @select="handleSelect"
    >
      <el-menu-item index="/frontHome">
        <i class="el-icon-s-home"></i>
        首页
      </el-menu-item>
      <el-menu-item index="/petAdoptFront">
        <i class="el-icon-s-cooperation"></i>
        宠物领养
      </el-menu-item>
      <el-menu-item index="/petGoods">
        <i class="el-icon-shopping-bag-1"></i>
        宠物商店
      </el-menu-item>
      <el-menu-item index="/petServices">
        <i class="el-icon-s-tools"></i>
        宠物服务
      </el-menu-item>
    </el-menu>

    <div class="user">
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
        <el-button type="text" @click="showNotifications" class="notification-btn">
          <i class="el-icon-bell"></i>
        </el-button>
      </el-badge>
      <UserAvatar v-if="userInfo" />
    </div>

    <el-drawer
      title="我的通知"
      :visible.sync="notificationDrawer"
      direction="rtl"
      size="400px"
      :modal="false"
      :append-to-body="true"
    >
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="未读消息" name="unread">
          <div v-if="unreadNotifications.length === 0" class="empty-text">暂无未读消息</div>
          <div v-else class="notification-list">
            <div 
              v-for="item in unreadNotifications" 
              :key="item.id" 
              class="notification-item"
              @click="markAsRead(item)"
            >
              <div class="notification-content">{{ item.message }}</div>
              <div class="notification-time">{{ item.timestamp }}</div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="已读消息" name="read">
          <div v-if="readNotifications.length === 0" class="empty-text">暂无已读消息</div>
          <div v-else class="notification-list">
            <div 
              v-for="item in readNotifications" 
              :key="item.id" 
              class="notification-item"
            >
              <div class="notification-content">{{ item.message }}</div>
              <div class="notification-time">{{ item.timestamp }}</div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>
  </div>
</template>

<script>
import UserAvatar from '../UserAvatar/index.vue';
import Request from '@/utils/request.js';

export default {
  name: 'navHeader',
  inject: ['userInfo'],
  components: {
    UserAvatar,
  },
  data() {
    return {
      notificationDrawer: false,
      activeTab: 'unread',
      unreadNotifications: [],
      readNotifications: [],
      unreadCount: 0,
      timer: null
    }
  },
  computed: {
    activeIndex() {
      // 根据当前路由返回激活的菜单项
      if (this.$route.path.includes('/petDetail')) {
        return '/petAdoptFront'
      }
      return this.$route.path
    }
  },
  created() {
    if (sessionStorage.getItem('user')) {
      this.fetchNotifications()
      this.timer = setInterval(() => {
        if (sessionStorage.getItem('user')) {
          this.fetchNotifications()
        }
      }, 30000)
    }
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    // 添加缺失的handleSelect方法
    handleSelect(index) {
      console.log('Menu selected:', index)
      // 路由跳转
      if (this.$route.path !== index) {
        this.$router.push(index).catch(err => {
          // 处理路由跳转可能的错误
          if (err.name !== 'NavigationDuplicated') {
            console.error('路由跳转错误:', err)
          }
        })
      }
    },

    showNotifications() {
      if (!sessionStorage.getItem('user')) {
        this.$message.warning('请先登录')
        return
      }
      this.notificationDrawer = true
      this.fetchNotifications()
    },

    handleTabClick() {
      this.fetchNotifications()
    },

    fetchNotifications() {
      const user = JSON.parse(sessionStorage.getItem('user'))
      if (!user) {
        this.unreadNotifications = []
        this.readNotifications = []
        this.unreadCount = 0
        return
      }

      Request.get(`/pet-notification/user/${user.id}`, {
        params: {
          status: 'unread',
          currentPage: 1,
          size: 50
        }
      }).then(response => {
        if (response.code === '0' && response.data) {
          this.unreadNotifications = response.data.records || []
          this.unreadCount = response.data.total || 0
        } else {
          this.unreadNotifications = []
          this.unreadCount = 0
        }
      }).catch(() => {
        this.unreadNotifications = []
        this.unreadCount = 0
      })

      Request.get(`/pet-notification/user/${user.id}`, {
        params: {
          status: 'read',
          currentPage: 1,
          size: 50
        }
      }).then(response => {
        if (response.code === '0' && response.data) {
          this.readNotifications = response.data.records || []
        } else {
          this.readNotifications = []
        }
      }).catch(() => {
        this.readNotifications = []
      })
    },

    markAsRead(notification) {
      Request.put(`/pet-notification/markAsRead/${notification.id}`).then(response => {
        if (response.code === '0') {
          this.fetchNotifications()
        }
      })
    }
  }
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #f8bbd0;
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  height: 64px;
}

.logo {
  height: 64px;
  padding: 5px 20px;
  margin-right: auto;
  display: flex;
  align-items: center;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-wrapper {
  display: flex;
  align-items: center;
}

.icon-wrapper .el-icon-star-on {
  color: #f8bbd0;
  font-size: 16px;
  margin: 0 5px;
  animation: twinkle 1.5s infinite alternate;
}

.main-icon:before {
  content: "\e6f3";
  font-size: 32px;
  background: linear-gradient(45deg, #e91e63, #f06292);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(1px 1px 1px rgba(233, 30, 99, 0.3));
}

.text-wrapper {
  display: flex;
  flex-direction: column;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(45deg, #e91e63, #f06292);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-family: "黑体", "SimHei", sans-serif;
  letter-spacing: 1px;
  line-height: 1;
}

.logo-subtitle {
  font-size: 12px;
  color: #f48fb1;
  font-family: "Arial", sans-serif;
  letter-spacing: 0.5px;
  font-style: italic;
  opacity: 0.8;
}

@keyframes twinkle {
  from {
    opacity: 0.6;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1.1);
  }
}

.el-menu-demo {
  border-bottom: none;
  margin-right: 20px;
}

.el-menu-item {
  height: 64px;
  line-height: 64px;
  font-size: 15px;
  padding: 0 25px;
  transition: all 0.3s ease;
}

.el-menu-item i {
  margin-right: 5px;
  font-size: 18px;
  color: #f06292;
}

.el-menu-item:hover {
  background-color: #fce4ec !important;
}

.el-menu-item.is-active {
  font-weight: 600;
  background-color: #f8bbd0 !important;
  border-bottom: 2px solid #e91e63;
}

.user {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-left: 20px;
  border-left: 1px solid #f8bbd0;
}

.notification-badge {
  margin-right: 5px;
}

.notification-btn {
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.notification-btn:hover {
  background-color: #fce4ec;
}

.notification-btn i {
  font-size: 20px;
  color: #f06292;
}

.notification-list {
  padding: 15px;
}

.notification-item {
  padding: 15px;
  border-radius: 8px;
  border-bottom: 1px solid #f8bbd0;
  transition: all 0.3s ease;
  cursor: pointer;
}

.notification-item:hover {
  background-color: #fce4ec;
  transform: translateX(5px);
}

.notification-content {
  font-size: 14px;
  color: #2c3e50;
  margin-bottom: 8px;
  line-height: 1.4;
}

.notification-time {
  font-size: 12px;
  color: #f06292;
}

.empty-text {
  text-align: center;
  color: #f06292;
  padding: 30px;
  font-size: 14px;
}

@media screen and (max-width: 768px) {
  .header {
    padding: 0 10px;
  }

  .logo {
    padding: 5px 10px;
  }

  .el-menu-item {
    padding: 0 15px;
  }

  .user {
    gap: 10px;
    padding-left: 10px;
  }
}
</style>