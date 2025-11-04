<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-section">
        <h3>关于我们</h3>
        <p>宠物之家致力于为每一个需要关爱的小生命找到温暖的家</p>
      </div>
      
      <div class="footer-section">
        <h3>联系方式</h3>
        <div class="contact-info">
          <p><i class="el-icon-phone"></i> 123-456-789</p>
          <p><i class="el-icon-message"></i> example@example.com</p>
        </div>
      </div>
    </div>
    
    <div class="footer-bottom">
      <p>&copy; 2025 宠物之家 版权所有</p>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'Footer'
}
</script>

<style scoped>
.footer {
  background-color: #fff;
  border-top: 1px solid #eee;
  padding: 20px 0;
  width: 100%;
  text-align: center;
}

.footer-content {
  width: 70%;
  margin: 0 auto;
  display: flex;
  justify-content: center;
  gap: 60px;
}

.footer-section {
  flex: 1;
  max-width: 300px;
  padding: 0 20px;
}

.footer-section h3 {
  color: #333;
  font-size: 16px;
  margin-bottom: 12px;
  font-weight: 600;
}

.footer-section p {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 5px 0;
}

.contact-info p {
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-section i {
  margin-right: 8px;
  color: #409EFF;
  font-size: 16px;
}

.footer-bottom {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.footer-bottom p {
  color: #999;
  font-size: 14px;
  margin: 0;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .footer-content {
    width: 90%;
    flex-direction: column;
    gap: 20px;
  }

  .footer-section {
    padding: 0;
    max-width: none;
  }
}
</style>