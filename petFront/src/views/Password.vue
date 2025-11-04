<template>
    <div class="password-change" style="display: flex; width: 100%; justify-content: center;">
        <el-card style="width: 40%; margin: 20px">
            <div style="margin: 15px; text-align: center;">
                <h3>修改密码</h3>
            </div>
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px">
                <el-form-item label="旧密码:" prop="oldPassword">
                    <el-input type="password" v-model="passwordForm.oldPassword" autocomplete="off" :show-password="true"></el-input>
                </el-form-item>
                <el-form-item label="新密码:" prop="newPassword">
                    <el-input type="password" v-model="passwordForm.newPassword" autocomplete="off"  :show-password="true"></el-input>
                </el-form-item>
                <el-form-item label="确认密码:" prop="confirmPassword">
                    <el-input type="password" v-model="passwordForm.confirmPassword" autocomplete="off" :show-password="true"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="submitForm">提交</el-button>
                    <el-button @click="resetForm">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script>
import request from '@/utils/request';

export default {
    inject: ['userInfo'],
    created() {

    },
    data() {
        const validatePassword = (rule, value, callback) => {
            if (value.length < 6) {
                callback(new Error('密码长度不能少于6位'));
            } else {
                callback();
            }
        };
        const validateConfirmPassword = (rule, value, callback) => {
            if (value !== this.passwordForm.newPassword) {
                callback(new Error('两次输入密码不一致'));
            } else {
                callback();
            }
        };
        return {
            correctPassword: this.userInfo.password,
            passwordForm: {
                oldPassword: '',
                newPassword: '',
                confirmPassword: '',
            },
            passwordRules: {
                oldPassword: [
                    { required: true, message: '请输入旧密码', trigger: 'blur' },
                ],
                newPassword: [
                    { required: true, validator: validatePassword, trigger: 'blur' },
                ],
                confirmPassword: [
                    { required: true, validator: validateConfirmPassword, trigger: 'blur' },
                ],
            },
        };
    },
    methods: {
        submitForm() {
            this.$refs.passwordForm.validate((valid) => {
                if (valid) {
                    request.put('/user/password/' + this.userInfo.id, this.passwordForm).then(response => {
                        if (response.code == '0') {
                            this.$message({
                                type: 'success',
                                message: '密码修改成功!请重新登录！',
                            });
                            this.resetForm();
                            sessionStorage.removeItem("user");
                            this.$router.push({ path: '/login'});
                        } else {
                            this.$message({
                                type: 'error',
                                message: response.msg,
                            });
                        }
                    });
                } else {
                    console.error('表单验证失败!');
                    return false;
                }
            });
        },
        resetForm() {
            this.$refs.passwordForm.resetFields();
        },
    },
};
</script>

<style scoped>
/* 可以添加一些样式来美化密码修改表单 */
</style>