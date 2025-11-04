<template>
  <div class="goods-container">
    <!-- 标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <h1>宠物用品商城</h1>
        <p>为您的宠物挑选优质用品</p>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" class="search-form">
        <el-form-item>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品名称"
            prefix-icon="el-icon-search"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData" icon="el-icon-search"
            >搜索</el-button
          >
          <el-button @click="reset" icon="el-icon-refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 商品列表 -->
    <div class="goods-grid">
      <el-row :gutter="20">
        <el-col
          :xs="24"
          :sm="12"
          :md="6"
          :lg="6"
          v-for="product in productList"
          :key="product.id"
        >
          <div class="goods-card">
            <div class="goods-image">
              <el-image :src="imgBaseUrl + product.img" fit="cover">
                <div slot="error" class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
              <div class="goods-tag">
                <i class="el-icon-shopping-bag-2"></i>
              </div>
            </div>

            <div class="goods-info">
              <h3 class="goods-name">{{ product.name }}</h3>
              <el-tooltip
                v-if="product.desrc && product.desrc.length >= 40"
                :content="product.desrc"
                placement="top"
                effect="light"
              >
                <p class="goods-desc">{{ product.desrc }}</p>
              </el-tooltip>
              <p v-else class="goods-desc">{{ product.desrc }}</p>

              <div class="goods-meta">
                <div class="price-section">
                  <span class="price">{{ product.price | currency }}</span>
                  <span class="unit">/ 份</span>
                </div>
                <div class="stock">库存: {{ product.num }}</div>
              </div>

              <div class="purchase-section">
                <el-input-number
                  v-model="product.buyNum"
                  :min="0"
                  :max="product.num"
                  size="small"
                  controls-position="right"
                  :placeholder="0"
                ></el-input-number>

                <div class="total-price">
                  总价:
                  <span>{{
                    (product.price * (product.buyNum || 0)) | currency
                  }}</span>
                </div>
              </div>

              <div class="action-section">
                <p v-if="userInfo.role != 'USER'" class="admin-tip">
                  <i class="el-icon-warning-outline"></i>
                  管理员不可购买商品
                </p>
                <el-button
                  type="primary"
                  round
                  :disabled="userInfo.role != 'USER'"
                  @click="addToCart(product)"
                >
                  <i class="el-icon-shopping-cart-2"></i>
                  立即购买
                </el-button>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'
export default {
  name: 'Buy',
  inject: ['userInfo'],
  created() {
    this.fetchData()
  },

  data() {
    return {
      // 搜索关键字
      searchKeyword: '',
      imgBaseUrl: this.HOST,
      orderInfo: {
        goodsId: undefined,
        num: undefined,
        userId: this.userInfo.id,
      },
      // 产品列表
      productList: [],
    }
  },
  methods: {
    // 搜索商品
    search() {
      // 根据关键字过滤产品列表
      this.productList = this.productList.filter((product) =>
        product.name.includes(this.searchKeyword)
      )
    },
    reset() {
      this.searchKeyword = ''
      this.fetchData()
    },
    fetchData() {
      request
        .get('/goods', {
          params: {
            name: this.searchKeyword,
          },
        })
        .then((response) => {
          if (response.code === '0') {
            // 为每个商品添加默认的购买数量
            this.productList = response.data.map((item) => ({
              ...item,
              buyNum: 0,
            }))
          }
        })
    },
    // 添加到购物车
    addToCart(product) {
      if (!product.buyNum || product.buyNum === 0) {
        this.$message({
          type: 'warning',
          message: '请选择购买数量!',
        })
        return
      }
      this.orderInfo.num = product.buyNum
      this.orderInfo.goodsId = product.id

      request.post('/orders', this.orderInfo).then((res) => {
        if (res.code == '0') {
          this.$message({
            type: 'success',
            message: '订单创建成功!',
          })
        } else {
          this.$message({
            type: 'error',
            message: '订单创建失败!',
          })
        }
      })
    },
  },
  filters: {
    // 货币格式化过滤器
    currency(value) {
      return '¥' + parseFloat(value).toFixed(2)
    },
  },
}
</script>

<style lang="less" scoped>
.goods-container {
  min-height: 100vh;
  background-color: #f9f9f9;
  padding: 0 0 40px 0;
}

.page-header {
  background: linear-gradient(135deg, #5aaa95 0%, #88b04b 100%);
  padding: 60px 0;
  margin-bottom: 40px;
  color: white;
  text-align: center;

  .header-content {
    h1 {
      font-size: 42px;
      margin: 0;
      font-weight: 600;
      text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
    }
    p {
      font-size: 18px;
      margin-top: 10px;
      opacity: 0.9;
    }
  }
}

.search-section {
  max-width: 1200px;
  margin: -70px auto 30px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(220, 237, 193, 0.5);

  .search-form {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    gap: 10px;

    .el-form-item {
      margin-bottom: 0;
    }
  }
}

.goods-grid {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.goods-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(220, 237, 193, 0.5);

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    border-color: #a8e6cf;

    .goods-tag {
      background-color: #5aaa95;
    }
  }

  .goods-image {
    width: 100%;
    padding-top: 100%;
    position: relative;
    overflow: hidden;

    .el-image {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      transition: transform 0.3s;

      &:hover {
        transform: scale(1.05);
      }
    }

    .goods-tag {
      position: absolute;
      top: 10px;
      right: 10px;
      width: 30px;
      height: 30px;
      border-radius: 50%;
      background-color: #88b04b;
      display: flex;
      justify-content: center;
      align-items: center;
      color: white;
      transition: all 0.3s ease;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);

      i {
        font-size: 16px;
      }
    }
  }

  .goods-info {
    padding: 16px;

    .goods-name {
      font-size: 18px;
      font-weight: 600;
      margin: 0 0 8px;
      color: #303133;
    }

    .goods-desc {
      color: #606266;
      font-size: 14px;
      margin: 0 0 12px;
      line-height: 1.4;
      height: 40px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .goods-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .price-section {
        .price {
          color: #5aaa95;
          font-size: 20px;
          font-weight: 600;
        }
        .unit {
          color: #909399;
          font-size: 14px;
          margin-left: 4px;
        }
      }

      .stock {
        color: #88b04b;
        font-size: 14px;
      }
    }

    .purchase-section {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .total-price {
        color: #606266;
        font-size: 14px;

        span {
          color: #5aaa95;
          font-weight: 600;
          font-size: 16px;
        }
      }
    }

    .action-section {
      text-align: right;

      .admin-tip {
        color: #ff9800;
        font-size: 12px;
        margin: 0 0 8px;

        i {
          margin-right: 4px;
        }
      }

      .el-button {
        padding: 8px 20px;
        background-color: #5aaa95;
        border-color: #5aaa95;

        i {
          margin-right: 4px;
        }

        &:hover,
        &:focus {
          background-color: #7bbeaa;
          border-color: #7bbeaa;
        }
      }
    }
  }
}

@media screen and (max-width: 768px) {
  .page-header {
    padding: 40px 0;

    .header-content {
      h1 {
        font-size: 32px;
      }
      p {
        font-size: 16px;
      }
    }
  }

  .search-section {
    margin: -50px 20px 30px;

    .search-form {
      flex-direction: column;

      .el-form-item {
        width: 100%;
        margin-right: 0;

        .el-input {
          width: 100%;
        }
      }
    }
  }

  .goods-grid {
    padding: 0 10px;
  }

  .goods-card {
    .goods-info {
      padding: 12px;

      .goods-name {
        font-size: 16px;
      }

      .purchase-section {
        flex-direction: column;
        align-items: stretch;
        gap: 8px;
      }
    }
  }
}
</style>
