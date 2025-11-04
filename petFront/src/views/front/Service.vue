<template>
    <div class="service-page">
        <!-- 标题区域 -->
        <div class="page-header">
            <div class="header-content">
                <h1>宠物服务</h1>
                <p>专业的服务，让您的宠物生活更美好</p>
            </div>
        </div>

        <!-- 服务介绍 -->
        <div class="service-intro">
            <div class="intro-content">
                <h2>我们的服务</h2>
                <div class="service-list">
                    <div class="service-item" v-for="item in serviceIntros" :key="item.title">
                        <i :class="item.icon"></i>
                        <div class="item-content">
                            <h3>{{ item.title }}</h3>
                            <p>{{ item.desc }}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 服务卡片 -->
        <div class="service-cards">
            <el-row :gutter="30">
                <el-col :xs="24" :sm="12" :md="8" v-for="(service, index) in services" :key="index">
                    <div class="service-card" @click="applyService(service.name)">
                        <div class="card-icon">
                            <img :src="service.img" :alt="service.name">
                        </div>
                        <div class="card-content">
                            <h3>{{ service.name }}</h3>
                            <p>{{ service.description }}</p>
                            <el-button type="primary" round>
                                立即预约
                                <i class="el-icon-arrow-right"></i>
                            </el-button>
                        </div>
                    </div>
                </el-col>
            </el-row>
        </div>

        <!-- 弹窗组件 -->
        <ApplyBoardingForm ref="applyBoardingForm"></ApplyBoardingForm>
        <SubmitApply ref="submitApply"></SubmitApply>
        <AiChatDialog ref="aiChatDialog"></AiChatDialog>
        <GroomingApply ref="groomingApply"></GroomingApply>
        <PetMedicalApply ref="petMedicalApply"></PetMedicalApply>
        <TrainingApply ref="trainingApply"></TrainingApply>
    </div>
</template>

<script>
import ApplyBoardingForm from '../../components/front/fosterApply.vue'
import SubmitApply from '@/components/front/submitApply.vue'
import AiChatDialog from '@/components/front/AiChatDialog.vue'
import GroomingApply from '@/components/front/groomingApply.vue'
import PetMedicalApply from '@/components/front/petMedicalApply.vue'
import TrainingApply from '@/components/front/trainingApply.vue'

export default {
    components: { 
        ApplyBoardingForm,
        SubmitApply,
        AiChatDialog,
        GroomingApply,
        PetMedicalApply,
        TrainingApply
    },
    data() {
        return {
            serviceIntros: [
                { 
                    title: '宠物寄养服务',
                    desc: '为您的爱宠提供温馨舒适的临时之家',
                    icon: 'el-icon-house'
                },
                {
                    title: '流浪宠物上报服务',
                    desc: '帮助流浪宠物找到温暖的新家',
                    icon: 'el-icon-position'
                },
                {
                    title: '宠物AI社区',
                    desc: '与您的宠物进行智能对话互动',
                    icon: 'el-icon-chat-dot-round'
                },
                {
                    title: '宠物美容服务',
                    desc: '专业美容，让您的爱宠焕然一新',
                    icon: 'el-icon-magic-stick'
                },
                {
                    title: '宠物医疗服务',
                    desc: '专业医疗团队，守护您爱宠的健康',
                    icon: 'el-icon-first-aid-kit'
                },
                {
                    title: '宠物训练服务',
                    desc: '科学训练方法，培养宠物良好习惯',
                    icon: 'el-icon-trophy'
                }
            ],
            services: [
                { 
                    name: '宠物寄养服务',
                    description: '为您的宠物提供温馨的临时之家',
                    img: require('../../assets/icon/宠物寄养.png')
                },
                {
                    name: '流浪宠物上报服务',
                    description: '帮助流浪宠物找到新家',
                    img: require('../../assets/icon/宠物上报.png')
                },
                {
                    name: '宠物AI社区',
                    description: '智能对话，增进与宠物的感情',
                    img: require('../../assets/icon/AI.png')
                },
                {
                    name: '宠物美容服务',
                    description: '专业美容，让您的爱宠更加亮丽',
                    img: require('../../assets/icon/宠物美容.png')
                },
                {
                    name: '宠物医疗服务',
                    description: '专业医疗，守护宠物健康',
                    img: require('../../assets/icon/宠物医疗.png')
                },
                {
                    name: '宠物训练服务',
                    description: '科学训练，养成良好习惯',
                    img: require('../../assets/icon/宠物训练.png')
                }
            ]
        }
    },
    mounted() {
   
    },
    methods: {
        applyService(serviceName) {
            switch(serviceName) {
                case '宠物寄养服务':
                    this.$refs.applyBoardingForm.handleCreate()
                    break
                case '流浪宠物上报服务':
                    this.$refs.submitApply.handleCreate()
                    break
                case '宠物AI社区':
                    this.$refs.aiChatDialog.handleCreate()
                    break
                case '宠物美容服务':
                    this.$refs.groomingApply.handleCreate()
                    break
                case '宠物医疗服务':
                    this.$refs.petMedicalApply.handleCreate()
                    break
                case '宠物训练服务':
                    this.$refs.trainingApply.handleCreate()
                    break
            }
        },

    }
}
</script>

<style lang="less" scoped>
.service-page {
    min-height: 100vh;
    background-color: #f6f8fa;
}

.page-header {
    background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
    padding: 60px 0;
    color: white;
    text-align: center;

    .header-content {
        h1 {
            font-size: 42px;
            margin: 0;
            font-weight: 600;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
        }
        p {
            font-size: 18px;
            margin-top: 10px;
            opacity: 0.9;
        }
    }
}

.service-intro {
    margin: -50px auto 30px;
    max-width: 1200px;
    padding: 0 20px;

    .intro-content {
        background: white;
        border-radius: 12px;
        padding: 30px;
        box-shadow: 0 4px 20px rgba(0,0,0,0.08);

        h2 {
            text-align: center;
            color: #303133;
            margin: 0 0 30px;
            font-size: 24px;
        }
    }

    .service-list {
        display: flex;
        flex-wrap: wrap;
        gap: 15px;
        justify-content: center;

        .service-item {
            flex: 0 0 200px;
            display: flex;
            align-items: flex-start;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 8px;
            transition: all 0.3s ease;
            text-align: center;
            flex-direction: column;
            align-items: center;

            &:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0,0,0,0.05);
            }

            i {
                font-size: 24px;
                color: #67c23a;
                margin-bottom: 12px;
            }

            .item-content {
                text-align: center;

                h3 {
                    margin: 0 0 8px;
                    font-size: 18px;
                    color: #303133;
                }

                p {
                    margin: 0;
                    color: #606266;
                    font-size: 14px;
                    line-height: 1.6;
                    white-space: nowrap;
                }
            }
        }
    }
}

.service-cards {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px 40px;

    .service-card {
        background: white;
        border-radius: 12px;
        overflow: hidden;
        margin-bottom: 30px;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow: 0 2px 12px rgba(0,0,0,0.04);

        &:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 24px rgba(0,0,0,0.12);
        }

        .card-icon {
            padding: 30px;
            text-align: center;
            background: #f8f9fa;

            img {
                width: 60px;
                height: 60px;
                object-fit: contain;
            }
        }

        .card-content {
            padding: 20px;
            text-align: center;

            h3 {
                margin: 0 0 10px;
                font-size: 18px;
                color: #303133;
            }

            p {
                margin: 0 0 20px;
                color: #606266;
                font-size: 14px;
                line-height: 1.6;
            }

            .el-button {
                width: 80%;
                
                i {
                    margin-left: 5px;
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

    .service-intro {
        margin-top: -30px;
        
        .intro-content {
            padding: 20px;
        }

        .service-list .service-item {
            flex: 0 0 100%;
            
            p {
                white-space: normal;
            }
        }
    }

    .service-cards {
        padding: 0 10px 20px;
    }
}
</style>