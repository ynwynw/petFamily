<template>
    <div>
        <el-dialog title="申请医疗服务" :visible.sync="visible" width="30%" class="dialog-form">
            <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="100px">
                <el-form-item prop="petName" label="宠物名称">
                    <el-input v-model="dialogForm.petName" autocomplete="off"></el-input>
                </el-form-item>

                <el-form-item prop="serviceDate" label="服务日期">
                    <el-date-picker v-model="dialogForm.date" type="date" placeholder="选择日期">
                    </el-date-picker>
                </el-form-item>
                <el-form-item prop="serviceType" label="服务类型">
                    <el-select v-model="dialogForm.type" placeholder="请选择服务类型">
                        <el-option v-for="item in typeList" :key="item.id" :label="item.name" :value="item.name">
                        </el-option>
                    </el-select>
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
            typeList: [],
            formRules: {
                petName: [
                    { required: true, message: '请输入宠物名称', trigger: 'blur' },
                ],
                date: [
                    { required: true, message: '请选择服务日期', trigger: 'blur' },
                    { type: 'date', validator: this.validateServiceDate, trigger: 'change' }
                ],
                type: [
                    { required: true, message: '请输入服务类型', trigger: 'blur' },
                ],
                status: [
                    { required: true, message: '请输入服务状态', trigger: 'blur' },
                ],
            },
            isSubmit: false,
        }
    },
    created() {
        this.loadType()

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
        loadType() {
            Request.get('/medicalServiceType/selectAll').then(res => {
                if (res.code == 0) {
                    this.typeList = res.data
                }
            })
        },
        handleAdd(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.post("/petMedicalService", this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '服务添加成功',
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
                    this.visible = false
                    return true;
                } else {
            
                    return false
                }
            })
        },
    }
}
</script>