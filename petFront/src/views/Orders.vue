<template>
    <div class="orders-wrapper">
        <el-card shadow="hover" class="main-card">
            <!-- 操作栏 -->
            <div class="action-bar">
                <div class="left-actions">
                    <el-popconfirm title="确认删除?" @confirm="batchDelete">
                        <template #reference>
                            <el-button type="danger" icon="el-icon-delete" size="medium">批量删除</el-button>
                        </template>
                    </el-popconfirm>
                </div>
                <div class="right-actions">
                    <el-button type="success" icon="el-icon-download" size="medium" @click="exportVisible = true">导出数据</el-button>
                </div>
            </div>

            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" label-width="90px" class="search-form">
                <el-form-item label="订单号">
                    <el-input v-model="listQuery.orderNo" placeholder="订单号" prefix-icon="el-icon-document" />
                </el-form-item>
                <el-form-item label="用户名">
                    <el-input v-model="listQuery.username" placeholder="用户名" prefix-icon="el-icon-user" />
                </el-form-item>
                <el-form-item label="商品名称">
                    <el-input v-model="listQuery.goodsName" placeholder="商品名称" prefix-icon="el-icon-goods" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" icon="el-icon-search" @click="onSubmit">查询</el-button>
                    <el-button icon="el-icon-refresh" @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 表格栏 -->
            <el-table ref="multipleTable" v-loading="listLoading" :data="tableData" tooltip-effect="dark"
                class="orders-table" border stripe @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="60" align="center" />
                <el-table-column prop="orderNo" label="订单号" align="center" sortable min-width="120" />
                <el-table-column prop="username" label="用户名" align="center" min-width="100" />
                <el-table-column prop="goodsName" label="商品名称" align="center" min-width="150" show-overflow-tooltip />
                <el-table-column prop="num" label="商品数量" align="center" width="100" />
                <el-table-column prop="totalAmount" label="总金额" align="center" width="100" />
                <el-table-column prop="status" label="订单状态" align="center" width="120">
                    <template slot-scope="scope">
                        <el-tag :type="statusType(scope.row.status)" size="small">{{ statusText(scope.row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="time" label="时间" align="center" min-width="120" />
                <el-table-column label="操作" align="center" width="200" fixed="right">
                    <template slot-scope="scope">
                        <el-button v-if="userInfo.role != 'USER'" type="text" size="small" icon="el-icon-delete" class="delete-btn"
                            @click="handleDelete(scope.$index, scope.row)">删除</el-button>
                        <el-button size="small" v-if="isShowUpdateButton(scope.row.status)" type="text"
                            :type="updateStatusButtonType(scope.row.status)" 
                            @click="handleUpdateStatus(scope.row)">
                            <i :class="getStatusActionIcon(scope.row.status)"></i>
                            {{ updateStatusButtonName(scope.row.status) }}
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页栏 -->
            <div class="pagination-container">
                <Pagination :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize"
                    @pagination="fetchData" />
            </div>

            <!-- 导出数据 弹出栏 -->
            <el-dialog title="导出数据" :visible.sync="exportVisible" width="500px" class="export-dialog" :modal="false">
                <div class="export-options">
                    <el-button type="primary" icon="el-icon-document" @click="exportTable('xlsx')">EXCEL格式</el-button>
                    <el-button type="success" icon="el-icon-document" @click="exportTable('csv')">CSV格式</el-button>
                    <el-button type="info" icon="el-icon-document" @click="exportTable('txt')">TXT格式</el-button>
                </div>
                <div class="export-tip">请选择要导出数据的格式</div>
            </el-dialog>
        </el-card>
    </div>
</template>

<script>
import excel from '../utils/excel.js'
import Pagination from '../components/Pagination/index.vue'
import Request from '../utils/request.js'


export default {
    name: 'Orders',
    inject: ['userInfo'],
    components: { Pagination },
    data() {
        return {

            // 数据列表加载动画
            listLoading: true,
            // 查询列表参数对象
            listQuery: {
                orderNo: undefined,
                username: undefined,
                goodsName: undefined,
                currentPage: 1,
                pageSize: 10
            },

            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            // 多选数据暂存数组
            multipleSelection: [],
            // 导出数据 弹出框显示/隐藏
            exportVisible: false
        }
    },
    created() {
        this.fetchData()
    },
    methods: {
        getStatusActionIcon(status) {
            switch(status) {
                case 'W_Pay': return 'el-icon-wallet';
                case 'W_Ship': return 'el-icon-box';
                case 'W_Pickup': return 'el-icon-box';
                default: return 'el-icon-check';
            }
        },
        statusType(status) {
            const statusMap = {
                'W_Pay': 'danger',     // 待支付
                'W_Ship': 'warning',   // 待发货
                'W_Pickup': 'primary', // 待自提
                'Completed': 'success' // 已完成
            };
            return statusMap[status] || 'info'; // 默认为info类型
        },

        statusText(status) {
            const statusTextMap = {
                'W_Pay': '待支付',
                'W_Ship': '待发货',
                'W_Pickup': '待自提',
                'Completed': '已完成'
            };
            return statusTextMap[status] || '未知状态'; // 默认为未知状态
        },
        isShowUpdateButton(currentStatus) {
            if (this.userInfo.role === 'USER') {
                if (currentStatus == 'W_Pay' || currentStatus == 'W_Pickup') {
                    return true;
                }
            }
            else {
                if (currentStatus === 'W_Ship') {
                    return true;
                }
            }
        },
        handleUpdateStatus(order) {
            var updateMsg;
            const currentStatus = order.status;
            switch (currentStatus) {
                case 'W_Pay':
                    this.handlePay(order.orderNo);
                    break
                case 'W_Ship':
                    // 修改为待自提状态
                    order.status = 'W_Pickup'
                    updateMsg = "货品已准备好，等待用户自提"
                    break
                case 'W_Pickup':
                    // 用户确认自提
                    this.handleConfirmPickup(order.orderNo);
                    break
                default:
                    updateMsg = ""
                    this.$message.error('非法操作!')
                    break
            }
            if (currentStatus != 'W_Pay' && currentStatus != 'W_Pickup' && !this.handleEditSave(order, updateMsg)) {
                order.status = currentStatus;
            }
            this.fetchData()
        },
        handlePay(orderNo){
            // 打开支付选择对话框
            this.$confirm('请选择支付方式', '支付选择', {
                confirmButtonText: '支付宝支付',
                cancelButtonText: '余额支付',
                distinguishCancelAndClose: true,
                closeOnClickModal: false,
                center: true,
                type: 'info',
                customClass: 'payment-dialog',
                roundButton: true
            }).then(() => {
                // 支付宝支付
                window.open('http://localhost:8889/alipay/pay?orderNo=' + orderNo);
            }).catch(action => {
                if (action === 'cancel') {
                    // 余额支付
                    this.handleBalancePay(orderNo);
                }
            });
        },
        
        // 处理余额支付
        handleBalancePay(orderNo) {
            // 显示加载中状态
            const loadingInstance = this.$loading({
                lock: true,
                text: '正在处理支付请求...',
                spinner: 'el-icon-loading',
                background: 'rgba(255, 255, 255, 0.7)'
            });
            
            // 获取订单信息
            Request.get('/orders/getByOrderNo/' + orderNo).then(response => {
                if (response.code === '0') {
                    const order = response.data;
                    
                    // 确认用户余额是否足够
                    if (this.userInfo.account < order.totalAmount) {
                        loadingInstance.close(); // 关闭加载
                        this.$message({
                            type: 'error',
                            message: '您的余额不足，请充值或选择其他支付方式',
                            showClose: true,
                            duration: 3000
                        });
                        return;
                    }
                    
                    // 调用后端接口进行余额支付
                    Request.post('/orders/balancePay', {
                        orderNo: orderNo,
                        userId: this.userInfo.id
                    }).then(res => {
                        loadingInstance.close(); // 关闭加载
                        if (res.code === '0') {
                            this.$message({
                                type: 'success',
                                message: '支付成功，您的余额已扣除',
                                showClose: true,
                                duration: 3000
                            });
                            // 更新用户信息中的余额
                            this.userInfo.account -= order.totalAmount;
                            this.fetchData(); // 刷新订单列表
                        } else {
                            this.$message({
                                type: 'error',
                                message: res.msg || '支付失败，请稍后再试',
                                showClose: true,
                                duration: 3000
                            });
                        }
                    }).catch(err => {
                        loadingInstance.close(); // 关闭加载
                        console.error(err);
                        this.$message({
                            type: 'error',
                            message: '支付出错，请稍后再试',
                            showClose: true,
                            duration: 3000
                        });
                    });
                } else {
                    loadingInstance.close(); // 关闭加载
                    this.$message({
                        type: 'error',
                        message: '获取订单信息失败',
                        showClose: true,
                        duration: 3000
                    });
                }
            }).catch(error => {
                loadingInstance.close(); // 关闭加载
                console.error(error);
                this.$message({
                    type: 'error',
                    message: '获取订单信息失败',
                    showClose: true,
                    duration: 3000
                });
            });
        },
        updateStatusButtonName(currentStatus) {
            switch (currentStatus) {
                case 'W_Pay': return "支付"; break;
                case 'W_Ship': return "确认备货完成"; break;
                case 'W_Pickup': return "确认自提"; break;
                default: return "订单异常";
            }
        },
        updateStatusButtonType(currentStatus) {
            switch (currentStatus) {
                case 'W_Pay': return "warning"; break;
                case 'W_Ship': return "warning"; break;
                case 'W_Pickup': return "primary"; break;
                case 'Completed': return "success"; break;
                default: return "danger";
            }
        },
        handleSelectionChange(val) {
            this.multipleSelection = val.map(item => item.id)
        },

        // 删除数据
        handleDelete(index, row) {
            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/orders/" + row.id).then(response => {
                    if (response.code == '0') {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        })

                        this.onReset()
                    } else {
                        this.$message({
                            type: 'error',
                            message: response.msg
                        })
                    }
                });

            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                })
            })
        },
        // 批量删除
        batchDelete() {
            if (this.multipleSelection.length < 1) {
                this.$message({
                    message: '请先选择要删除的数据！',
                    type: 'warning'
                })
            } else {
                Request.delete(`/orders/deleteBatch?ids=${this.multipleSelection.join(',')}`).then(res => {
                    if (res.code === '0') {
                        this.$message({
                            showClose: true,
                            message: '批量删除成功',
                            type: 'success'
                        });

                        this.onReset()
                    }
                    else {
                        this.$message({
                            showClose: true,
                            message: res.msg,
                            type: 'error',
                        });
                    }
                })

            }
        },
        
        // 获取数据列表
        fetchData() {
            this.listLoading = true
            Request.get("/orders/page", {
                params: {
                    orderNo: this.listQuery.orderNo,
                    username: this.listQuery.username,
                    goodsName: this.listQuery.goodsName,
                    userRole: this.userInfo.role,
                    userId: this.userInfo.id,
                    currentPage: this.listQuery.currentPage,
                    size: this.listQuery.pageSize,

                }
            }).then(response => {
                const data = response.data;
                if (response.code === '0') {
                    this.total = data.total
                    this.tableData = data.records
                }

            })
            this.listLoading = false

        },
        // 查询数据
        onSubmit() {
            this.listQuery.currentPage = 1
            this.fetchData()
        },
        onReset() {
            this.listQuery.currentPage = 1
            this.listQuery.pageSize = 10;
            this.listQuery.goodsName = ''
            this.listQuery.username = ''
            this.listQuery.orderNo = ''
            this.fetchData()
        },

        handleEditSave(order, updateMsg) {
            Request.put("/orders/" + order.id, order).then(response => {
                if (response.code == 0) {
                    this.$message({
                        showClose: true,
                        message: updateMsg,
                        type: 'success',
                    });
                    this.fetchData()
                    return true;

                } else {
                    this.$message({
                        showClose: true,
                        message: response.msg,
                        type: 'error',
                    });
                    this.fetchData()
                    return false;
                }
            });


        },

        // 导出数据--excle格式
        exportTable(type) {
            if (this.tableData.length) {
                const params = {
                    header: ['编号', '订单号', '用户名', '商品名称', '商品ID', '用户ID', '商品数量', '订单状态', '时间'],
                    key: ['id', 'orderNo', 'username', 'goodsName', 'goodsId', 'userId', 'num', 'status', 'time'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '订单数据',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            }
        },
        
        handleSizeChange(val) {
            this.listQuery.pageSize = val
            this.fetchData()
        },
        
        handleCurrentChange(val) {
            this.listQuery.currentPage = val
            this.fetchData()
        },

        // 处理用户确认自提
        handleConfirmPickup(orderNo) {
            this.$confirm('确认已自提商品?', '确认自提', {
                confirmButtonText: '确认',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                // 显示加载中状态
                const loadingInstance = this.$loading({
                    lock: true,
                    text: '正在处理自提确认...',
                    spinner: 'el-icon-loading',
                    background: 'rgba(255, 255, 255, 0.7)'
                });
                
                // 调用后端接口确认自提
                Request.post('/orders/confirmPickup', {
                    orderNo: orderNo,
                    userId: this.userInfo.id
                }).then(res => {
                    loadingInstance.close(); // 关闭加载
                    if (res.code === '0') {
                        this.$message({
                            type: 'success',
                            message: '确认自提成功，订单已完成',
                            showClose: true,
                            duration: 3000
                        });
                        this.fetchData(); // 刷新订单列表
                    } else {
                        this.$message({
                            type: 'error',
                            message: res.msg || '确认自提失败，请稍后再试',
                            showClose: true,
                            duration: 3000
                        });
                    }
                }).catch(err => {
                    loadingInstance.close(); // 关闭加载
                    console.error(err);
                    this.$message({
                        type: 'error',
                        message: '确认自提出错，请稍后再试',
                        showClose: true,
                        duration: 3000
                    });
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消确认'
                });
            });
        },
    }
}
</script>
<style lang="less">
.orders-wrapper {
    padding: 20px;
    background-color: #ffffff;
    min-height: 100vh;

    .main-card {
        border-radius: 8px;
        background-color: #ffffff;
        min-height: 656px;
    }

    .action-bar {
        display: flex;
        justify-content: space-between;
        margin-bottom: 20px;

        .right-actions {
            .el-button {
                margin-left: 10px;
            }
        }
    }

    .search-form {
        background-color: #f7f8fb;
        padding: 20px;
        border-radius: 8px;
        margin-bottom: 20px;
        box-shadow: 0 2px 12px 0 rgba(0,0,0,0.03);

        .el-form-item {
            margin-bottom: 0;
        }
    }

    .orders-table {
        margin-bottom: 20px;
        background-color: #ffffff;

        .el-table__header-wrapper {
            th {
                background-color: #f2f3f7;
                color: #606266;
                font-weight: 600;
            }
        }

        .delete-btn {
            color: #F56C6C;
        }
    }

    .pagination-container {
        text-align: right;
        margin-top: 20px;
    }

    .dialog-form {
        .el-input {
            width: 100%;
        }

        .dialog-footer {
            margin-top: 30px;
            text-align: right;
        }
    }

    .export-dialog {
        .export-options {
            display: flex;
            justify-content: space-around;
            margin-bottom: 20px;
        }

        .export-tip {
            text-align: center;
            color: #909399;
            font-size: 12px;
        }
    }

    .hints {
        font-size: 12px;
        color: #aaa;
        text-align: center;
    }
}

/* 支付对话框样式 */
.payment-dialog {
    background-color: white !important;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 5px 15px rgba(0,0,0,0.2);
    
    .el-message-box__header {
        padding: 20px 20px 10px;
        background-color: #f8f9fa;
        border-bottom: 1px solid #eee;
    }
    
    .el-message-box__content {
        padding: 30px 20px;
        font-size: 16px;
    }
    
    .el-message-box__message p {
        margin: 0;
        font-weight: 500;
        color: #333;
    }
    
    .el-message-box__btns {
        padding: 15px 20px 20px;
        text-align: center;
        
        .el-button {
            margin: 0 10px;
            padding: 12px 25px;
            font-size: 14px;
            border-radius: 25px;
            
            &:first-child {
                background-color: #67c23a;
                border-color: #67c23a;
                color: white;
                
                &:hover {
                    background-color: #85ce61;
                    border-color: #85ce61;
                }
            }
            
            &:last-child {
                background-color: #409eff;
                border-color: #409eff;
                color: white;
                
                &:hover {
                    background-color: #66b1ff;
                    border-color: #66b1ff;
                }
            }
        }
    }
}

/* 修复对话框变暗问题 */
.v-modal {
    opacity: 0.6 !important;
}
</style>