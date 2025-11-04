<template>
  <div class="layout">
    <header >
      <NavHeader />
    </header>
    <main>
      <router-view @update:user="updateUser" style="flex: 1;" />
    </main>
    <footer>
      <Footer />
    </footer>
  </div>
</template>

<script>
import NavHeader from '../../components/front/navHeader.vue'
import Footer from '../../components/front/footer.vue'

export default {
  name: 'Layout',
  components: {
    NavHeader,
    Footer
  },
  data() {
    return {
      userInfo: JSON.parse(sessionStorage.getItem("user") || {}),

    }
  },
  provide() {
    return {
      userInfo: this.userInfo
    };
  },
  methods: {
    updateUser(user) {
      this.userInfo = user;
    }

  }
}
</script>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  /* min-height: 100vh; */
  position: relative;
  overflow-y: scroll;
  width: 100%;
}
.layout::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* background: url('../../assets/frontBg.jpeg') no-repeat center center fixed; */
  background-size: cover;
  opacity: 0.8; /* 设置半透明效果 */
  z-index: -1;
}
header {

  background-color: #333;
  color: #fff;

  /* padding: 20px; */
  /* text-align: center; */
}

main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  height: 100%;
  /* height: calc(100vh - 120px); */
}

footer {

  background-color: #333;
  color: #fff;
  flex:1;
  /* padding: 20px; */
  text-align: center;
  clear: both;
}
</style>