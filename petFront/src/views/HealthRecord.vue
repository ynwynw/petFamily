<template>
    <div class="pet-health-wrapper">
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
                    style="float:right;background-color: blue;border-color: blue;">添加宠物健康记录</el-button>

                <el-button type="primary" size="medium" @click="exportVisible = true"
                    style="float:right;margin-right: 10px;background-color: blue;border-color: blue;">导出数据</el-button>

            </div>
            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" label-width="90px" class="search-form">

                <el-form-item label="宠物姓名">
                    <el-input v-model="listQuery.petName" placeholder="宠物姓名" />
                </el-form-item>

                <el-form-item label="记录日期">
                    <el-date-picker v-model="listQuery.recordDate" type="date" placeholder="记录日期" />
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
            <el-table ref="multipleTable" v-loading="listLoading" :data="tableData"
                @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="60" />
                <el-table-column prop="id" label="编号" width="80" sortable />
                <!-- <el-table-column prop="petId" label="宠物ID" show-overflow-tooltip></el-table-column> -->
                <el-table-column prop="petName" label="宠物姓名"></el-table-column>
                <el-table-column prop="recordDate" label="记录日期"></el-table-column>
                <el-table-column prop="temperature" label="体温"></el-table-column>
                <el-table-column prop="weight" label="体重"></el-table-column>
                <el-table-column prop="height" label="身高"></el-table-column>
                <el-table-column prop="vaccinationDate" label="疫苗接种日期"></el-table-column>
                <el-table-column prop="healthStatus" label="健康状态"></el-table-column>
                <el-table-column label="操作" width="200">
                    <template slot-scope="scope">
                        <el-button size="mini" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
                        <el-button size="mini" type="danger" @click="handleDelete(scope.$index, scope.row)"
                            v-if="scope.row.role !== 'SUPER_ADMIN'">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <!-- 分页栏 -->
            <Pagination :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize"
                @pagination="fetchData" />
            <!-- 新增/编辑 弹出栏 -->
            <el-dialog title="添加宠物健康记录" :visible.sync="formVisibleAdd" width="30%" class="dialog-form" :modal="false">

                <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="120px">
                    <el-form-item prop="petId" label="宠物">
                        <el-select v-model="dialogForm.petId" placeholder="请选择宠物">
                            <el-option v-for="pet in petList" :key="pet.id" :label="pet.name"
                                :value="pet.id"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item prop="recordDate" label="记录日期">
                        <el-date-picker v-model="dialogForm.recordDate" type="date" placeholder="记录日期" />
                    </el-form-item>
                    <el-form-item prop="temperature" label="体温">
                        <el-input v-model="dialogForm.temperature" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="weight" label="体重">
                        <el-input v-model="dialogForm.weight" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="height" label="身高">
                        <el-input v-model="dialogForm.height" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="vaccinationDate" label="疫苗接种日期">
                        <el-date-picker v-model="dialogForm.vaccinationDate" type="date" placeholder="疫苗接种日期" />
                    </el-form-item>
                    <el-form-item prop="healthStatus" label="健康状态">
                        <el-input v-model="dialogForm.healthStatus" autocomplete="off"></el-input>
                    </el-form-item>

                    <div class="footer-item">
                        <el-button @click="formVisibleAdd = false">取 消</el-button>
                        <el-button type="primary" :disabled="isSubmit" @click="handleAdd('dialogForm')">确 定</el-button>
                    </div>
                </el-form>
            </el-dialog>

            <el-dialog title="修改宠物健康记录" :visible.sync="formVisibleEdit" width="30%" class="dialog-form">
                <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="120px">
                    <el-form-item prop="petId" label="宠物">
                        <el-select v-model="dialogForm.petId" placeholder="请选择宠物">
                            <el-option v-for="pet in petList" :key="pet.id" :label="pet.name"
                                :value="pet.id"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item prop="recordDate" label="记录日期">
                        <el-date-picker v-model="dialogForm.recordDate" type="date" placeholder="记录日期" />
                    </el-form-item>
                    <el-form-item prop="temperature" label="体温">
                        <el-input v-model="dialogForm.temperature" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="weight" label="体重">
                        <el-input v-model="dialogForm.weight" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="height" label="身高">
                        <el-input v-model="dialogForm.height" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="vaccinationDate" label="疫苗接种日期">
                        <el-date-picker v-model="dialogForm.vaccinationDate" type="date" placeholder="疫苗接种日期" />
                    </el-form-item>
                    <el-form-item prop="healthStatus" label="健康状态">
                        <el-input v-model="dialogForm.healthStatus" autocomplete="off"></el-input>
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
    name: 'PetHealthManager',
    components: { Pagination, Upload, Hints },
    data() {
        return {
            // 数据列表加载动画
            listLoading: true,
            // 查询列表参数对象
            listQuery: {
                petName: undefined,
                recordDate: undefined,
                currentPage: 1,
                pageSize: 10

            },
            // 新增/编辑提交表单对象
            dialogForm: {
                petId: undefined,
                recordDate: undefined,
                temperature: undefined,
                weight: undefined,
                height: undefined,
                vaccinationDate: undefined,
                healthStatus: undefined
            },

            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            petList: [],
            // 多选数据暂存数组
            multipleSelection: [],
            // 新增/编辑 弹出框显示/隐藏
            formVisible: false,
            formVisibleAdd: false,
            formVisibleEdit: false,
            // 导入数据 弹出框 关闭事件
            // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
            formRules: {
                petId: [
                    { required: true, message: '请输入宠物ID', trigger: 'blur' },
                ],
                recordDate: [
                    { required: true, message: '请输入记录日期', trigger: 'blur' },
                ],
                temperature: [
                    { required: true, message: '请输入体温', trigger: 'blur' },
                ],
                weight: [
                    { required: true, message: '请输入体重', trigger: 'blur' },
                ],
                height: [
                    { required: true, message: '请输入身高', trigger: 'blur' },
                ],
                vaccinationDate: [
                    { required: true, message: '请输入疫苗接种日期', trigger: 'blur' },
                ],
                healthStatus: [
                    { required: true, message: '请输入健康状态', trigger: 'blur' },
                ]
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
        Request.get("/animal/selectAll").then(response => {
                this.petList = response.data;
            });
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
                    Request.post("/health-record", this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '宠物健康记录添加成功',
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
        // 删除数据
        handleDelete(index, row) {
            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/health-record/" + row.id).then(response => {
                    if (response.code == 0) {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        })
                        this.onReset()
                    } else {
                        this.$message({
                            type: 'success',
                            error: '删除失败!'
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
                Request.delete(`/health-record/deleteBatch?ids=${this.multipleSelection.join(',')}`).then(res => {
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
            Request.get("/health-record/page", {
                params: {
                    petName: this.listQuery.petName,
                    recordDate: this.listQuery.recordDate,
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
            this.listQuery.petName = "";
            this.listQuery.recordDate = "";

            this.fetchData()
        },



        handleEditSave(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.put("/health-record/" + this.dialogForm.id, this.dialogForm).then(response => {
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
                                message: '更新失败',
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
                    header: ['编号', '宠物ID', '记录日期', '体温', '体重', '身高', '疫苗接种日期', '健康状态'],
                    key: ['id', 'petId', 'recordDate', 'temperature', 'weight', 'height', 'vaccinationDate', 'healthStatus'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '宠物健康记录表',
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
.pet-health-wrapper {
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