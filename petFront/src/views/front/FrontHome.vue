<template>
    <div class="home-page">
      <!-- 轮播图区域 -->
      <div class="carousel-section">
        <pet-carousel :breeds="silderList"></pet-carousel>
      </div>
      
      <!-- 智能推荐区域 (新增) -->
      <div class="section-container recommendation-section">
        <pet-recommendation></pet-recommendation>
      </div>
      
      <!-- 宠物推荐区域 -->
      <div class="section-container">
        <div class="section-header">
          <h2>宠物推荐</h2>
          <div class="section-divider"></div>
        </div>
        <div class="section-content">
          <pet-showcase :pets="pets"></pet-showcase>
        </div>
      </div>
      
      <!-- 宠物用品区域 -->
      <div class="section-container">
        <div class="section-header">
          <h2>宠物用品</h2>
          <div class="section-divider"></div>
        </div>
        <div class="section-content">
          <pet-products :products="products"></pet-products>
        </div>
      </div>
    </div>
  </template>
</template>
  
  <script>
  import PetCarousel from '../../components/front/PetCarousel.vue'
  import PetShowcase from '../../components/front/PetShowcase.vue'
  import PetProducts from '../../components/front/PetProducts.vue'
import PetRecommendation from '../../components/front/PetRecommendation.vue'
  import Request from '../../utils/request.js'
  export default {
  components: { PetCarousel, PetShowcase, PetProducts, PetRecommendation },
    created(){
      this.loadPets();  // 获取物数据
      this.loadProducts();  // 获取物品数据
      this.loadSlider();  // 获取轮播数据
    },
    data() {
      return {
        pets:[],
        products:[],
        silderList:[],
        breeds: [
          { id: 1, name: '拉布拉多',  desrc: '温和友善、聪明易训练',img:'/img/testImg/1.png' },
          { id: 2, name: '金吉拉',  desrc: '波斯猫的选育后',img:'/img/testImg/2.jpeg' },
          { id: 3, name: '豹纹守宫', desrc: '温顺性格和易于照料',img:'/img/testImg/3.jpeg' }
        ],
      }
    },
    methods: {
      loadSlider(){
        Request.get('/slider/selectAll').then(res=>{
            if(res.code==0){
              this.silderList = res.data;
              console.log("silderList"+this.silderList)
            }
        })
      },
      loadPets(){
        Request.get('/animal/selectAll').then(res=>{
          if(res.code==0){
            this.pets = res.data;
          }else{  
            // console.log('获取物数据失败');
          }
        })
      },
      loadProducts(){
        Request.get('/goods/selectAll').then(res=>{
          if(res.code==0){
            this.products = res.data;
          }else{  
            // console.log('获取物数据失败');
          }
        })
      },
    },
  }
  </script>
  
  <style scoped>
  .home-page {
    display: flex;
    width: 100%;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background-color: #f9f9f9;
    padding-bottom: 40px;
  }
  
  .carousel-section {
    width: 100%;
    margin-bottom: 30px;
    padding: 20px 0;
  }
  
  .section-container {
  width: 100%;
    max-width: 1200px;
  margin: 20px auto;
  padding: 0 15px;
}

.recommendation-section {
  margin-top: 30px;
  }
  
  .section-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    position: relative;
  text-align: center;
  }
  
  .section-header h2 {
    font-size: 24px;
    font-weight: 600;
    color: #5aaa95;
    margin: 0;
    padding-right: 15px;
    position: relative;
    z-index: 1;
    background-color: #f9f9f9;
  }
  
  .section-divider {
    flex: 1;
    height: 2px;
    background: linear-gradient(to right, #a8e6cf, transparent);
    margin-left: 10px;
  height: 3px;
  width: 60px;
  background-color: #409eff;
  margin: 10px auto;
  }
  
  .section-content {
    background-color: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
    border: 1px solid rgba(220, 237, 193, 0.5);
  }
  
  @media (max-width: 768px) {
    .carousel-section {
      padding: 10px 0;
    }
    
    .section-container {
      width: 95%;
    }
    
    .section-header h2 {
      font-size: 20px;
    }
    
    .section-content {
      padding: 15px;
    }
  }
  </style>