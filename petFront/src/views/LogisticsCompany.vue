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
                    style="float:right;background-color: blue;border-color: blue;">添加物流公司</el-button>

                <el-button type="primary" size="medium" @click="exportVisible = true"
                    style="float:right;margin-right: 10px;background-color: blue;border-color: blue;">导出数据</el-button>

            </div>
            <!-- 查询栏 -->
            <el-form :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="公司名称">
                    <el-input v-model="listQuery.companyName" placeholder="请输入公司名称" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="onSubmit">查询</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>
            <!-- 表格栏 -->
            <el-table :data="tableData" @selection-change="handleSelectionChange" 
                :header-cell-style="{background:'#f5f7fa',color:'#606266'}"
                :cell-style="{padding:'8px 0'}" stripe border>
                <el-table-column type="selection" width="55" align="center" />
                <el-table-column prop="companyId" label="公司ID" width="80" align="center" />
                <el-table-column prop="companyName" label="公司名称" width="180" show-overflow-tooltip />
                <el-table-column prop="contactPhone" label="联系电话" width="150" align="center">
                    <template slot-scope="scope">
                        <el-tag type="info">{{ scope.row.contactPhone }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="notes" label="备注信息" min-width="200" show-overflow-tooltip />
                <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
                    <template slot-scope="scope">
                        <el-tag type="success">{{ scope.row.createdAt }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="updatedAt" label="更新时间" width="180" align="center">
                    <template slot-scope="scope">
                        <el-tag type="warning">{{ scope.row.updatedAt }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="180" fixed="right">
                    <template slot-scope="scope">
                        <el-button size="mini" type="primary" plain @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
                        <el-button size="mini" type="danger" plain @click="handleDelete(scope.$index, scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <!-- 分页栏 -->
            <Pagination :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize"
                @pagination="fetchData" />
            <!-- 新增/编辑 弹出栏 -->
            <el-dialog title="添加物流公司" :visible.sync="formVisibleAdd" width="30%" class="dialog-form" :modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="公司ID" v-if="dialogForm.companyId">
                        <el-input v-model="dialogForm.companyId" disabled></el-input>
                    </el-form-item>
                    <el-form-item label="公司名称" prop="companyName">
                        <el-input v-model="dialogForm.companyName"></el-input>
                    </el-form-item>
                    <el-form-item label="联系电话" prop="contactPhone">
                        <el-input v-model="dialogForm.contactPhone"></el-input>
                    </el-form-item>
                    <el-form-item label="备注信息" prop="notes">
                        <el-input type="textarea" v-model="dialogForm.notes"></el-input>
                    </el-form-item>

                    <div class="footer-item">
                        <el-button @click="formVisibleAdd = false">取 消</el-button>
                        <el-button type="primary" :disabled="isSubmit" @click="handleAdd('dialogForm')">确 定</el-button>
                    </div>
                </el-form>
            </el-dialog>

            <el-dialog title="修改公司信息" :visible.sync="formVisibleEdit" width="30%" class="dialog-form">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="公司ID" v-if="dialogForm.companyId">
                        <el-input v-model="dialogForm.companyId" disabled></el-input>
                    </el-form-item>
                    <el-form-item label="公司名称" prop="companyName">
                        <el-input v-model="dialogForm.companyName"></el-input>
                    </el-form-item>
                    <el-form-item label="联系电话" prop="contactPhone">
                        <el-input v-model="dialogForm.contactPhone"></el-input>
                    </el-form-item>
                    <el-form-item label="备注信息" prop="notes">
                        <el-input type="textarea" v-model="dialogForm.notes"></el-input>
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
    name: 'Catetory',
    components: { Pagination, Upload, Hints },
    data() {
        return {
            // 数据列表加载动画
            listLoading: true,
            // 查询列表参数对象
            listQuery: {
                companyName: '',
                currentPage: 1,
                pageSize: 10,
            },
            // 新增/编辑提交表单对象
            dialogForm: {
                companyId: null,
                companyName: '',
                contactPhone: '',
                notes: '',
                createdAt: null,
                updatedAt: null,
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
                companyName: [
                    { required: true, trigger: 'blur', message: '公司名称不能为空' }
                ],
                contactPhone: [
                    { required: true, trigger: 'blur', message: '联系电话不能为空' }
                ],
                notes: [
                    { max: 200, message: '备注信息不能超过200个字符', trigger: 'blur' }
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
            this.multipleSelection = val.map(v => v.companyId)
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
            const delId = row.companyId

            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/logisticsCompany/" + row.companyId).then(response => {
                    if (response.code === '0') {
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
                Request.delete(`/logisticsCompany/deleteBatch?ids=${this.multipleSelection.join(',')}`).then(res => {
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
            Request.get("/logisticsCompany/page", {
                params: {
                    companyName: this.listQuery.companyName,
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
            this.listQuery.companyName = "";
            this.fetchData()
        },


        handleAdd(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.post("/logisticsCompany", this.dialogForm).then(response => {
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
        handleEditSave(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.put("/logisticsCompany/" + this.dialogForm.companyId, this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '更新成功',
                                type: 'success',
                            });
                            this.formVisibleEdit = false
                            this.resetListQuery()
                            this.fetchData()
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
                    header: ['公司ID', '公司名称', '联系电话', '备注信息', '创建时间', '更新时间'], // 更新表头
                    key: ['companyId', 'companyName', 'contactPhone', 'notes', 'createdAt', 'updatedAt'], // 更新键值
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '物流公司数据', // 更新文件名
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
.category-wrapper {
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