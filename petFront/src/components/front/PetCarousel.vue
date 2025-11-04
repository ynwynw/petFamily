<template>
    <div class="carousel-container">
        <el-carousel 
            :interval="4000" 
            height="350px"
            :autoplay="true"
            indicator-position="outside"
        >
            <el-carousel-item v-for="item in breeds" :key="item.id">
                <div class="carousel-item">
                    <div class="image-wrapper">
                        <el-image 
                            :src="HOST + '/img/' + item.img" 
                            fit="cover"
                        >
                            <div slot="error" class="image-slot">
                                <i class="el-icon-picture-outline"></i>
                            </div>
                        </el-image>
                    </div>
                    <div class="content-overlay">
                        <div class="content-text">
                            <h3>{{ item.name }}</h3>
                            <p>{{ item.desrc }}</p>
                        </div>
                    </div>
                </div>
            </el-carousel-item>
        </el-carousel>
    </div>
</template>

<script>
import { Carousel, CarouselItem } from 'element-ui'

export default {
    components: { Carousel, CarouselItem },
    props: {
        breeds: Array
    }
}
</script>

<style lang="less" scoped>
.carousel-container {
    width: 100%;
    max-width: 1000px;
    margin: 0 auto 30px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);

    :deep(.el-carousel__indicators) {
        bottom: 20px;
    }
    
    :deep(.el-carousel__indicator) {
        padding: 0 6px;
        
        .el-carousel__button {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            background-color: rgba(255, 255, 255, 0.7);
        }
        
        &.is-active .el-carousel__button {
            background-color: #5aaa95;
        }
    }

    :deep(.el-carousel__item) {
        border-radius: 0;
    }

    .carousel-item {
        position: relative;
        height: 100%;
    }

    .image-wrapper {
        height: 100%;

        .el-image {
            width: 100%;
            height: 100%;

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
        }

        .image-slot {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100%;
            background: #f5f7fa;
            color: #909399;
            font-size: 30px;
        }
    }

    .content-overlay {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 25px;
        background: linear-gradient(0deg, rgba(0,0,0,0.6) 0%, rgba(0,0,0,0) 100%);
        color: white;
        display: flex;
        align-items: flex-end;
        height: 50%;
        
        .content-text {
            max-width: 800px;
            margin: 0 auto;
        }

        h3 {
            margin: 0 0 8px;
            font-size: 24px;
            font-weight: 600;
            text-shadow: 0 2px 4px rgba(0,0,0,0.3);
        }

        p {
            margin: 0;
            font-size: 16px;
            opacity: 0.9;
            text-shadow: 0 1px 2px rgba(0,0,0,0.3);
        }
    }
}

@media screen and (max-width: 768px) {
    .carousel-container {
        border-radius: 0;
        
        .content-overlay {
            padding: 20px;
            
            h3 {
                font-size: 20px;
            }

            p {
                font-size: 14px;
            }
        }
    }
}
</style>