<template>
    <div class="room-manager">
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
                    <el-button type="primary" icon="el-icon-plus" size="medium" @click="handleCreate">添加房间</el-button>
                    <el-button type="success" icon="el-icon-download" size="medium" @click="exportVisible = true">导出数据</el-button>
                </div>
            </div>

            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="房间编号">
                    <el-input v-model="listQuery.name" placeholder="请输入房间编号" prefix-icon="el-icon-search" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" icon="el-icon-search" @click="onSubmit">查询</el-button>
                    <el-button icon="el-icon-refresh" @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 表格栏 -->
            <el-table ref="multipleTable" v-loading="listLoading" :data="tableData" @selection-change="handleSelectionChange" 
                class="room-table" border stripe>
                <el-table-column type="selection" width="55" align="center" />
                <el-table-column prop="id" label="序号" width="80" align="center" sortable />
                <el-table-column prop="name" label="房间编号" min-width="120" />
                <el-table-column prop="status" label="房间状态" width="120" align="center">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.status === '空闲' ? 'success' : 'warning'" size="small">
                            {{ scope.row.status }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="animal" label="寄养宠物" min-width="120" />
                <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template slot-scope="scope">
                        <el-button type="text" size="small" icon="el-icon-edit" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
                        <el-button type="text" size="small" icon="el-icon-delete" class="delete-btn"
                            @click="handleDelete(scope.$index, scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页栏 -->
            <div class="pagination-container">
                <Pagination 
                    :total="total" 
                    :page.sync="listQuery.currentPage"
                    :limit.sync="listQuery.pageSize"
                    @pagination="fetchData" 
                />
            </div>
        </el-card>

        <!-- 新增对话框 -->
        <el-dialog title="添加房间" :visible.sync="formVisibleAdd" width="500px" class="room-dialog" :close-on-click-modal="false" :modal="false">
            <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="100px" class="dialog-form">
                <el-form-item prop="name" label="房间编号">
                    <el-input v-model="dialogForm.name" placeholder="请输入房间编号" />
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="formVisibleAdd = false">取 消</el-button>
                <el-button type="primary" :disabled="isSubmit" @click="handleAdd('dialogForm')">确 定</el-button>
            </div>
        </el-dialog>

        <!-- 编辑对话框 -->
        <el-dialog title="修改房间信息" :visible.sync="formVisibleEdit" width="500px" class="room-dialog" :close-on-click-modal="false">
            <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="100px" class="dialog-form">
                <el-form-item prop="name" label="房间编号">
                    <el-input v-model="dialogForm.name" placeholder="请输入房间编号" />
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="formVisibleEdit = false">取 消</el-button>
                <el-button type="primary" :disabled="isSubmit" @click="handleEditSave('dialogForm')">确 定</el-button>
            </div>
        </el-dialog>

        <!-- 导出数据对话框 -->
        <el-dialog title="导出数据" :visible.sync="exportVisible" width="500px" class="export-dialog">
            <div class="export-options">
                <el-button type="primary" icon="el-icon-document" @click="exportTable('xlsx')">EXCEL格式</el-button>
                <el-button type="success" icon="el-icon-document" @click="exportTable('csv')">CSV格式</el-button>
                <el-button type="info" icon="el-icon-document" @click="exportTable('txt')">TXT格式</el-button>
            </div>
            <div class="export-tip">请选择要导出数据的格式</div>
        </el-dialog>
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
    name: 'Room',
    components: { Pagination, Upload, Hints },
    data() {
        return {
            uploadUrl: '/api/file/upload',
            imgBaseUrl: this.HOST,
            // 数据列表加载动画
            listLoading: true,
            // 查询列表参数对象
            listQuery: {
                name: undefined,
                currentPage: 1,
                pageSize: 10
            },
            // 新增/编辑提交表单对象
            dialogForm: {
                name: ''
            },
            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            // 多选数据暂存数组
            multipleSelection: [],
            // 新增/编辑 弹出框显示/隐藏
            formVisible: false,
            formVisibleAdd: false,
            formVisibleEdit: false,
            categoryList: [],
            // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
            formRules: {
                name: [
                    { required: true, message: '请输入房间编号', trigger: 'blur' },
                ],
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
        this.fetchData()
    },
    methods: {
        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },
        // 新建数据
        handleCreate() {
            this.formVisibleAdd = true
            this.dialogForm = {
                name: ''
            }
        },
        // 编辑数据
        handleEdit(index, row) {
            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.formVisibleEdit = true
        },
        handleAdd(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    this.isSubmit = true;
                    Request.post("/room", this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '房间添加成功',
                                type: 'success',
                            });
                            this.formVisibleAdd = false;
                            this.fetchData();
                        } else {
                            this.$message({
                                showClose: true,
                                message: response.msg,
                                type: 'error',
                            });
                        }
                        this.isSubmit = false;
                    }).catch(() => {
                        this.isSubmit = false;
                    });
                    return true;
                } else {
                    this.isSubmit = false;
                    return false;
                }
            });
        },
        // 删除数据
        handleDelete(index, row) {
            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/room/" + row.id).then(response => {
                    if (response.code == 0) {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        });
                        this.onReset();
                    } else {
                        this.$message({
                            type: 'error',
                            message: response.msg || '删除失败!'
                        });
                    }
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });
        },
        // 批量删除
        batchDelete() {
            if (this.multipleSelection.length < 1) {
                this.$message({
                    message: '请先选择要删除的数据！',
                    type: 'warning'
                });
            } else {
                Request.delete(`/room/deleteBatch?ids=${this.multipleSelection.join(',')}`).then(res => {
                    if (res.code === '0') {
                        this.$message({
                            showClose: true,
                            message: '批量删除成功',
                            type: 'success'
                        });
                        this.onReset();
                    } else {
                        this.$message({
                            showClose: true,
                            message: res.msg,
                            type: 'error',
                        });
                    }
                });
            }
        },
        // 获取数据列表
        fetchData() {
            this.listLoading = true;
            Request.get("/room/selectPage", {
                params: {
                    name: this.listQuery.name,
                    pageNum: this.listQuery.currentPage,
                    size: this.listQuery.pageSize,
                }
            }).then(response => {
                const data = response.data;
                if (response.code === '0') {
                    this.total = data.total;
                    this.tableData = data.records;
                }
                this.listLoading = false;
            }).catch(() => {
                this.listLoading = false;
            });
        },
        // 查询数据
        onSubmit() {
            this.listQuery.currentPage = 1;
            this.fetchData();
        },
        onReset() {
            this.listQuery.currentPage = 1;
            this.listQuery.pageSize = 10;
            this.listQuery.name = "";
            this.fetchData();
        },
        handleEditSave(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    this.isSubmit = true;
                    Request.put("/room/" + this.dialogForm.id, this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '更新成功',
                                type: 'success',
                            });
                            this.formVisibleEdit = false;
                            this.fetchData();
                        } else {
                            this.$message({
                                showClose: true,
                                message: response.msg || '更新失败',
                                type: 'error',
                            });
                        }
                        this.isSubmit = false;
                    }).catch(() => {
                        this.isSubmit = false;
                    });
                } else {
                    this.isSubmit = false;
                }
            });
        },
        // 导出数据--excle格式
        exportTable(type) {
            if (this.tableData.length) {
                const params = {
                    header: ['序号', '房间编号', '房间状态', '寄养宠物'],
                    key: ['id', 'name', 'status', 'animal'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '房间信息表',
                    bookType: type
                };
                excel.exportDataToExcel(params);
                this.exportVisible = false;
            }
        },
    }
}
</script>

<style lang="less">
.room-manager {
    padding: 20px;
    background-color: #ffffff;
    min-height: 100vh;

    .main-card {
        border-radius: 8px;
        background-color: #ffffff;
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
        background-color: #ffffff;
        padding: 20px;
        border-radius: 8px;
        margin-bottom: 20px;
        box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);

        .el-form-item {
            margin-bottom: 0;
        }
    }

    .room-table {
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

    .room-dialog {
        .dialog-form {
            .el-input {
                width: 100%;
            }
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
}
</style>