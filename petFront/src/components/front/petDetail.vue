<template>
    <div class="pet-detail-container">
        <div class="pet-detail-header">
            <h1>宠物简历</h1>
            <el-button 
                type="primary" 
                style="z-index: 1000;"
                round 
                @click="$refs.medicalApply.handleCreate()"
                class="health-record-btn"
            >   
                <i class="el-icon-document"></i>
                健康记录
            </el-button>
        </div>

        <div class="pet-detail-content">
            <div class="pet-avatar-section">
                <el-avatar 
                    :size="120" 
                    :src="HOST + animal.img"
                    class="pet-avatar"
                ></el-avatar>
                <h2 class="pet-name">{{ animal.name }}</h2>
                <div class="pet-status">
                    <el-tag :type="getStatusType(animal.status)">{{ animal.status }}</el-tag>
                </div>
            </div>

            <el-descriptions 
                :column="2" 
                border 
                class="pet-info"
            >
                <el-descriptions-item label="性别">
                    <i class="el-icon-user"></i>
                    {{ animal.sex }}
                </el-descriptions-item>
                <el-descriptions-item label="年龄">
                    <i class="el-icon-time"></i>
                    {{ animal.age }}岁
                </el-descriptions-item>
                <el-descriptions-item label="种类">
                    <i class="el-icon-pet"></i>
                    {{ animal.type }}
                </el-descriptions-item>
                <el-descriptions-item label="品种">
                    {{ animal.breed }}
                </el-descriptions-item>
                <el-descriptions-item label="注册日期">
                    <i class="el-icon-date"></i>
                    {{ animal.registrationDate }}
                </el-descriptions-item>
                <el-descriptions-item label="颜色">
                    {{ animal.color }}
                </el-descriptions-item>
                <el-descriptions-item label="训练状况">
                    {{ animal.trainingLevel }}
                </el-descriptions-item>
                <el-descriptions-item label="健康状况">
                    {{ animal.sterilizationStatus }}
                </el-descriptions-item>
            </el-descriptions>

            <div class="detail-item" v-if="animal.status === '审核中' || animal.status === '审核不通过'">
                <h3><i class="el-icon-info-circle"></i> 领养状态信息</h3>
                <div class="adoption-info">
                    <el-alert
                        :title="getAdoptStatusTitle(animal.status)"
                        :type="getAdoptStatusType(animal.status)"
                        :description="getAdoptStatusDescription(animal.status)"
                        show-icon
                    >
                    </el-alert>
                    <div v-if="adoptRecord && adoptRecord.reviewComment" class="review-comment">
                        <h4>审核意见：</h4>
                        <p>{{ adoptRecord.reviewComment }}</p>
                        <p v-if="adoptRecord.reviewTime"><small>审核时间: {{ adoptRecord.reviewTime }}</small></p>
                    </div>
                </div>
            </div>

            <div class="pet-details-section">
                <div class="detail-item">
                    <h3><i class="el-icon-star-on"></i> 行为特征</h3>
                    <p>{{ animal.behaviorTraits }}</p>
                </div>
                <div class="detail-item">
                    <h3><i class="el-icon-warning-outline"></i> 特殊要求</h3>
                    <p>{{ animal.specialNeeds }}</p>
                </div>
     
            </div>
        </div>

        <HealthRecordDialog 
            ref="medicalApply" 
            :petId="Number(animal.id || $route.params.id || ($route.params.pet && $route.params.pet.id) || 0)" 
        />
    </div>
</template>

<script>
import HealthRecordDialog from './HealthRecordDialog.vue'
import Request from '../../utils/request.js'

export default {
    components: { HealthRecordDialog },
    data() {
        return {
            animal: {},
            currentPetId: null,
            adoptRecord: null
        }
    },
    created() {
        this.initPetData();
    },
    mounted() {
        // 保证页面数据更新
        if (this.$route.params.pet && this.$route.params.pet.id) {
            this.fetchPetData(this.$route.params.pet.id);
        }
    },
    watch: {
        '$route'(to) {
            // 当路由发生变化时重新获取数据
            if (to.params.pet && to.params.pet.id) {
                this.currentPetId = Number(to.params.pet.id);
                this.animal = to.params.pet;
                this.fetchPetData(this.currentPetId);
            } else if (to.params.id) {
                this.currentPetId = Number(to.params.id);
                this.fetchPetData(this.currentPetId);
            }
        }
    },
    computed: {
        petId() {
            return Number(this.animal.id || this.currentPetId || 0);
        }
    },
    methods: {
        initPetData() {
            // 优先使用路由直接传递的宠物对象
            if (this.$route.params.pet && this.$route.params.pet.id) {
                this.animal = this.$route.params.pet;
                this.currentPetId = Number(this.$route.params.pet.id);
                this.fetchAdoptRecord(this.currentPetId);
            } 
            // 其次使用路由参数中的id
            else if (this.$route.params.id) {
                this.currentPetId = Number(this.$route.params.id);
                this.fetchPetData(this.currentPetId);
            }
        },
        getStatusType(status) {
            switch(status) {
                case '健康':
                case '领养中':
                    return 'success';
                case '生病':
                case '审核不通过':
                    return 'danger';
                case '治疗中':
                case '审核中':
                    return 'warning';
                default:
                    return 'info';
            }
        },
        getAdoptStatusTitle(status) {
            if (status === '审核中') {
                return '您的领养申请正在审核中';
            } else if (status === '审核不通过') {
                return '您的领养申请未通过审核';
            }
            return '';
        },
        getAdoptStatusType(status) {
            if (status === '审核中') {
                return 'warning';
            } else if (status === '审核不通过') {
                return 'error';
            }
            return 'info';
        },
        getAdoptStatusDescription(status) {
            if (status === '审核中') {
                return '请耐心等待工作人员审核您的领养申请，我们会尽快处理。';
            } else if (status === '审核不通过') {
                return '很遗憾，您的领养申请未能通过审核，您可以重新提交申请或选择其他宠物。';
            }
            return '';
        },
        fetchAdoptRecord(petId) {
            if (!petId) return;
            
            const params = { animalId: petId };
            Request.get('/adopt/selectAll', { params })
              .then(response => {
                  if (response.code === '0' && response.data && response.data.length > 0) {
                      this.adoptRecord = response.data[0];
                  }
              })
              .catch(error => {
                  console.error('获取领养记录失败:', error);
              });
        },
        fetchPetData(petId) {
            if (!petId) return;
            
            Request.get(`/animal/selectById/${petId}`).then(response => {
                if (response.code === '0' && response.data) {
                    this.animal = response.data;
                    this.fetchAdoptRecord(petId);
                }
            }).catch(error => {
                console.error('Error loading pet data:', error);
                this.$message.error('获取宠物信息失败');
            });
        }
    }
}
</script>

<style scoped>
.body {
    overflow: scroll !important;
}  
.pet-detail-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    min-height: 100vh;
    overflow-y: auto;
}

.pet-detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
}

.pet-detail-header h1 {
    font-size: 32px;
    color: #303133;
    margin: 0;
    font-weight: 600;
}

.health-record-btn {
    font-size: 16px;
}

.pet-detail-content {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
    padding: 30px;
}

.pet-avatar-section {
    text-align: center;
    margin-bottom: 30px;
}

.pet-avatar {
    border: 4px solid #fff;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.pet-name {
    margin: 15px 0 10px;
    font-size: 24px;
    color: #303133;
}

.pet-status {
    margin-bottom: 20px;
}

.pet-info {
    margin-bottom: 30px;
}

::v-deep .el-descriptions-item__label {
    background-color: #f5f7fa;
    color: #606266;
    font-weight: bold;
}

.pet-details-section {
    margin-top: 30px;
}

.detail-item {
    margin-bottom: 25px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
}

.detail-item h3 {
    color: #303133;
    font-size: 18px;
    margin: 0 0 10px;
    display: flex;
    align-items: center;
}

.detail-item h3 i {
    margin-right: 8px;
    color: #409EFF;
}

.detail-item p {
    margin: 0;
    color: #606266;
    line-height: 1.6;
}

::v-deep .el-descriptions {
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
}

::v-deep .el-descriptions-item__content {
    display: flex;
    align-items: center;
}

::v-deep .el-descriptions-item__content i {
    margin-right: 8px;
    color: #409EFF;
}

.adoption-info {
    margin-top: 15px;
}

.review-comment {
    margin-top: 15px;
    padding: 15px;
    background: #fff;
    border-radius: 5px;
    border-left: 3px solid #e6a23c;
}

.review-comment h4 {
    margin: 0 0 10px;
    color: #606266;
    font-size: 16px;
}

.review-comment p {
    margin: 0 0 10px;
}

.review-comment small {
    color: #909399;
}

@media (max-width: 768px) {
    .pet-detail-container {
        padding: 10px;
    }

    .pet-detail-content {
        padding: 15px;
    }

    ::v-deep .el-descriptions-item__label {
        width: 100px;
    }
}
</style>