<template>
    <div class="notice-wrapper">
        <el-card shadow="always">
            <!-- 操作栏 -->
            <div class="control-btns">
                <el-popconfirm title="确认删除?" @confirm="batchDelete">
                    <template #reference>
                        <el-button type="danger" size="medium">批量删除</el-button>
                    </template>
                </el-popconfirm>

                <el-button type="primary" size="medium" @click="exportVisible = true" style="float:right;margin-right: 10px;">
                    导出数据
                </el-button>
            </div>
            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" label-width="70px" class="search-form">
                <el-form-item label="订单编号">
                    <el-input v-model="listQuery.orderNo" placeholder="请输入订单编号" clearable />
                </el-form-item>
                <el-form-item label="商品名称">
                    <el-input v-model="listQuery.goodsName" placeholder="请输入商品名称" clearable />
                </el-form-item>
                <el-form-item label="用户姓名">
                    <el-input v-model="listQuery.username" placeholder="请输入用户姓名" clearable />
                </el-form-item>
                <el-form-item label="物流编号">
                    <el-input v-model="listQuery.trackingNumber" placeholder="请输入物流编号" clearable />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="onSubmit">查询</el-button>
                    <el-button @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>
            <!-- 表格栏 -->
            <el-table ref="multipleTable" v-loading="listLoading" :data="tableData" 
                :header-cell-style="{background:'#f5f7fa',color:'#606266'}"
                :cell-style="{padding:'8px 0'}" stripe border>
                <el-table-column type="selection" width="55" align="center" />
                <el-table-column prop="orderNo" label="订单编号" width="180" show-overflow-tooltip />
                <el-table-column prop="username" label="用户名" width="120" />
                <el-table-column prop="goodsName" label="商品名称" width="150" show-overflow-tooltip />
                <el-table-column prop="deliveryStatus" label="发货状态" width="100" align="center">
                    <template slot-scope="scope">
                        <el-tag :type="getStatusType(scope.row.deliveryStatus)">
                            {{ getStatusText(scope.row.deliveryStatus) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="companyName" label="物流公司" width="150" show-overflow-tooltip />
                <el-table-column prop="trackingNumber" label="快递单号" width="150" show-overflow-tooltip />
                <el-table-column prop="deliveryTime" label="发货时间" width="180" align="center" />
                <el-table-column prop="deliveryAddress" label="发货地址" min-width="200" show-overflow-tooltip />
                <el-table-column prop="recipientAddress" label="收货地址" min-width="200" show-overflow-tooltip />
                <el-table-column label="操作" width="180" fixed="right" align="center">
                    <template slot-scope="scope">
                        <el-button size="mini" type="primary" plain :disabled="scope.row.forbid"
                            @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
                        <el-button size="mini" type="danger" plain
                            @click="handleDelete(scope.$index, scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <!-- 分页栏 -->
            <Pagination :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize"
                @pagination="fetchData" />

            <el-dialog title="编辑物流信息" :visible.sync="formVisibleEdit" width="30%" class="dialog-form" :close-on-click-modal="false" :modal="false">
                <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="110px">
                    <!-- 发货状态选择器 -->
                    <!-- 发货状态显示 -->
                    <el-form-item label="发货状态">
                        <el-tag v-if="dialogForm.deliveryStatus === 'In_Ship'">已发货</el-tag>
                        <el-tag v-else-if="dialogForm.deliveryStatus === 'W_Take'">待签收</el-tag>
                        <el-tag v-else-if="dialogForm.deliveryStatus === 'Take'">已签收</el-tag>
                        <el-tag v-else>未知状态</el-tag>
                    </el-form-item>

         

                    <!-- 发货地址输入框 -->
                    <el-form-item label="发货地址" prop="deliveryAddress">
                        <el-input v-model="dialogForm.deliveryAddress" type="textarea" autosize placeholder="请输入发货地址" />
                    </el-form-item>

                    <!-- 物流公司名称选择器 -->
                    <el-form-item label="物流公司名称" prop="courierCompanyId">
                        <el-select v-model="dialogForm.courierCompanyId" placeholder="请选择物流公司">
                            <el-option v-for="company in courierCompanyList" :key="company.companyId"
                                :label="company.companyName" :value="company.companyId">
                            </el-option>
                        </el-select>
                    </el-form-item>

                    <!-- 收货信息输入区域 -->
                    <el-form-item label="收货信息" prop="recipientAddress">
                        <el-input v-model="dialogForm.recipientAddress" type="textarea" autosize
                            placeholder="请输入收货信息" />
                    </el-form-item>

                    <div class="footer-item">
                        <el-button @click="formVisibleEdit = false">取 消</el-button>
                        <el-button type="primary" :disabled="isSubmit" @click="handleEditSave('dialogForm')">确
                            定</el-button>
                    </div>
                </el-form>
            </el-dialog>



            <!-- 导出数据 弹出栏 -->
            <el-dialog title="导出数据" :visible.sync="exportVisible" width="500px">
                <div class="export-data" style="display: flex;flex-direction: row;justify-content: space-between;">
                    <el-button type="primary" @click="exportTable('xlsx')">EXCEL格式</el-button>
                    <el-button type="primary" @click="exportTable('csv')">CSV格式</el-button>
                    <el-button type="primary" @click="exportTable('txt')">TXT格式</el-button>
                </div>
                <div class="hints">TIP：请选择要导出数据的格式。</div>
            </el-dialog>
        </el-card>
    </div>
</template>

<script>
import excel from '../utils/excel.js'
import Pagination from '../components/Pagination/index.vue'
import Upload from '../components/Upload/index.vue'
import Hints from '../components/Hints/index.vue'
import Request from '../utils/request.js'
export default {
    name: 'NoticeList',
    components: { Pagination, Upload, Hints },
    data() {
        return {
            // 数据列表加载动画
            listLoading: true,
            // 查询列表参数对象
            listQuery: {
                orderNo: undefined,
                username: undefined,
                trackingNumber: undefined,
    
                goodsName: undefined,
                currentPage: 1,
                pageSize: 10
            },
            // 新增/编辑提交表单对象
            dialogForm: {
                title: undefined,
                content: undefined,
            },

            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            // 多选数据暂存数组
            multipleSelection: [],
    
            courierCompanyList: [],

            formVisibleEdit: false,
            // 导入数据 弹出框 关闭事件
            // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
            formRules: {
                deliveryStatus: [
                    { required: true, message: '请选择发货状态', trigger: 'change' }
                ],
        
                deliveryAddress: [
                    { required: true, message: '请输入发货地址', trigger: 'blur' }
                ],
                companyName: [
                    { required: true, message: '请选择物流公司名称', trigger: 'change' }
                ],
                recipientAddress: [
                    { required: true, message: '请输入收货信息', trigger: 'blur' }
                ],
                // ... 其他表单项的验证规则
            },
            // 防止多次连续提交表单
            isSubmit: false,

            // 导出文件格式
            filesFormat: '.txt, .csv, .xls, .xlsx',
            // 导出数据 弹出框显示/隐藏
            exportVisible: false
        }
    },
    created() {
        this.fetchCourierCompany();

        this.fetchData()
    },
    methods: {
        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },

        // 编辑数据
        handleEdit(index, row) {

            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.formVisibleEdit = true

        },

        fetchCourierCompany() {
            Request.get("/logisticsCompany").then(response => {
                if (response.code === '0') {
                    this.courierCompanyList = response.data
                }
            })
        },
        // 删除数据
        handleDelete(index, row) {
            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/transportation/" + row.id).then(response => {
                    if (response.code == 0) {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        })
                        this.onReset()
                    } else {
                        this.$message({
                            type: 'success',
                            errpr: '删除失败!'
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
                Request.delete(`/transportation/deleteBatch?ids=${this.multipleSelection.join(',')}`).then(res => {
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
            Request.get("/transportation/page", {
                params: {
                    orderNo: this.listQuery.orderNo,
                    username: this.listQuery.username,
                    trackingNumber: this.listQuery.trackingNumber,
 
                    goodsName: this.listQuery.goodsName,
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
            this.listQuery.orderNo = '',
                this.listQuery.username = '',
                this.listQuery.trackingNumber = '',
      
                this.listQuery.goodsName = '',
                this.fetchData()
        },
        handleEditSave(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.put("/transportation/" + this.dialogForm.id, this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '更新成功',
                                type: 'success',
                            });
                            this.fetchData()
                            this.formVisibleEdit = false
                        } else {
                            this.$message({
                                showClose: true,
                                message: response.msg,
                                type: 'error',
                            });
                        }
                    });

                } else {

                }
            });
        },

        // 导出数据--excle格式
        exportTable(type) {
            if (this.tableData.length) {
                const params = {
                    header: [  // 表头数组
                        '订单编号',
                        '用户名',
                        '商品名称',
                        '发货状态',
                 
                        '物流公司名称',
                        '快递单号',
                        '发货时间',
                        '发货地址',
                        '收货地址'
                    ],
                    key: [    // 对应的数据字段
                        'orderNo',
                        'username',
                        'goodsName',
                        'deliveryStatus',
           
                        'companyName',
                        'trackingNumber',
                        'deliveryTime',
                        'deliveryAddress',
                        'recipientAddress'
                    ],
                    data: this.tableData, // 需要导出的数据
                    autoWidth: true,     // 根据内容自动调整列宽
                    fileName: '物流信息表', // 文件名
                    bookType: type       // 导出的文件类型
                };
                excel.exportDataToExcel(params); // 导出函数调用
                this.exportVisible = false;     // 关闭导出对话框
            }
        },
        getStatusType(status) {
            const typeMap = {
                'In_Ship': 'success',
                'W_Take': 'warning',
                'Take': 'info'
            }
            return typeMap[status] || 'info'
        },
        getStatusText(status) {
            const textMap = {
                'In_Ship': '已发货',
                'W_Take': '待签收',
                'Take': '已签收'
            }
            return textMap[status] || '未知状态'
        },
    }
}
</script>

<style lang="less">
.notice-wrapper {
    .el-card {
        min-height: 656px;
    }

    .control-btns {
        margin-bottom: 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .search-form {
        padding: 15px !important;
        background-color: #f7f8fb;
        border-radius: 4px;
        margin-bottom: 20px;
        display: flex;
        flex-wrap: wrap;
        align-items: center;

        .el-form-item {
            margin-bottom: 0;
            margin-right: 15px;
        }

        .el-input {
            width: 200px;
        }
    }

    .el-table {
        margin-top: 20px;
        
        &:hover {
            box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        }

        th {
            background-color: #f5f7fa;
            color: #606266;
            font-weight: 600;
            padding: 12px 0;
        }

        td {
            padding: 8px 0;
        }

        .el-button {
            padding: 5px 10px;
            margin: 0 2px;
        }

        .el-tag {
            margin: 2px;
        }
    }

    .dialog-form {
        .el-input {
            width: 90%;
        }

        .footer-item {
            margin-top: 50px;
            text-align: right;
        }
    }

    .export-data {
        width: 300px;
        margin: 0 auto 30px;
        display: flex;
        justify-content: space-between;
    }

    .hints {
        font-size: 12px;
        color: #aaa;
        text-align: center;
        margin-top: 10px;
    }
}
</style>