<template>
    <el-card class="reset-password-box">
        <div class="reset-password-title">
            <h2 class="title-text">找回密码</h2>
        </div>
        <el-form ref="resetForm" :model="resetForm" :rules="rules">
            <el-form-item prop="email">
                <el-input v-model="resetForm.email" placeholder="邮箱">
                    <template slot="prepend">
                        <i class="el-icon-message"></i>
                    </template>
                </el-input>
            </el-form-item>
            <el-form-item prop="code">
                <div style="display: flex; align-items: center;">
                    <el-input v-model="resetForm.code" placeholder="请输入邮箱验证码" clearable
                        style="flex-grow: 1; margin-right: 10px;">
                        <template slot="prepend">
                            <i class="el-icon-chat-line-round"></i>
                        </template>
                    </el-input>
                    <el-button type="primary" size="small" style="height: 38px;background-color: blue; border: blue;" @click="sendVerificationCode"
                        :disabled="disabled">
                        {{ buttonContent }}
                    </el-button>
                </div>
            </el-form-item>
            <el-form-item prop="newPassword">
                <el-input type="password" v-model="resetForm.newPassword" placeholder="设置新密码">
                    <template slot="prepend">
                        <i class="el-icon-lock"></i>
                    </template>
                </el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" style="width: 100%; background-color: blue; border: blue;" @click="onResetPassword">提交</el-button>
            </el-form-item>
            <div style="display: flex; justify-content: space-between;">

<div style="cursor: pointer;color: #0f9876;" @click="back">返回</div>
</div>
        </el-form>
    </el-card>
</template>

<script>
import request from "@/utils/request";

export default {
    name: 'ResetPassword',
    data() {
        return {
            countdown: 0,
            disabled: false,
            timer: null,
            emailCode: '',
            buttonContent: '发送验证码',
            resetForm: {
                email: '',
                code: '',
                newPassword: '',
            },
            rules: {
                email: [
                    { required: true, message: '请输入邮箱', trigger: 'blur' },
                    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
                ],
                code: [
                    { required: true, message: '请输入验证码', trigger: 'blur' }
                ],
                newPassword: [
                    { required: true, message: '请输入新密码', trigger: 'blur' },
                    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
                ]
            }
        };
    },
    methods: {
        back(){
            this.$router.push("/login");
        },
        sendVerificationCode() {
            // 发送重置密码请求到后端
            if (this.disabled) return; // 如果按钮已禁用，则不执行发送逻辑

     
            request.get("/email/findByEmail",{
                params: {
                    email:this.resetForm.email
                }
            }).then(res => {
                if (res.code == '0') {
                    this.$message({
                        type: 'success',
                        message: "验证码已发送到您的邮箱,请查收"
                    })
                    this.emailCode = res.data;
                } else {
                    this.$message({
                        type: 'error',
                        message: res.msg
                    })
                    return
                }
            })
            this.countdown = 60; // 设置倒计时为60秒
            this.disabled = true; // 禁用按钮
            this.buttonContent = `${this.countdown}秒后可重发`; // 更新按钮文本为倒计时

            this.timer = setInterval(() => {
                if (this.countdown > 0) {
                    this.countdown--; // 每秒减少1秒
                    this.buttonContent = `${this.countdown}秒后可重发`; // 更新按钮文本
                } else {
                    clearInterval(this.timer); // 清除定时器
                    this.countdown = 0; // 重置倒计时
                    this.disabled = false; // 启用按钮
                    this.buttonContent = '发送验证码'; // 重置按钮文本
                }
            }, 1000);

        
    },
    onResetPassword() {
        this.$refs.resetForm.validate((valid) => {
            if (valid) {
                if (this.resetForm.code != this.emailCode) {
                    this.$message({
                        type: 'error',
                        message: '验证码不正确'
                    });
                    return;
                }
                // 发送重置密码请求到后端
                request.get("/user/forget", {
                    params: {
                        email: this.resetForm.email,
                        newPassword: this.resetForm.newPassword,

                    },
                }).then(res => {
                    if (res.code == '0') {
                        this.$message({
                            type: 'success',
                            message: "密码重置成功"
                        });
                        this.$router.push("/login");
                    } else {
                        this.$message({
                            type: 'error',
                            message: res.msg
                        });
                    }
                });
            } else {
                console.error('重置密码失败: 表单校验失败');
                return false;
            }
        });
    }
},
beforeDestroy() {
    clearInterval(this.timer);
},
  };
</script>

<style scoped>
.reset-password-box {
  width: 480px;
  padding: 45px;
  background: white;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  border-radius: 20px;
  transition: transform 0.3s ease;
  position: relative;
}

.reset-password-box:hover {
  transform: translateY(-5px);
}

.reset-password-title {
  text-align: center;
  margin-bottom: 30px;
}

.title-text {
  font-weight: bold;
  color: #8e44ad;
  font-size: 28px;
  margin: 0;
}

/* 新增的图标和输入框样式 */
/deep/ .el-input-group__prepend {
  background: transparent !important;
  border-right: none;
  color: #8e44ad;
  border-top-left-radius: 8px;
  border-bottom-left-radius: 8px;
  padding: 0 10px;
}

/deep/ .el-input-group__prepend i {
  font-size: 16px;
}

/deep/ .el-input-group {
  align-items: center;
}

/deep/ .el-input__inner {
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  padding: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/deep/ .el-input__inner:focus {
  border-color: #8e44ad;
  box-shadow: 0 0 0 2px rgba(142, 68, 173, 0.1);
}

/* 验证码输入框特殊样式 */
.code-input-container {
  display: flex;
  align-items: center;
}

.code-input-container .el-input {
  flex-grow: 1;
  margin-right: 10px;
}

/* 按钮样式 */
/deep/ .el-button--primary {
  background-color: #8e44ad !important;
  border-color: #8e44ad !important;
  border-radius: 8px;
  padding: 12px 20px;
  transition: all 0.3s ease;
}

/deep/ .el-button--primary:hover {
  background-color: #9b59b6 !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(142, 68, 173, 0.2);
}

/* 添加可爱的动物足迹装饰 */
.reset-password-box::before {
  content: '';
  position: absolute;
  top: 20px;
  right: 20px;
  width: 30px;
  height: 30px;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><path fill="%238e44ad" d="M256 224c-79.5 0-144 64.5-144 144s64.5 144 144 144 144-64.5 144-144-64.5-144-144-144zm0 240c-52.9 0-96-43.1-96-96s43.1-96 96-96 96 43.1 96 96-43.1 96-96 96zm-58.7-161.3c-17.7 0-32-14.3-32-32s14.3-32 32-32 32 14.3 32 32-14.3 32-32 32zm117.4 0c-17.7 0-32-14.3-32-32s14.3-32 32-32 32 14.3 32 32-14.3 32-32 32z"/></svg>');
  background-repeat: no-repeat;
  opacity: 0.1;
}

/* 返回按钮样式 */
div[style*="cursor: pointer"] {
  color: #8e44ad !important;
  transition: all 0.3s ease;
}

div[style*="cursor: pointer"]:hover {
  color: #9b59b6 !important;
  text-decoration: underline;
}
</style>