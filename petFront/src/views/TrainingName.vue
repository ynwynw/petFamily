<template>
    <div class="category-wrapper">
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
                    style="float:right;background-color: blue;border-color: blue;">添加训练名称</el-button>

                <el-button type="primary" size="medium" @click="exportVisible = true"
                    style="float:right;margin-right: 10px;background-color: blue;border-color: blue;">导出数据</el-button>

            </div>
            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" label-width="90px" class="search-form">
                <el-form-item label="训练名称">
                    <el-input v-model="listQuery.name" placeholder="请输入训练名称" />
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
                <el-table-column prop="id" label="序号" width="80" align="center" sortable></el-table-column>
                <el-table-column prop="name" label="训练名称" show-overflow-tooltip></el-table-column>
                <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
                <el-table-column label="操作" width="200">
                    <template slot-scope="scope">
                        <el-button size="mini" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
                        <el-button size="mini" type="danger" @click="handleDelete(scope.$index, scope.row)"
                            v-if="scope.row.role!== 'SUPER_ADMIN'">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <!-- 分页栏 -->
            <Pagination :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize"
                @pagination="fetchData" />
            <!-- 新增/编辑 弹出栏 -->
            <el-dialog title="添加训练名称" :visible.sync="formVisibleAdd" width="30%" class="dialog-form" :modal="false">

                <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="100px">
                    <el-form-item prop="name" label="训练名称">
                        <el-input v-model="dialogForm.name" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="description" label="训练描述">
                        <el-input type="textarea" v-model="dialogForm.description" autocomplete="off" resize="false"></el-input>
                    </el-form-item>
                    <div class="footer-item">
                        <el-button @click="formVisibleAdd = false">取 消</el-button>
                        <el-button type="primary" :disabled="isSubmit" @click="handleAdd('dialogForm')">确 定</el-button>
                    </div>
                </el-form>
            </el-dialog>

            <el-dialog title="修改训练名称" :visible.sync="formVisibleEdit" width="30%" class="dialog-form">
                <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="100px">
                    <el-form-item prop="name" label="训练名称">
                        <el-input v-model="dialogForm.name" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="description" label="训练描述">
                        <el-input type="textarea" v-model="dialogForm.description" autocomplete="off" resize="false"></el-input>
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
    name: 'TrainingNameManager',
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
                name: '',
                description:''
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
            // 导入数据 弹出框 关闭事件
            // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
            formRules: {
                name: [
                    { required: true, message: '请输入训练名称', trigger: 'blur' },
                ],
            },
            // 防止多次连续提交表单
            isSubmit: false,

            // 导出文件格式
            filesFormat: '.txt,.csv,.xls,.xlsx',
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
        handleAdd(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.post("/trainingName", this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '训练名称添加成功',
                                type:'success',
                            });
                            this.fetchData()
                        } else {
                            this.$message({
                                showClose: true,
                                message: response.msg,
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
        // 删除数据
        handleDelete(index, row) {
            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/trainingName/" + row.id).then(response => {
                    if (response.code == 0) {
                        this.$message({
                            type:'success',
                            message: '删除成功!'
                        })
                        this.onReset()
                    } else {
                        this.$message({
                            type:'success',
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
                Request.delete(`/trainingName/deleteBatch?ids=${this.multipleSelection.join(',')}`).then(res => {
                    if (res.code === '0') {
                        this.$message({
                            showClose: true,
                            message: '批量删除成功',
                            type:'success'
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
            Request.get("/trainingName/selectPage", {
                params: {
                    name: this.listQuery.name,
                    pageNum: this.listQuery.currentPage,
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
            this.listQuery.name = "";
            this.fetchData()
        },


        handleEditSave(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.put("/trainingName/" + this.dialogForm.id, this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '更新成功',
                                type:'success',
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
      header: ['序号', '训练名称'],
      key: ['id', 'name'],
      data: this.tableData,
      autoWidth: true,
      fileName: '训练名称表',
      bookType: type
    }
    excel.exportDataToExcel(params)
    this.exportVisible = false
  }
},
    }
}
</script>

<style lang="less" scoped>
.category-wrapper {
   .el-card {
        min-height: 656px;
    }

   .control-btns {
        margin-bottom: 20px;
    }


   .search-form {


        padding-bottom: 20px!important;
        margin-bottom: 15px;
        background-color: #f7f8fb;
        height: 35px!important;
        line-height: 35px!important;
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