<template>
  <div class="complete-info-wrapper">
    <el-card class="complete-info-card">
      <div class="card-header">
        <div class="header-icon">
          <i class="el-icon-edit"></i>
        </div>
        <h2 class="header-title">完善个人信息</h2>
        <p class="header-subtitle">让我们了解您更多一点</p>
      </div>

      <el-form
        ref="completeInfoForm"
        :model="completeInfoForm"
        :rules="rules"
        :hide-required-asterisk="true"
        label-width="80px"
      >
        <!-- 头像上传 -->
        <el-form-item label="头像：" prop="avatar">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-image
              v-if="completeInfoForm.avatar"
              :src="imgBaseUrl + completeInfoForm.avatar"
              class="avatar"
            />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>

        <el-form-item label="姓名：" prop="name">
          <el-input
            v-model="completeInfoForm.name"
            placeholder="请输入真实姓名"
            prefix-icon="el-icon-user"
            round
          />
        </el-form-item>

        <!-- 性别选择 -->
        <el-form-item label="性别：" prop="sex">
          <el-radio-group v-model="completeInfoForm.sex">
            <el-radio label="男"> <i class="el-icon-male"></i> 男 </el-radio>
            <el-radio label="女"> <i class="el-icon-female"></i> 女 </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 电话号码 -->
        <el-form-item label="电话：" prop="phone">
          <el-input
            v-model="completeInfoForm.phone"
            placeholder="请输入电话号码"
            prefix-icon="el-icon-phone"
            round
          />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            round
            @click="onCompleteInfo"
          >
            提交信息
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 装饰元素 -->
      <div class="decoration">
        <div class="paw paw-1"></div>
        <div class="paw paw-2"></div>
      </div>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'
export default {
  name: 'CompleteInfo',

  created() {
    const registerUser = this.$route.query.registerUser
    if (registerUser) {
      // 使用 Object.assign 或展开运算符复制对象
      Object.assign(this.completeInfoForm, registerUser)
    }
  },
  data() {
    return {
      imgBaseUrl: this.HOST,
      uploadUrl: '/api/file/upload', // 假设的上传接口地址
      fileList: [],
      completeInfoForm: {
        name: '', // 用户姓名
        avatar: '', // 用户头像地址
        sex: '男', // 性别，默认为男
        phone: '', // 电话号码
      },
      rules: {
        avatar: [{ required: true, message: '请上传头像', trigger: 'change' }],
        name: [
          { required: true, message: '请输入您的姓名', trigger: 'blur' },
          {
            min: 2,
            max: 5,
            message: '姓名长度在 2 到 5 个字符',
            trigger: 'blur',
          },
        ],
        sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
        phone: [
          { required: true, message: '请输入电话号码', trigger: 'blur' },
          {
            pattern: /^1[3-9]\d{9}$/,
            message: '电话号码格式不正确',
            trigger: 'blur',
          },
        ],
      },
    }
  },
  methods: {
    handleAvatarSuccess(res, file) {
      // 头像上传成功后的回调
      this.completeInfoForm.avatar = res.data
      this.$forceUpdate()
    },
    beforeAvatarUpload(file) {
      // 头像上传前的校验
      const isImage = file.type.match('image/jpeg|image/png')
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isImage) {
        this.$message.error('上传头像图片只能是 JPG 或 PNG 格式!')
      }
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!')
      }
      return isImage && isLt2M
    },
    onCompleteInfo() {
      this.$refs.completeInfoForm.validate((valid) => {
        if (valid) {
          request
            .put('/user/' + this.completeInfoForm.id, this.completeInfoForm)
            .then((response) => {
              if (response.code == '0') {
                this.$message({
                  type: 'success',
                  message: '信息保存成功!',
                })
                setTimeout(() => {
                  this.$router.push({
                    path: '/login',
                  })
                }, 1000)
              } else {
                this.$message({
                  type: 'error',
                  message: response.msg,
                })
              }
            })
        } else {
          console.error('信息提交失败: 表单校验失败')
          return false
        }
      })
    },
  },
}
</script>

<style scoped>
.complete-info-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.complete-info-card {
  width: 450px;
  border-radius: 15px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.card-header {
  text-align: center;
  margin-bottom: 20px;
  position: relative;
}

.header-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5aaa95, #88b04b);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 15px;
  font-size: 24px;
}

.header-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
  color: #5aaa95;
}

.header-subtitle {
  font-size: 14px;
  color: #88b04b;
  margin: 5px 0 0;
}

.avatar-uploader {
  text-align: center;
  display: flex;
  justify-content: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 50%;
  border: 3px solid #a8e6cf;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #a8e6cf;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
  border: 2px dashed #a8e6cf;
  border-radius: 50%;
}

.avatar-uploader-icon:hover {
  border-color: #5aaa95;
  color: #5aaa95;
}

.el-radio {
  margin-right: 20px;
}

.el-radio i {
  margin-right: 5px;
  color: #88b04b;
}

/* 装饰元素 */
.decoration {
  position: absolute;
  bottom: 10px;
  right: 10px;
  opacity: 0.2;
}

.paw {
  position: absolute;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><path d="M256 224c-79.5 0-144 64.5-144 144s64.5 144 144 144 144-64.5 144-144-64.5-144-144-144zm0 240c-52.9 0-96-43.1-96-96s43.1-96 96-96 96 43.1 96 96-43.1 96-96 96zm-58.7-161.3c-17.7 0-32-14.3-32-32s14.3-32 32-32 32 14.3 32 32-14.3 32-32 32zm117.4 0c-17.7 0-32-14.3-32-32s14.3-32 32-32 32 14.3 32 32-14.3 32-32 32z"/></svg>');
  background-repeat: no-repeat;
  background-size: contain;
  width: 25px;
  height: 25px;
}

.paw-1 {
  right: 0;
  bottom: 0;
  transform: rotate(15deg);
}

.paw-2 {
  right: 30px;
  bottom: 10px;
  transform: rotate(-10deg);
}

.complete-info-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #e91e63, #f06292);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.form-title {
  text-align: center;
  margin-bottom: 30px;
  color: #e91e63;
  font-size: 28px;
  font-weight: 600;
}

.form-subtitle {
  text-align: center;
  margin-bottom: 30px;
  color: #f06292;
  font-size: 16px;
}

.avatar-uploader {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.avatar-uploader .el-upload {
  border: 3px solid #f8bbd0;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.avatar-uploader .el-upload:hover {
  border-color: #e91e63;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #f8bbd0;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
  border: 2px dashed #f8bbd0;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.avatar-uploader-icon:hover {
  border-color: #e91e63;
  color: #e91e63;
}

/* 装饰元素 */
.decoration {
  position: absolute;
  bottom: 10px;
  right: 10px;
  opacity: 0.2;
}

.paw {
  position: absolute;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><path d="M256 224c-79.5 0-144 64.5-144 144s64.5 144 144 144 144-64.5 144-144-64.5-144-144-144zm0 240c-52.9 0-96-43.1-96-96s43.1-96 96-96 96 43.1 96 96-43.1 96-96 96zm-58.7-161.3c-17.7 0-32-14.3-32-32s14.3-32 32-32 32 14.3 32 32-14.3 32-32 32zm117.4 0c-17.7 0-32-14.3-32-32s14.3-32 32-32 32 14.3 32 32-14.3 32-32 32z"/></svg>');
  background-repeat: no-repeat;
  background-size: contain;
  width: 25px;
  height: 25px;
}

.paw-1 {
  right: 0;
  bottom: 0;
  transform: rotate(15deg);
}

.paw-2 {
  right: 30px;
  bottom: 10px;
  transform: rotate(-10deg);
}

.submit-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  font-weight: 600;
  color: #f06292;
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

::v-deep .el-input__inner {
  border-color: #f8bbd0;
}

::v-deep .el-input__inner:focus {
  border-color: #f06292;
}

::v-deep .el-input__prefix {
  left: 10px;
  color: #f06292;
}
</style>
