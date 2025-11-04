<template>
  <div class="login-wrapper">
    <el-card class="login-card">
      <div class="card-content">
        <!-- 左侧图片 -->
        <div class="login-image">
          <img src="../assets/猫.jpg" alt="宠物之家" />
          <div class="image-overlay"></div>
        </div>
        
        <!-- 右侧登录/注册表单 -->
        <div class="form-container">
          <!-- 标题 -->
          <div class="title-container">
            <h1 class="main-title">萌宠之家</h1>
            <p class="sub-title">Pet Paradise</p>
          </div>
          
          <!-- 切换按钮 -->
          <div class="toggle-container">
            <el-button 
              :type="isLoginActive ? 'primary' : ''" 
              round
              @click="switchMode('login')"
            >
              登录
            </el-button>
            <el-button 
              :type="!isLoginActive ? 'primary' : ''" 
              round
              @click="switchMode('register')"
            >
              注册
            </el-button>
          </div>
          
          <!-- 登录表单 -->
          <el-form 
            v-if="isLoginActive" 
            ref="loginForm" 
            :model="loginForm" 
            :rules="loginRules"
          >
            <el-form-item prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="用户名"
                prefix-icon="el-icon-user"
                round
              ></el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                type="password" 
                v-model="loginForm.password" 
                placeholder="密码"
                prefix-icon="el-icon-lock"
                round
              ></el-input>
            </el-form-item>
            
            <el-form-item>
              <div class="verify-code-container">
                <el-input 
                  v-model="loginForm.validCode" 
                  placeholder="验证码"
                  class="verify-input"
                  round
                ></el-input>
                <ValidCode @input="createValidCode" class="valid-code" />
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" style="width: 100%;" round @click="onLogin">
                登录
              </el-button>
            </el-form-item>
            
            <div class="form-footer">
              <span class="forget-link" @click="toForget">忘记密码?</span>
            </div>
          </el-form>
          
          <!-- 注册表单 -->
          <el-form 
            v-else 
            ref="registerForm" 
            :model="registerForm" 
            :rules="registerRules"
          >
            <el-form-item prop="username">
              <el-input 
                v-model="registerForm.username" 
                placeholder="用户名"
                prefix-icon="el-icon-user"
                round
              ></el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                type="password" 
                v-model="registerForm.password" 
                placeholder="密码"
                prefix-icon="el-icon-lock"
                round
              ></el-input>
            </el-form-item>
            
            <el-form-item prop="email">
              <el-input 
                v-model="registerForm.email" 
                placeholder="邮箱"
                prefix-icon="el-icon-message"
                round
              ></el-input>
            </el-form-item>
            
            <el-form-item prop="code">
              <div class="verify-code-container">
                <el-input 
                  v-model="registerForm.code" 
                  placeholder="邮箱验证码"
                  class="verify-input"
                  round
                ></el-input>
                <el-button 
                  type="primary" 
                  class="verify-btn" 
                  round
                  @click="sendVerificationCode"
                  :disabled="sendDisabled"
                >
                  {{ buttonContent }}
                </el-button>
              </div>
            </el-form-item>
            
            <el-form-item prop="role">
              <el-select v-model="registerForm.role" placeholder="请选择身份" style="width: 100%;">
                <el-option label="普通用户" value="USER"></el-option>
                <el-option label="管理员" value="ADMIN"></el-option>
              </el-select>
            </el-form-item>
            
            <template v-if="registerForm.role === 'ADMIN'">
              <el-form-item prop="invitationCode">
                <el-input 
                  v-model="registerForm.invitationCode" 
                  placeholder="邀请码"
                  prefix-icon="el-icon-unlock"
                  round
                ></el-input>
              </el-form-item>
            </template>
            
            <el-form-item>
              <el-button type="primary" style="width: 100%;" round @click="onRegister">
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import ValidCode from "../components/Validate";
import request from "@/utils/request";
import {setRoutes} from "@/router";

export default {
  name: 'Login',
  components: {
    ValidCode
  },
  data() {
    return {
      adminVitationCode: '123456',
      countdown: 0,
      sendDisabled: false,
      timer: null,
      emailCode: '',
      buttonContent: '发送验证码',
      validCode: '',
      isLoginActive: true,
      loginForm: {
        username: '',
        password: '',
        validCode: ''
      },
      registerForm: {
        username: '',
        password: '',
        email: '',
        code: '',
        role: 'USER',
        invitationCode: ''
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger:['blur','change'] },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符',trigger:['blur','change'] }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ]
      },
      registerRules: {
        username: [
          { required: true, message: '请输入用户名',  },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
        ],
        invitationCode: [
          { required: true, message: '请输入邀请码', trigger: 'blur' },
          { min: 6, max: 6, message: '邀请码长度必须为6位', trigger: 'blur' }
        ]
      }
    };
  },
  methods: {
    switchMode(mode) {
      this.isLoginActive = mode === 'login';
      if(this.isLoginActive==false) {
        this.$nextTick(() => {
          this.$refs['registerForm'].clearValidate()
        })
      }
      else{
        this.$nextTick(() => {
          this.$refs['loginForm'].clearValidate()
        })
      }
    },
    sendVerificationCode() {
      if (this.sendDisabled) return;

      request.post("/email/sendEmail", this.registerForm).then(res => {
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
      this.countdown = 60;
      this.sendDisabled = true;
      this.buttonContent = `${this.countdown}秒后可重发`;

      this.timer = setInterval(() => {
        if (this.countdown > 0) {
          this.countdown--;
          this.buttonContent = `${this.countdown}秒后可重发`;
        } else {
          clearInterval(this.timer);
          this.countdown = 0;
          this.sendDisabled = false;
          this.buttonContent = '发送验证码';
        }
      }, 1000);
    },
    onRegister() {
      this.$refs.registerForm.validate((valid) => {
        if (valid) {
          if (this.registerForm.code != this.emailCode) {
            this.$message({
              type: 'error',
              message: '验证码不正确'
            });
            return;
          }
          if (this.registerForm.role == 'ADMIN') {
            if (this.registerForm.invitationCode != this.adminVitationCode) {
              this.$message({
                type: 'error',
                message: '邀请码错误'
              });
              return;
            }
          }
          request.post("/user", this.registerForm).then(res => {
            if (res.code == '0') {
              this.$message({
                type: 'success',
                message: "注册成功",
              })
              this.$router.push({
                path: '/completeInfo',
                query: { registerUser: res.data }
              });
            } else {
              this.$message({
                type: 'error',
                message: res.msg
              });
            }
          })
        } else {
          console.error('注册失败: 表单校验失败');
          return false;
        }
      });
    },
    toForget() {
      this.$router.push("/forget");
    },
    createValidCode(data) {
      this.validCode = data
    },
    onLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          if (!this.loginForm.validCode) {
            this.$message({
              showClose:true,
              message: "请填写验证码",
              type: "error",
            });
            return
          }
          if (this.loginForm.validCode.toLowerCase() !== this.validCode.toLowerCase()) {
            this.$message({
              showClose:true,
              message: "验证码错误",
              type: "error",
            });
            return
          }

          request.post("/user/login", this.loginForm).then(res => {
            if (res.code === '0') {
              this.$message({
                showClose: true,
                message: "登录成功",
                type: "success"
              });
              // 跳转到首页
              sessionStorage.setItem("user", JSON.stringify(res.data))
              sessionStorage.setItem("userMenuList", JSON.stringify(res.data.menuList))
              setRoutes();
              if(res.data.role=='USER'){
                this.$router.push("/front");
              }
              else{
                this.$router.push("/showView");
              }
            } else {
              this.$message({
                showClose: true,
                message: res.msg,
                type: "error"
              });
            }
          })
        } else {
          console.error('登录失败: 表单校验失败');
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
.login-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 850px;
  border-radius: 15px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-content {
  display: flex;
}

/* 左侧图片 */
.login-image {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.login-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(168, 230, 207, 0.3) 0%, rgba(220, 237, 193, 0.3) 100%);
}

/* 右侧表单 */
.form-container {
  flex: 1;
  padding: 25px;
  background-color: #ffffff;
}

/* 标题样式 */
.title-container {
  text-align: center;
  margin-bottom: 25px;
}

.main-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  color: #e91e63;
}

.sub-title {
  font-size: 14px;
  color: #f06292;
  margin: 5px 0 0;
}

/* 切换按钮 */
.toggle-container {
  display: flex;
  justify-content: center;
  margin-bottom: 25px;
}

.toggle-container .el-button {
  margin: 0 10px;
  padding: 10px 25px;
}

/* 验证码容器 */
.verify-code-container {
  display: flex;
}

.verify-input {
  flex: 1;
  margin-right: 10px;
}

.valid-code {
  width: 100px;
  height: 38px;
}

.verify-btn {
  width: 110px;
}

/* 底部链接 */
.form-footer {
  margin-top: 15px;
  text-align: right;
}

.forget-link {
  color: #f06292;
  cursor: pointer;
  font-size: 14px;
}

.forget-link:hover {
  color: #e91e63;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .login-card {
    width: 95%;
  }
  
  .card-content {
    flex-direction: column;
  }
  
  .login-image {
    height: 200px;
  }
}

/* 自定义Element UI样式 */
::v-deep .el-button--primary {
  background-color: #e91e63;
  border-color: #e91e63;
}

::v-deep .el-button--primary:hover,
::v-deep .el-button--primary:focus {
  background-color: #f48fb1;
  border-color: #f48fb1;
}
</style>