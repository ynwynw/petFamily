<template>
    <div class="not-found">
      <h1>404</h1>
      <h2>页面不存在</h2>
      <p>抱歉，您访问的页面不存在或已被删除。</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </template>
  
  <script>
  export default {
    name: 'NotFound',
    methods: {
      goHome() {
        this.$router.push("/showView")
      }
    }
  };
  </script>
  
  <style scoped>
  .not-found {
    text-align: center;
    padding-top: 50px;
  }
  .not-found h1 {
    font-size: 100px;
    color: #f56c6c;
    margin-bottom: 20px;
  }
  .not-found h2 {
    font-size: 24px;
    color: #909399;
    margin-bottom: 10px;
  }
  .not-found p {
    font-size: 16px;
    color: #909399;
    margin-bottom: 20px;
  }
  </style>