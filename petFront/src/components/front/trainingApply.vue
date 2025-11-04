<template>
    <el-dialog title="申请训练服务" :visible.sync="formVisibleAdd" width="30%" class="dialog-form">
        <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="120px">
            <el-form-item prop="petName" label="宠物名称" required>
                <el-input v-model="dialogForm.petName" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item prop="serviceType" label="训练服务类型">
                <el-select placement="bottom-start" v-model="dialogForm.serviceType" placeholder="请选择训练服务类型"  :popper-append-to-body="false" >
                    <el-option v-for="item in serviceTypeList" :key="item.id" :label="item.name" :value="item.name"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item prop="endDate" label="预计结束日期" :popper-append-to-body='false'>
                <el-date-picker v-model="dialogForm.endDate" type="date" placeholder="选择日期"></el-date-picker>
            </el-form-item>
            <el-form-item prop="trainingLevel" label="训练级别">
                <el-input v-model="dialogForm.trainingLevel" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item prop="trainingGoals" label="训练目标">
                <el-input v-model="dialogForm.trainingGoals" autocomplete="off"></el-input>
            </el-form-item>
        
            <div class="footer-item">
                <el-button @click="formVisibleAdd = false">取 消</el-button>
                <el-button type="primary" :disabled="isSubmit" @click="handleAdd('dialogForm')">确 定</el-button>
            </div>
        </el-form>
    </el-dialog>
</template>

<script>
import Request from '../../utils/request.js'
export default {
    inject: ['userInfo'],
    data() {
        return {
          
            formVisibleAdd:false,
            dialogForm: {
                serviceType: '',
                endDate: '',
                trainingLevel: '',
                trainingGoals: '',
                completionStatus: '',
                petName:'',
                userName:this.userInfo.username
            },
            serviceTypeList: [],
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
            isSubmit: false
        }
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
        loadServiceType() {
            Request.get('/trainingName/selectAll').then(res => {
                if (res.code == 0) {
                    this.serviceTypeList = res.data
                }
            })
        },
        handleCreate(){
            this.loadServiceType()
            this.formVisibleAdd = true
        },
        handleAdd(formName) {
            
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.post("/petTrainingService", this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '添加成功',
                                type: 'success',
                            });
                            this.formVisibleAdd = false
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
        }
    }
}
</script>

<style scoped>
.el-select-dropdown{
    /* position: absolute!important;
    top:30px!important;
    left:0px!important; */
}
</style>


