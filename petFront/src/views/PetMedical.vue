<template>
    <div class="service-wrapper">
        <el-card shadow="always">
            <!-- 操作栏 -->
            <div class="control-btns">
                <el-popconfirm title="确认删除?" @confirm="batchDelete">
                    <template #reference>
                        <el-button type="danger" size="medium"
                            style="background-color: red;border-color: red;">批量删除</el-button>

                    </template>
                </el-popconfirm>



                <el-button type="primary" size="medium" @click="exportVisible = true"
                    style="float:right;margin-right: 10px;background-color: blue;border-color: blue;">导出数据</el-button>

            </div>
            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" label-width="90px" class="search-form">
                <el-form-item label="宠物名称">
                    <el-input v-model="listQuery.petName" placeholder="请输入宠物名称" />
                </el-form-item>
                <el-form-item v-if="this.userInfo.role!='USER'" label="用户名">
                    <el-input v-model="listQuery.username" placeholder="请输入用户名" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="onSubmit">查询</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="danger" @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 表格栏 -->
            <el-table ref="multipleTable" v-loading="listLoading" :data="tableData"
                @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="55" align="center"></el-table-column>
                <el-table-column prop="petName" label="宠物名称" show-overflow-tooltip></el-table-column>
                <el-table-column prop="username" label="用户名" show-overflow-tooltip></el-table-column>
                <el-table-column prop="date" label="服务日期" show-overflow-tooltip></el-table-column>
                <el-table-column prop="createdAt" label="申请日期" show-overflow-tooltip></el-table-column>
                <el-table-column prop="type" label="服务类型" show-overflow-tooltip></el-table-column>
                <el-table-column prop="status" label="服务状态" show-overflow-tooltip></el-table-column>
                <el-table-column label="操作" v-if="userInfo.role!='USER'" >
                    <template slot-scope="scope">
                        <el-button size="mini" v-if="scope.row.status === '未开始'"
                            @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
                        <el-button size="mini" type="danger" @click="handleDelete(scope.$index, scope.row)"
                            v-if="scope.row.status === '完成'">删除</el-button>

                        <el-button plain type="warning" size="mini" v-if="scope.row.status === '未开始'"
                            @click="handleModifyStatus(scope.$index, scope.row, '服务中')">开始服务</el-button>
                        <el-button plain type="primary" size="mini" v-else-if="scope.row.status === '服务中'"
                            @click="handleModifyStatus(scope.$index, scope.row, '完成')">服务结束</el-button>
                        <el-button plain type="success" size="mini" v-else disabled>已结束</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <!-- 分页栏 -->
            <Pagination :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize"
                @pagination="fetchData" />


            <el-dialog title="修改服务" :visible.sync="formVisibleEdit" width="30%" class="dialog-form" :modal="false">
                <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="100px">
                    <el-form-item prop="petName" label="宠物名称">
                        <el-input v-model="dialogForm.petName" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="username" label="用户名">
                        <el-input :disabled="true" v-model="dialogForm.username" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="serviceDate" label="服务日期">
                        <el-date-picker v-model="dialogForm.date" type="date" placeholder="选择日期">
                        </el-date-picker>
                    </el-form-item>
                    <el-form-item prop="serviceType" label="服务类型">
                        <el-select v-model="dialogForm.type" placeholder="请选择服务类型">
                            <el-option v-for="item in typeList" :key="item.id" :label="item.name" :value="item.name">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item prop="status" label="服务状态">
                        <el-input :disabled="true" v-model="dialogForm.status" autocomplete="off"></el-input>
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
    inject: ['userInfo'],
    name: 'MedicalServiceManager',
    components: { Pagination, Upload, Hints },
    data() {
        return {
            uploadUrl: '/api/file/upload',
            imgBaseUrl: this.HOST,
            // 数据列表加载动画
            listLoading: true,
            // 查询列表参数对象
            listQuery: {
                petName: undefined,
                username: undefined,
                currentPage: 1,
                pageSize: 10

            },
            // 新增/编辑提交表单对象
            dialogForm: {
                petName: '',
                username: '',
                date: '',
                type: '',
                status: ''
            },

            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            // 多选数据暂存数组
            multipleSelection: [],
            // 新增/编辑 弹出框显示/隐藏

            formVisibleEdit: false,

            typeList: [],
            // 导入数据 弹出框 关闭事件
            // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
            formRules: {
                petName: [
                    { required: true, message: '请输入宠物名称', trigger: 'blur' },
                ],

                date: [
                    { required: true, message: '请选择服务日期', trigger: 'blur' },
                    { type: 'date', validator: this.validateServiceDate, trigger: 'change' }
                ],
                type: [
                    { required: true, message: '请输入服务类型', trigger: 'blur' },
                ],

            },
            // 防止表单提交
            isSubmit: false,


            // 导出文件格式
            filesFormat: '.txt,.csv,.xls,.xlsx',
            // 导出数据 弹出框显示/隐藏
            exportVisible: false
        }
    },
    methods: {
        validateServiceDate(rule, value, callback) {
            if (value) {
                const serviceDate = new Date(value);
                const now = new Date();
                if (serviceDate.getTime() < now.getTime()) {
                    callback(new Error('服务日期不得早于当前日期'));
                } else {
                    callback();
                }
            } else {
                callback();
            }
        },
        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },
        // 新建数据
        handleCreate() {
            this.formVisibleAdd = true
            this.dialogForm = {}

        },
        handleModifyStatus(index, row, status) {

            var data = JSON.parse(JSON.stringify(row))
            data.status = status
            Request.put("/petMedicalService/" + data.id, data).then(res => {
                if (res.code == 0) {
                    this.$message({
                        showClose: true,
                        message: '修改成功',
                        type: 'success',
                    });
                    this.fetchData()
                } else {
                    this.$message({
                        showClose: true,
                        message: res.msg,
                        type: 'error',
                    });
                }
            })

        },

        // 编辑数据
        handleEdit(index, row) {

            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.formVisibleEdit = true
        },

        loadType() {
            Request.get('/medicalServiceType/selectAll').then(res => {
                if (res.code == 0) {
                    this.typeList = res.data
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
                Request.delete("/petMedicalService/" + row.id).then(response => {
                    if (response.code == 0) {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        })
                        this.onReset()
                    } else {
                        this.$message({
                            type: 'success',
                            error: response.msg
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
                Request.delete(`/petMedicalService/deleteBatch?ids=${this.multipleSelection.join(',')}`).then(res => {
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
            if(this.userInfo.role=='USER'){
               this.listQuery.username = this.userInfo.username;
            }
            Request.get("/petMedicalService/selectPage", {
                params: {
                    petName: this.listQuery.petName,
                    username: this.listQuery.username,
                    pageNum: this.listQuery.currentPage,
                    size: this.listQuery.pageSize,
                }
            }).then(response => {
                const data = response.data;
                if (response.code === '0') {
                    this.total = data.total
                    this.tableData = data.records
                    console.log(data.records);
                    // if(this.userInfo.role=='USER'){
                    //     this.tableData = data.records.filter(record => record.userName === this.userInfo.name);
                    // }
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
            this.listQuery.petName = "";
            this.listQuery.username = "";
            this.fetchData()
        },


        handleEditSave(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.put("/petMedicalService/" + this.dialogForm.id, this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '更新成功',
                                type: 'success',
                            });
                            this.formVisibleEdit = false
                            this.fetchData()
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
                    header: ['序号', '宠物名称', '用户名', '服务日期', '服务类型', '服务状态'],
                    key: ['id', 'petName', 'username', 'serviceDate', 'serviceType', 'status'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '服务表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            }
        },
    },
    created() {
        this.loadType();
        this.fetchData();
    },
}
</script>

<style lang='less' scoped>
.service-wrapper {
    .el-card {
        min-height: 656px;
    }

    .control-btns {
        margin-bottom: 20px;
    }

    .search-form {
        padding-bottom: 20px !important;
        margin-bottom: 15px;
        background-color: #f7f8fb;
        height: 35px !important;
        line-height: 35px !important;
    }

    .el-table thead {
        font-weight: 600;

        th {
            background-color: #f2f3f7;
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

    .upload-box,
    .export-data {
        width: 300px;
        margin: 0 auto 30px;
    }

    .upload-box {
        width: 156px;

        .files-upload {
            color: #20a0ff;
        }
    }

    .hints {
        font-size: 12px;
        color: #aaa;
        text-align: center;
    }
}

.avatar-uploader.el-upload {
    cursor: pointer;
    position: relative;
    overflow: hidden;
}

.avatar-uploader-icon {
    font-size: 28px;
    border: 1.5px dashed #d9d9d9;
    background-color: rgba(255, 255, 255, 0.479);
    border-radius: 6px;
    color: #006aff;
    width: 178px;
    height: 178px;
    line-height: 178px;
    transition: 0.5s;
    text-align: center;
    border-radius: 50%;
}

.avatar {
    width: 178px;
    height: 178px;
    display: block;
    border-radius: 50%;
}
</style>