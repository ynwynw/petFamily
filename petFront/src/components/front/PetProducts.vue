<template>
    <div class="pet-products">
        <el-row :gutter="24">
            <el-col :xs="24" :sm="12" :md="6" :lg="4" v-for="(product, index) in products.slice(0, 5)" :key="index">
                <div class="product-card" @click="handleProductClick(product)">
                    <div class="product-image">
                        <el-image 
                            :src="HOST + product.img" 
                            :alt="product.name"
                            fit="cover"
                        >
                            <div slot="error" class="image-slot">
                                <i class="el-icon-picture-outline"></i>
                            </div>
                        </el-image>
                        <div class="product-tag">
                            <i class="el-icon-shopping-bag-2"></i>
                        </div>
                    </div>
                    <div class="product-info">
                        <h3 class="product-name" :title="product.name">{{ product.name }}</h3>
                        <div class="product-desc">
                            <el-tag size="mini" effect="plain" type="success">{{ product.desrc }}</el-tag>
                        </div>
                        <div class="product-price">
                            <span class="price-symbol">¥</span>
                            <span class="price-value">{{ product.price }}</span>
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
                @click="$router.push({ name: 'petGoods' })"
            >
                查看更多商品
                <i class="el-icon-arrow-right"></i>
            </el-button>
        </div>
    </div>
</template>

<script>
export default {
    props: {
        products: {
            type: Array,
            default: () => []
        }
    },
    methods: {
        handleProductClick(product) {
            // 可以添加商品点击处理逻辑
            console.log('Product clicked:', product)
        }
    }
}
</script>

<style scoped>
.pet-products {
    padding: 20px 0;

    .product-card {
        border: 1px solid #f8bbd0;
        border-radius: 12px;
        overflow: hidden;
        transition: all 0.3s ease;
        background: white;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    }

    .product-card:hover {
        transform: translateY(-5px);
        box-shadow: 0 8px 25px rgba(233, 30, 99, 0.15);
        border-color: #f8bbd0;
    }

        .product-image {
            width: 100%;
            padding-top: 75%;
            position: relative;
            overflow: hidden;

            .el-image {
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                transition: transform 0.3s ease;
            }

            .image-slot {
                display: flex;
                justify-content: center;
                align-items: center;
                width: 100%;
                height: 100%;
                background: #f5f7fa;
                color: #909399;
                font-size: 30px;
            }
            
            .product-tag {
                position: absolute;
                top: 10px;
                right: 10px;
                width: 30px;
                height: 30px;
                border-radius: 50%;
                background-color: #e91e63;
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

        .product-info {
            padding: 12px;

            .product-name {
                margin: 0 0 6px;
                font-size: 14px;
                font-weight: 600;
                color: #303133;
                line-height: 1.4;
                height: 40px;
                overflow: hidden;
                text-overflow: ellipsis;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
            }

            .product-desc {
                margin-bottom: 8px;
                min-height: 20px;

                .el-tag {
                    border-radius: 12px;
                    padding: 0 10px;
                    color: #e91e63;
                    border-color: #f8bbd0;
                }
            }

            .product-price {
                display: flex;
                align-items: baseline;
                color: #e91e63;

                .price-symbol {
                    font-size: 12px;
                    margin-right: 2px;
                }

                .price-value {
                    font-size: 18px;
                    font-weight: 600;
                    color: #e91e63;
                    margin-bottom: 8px;
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
            background-color: #e91e63;
            border-color: #e91e63;
            
            i {
                margin-left: 5px;
            }

            &:hover {
                transform: translateY(-2px);
                background-color: #f48fb1;
                border-color: #f48fb1;
            }
        }
    }


/* 响应式布局 */
@media screen and (max-width: 768px) {
    .pet-products {
        padding: 10px;

        .product-card {
            .product-info {
                padding: 12px;

                .product-name {
                    font-size: 14px;
                    height: 40px;
                }

                .product-price {
                    .price-value {
                        font-size: 18px;
                    }
                }
            }
        }
    }
}
</style>