<template>
  <el-dialog 
    title="宠物健康记录" 
    :visible="visible" 
    width="800px"
    custom-class="health-record-dialog"
    :close-on-click-modal="false"
    :before-close="handleClose"
  >
    <div class="health-record-content">
      <el-table 
        v-if="healthRecords.length > 0"
        :data="healthRecords" 
        border 
        stripe
        style="width: 100%"
      >
        <el-table-column prop="recordDate" label="记录日期" width="120">
          <template slot-scope="scope">
            <i class="el-icon-date"></i>
            {{ scope.row.recordDate }}
          </template>
        </el-table-column>
        
        <el-table-column prop="temperature" label="体温" width="100">
          <template slot-scope="scope">
            <el-tag size="medium" :type="getTemperatureType(scope.row.temperature)">
              {{ scope.row.temperature }}°C
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="weight" label="体重" width="100">
          <template slot-scope="scope">
            {{ scope.row.weight }}kg
          </template>
        </el-table-column>
        
        <el-table-column prop="height" label="身高" width="100">
          <template slot-scope="scope">
            {{ scope.row.height }}cm
          </template>
        </el-table-column>
        
        <el-table-column prop="vaccinationDate" label="疫苗接种日期" width="120">
          <template slot-scope="scope">
            <i class="el-icon-timer"></i>
            {{ scope.row.vaccinationDate }}
          </template>
        </el-table-column>
        
        <el-table-column prop="healthStatus" label="健康状态">
          <template slot-scope="scope">
            <el-tag :type="getHealthStatusType(scope.row.healthStatus)">
              {{ scope.row.healthStatus }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div v-else class="empty-state">
        <i class="el-icon-document"></i>
        <p>暂无健康记录</p>
      </div>
    </div>
    
    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">关 闭</el-button>
    </span>
  </el-dialog>
</template>

<script>
import Request from '../../utils/request.js'

export default {
  props: {
    petId: {
      type: Number,
      required: true,
      default: 0
    }
  },
  data() {
    return {
      healthRecords: [],
      visible: false,
    }
  },
  methods: {
    handleCreate() {
      this.visible = true
      this.fetchHealthRecords()
      document.body.style.overflow = 'auto'
    },
    handleClose() {
      this.visible = false
      this.healthRecords = []
      document.body.style.overflow = 'auto'
    },
    fetchHealthRecords() {
      Request.get(`/health-record/selectByPetId/${this.petId}`)
        .then(response => {
          if (response.code == 0) {
            this.healthRecords = response.data || []
          } else {
            this.$message.error(response.msg || '获取健康记录失败')
          }
        })
        .catch(error => {
          console.error('获取健康记录失败:', error)
          this.$message.error('获取健康记录失败')
        })
    },
    getTemperatureType(temp) {
      if (!temp) return 'info'
      const temperature = parseFloat(temp)
      if (temperature > 39.5) return 'danger'
      if (temperature < 37.5) return 'warning'
      return 'success'
    },
    getHealthStatusType(status) {
      switch (status) {
        case '健康':
          return 'success'
        case '生病':
          return 'danger'
        case '治疗中':
          return 'warning'
        default:
          return 'info'
      }
    }
  },
  beforeDestroy() {
    document.body.style.overflow = 'auto'
  }
}
</script>

<style scoped>
.health-record-content {
  padding: 10px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  background: #f8f9fa;
  border-radius: 4px;
  margin: 10px 0;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 10px;
}

.empty-state p {
  font-size: 16px;
  margin: 0;
}

:deep(.health-record-dialog) {
  border-radius: 8px;
}

:deep(.el-dialog__header) {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #f5f7fa;
  border-radius: 8px 8px 0 0;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-table) {
  border-radius: 4px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table td) {
  padding: 8px 0;
}

:deep(.el-tag) {
  font-weight: 500;
}

:deep(.el-table__empty-block) {
  min-height: 200px;
}

:deep(.el-table__row) {
  transition: background-color 0.3s;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

:deep(.el-table td, .el-table th) {
  text-align: center;
}

:deep(.el-table .cell) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-table .cell i) {
  margin-right: 5px;
  color: #409EFF;
}

@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 95% !important;
    margin: 10px auto !important;
  }
}

.dialog-footer {
  text-align: center;
  width: 100%;
  display: block;
}
</style>