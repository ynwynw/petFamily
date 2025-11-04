<template>
    <div class="training-wrapper">
        <el-card shadow="always">
      
            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" label-width="120px" class="search-form">
                <el-form-item label="训练服务类型">
                    <el-select v-model="listQuery.serviceType" placeholder="请选择训练服务类型">
                        <el-option v-for="item in serviceTypeList" :key="item.id" :label="item.name" :value="item.name"></el-option>
                    </el-select>
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
            
                <el-table-column prop="petName" label="宠物名称" ></el-table-column>
                <el-table-column prop="userName"  label="用户名"></el-table-column>
                <el-table-column prop="serviceType" label="训练服务类型" show-overflow-tooltip></el-table-column>
                <el-table-column prop="endDate" label="预计结束日期" show-overflow-tooltip></el-table-column>
                <el-table-column prop="trainingLevel" label="训练级别" show-overflow-tooltip></el-table-column>
                <el-table-column prop="trainingGoals" label="训练目标" show-overflow-tooltip></el-table-column>
                <el-table-column prop="completionStatus" label="完成状态" show-overflow-tooltip></el-table-column>
                <el-table-column v-if="userInfo.role!='USER'" label="操作" >
                    <template slot-scope="scope">
                        <el-button v-if="scope.row.completionStatus === '未开始'"  size="mini" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
                        <el-button  v-if="scope.row.completionStatus === '完成'" size="mini" type="danger"
                            @click="handleDelete(scope.$index, scope.row)">删除</el-button>

                        <el-button plain type="warning" size="mini" v-if="scope.row.completionStatus === '未开始'"
                            @click="handleModifyTraining(scope.$index, scope.row,'训练中')">开始训练</el-button>
                        <el-button plain type="primary" size="mini" v-else-if="scope.row.completionStatus === '训练中'"
                            @click="handleModifyTraining(scope.$index, scope.row,'完成')">训练结束</el-button>
                        <el-button plain type="success" size="mini" v-else disabled>已结束</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <!-- 分页栏 -->
            <Pagination :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize"
                @pagination="fetchData" />

            <el-dialog title="修改训练服务" :visible.sync="formVisibleEdit" width="30%" class="dialog-form" :modal="false">
                <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="120px">
                    <el-form-item prop="userName" label="用户名">
                        <el-input :disabled="true" v-model="dialogForm.userName" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="petName" label="宠物">
                        <el-input v-model="dialogForm.petName" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="serviceType" label="训练服务类型">
                <el-select v-model="dialogForm.serviceType" placeholder="请选择训练服务类型">
                    <el-option v-for="item in serviceTypeList" :key="item.id" :label="item.name" :value="item.name"></el-option>
                </el-select>
            </el-form-item>
            
                    <el-form-item prop="endDate" label="预计结束日期">
                        <el-date-picker v-model="dialogForm.endDate" type="date" placeholder="选择日期"></el-date-picker>
                    </el-form-item>
                    <el-form-item prop="trainingLevel" label="训练级别">
                        <el-input v-model="dialogForm.trainingLevel" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="trainingGoals" label="训练目标">
                        <el-input v-model="dialogForm.trainingGoals" autocomplete="off"></el-input>
                    </el-form-item>
          
                    <div class="footer-item">
                        <el-button @click="formVisibleEdit = false">取 消</el-button>
                        <el-button type="primary" :disabled="isSubmit" @click="handleEditSave('dialogForm')">确
                            定</el-button>
                    </div>
                </el-form>
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
    name: 'TrainingManager',
    components: { Pagination, Upload, Hints },
    data() {
        return {
            uploadUrl: '/api/file/upload',

            // 数据列表加载动画
            listLoading: true,
            // 查询列表参数对象
            listQuery: {
                serviceType: undefined,
                currentPage: 1,
                pageSize: 10
            },
            // 新增/编辑提交表单对象
            dialogForm: {
                petName:'',
                serviceType: '',
                endDate: '',
                trainingLevel: '',
                trainingGoals: '',
        
            },
            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            // 多选数据暂存数组
            multipleSelection: [],


            formVisibleEdit: false,
            // 训练服务类型列表
            serviceTypeList: [],
            // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
            formRules: {
                serviceType: [
                    { required: true, message: '请选择训练服务类型', trigger: 'blur' }
                ],
                endDate: [
          { required: true, message: '请选择预计结束日期', trigger: 'blur' },
          { type: 'date', validator: this.validateEndDate, trigger: 'change' }
        ],
                trainingLevel: [
                    { required: true, message: '请输入训练级别', trigger: 'blur' }
                ],
                trainingGoals: [
                    { required: true, message: '请输入训练目标', trigger: 'blur' }
                ],
                petName: [
                    { required: true, message: '宠物名称', trigger: 'blur' }
                ]
            },
            // 防止多次连续提交表单
            isSubmit: false
        }
    },
    created() {
        this.loadServiceType();
        this.fetchData()
    },
    methods: {
        validateEndDate(rule, value, callback) {
      if (value) {
        const endDate = new Date(value);
        const now = new Date();
        if (endDate.getTime() < now.getTime()) {
          callback(new Error('预计结束日期不得早于当前日期'));
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

        // 编辑数据
        handleEdit(index, row) {
    
            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.formVisibleEdit = true
        },
        loadServiceType() {
            Request.get('/trainingName/selectAll').then(res => {
                if (res.code == 0) {
                    this.serviceTypeList = res.data
                }
            })
        },
        handleDelete(index, row) {
            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/petTrainingService/" + row.id).then(response => {
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
        handleModifyTraining(index, row,status) {
              
                var data = JSON.parse(JSON.stringify(row))
                data.completionStatus=status
                Request.put("/petTrainingService/"+data.id,data).then(res=>{
                    if(res.code == 0){
                        this.$message({
                            showClose: true,
                            message: '修改成功',
                            type:'success',
                        });
                        this.fetchData()
                    }else{
                        this.$message({
                            showClose: true,
                            message: res.msg,
                            type: 'error',
                        });
                    }
                })

        },
        handleEndTraining(index, row) {
            // 训练结束的逻辑
        },
        handleEditSave(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.put("/petTrainingService/" + this.dialogForm.id, this.dialogForm).then(response => {
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
                    this.isSubmit = false
                    return false
                }
            })
        },
        // 获取数据列表
        fetchData() {
            this.listLoading = true
            
            Request.get("/petTrainingService/selectPage", {
                params: {
                    username:this.userInfo.role=='USER'?this.userInfo.username:'',
                    serviceType: this.listQuery.serviceType,
                    currentPage: this.listQuery.currentPage,
                    pageSize: this.listQuery.pageSize,
                }
            }).then(response => {
                const data = response.data;
                if (response.code === '0') {
                    this.total = data.total
                    this.tableData = data.records
                    console.log(this.userInfo)
                    // if(this.userInfo.role=='USER'){
                    //     this.tableData = data.records.filter(record => record.userName === this.userInfo.username);
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
            this.listQuery.serviceType = "";
            this.fetchData()
        }
    }
}
</script>

<style lang="less" scoped>
.training-wrapper {
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
</style>