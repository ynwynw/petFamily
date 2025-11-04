<template>
    <div class="notice-wrapper">
        <el-card shadow="always">
            <!-- 操作栏 -->
            <div class="control-btns">
                <el-popconfirm title="确认删除?" @confirm="batchDelete">
                    <template #reference>
                        <el-button type="danger" size="medium"
                            style="background-color: red;border-color: red;">批量删除</el-button>
                    </template>
                </el-popconfirm>
                <el-button type="primary" size="medium" @click="handleCreate"
                    style="float:right;background-color: blue;border-color: blue;">发布通知</el-button>

                <el-button type="primary" size="medium" @click="exportVisible = true"
                    style="float:right;margin-right: 10px;background-color: blue;border-color: blue;">导出数据</el-button>

            </div>
            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" label-width="90px" class="search-form">
                <el-form-item label="标题">
                    <el-input v-model="listQuery.title" placeholder="通知标题" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="medium" style="background-color: blue;border-color: blue;"
                        @click="onSubmit">查询</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="danger" size="medium" style="background-color: red;border-color: red;"
                        @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>
            <!-- 表格栏 -->
            <el-table ref="multipleTable" v-loading="listLoading" :data="tableData" tooltip-effect="dark"
                style="width: 100%" size="medium" @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="60" />
                <el-table-column prop="id" label="编号" align="center" width="120" sortable />
                <el-table-column prop="title" label="标题" align="center" />
                <el-table-column prop="content" label="内容" align="center" show-overflow-tooltip />
                <el-table-column prop="time" label="发布时间" align="center" />
                <el-table-column label="操作" align="center" width="200">
                    <template slot-scope="scope">
                        <el-button size="mini" :disabled="scope.row.forbid"
                            @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
                        <el-button size="mini" type="danger"
                            @click="handleDelete(scope.$index, scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <!-- 分页栏 -->
            <Pagination :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize"
                @pagination="fetchData" />
            <!-- 新增/编辑 弹出栏 -->
                <el-dialog title="添加" :visible.sync="formVisibleAdd" width="30%" class="dialog-form" :modal="false">
                <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="100px">
                    <el-form-item label="标题" prop="title">
                        <el-input v-model="dialogForm.title" />
                    </el-form-item>
                    <el-form-item label="内容" prop="content">
                        <el-input v-model="dialogForm.content" type="textarea" :autosize="{ minRows: 3, maxRows: 5 }"
                            placeholder="请输入内容" maxlength="120" show-word-limit />
                    </el-form-item>

                    <div class="footer-item">
                        <el-button @click="formVisibleAdd = false">取 消</el-button>
                        <el-button type="primary" :disabled="isSubmit" @click="handleAdd('dialogForm')">确 定</el-button>
                    </div>
                </el-form>
            </el-dialog>

            <el-dialog title="编辑" :visible.sync="formVisibleEdit" width="30%" class="dialog-form">
                <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="100px">
                    <el-form-item label="标题" prop="title">
                        <el-input v-model="dialogForm.title" />
                    </el-form-item>
                    <el-form-item label="内容" prop="content">
                        <el-input v-model="dialogForm.content" type="textarea" :autosize="{ minRows: 3, maxRows: 5 }"
                            placeholder="请输入内容" maxlength="120" show-word-limit />
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
                title: undefined,
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
            // 新增/编辑 弹出框显示/隐藏
            formVisible: false,
            formVisibleAdd: false,
            formVisibleEdit: false,
            // 导入数据 弹出框 关闭事件
            // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
            formRules: {
                title: [
                    { required: true, message: '请输入标题', trigger: 'blur' }
                ],
                content: [
                    { required: true, message: '请输入内容', trigger: 'blur' },

                ]
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
            this.dialogForm = {}

        },
        // 编辑数据
        handleEdit(index, row) {
            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.formVisibleEdit = true

        },
        // 删除数据
        handleDelete(index, row) {
            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/notice/" + row.id).then(response => {
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
                Request.delete(`/notice/deleteBatch?ids=${this.multipleSelection.join(',')}`).then(res => {
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
            Request.get("/notice/page", {
                params: {
                    title: this.listQuery.title,
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
            this.listQuery.title = "";
            this.fetchData()
        },


        handleAdd(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.post("/notice", this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '添加成功',
                                type: 'success',
                            });
                            this.fetchData()
                        } else {
                            this.$message({
                                showClose: true,
                                message: '添加失败',
                                type: 'error',
                            });
                        }

                    });
                    this.formVisibleAdd = false
                    return true;
                } else {
                    this.isSubmit = false
                    return false
                }
            })
        },
        handleEditSave(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.put("/notice/" + this.dialogForm.id, this.dialogForm).then(response => {
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
                    header: ['编号', '标题', '内容', '发布时间'],
                    key: ['id', 'title', 'content', 'time'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '通知表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            }
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
    }

    .search-form {


        padding: 10px !important;
        background-color: #f7f8fb;
        height: 40px !important;
        line-height: 40px !important;
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
</style>