<template>
    <div>
        <el-dialog title="申请寄养" :visible.sync="formVisibleAdd" width="30%" class="dialog-form"
            :close-on-click-modal="false">
            <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="100px">
                <el-form-item prop="name" label="宠物昵称">
                    <el-input v-model="dialogForm.name" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item prop="time" label="寄养时间">
                    <el-date-picker style="width: 100%" v-model="dialogForm.time" type="date" value-format="yyyy-MM-dd"
                        placeholder="选择日期">
                    </el-date-picker>
                </el-form-item>
                <el-form-item prop="days" label="寄养天数">
                    <el-input v-model.number="dialogForm.days" autocomplete="off"></el-input>
                </el-form-item>
                <div class="footer-item">
                    <el-button @click="formVisibleAdd = false">取 消</el-button>
                    <el-button type="primary" :disabled="isSubmit" @click="handleAdd('dialogForm')">确 定</el-button>
                </div>
            </el-form>
        </el-dialog>
    </div>
</template>

<script>
  import Request from '../../utils/request.js'
export default {
    inject: ['userInfo'],
    data() {
        return {
            formVisibleAdd: false,
            dialogForm: {
                userId:'',
                name: '',
                time: '',
                days: '',
                status: '',
                roomId: ''
            },
            formRules: {
                name: [
                    { required: true, message: '请输入房间编号', trigger: 'blur' },
                ],
                time: [
                    { required: true, message: '请选择寄养时间', trigger: 'blur' },
                ],
                days: [
                    { required: true, message: '请填写寄养天数', trigger: 'blur' },
                    { type: 'number', message: '请填写数字', trigger: 'blur' },
                ],
            },
            isSubmit: false
        }
    },
    methods: {
        handleCreate() {
            this.formVisibleAdd = true
            this.dialogForm = {}
        },
        handleAdd(formName) {
            this.dialogForm.userId=this.userInfo.id
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.post("/foster", this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '申请寄养成功',
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
    }
}
</script>

<style>
.footer-item {
    text-align: center;
    margin-top: 20px;
}
</style>