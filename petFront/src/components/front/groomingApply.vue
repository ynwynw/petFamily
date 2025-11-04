<template>
    <div>
        <el-dialog title="申请美容服务" :visible.sync="visible" width="30%" class="dialog-form">
            <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="100px">
                <el-form-item prop="petName" label="宠物名称">
                    <el-input v-model="dialogForm.petName" autocomplete="off"></el-input>
                </el-form-item>

                <el-form-item prop="serviceDate" label="服务日期">
                    <el-date-picker v-model="dialogForm.serviceDate" type="date" placeholder="选择日期">
                    </el-date-picker>
                </el-form-item>
                <el-form-item prop="serviceType" label="服务类型">
                    <el-input v-model="dialogForm.serviceType" autocomplete="off"></el-input>
                </el-form-item>

                <div class="footer-item">
                    <el-button @click="visible = false">取 消</el-button>
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
            visible: false,
            dialogForm: {
                petName: '',
                username: this.userInfo.username,
                serviceDate: '',
                serviceType: '',
                status: ''
            },
            formRules: {
                petName: [
                    { required: true, message: '请输入宠物名称', trigger: 'blur' },
                ],
                username: [
                    { required: true, message: '请输入用户名', trigger: 'blur' },
                ],
                serviceDate: [
                    { required: true, message: '请选择服务日期', trigger: 'blur' },
                    { type: 'date', validator: this.validateServiceDate, trigger: 'change' }
                ],
                serviceType: [
                    { required: true, message: '请输入服务类型', trigger: 'blur' },
                ],
                status: [
                    { required: true, message: '请输入服务状态', trigger: 'blur' },
                ],
            },
            isSubmit: false,
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
            handleCreate() {
                this.visible = true;
            },
            handleAdd(formName) {
                this.$refs[formName].validate(valid => {
                    if (valid) {
                        Request.post('/petGroomingService', this.dialogForm).then(res => {
                            if (res.code == 0) {
                                this.$message({
                                    type: 'success',
                                    message: '申请成功!'
                                })
                            } else {
                            }
                        })
                        console.log(this.dialogForm)
                        this.visible = false
                    } else {
                        this.isSubmit = false
                        return false
                    }
                })
            }
        }
    }
</script>