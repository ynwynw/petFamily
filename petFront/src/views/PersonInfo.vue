<template>
  <div
    class="user-info"
    style="display: flex; width: 100%; justify-content: center"
  >
    <el-card style="width: 40%; margin: 20px">
      <div style="margin: 15px; text-align: center">
        <el-upload
          class="avatar-uploader"
          :action="uploadUrl"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
        >
          <el-image
            v-if="userInfo.avatar"
            :src="imgBaseUrl + userInfo.avatar"
            class="avatar"
          />
          <i v-else class="el-icon-plus avatar-uploader-icon"></i>
        </el-upload>
      </div>
      <el-form
        :model="userInfo"
        :rules="rules"
        ref="userInfoForm"
        label-width="80px"
      >
        <el-form-item label="用户名：" prop="username" style="width: 100%">
          <el-input v-model="userInfo.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名：" prop="name" style="width: 100%">
          <el-input v-model="userInfo.name"></el-input>
        </el-form-item>
        <el-form-item label="性别：" prop="sex">
          <el-select
            v-model="userInfo.sex"
            placeholder="请选择"
            style="width: 100%"
          >
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱：" prop="email" style="width: 100%">
          <el-input v-model="userInfo.email"></el-input>
        </el-form-item>
        <el-form-item label="电话：" prop="phone" style="width: 100%">
          <el-input v-model="userInfo.phone"></el-input>
        </el-form-item>
        <el-form-item label="角色：" style="width: 100%">
          <div>
            <el-tag type="success">{{ getRoleName(userInfo.role) }}</el-tag>
          </div>
        </el-form-item>

        <el-form-item
          v-if="userInfo.role == 'USER'"
          label="账户余额:"
          style="width: 100%"
        >
          <div class="balance-container">
            <span class="balance-amount">{{ userInfo.account || 0 }}元</span>
            <el-button type="primary" size="small" @click="showRechargeDialog"
              >充值</el-button
            >
          </div>
        </el-form-item>

        <el-form-item
          v-if="userInfo.role == 'USER'"
          label="收货地址:"
          prop="address"
          style="width: 100%"
        >
          <el-input type="textarea" v-model="userInfo.address"></el-input>
        </el-form-item>
        <div style="text-align: center">
          <el-button type="success" @click="update">保存</el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 余额充值对话框 -->
    <el-dialog
      title="账户充值"
      :visible.sync="rechargeDialogVisible"
      width="30%"
      center
    >
      <el-form
        :model="rechargeForm"
        :rules="rechargeRules"
        ref="rechargeForm"
        label-width="100px"
      >
        <el-form-item label="充值金额:" prop="amount">
          <el-input-number
            v-model="rechargeForm.amount"
            :min="10"
            :max="10000"
            :step="10"
            controls-position="right"
            style="width: 200px"
          >
          </el-input-number>
        </el-form-item>
        <el-form-item label="支付方式:">
          <el-radio-group v-model="rechargeForm.paymentMethod">
            <el-radio label="alipay">支付宝</el-radio>
            <el-radio label="wechat">微信支付</el-radio>
            <el-radio label="creditCard">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rechargeDialogVisible = false">取 消</el-button>
        <el-button
          type="primary"
          @click="handleRecharge"
          :loading="rechargeLoading"
          >确认充值</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
export default {
  name: 'PersonInfo',
  inject: ['userInfo'],
  created() {},
  data() {
    return {
      imgBaseUrl: this.HOST,
      uploadUrl: '/api/file/upload', // 假设的上传接口地址

      // 余额充值相关
      rechargeDialogVisible: false,
      rechargeLoading: false,
      rechargeForm: {
        amount: 100,
        paymentMethod: 'alipay',
      },
      rechargeRules: {
        amount: [
          { required: true, message: '请输入充值金额', trigger: 'blur' },
          {
            type: 'number',
            min: 10,
            max: 10000,
            message: '充值金额必须在10-10000之间',
            trigger: 'blur',
          },
        ],
      },

      rules: {
        name: [
          { required: true, message: '姓名不能为空', trigger: 'blur' },
          {
            min: 2,
            max: 10,
            message: '姓名长度必须在2到10个字符之间',
            trigger: 'blur',
          },
        ],
        sex: [{ required: true, message: '性别不能为空', trigger: 'change' }],
        email: [
          { required: true, message: '邮箱不能为空', trigger: 'blur' },
          {
            type: 'email',
            message: '请输入正确的邮箱地址',
            trigger: ['blur', 'change'],
          },
        ],
        phone: [
          { required: true, message: '电话不能为空', trigger: 'blur' },
          {
            pattern: /^1[3-9]\d{9}$/,
            message: '请输入正确的手机号码',
            trigger: 'blur',
          },
        ],
      },
    }
  },

  methods: {
    // 显示充值对话框
    showRechargeDialog() {
      this.rechargeDialogVisible = true
    },

    // 处理充值请求
    handleRecharge() {
      this.$refs.rechargeForm.validate((valid) => {
        if (valid) {
          this.rechargeLoading = true
          // 模拟支付处理，实际中应调用支付接口
          setTimeout(() => {
            // 充值成功后更新用户余额
            const newBalance =
              (this.userInfo.account || 0) + this.rechargeForm.amount

            // 调用后端接口更新余额
            request
              .put('/user/recharge', {
                userId: this.userInfo.id,
                amount: this.rechargeForm.amount,
              })
              .then((response) => {
                this.rechargeLoading = false
                if (response.code === '0') {
                  // 更新成功
                  this.userInfo.account = newBalance
                  this.$message({
                    type: 'success',
                    message: `充值成功，账户余额已增加${this.rechargeForm.amount}元`,
                  })

                  // 更新本地存储的用户信息
                  sessionStorage.setItem('user', JSON.stringify(this.userInfo))

                  // 关闭对话框
                  this.rechargeDialogVisible = false
                } else {
                  this.$message.error(response.msg || '充值失败，请稍后再试')
                }
              })
              .catch((error) => {
                this.rechargeLoading = false
                console.error('充值请求出错', error)
                this.$message.error('充值请求失败，请检查网络连接')
              })
          }, 1000)
        }
      })
    },

    handleAvatarSuccess(res, file) {
      // 处理头像上传成功后的逻辑
      this.userInfo.avatar = res.data
      this.$forceUpdate()
    },
    beforeAvatarUpload(file) {
      // 限制文件类型和大小
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
    getRoleName(role) {
      switch (role) {
        case 'ADMIN':
          return '管理员'
        case 'USER':
          return '普通用户'
        case 'SUPER_ADMIN':
          return '超级管理员'
        default:
          return '未知角色'
      }
    },
    update() {
      this.$refs.userInfoForm.validate((valid) => {
        if (valid) {
          request
            .put('/user/' + this.userInfo.id, this.userInfo)
            .then((response) => {
              if (response.code == '0') {
                this.$message({
                  type: 'success',
                  message: '信息保存成功!',
                })

                sessionStorage.setItem('user', JSON.stringify(this.userInfo))
                this.$emit('update:user', this.userInfo)
              } else {
                this.$message({
                  type: 'error',
                  message: response.msg,
                })
              }
            })
        } else {
          return false
        }
      })
    },
  },
}
</script>

<style scoped>
div /deep/.el-form-item__label {
  font-weight: bold;
}

div /deep/.el-upload {
  border-radius: 50%;
}

.avatar-uploader .el-upload {
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader-icon {
  font-size: 28px;
  border: 1.5px dashed #d9d9d9;
  background-color: rgba(255, 255, 255, 0.479);
  border-radius: 6px;
  color: #006aff;
  width: 178px;
  height: 178px;
  line-height: 178px;
  transition: 0.5s;
  text-align: center;
  border-radius: 50%;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  border-radius: 50%;
}

/* 余额显示样式 */
.balance-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 300px;
}

.balance-amount {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
}

/* 充值对话框样式 */
.el-dialog__body {
  padding: 20px 30px 30px 30px;
}
</style>
