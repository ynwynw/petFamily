<template>
    <div class="pet-showcase">
        <el-row :gutter="16">
            <el-col :span="4" v-for="(pet, index) in pets.slice(0, 5)" :key="index">
                <div class="pet-card" @click="handleClick(pet)">
                    <div class="pet-image">
                        <el-image 
                            :src="HOST + pet.img" 
                            :alt="pet.name"
                            fit="cover"
                        >
                            <div slot="error" class="image-slot">
                                <i class="el-icon-picture-outline"></i>
                            </div>
                        </el-image>
                        <div class="pet-badge">
                            <i class="el-icon-paw"></i>
                        </div>
                    </div>
                    <div class="pet-info">
                        <div class="pet-header">
                            <h3 class="pet-name">{{ pet.name }}</h3>
                            <el-tag size="mini" effect="plain" type="success">{{ pet.breed }}</el-tag>
                        </div>
                        <div class="pet-meta">
                            <span class="meta-item">
                                <i class="el-icon-location"></i>
                                {{ pet.type }}
                            </span>
                            <span class="meta-item">
                                <i class="el-icon-user"></i>
                                {{ pet.sex }}
                            </span>
                            <span class="meta-item">
                                <i class="el-icon-time"></i>
                                {{ pet.age }}岁
                            </span>
                        </div>
                    </div>
                </div>
            </el-col>
        </el-row>

        <div class="view-more">
            <el-button 
                type="success" 
                size="medium" 
                round
                @click="$router.push({ name: 'petAdoptFront' })"
            >
                查看更多宠物
                <i class="el-icon-arrow-right"></i>
            </el-button>
        </div>
    </div>
</template>

<script>
export default {
    props: {
        pets: Array
    },
    methods: {
        handleClick(pet) {
            console.log("handleClick")
            this.$router.push({ name: 'petDetail', params: { id: pet.id } })
        }
    },
}
</script>

<style scoped>
.pet-showcase {
    padding: 20px 0;

    .pet-card {
        background: white;
        border-radius: 12px;
        overflow: hidden;
        transition: all 0.3s ease;
        margin: 0 5px;
        cursor: pointer;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
        border: 1px solid rgba(220, 237, 193, 0.5);

        &:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
            border-color: #a8e6cf;

            .pet-image {
                img {
                    transform: scale(1.05);
                }
            }
            
            .pet-badge {
                background-color: #5aaa95;
            }
        }

        .pet-image {
            width: 100%;
            padding-top: 75%; /* 4:3 比例 */
            position: relative;
            overflow: hidden;

            .el-image {
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                object-fit: cover;

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                    transition: transform 0.3s ease;
                }
            }

            .image-slot {
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100%;
                background: #f5f7fa;
                color: #909399;
                font-size: 24px;
            }
            
            .pet-badge {
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

        .pet-info {
            padding: 16px;

            .pet-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 12px;

                .pet-name {
                    margin: 0;
                    font-size: 16px;
                    font-weight: 600;
                    color: #303133;
                }

                .el-tag {
                    border-radius: 12px;
                    padding: 0 10px;
                    color: #5aaa95;
                    border-color: #a8e6cf;
                }
            }

            .pet-meta {
                display: flex;
                gap: 12px;
                color: #606266;
                font-size: 13px;

                .meta-item {
                    display: flex;
                    align-items: center;
                    
                    i {
                        margin-right: 4px;
                        font-size: 14px;
                        color: #88b04b;
                    }
                }
            }
        }
    }

    .view-more {
        text-align: center;
        margin-top: 25px;

        .el-button {
            padding: 12px 25px;
            font-size: 14px;
            background-color: #5aaa95;
            border-color: #5aaa95;
            
            i {
                margin-left: 5px;
            }

            &:hover {
                transform: translateY(-2px);
                background-color: #7bbeaa;
                border-color: #7bbeaa;
            }
        }
    }
}

@media screen and (max-width: 768px) {
    .pet-showcase {
        padding: 10px;

        .pet-card {
            .pet-info {
                padding: 12px;

                .pet-name {
                    font-size: 14px;
                }

                .pet-meta {
                    font-size: 12px;
                }
            }
        }
    }
}

.adopt-btn {
  width: 100%;
  background-color: #f06292;
  border-color: #f06292;
  color: white;
  font-weight: 500;
  transition: all 0.3s ease;
}

.pet-breed {
  font-size: 16px;
  font-weight: 500;
  color: #e91e63;
  margin-bottom: 8px;
}

.pet-age {
  color: #f06292;
  font-size: 14px;
  margin-bottom: 4px;
}

.view-details-btn {
  background-color: #e91e63;
  border-color: #e91e63;
  transition: all 0.3s ease;
}

.view-details-btn:hover, .view-details-btn:focus {
  background-color: #f48fb1;
  border-color: #f48fb1;
}
</style>