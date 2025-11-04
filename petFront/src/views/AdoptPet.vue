<template>
  <div class="adopt-container">
    <!-- 标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <h1>宠物领养</h1>
        <p>每一个生命都值得被温柔以待</p>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input
            v-model="searchForm.name"
            placeholder="搜索宠物名称"
            prefix-icon="el-icon-search"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.type"
            placeholder="选择宠物类型"
            clearable
          >
            <el-option label="猫" value="猫"></el-option>
            <el-option label="狗" value="狗"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" icon="el-icon-search"
            >搜索</el-button
          >
          <el-button @click="resetSearch" icon="el-icon-refresh"
            >重置</el-button
          >
        </el-form-item>
      </el-form>
    </div>

    <!-- 我的申请按钮 -->
    <div class="my-adopts" v-if="userInfo.role === 'USER'">
      <el-button type="primary" @click="showMyAdopts" icon="el-icon-document"
        >我的领养申请</el-button
      >
    </div>

    <!-- 宠物卡片区域 -->
    <div class="pet-grid">
      <el-row :gutter="20">
        <el-col
          :xs="24"
          :sm="12"
          :md="6"
          :lg="6"
          v-for="item in animalData"
          :key="item.id"
        >
          <div class="pet-card" @click="toDetail(item)">
            <div class="pet-image">
              <el-image
                :src="imgBaseUrl + item.img"
                fit="cover"
                :preview-src-list="[imgBaseUrl + item.img]"
              >
                <div slot="error" class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
              <div class="pet-status" :class="getStatusClass(item.status)">
                {{ item.status }}
              </div>
            </div>

            <div class="pet-info">
              <div class="pet-header">
                <h3>{{ item.name }}</h3>
                <span class="pet-age">{{ item.age }}岁</span>
              </div>
              <div class="pet-tags">
                <span class="tag">{{ item.type }}</span>
                <span class="tag">{{ item.sex }}</span>
              </div>
              <p class="pet-desc">{{ item.descr }}</p>
              <el-button
                class="adopt-btn"
                :type="getButtonType(item.status)"
                round
                @click.stop="handleAdopt(item.id)"
                :disabled="userInfo.role != 'USER' || !canAdopt(item.status)"
              >
                {{ adoptBtn(item.status) }}
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageParams.currentPage"
        :page-sizes="[8, 16, 24, 32]"
        :page-size="pageParams.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      >
      </el-pagination>
    </div>

    <!-- 我的领养申请对话框 -->
    <el-dialog title="我的领养申请" :visible.sync="myAdoptsVisible" width="70%">
      <el-table :data="myAdopts" v-loading="myAdoptsLoading" stripe>
        <el-table-column prop="animalName" label="宠物姓名"></el-table-column>
        <el-table-column prop="time" label="申请时间"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template slot-scope="scope">
            <el-tag :type="getTagType(scope.row.status)">{{
              scope.row.status
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="reviewComment"
          label="审核意见"
          show-overflow-tooltip
        ></el-table-column>
        <el-table-column prop="reviewTime" label="审核时间"></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import Request from '../utils/request.js'

export default {
  inject: ['userInfo'],
  data() {
    return {
      imgBaseUrl: this.HOST,
      animalData: [],
      total: 0,
      pageParams: {
        currentPage: 1,
        size: 8,
      },
      searchForm: {
        name: '',
        type: '',
      },
      adoptForm: {
        id: '',
        userId: this.userInfo.id,
        animalId: '',
      },
      myAdoptsVisible: false,
      myAdoptsLoading: false,
      myAdopts: [],
    }
  },
  created() {
    this.loadAnimal()
  },
  methods: {
    // 根据状态获取按钮文本
    adoptBtn(status) {
      const statusMap = {
        领养中: '已领养',
        待领养: '去领养',
        审核中: '审核中',
        审核不通过: '重新申请',
      }
      return statusMap[status] || '去领养'
    },

    // 根据状态判断是否可领养
    canAdopt(status) {
      return status === '待领养' || status === '审核不通过'
    },

    // 根据状态获取按钮类型
    getButtonType(status) {
      const typeMap = {
        领养中: 'info',
        待领养: 'success',
        审核中: 'warning',
        审核不通过: 'danger',
      }
      return typeMap[status] || 'success'
    },

    // 根据状态获取标签类型
    getTagType(status) {
      const typeMap = {
        领养中: 'success',
        待领养: 'info',
        审核中: 'warning',
        审核不通过: 'danger',
      }
      return typeMap[status] || 'info'
    },

    // 根据状态获取样式类
    getStatusClass(status) {
      const classMap = {
        领养中: 'adopted',
        审核中: 'pending',
        审核不通过: 'rejected',
      }
      return classMap[status] || ''
    },

    // 显示我的领养申请
    showMyAdopts() {
      this.myAdoptsLoading = true
      this.myAdoptsVisible = true

      Request.get(`/adopt/user/${this.userInfo.id}`)
        .then((res) => {
          if (res.code === '0') {
            this.myAdopts = res.data || []
          } else {
            this.$message.error(res.msg || '获取领养申请失败')
          }
          this.myAdoptsLoading = false
        })
        .catch(() => {
          this.myAdoptsLoading = false
          this.$message.error('获取领养申请失败')
        })
    },

    loadAnimal() {
      const params = {
        ...this.pageParams,
        ...this.searchForm,
      }
      Request.get('/animal/page', { params }).then((res) => {
        if (res.code === '0') {
          this.animalData = res.data.records
          this.total = res.data.total
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleAdopt(animalId) {
      this.$confirm('确定要领养这只宠物吗？', '领养确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          this.adoptForm.animalId = animalId
          Request.post('/adopt', this.adoptForm).then((res) => {
            if (res.code === '0') {
              this.$message.success('领养申请提交成功，请等待管理员审核')
              this.loadAnimal()
            } else {
              this.$message.error(res.msg)
            }
          })
        })
        .catch(() => {})
    },
    handleSearch() {
      this.pageParams.currentPage = 1
      this.loadAnimal()
    },
    resetSearch() {
      this.searchForm = {
        name: '',
        type: '',
      }
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.pageParams.size = val
      this.loadAnimal()
    },
    handleCurrentChange(val) {
      this.pageParams.currentPage = val
      this.loadAnimal()
    },
    toDetail(item) {
      this.$router.push({
        name: 'petDetail',
        params: { id: item.id },
        query: { _t: Date.now() },
      })
    },
  },
}
</script>

<style lang="less" scoped>
.adopt-container {
  min-height: 100vh;
  background-color: #f6f8fa;
  padding: 0 0 40px 0;
}

.page-header {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
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

.my-adopts {
  max-width: 1200px;
  margin: 0 auto 20px;
  padding: 0 20px;
  text-align: right;
  position: relative;
  z-index: 1;
}

.pet-grid {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.pet-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  .pet-image {
    position: relative;
    width: 100%;
    padding-top: 80%;

    .el-image {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
    }

    .pet-status {
      position: absolute;
      top: 12px;
      right: 12px;
      padding: 6px 12px;
      border-radius: 20px;
      background: #67c23a;
      color: white;
      font-size: 12px;
      font-weight: 500;
      z-index: 1;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      &.adopted {
        background: #909399;
      }

      &.pending {
        background: #e6a23c;
      }

      &.rejected {
        background: #f56c6c;
      }
    }
  }

  .pet-info {
    padding: 16px;

    .pet-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      h3 {
        margin: 0;
        font-size: 18px;
        color: #303133;
        font-weight: 600;
      }

      .pet-age {
        color: #909399;
        font-size: 14px;
      }
    }

    .pet-tags {
      margin-bottom: 12px;

      .tag {
        display: inline-block;
        padding: 4px 8px;
        margin-right: 8px;
        border-radius: 4px;
        font-size: 12px;
        color: #67c23a;
        background: #f0f9eb;
        border: 1px solid #c2e7b0;
      }
    }

    .pet-desc {
      color: #606266;
      font-size: 14px;
      line-height: 1.5;
      height: 42px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      margin-bottom: 16px;
    }

    .adopt-btn {
      width: 100%;
      font-weight: 500;
      letter-spacing: 1px;
    }
  }
}

.pagination-container {
  max-width: 1200px;
  margin: 40px auto 0;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
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

        .el-input,
        .el-select {
          width: 100%;
        }
      }
    }
  }

  .pet-grid {
    padding: 0 10px;
  }

  .my-adopts {
    padding: 0 10px;
    text-align: center;
    margin-bottom: 20px;
    position: relative;
    z-index: 10000;
  }
}
</style>
