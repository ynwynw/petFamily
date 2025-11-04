<template>
  <div class="header-bar clear-fix">
    <BreadCrumbs />
    <div class="header-right clear-fix">
      <el-button
        size="small"
        plain
        v-if="userInfo.role == 'USER'"
        type="warning"
        @click.native="$router.push('/front')"
        >返回主页</el-button
      >
      <UserAvatar />
    </div>
  </div>
</template>

<script>
import BreadCrumbs from './BreadCrumbs/index.vue'
import UserAvatar from '../UserAvatar/index.vue'
export default {
  inject: ['userInfo'],
  name: 'header',
  components: {
    BreadCrumbs,
    UserAvatar,
  },
}
</script>

<style>
.header-bar {
  width: 100%;
  background-color: rgba(248, 187, 208, 0.5);
  display: flex;
  flex-direction: row;
  height: 32px;
  /* margin-top: 5px; */
  line-height: 32px;
  padding: 16px 20px;
  box-shadow: 0px 2px 4px rgba(0, 0, 0, 0.1);
  /* 只出现在下边框的阴影效果 */
}

.header-right {
  margin-left: auto;
  /* float: right; */
  width: 250px;
}
</style>
