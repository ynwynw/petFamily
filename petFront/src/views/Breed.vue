<template>
    <div class="breed-management-wrapper">
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
                    <el-button type="primary" icon="el-icon-plus" size="medium" @click="handleCreate">添加品种</el-button>
                    <el-button type="success" icon="el-icon-download" size="medium" @click="exportVisible = true">导出数据</el-button>
                </div>
            </div>
            
            <!-- 查询栏 -->
            <el-form :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="品种名称">
                    <el-input v-model="listQuery.breedName" placeholder="请输入品种名称" prefix-icon="el-icon-search" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" icon="el-icon-search" @click="onSubmit">查询</el-button>
                    <el-button icon="el-icon-refresh" @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>
            
            <!-- 表格栏 -->
            <el-table :data="tableData" @selection-change="handleSelectionChange" class="breed-table" border stripe>
                <el-table-column type="selection" width="60" align="center" />
                <el-table-column prop="breedId" label="品种ID" min-width="120" align="center" />
                <el-table-column prop="breedName" label="品种名称" min-width="180" />
                <el-table-column label="操作" width="200" align="center" fixed="right">
                    <template slot-scope="scope">
                        <el-button type="text" size="small" icon="el-icon-edit" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
                        <el-button type="text" size="small" icon="el-icon-delete" class="delete-btn" @click="handleDelete(scope.$index, scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            
            <!-- 分页栏 -->
            <div class="pagination-container">
                <Pagination :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize"
                    @pagination="fetchData" />
            </div>
            
            <!-- 新增/编辑 弹出栏 -->
            <el-dialog :title="formVisible ? '添加品种' : '修改品种信息'" :visible.sync="formVisible"
                width="500px" class="dialog-form">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="品种ID" v-if="dialogForm.breedId">
                        <el-input v-model="dialogForm.breedId" disabled></el-input>
                    </el-form-item>
                    <el-form-item label="品种名称" prop="breedName">
                        <el-input v-model="dialogForm.breedName" placeholder="请输入品种名称"></el-input>
                    </el-form-item>
                    <div class="dialog-footer">
                        <el-button @click="formVisible = false">取消</el-button>
                        <el-button type="primary" :disabled="isSubmit"
                            @click="addDialogFlag ? handleAdd('dialogForm') : handleEditSave('dialogForm')">确定</el-button>
                    </div>
                </el-form>
            </el-dialog>
            
            <!-- 导出数据 弹出栏 -->
            <el-dialog title="导出数据" :visible.sync="exportVisible" width="500px" class="export-dialog">
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
import Upload from '../components/Upload/index.vue'
import Hints from '../components/Hints/index.vue'
import Request from '../utils/request.js'
export default {
    name: 'Catetory',
    components: { Pagination, Upload, Hints },
    data() {
        return {
            // 数据列表加载动画
            listLoading: true,
            addDialogFlag:true,
            // 查询列表参数对象
            listQuery: {
                companyName: '',
                currentPage: 1,
                pageSize: 10,
            },
            // 新增/编辑提交表单对象
            dialogForm: {
                breedId: null,
                breedName: '',

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
            // 导入数据 弹出框 关闭事件
            // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
            formRules: {
                breedName: [
                    { required: true, trigger: 'blur', message: '品种名称不能为空' }
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
            this.multipleSelection = val.map(v => v.breedId)
        },
        handleCreate() {
           this. addDialogFlag=true;
            this.dialogForm = {};
            
            this.formVisible = true;
        },
        // 编辑数据
        handleEdit(index, row) {
            this. addDialogFlag=false;
            this.dialogForm = JSON.parse(JSON.stringify(row));
            this.formVisible = true;
        },

        handleDelete(index, row) {
            const delId = row.breedId;
            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/breed/" + delId).then(response => {
                    if (response.code === '0') {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        });
                        this.fetchData();
                    } else {
                        this.$message({
                            type: 'error',
                            message: '删除失败!'
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
                Request.delete(`/breed/deleteBatch?ids=${this.multipleSelection.join(',')}`).then(res => {
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
            Request.get("/breed/selectPage", {
                params: {
                    name: this.listQuery.breedName,
                    pageNum: this.listQuery.currentPage,
                    size: this.listQuery.pageSize,
                }
            }).then(response => {
                if (response.code === '0') {
                    this.total = response.data.total;
                    this.tableData = response.data.records;
                }
            }).finally(() => {
                this.listLoading = false;
            });
        },
        onSubmit() {
            this.listQuery.currentPage = 1
            this.fetchData()
        },
        onReset() {
            this.listQuery.currentPage = 1
            this.listQuery.pageSize = 10;
            this.listQuery.breedName = undefined;
            this.fetchData()
        },

        handleAdd(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.post("/breed", this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '添加成功',
                                type: 'success',
                            });
                            this.fetchData();
                        } else {
                            this.$message({
                                showClose: true,
                                message: response.msg,
                                type: 'error',
                            });
                        }
                    });
                    this.formVisible = false;
                    return true;
                } else {
                    this.isSubmit = false;
                    return false;
                }
            });
        },
        handleEditSave(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.put("/breed/" + this.dialogForm.breedId, this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '更新成功',
                                type: 'success',
                            });
                            this.formVisible = false;
                            this.fetchData();
                        } else {
                            this.$message({
                                showClose: true,
                                message: '更新失败',
                                type: 'error',
                            });
                        }
                    });
                } else {
                }
            });
        },

        exportTable(type) {
            if (this.tableData.length) {
                const params = {
                    header: ['品种ID', '品种名称'], // 更新表头
                    key: ['breedId', 'breedName'], // 更新键值
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '品种数据', // 更新文件名
                    bookType: type
                }
                excel.exportDataToExcel(params);
                this.exportVisible = false;
            }
        },
    }
}
</script>

<style lang="less">
.breed-management-wrapper {
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

    .breed-table {
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
</style>