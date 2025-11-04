<template>
  <div class="user-manager">
    <el-card shadow="hover" class="main-card">
      <!-- 操作栏 -->
      <div class="action-bar">
        <div class="left-actions">
          <el-popconfirm title="确认删除选中用户?" @confirm="batchDelete">
            <template #reference>
              <el-button type="danger" icon="el-icon-delete" size="medium"
                >批量删除</el-button
              >
            </template>
          </el-popconfirm>
        </div>
        <div class="right-actions">
          <el-button
            type="primary"
            icon="el-icon-plus"
            size="medium"
            @click="handleCreate"
            >添加用户</el-button
          >
          <el-button
            type="success"
            icon="el-icon-download"
            size="medium"
            @click="exportVisible = true"
            >导出数据</el-button
          >
        </div>
      </div>

      <!-- 查询栏 -->
      <el-form
        ref="searchForm"
        :inline="true"
        :model="listQuery"
        class="search-form"
      >
        <el-form-item label="用户名">
          <el-input
            v-model="listQuery.username"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input
            v-model="listQuery.name"
            placeholder="请输入姓名"
            prefix-icon="el-icon-s-custom"
          />
        </el-form-item>
        <el-form-item v-show="userInfo.role == 'SUPER_ADMIN'" label="角色">
          <el-select
            v-model="listQuery.role"
            placeholder="请选择角色"
            style="width: 120px"
          >
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="普通用户" value="USER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-select
            v-model="listQuery.sex"
            placeholder="请选择性别"
            style="width: 120px"
          >
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="onSubmit"
            >查询</el-button
          >
          <el-button icon="el-icon-refresh" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格栏 -->
      <el-table
        ref="multipleTable"
        v-loading="listLoading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        class="user-table"
        border
        stripe
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column
          prop="id"
          label="编号"
          width="80"
          align="center"
          sortable
        />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="avatar" label="头像" width="100" align="center">
          <template slot-scope="scope">
            <el-avatar :size="40" :src="imgBaseUrl + scope.row.avatar">
              <img
                src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"
              />
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column
          v-if="userInfo.role == 'SUPER_ADMIN'"
          prop="role"
          label="角色"
          width="100"
          align="center"
        >
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.role == 'ADMIN' ? 'warning' : 'success'"
              size="small"
            >
              {{ getRoleName(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sex" label="性别" width="80" align="center" />
        <el-table-column prop="phone" label="电话" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="account" label="余额" width="100" align="right">
          <template slot-scope="scope">
            <span>¥{{ Number(scope.row.account).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              icon="el-icon-edit"
              @click="handleEdit(scope.$index, scope.row)"
              >编辑</el-button
            >
            <el-button
              type="text"
              size="small"
              icon="el-icon-delete"
              class="delete-btn"
              @click="handleDelete(scope.$index, scope.row)"
              v-if="scope.row.role !== 'SUPER_ADMIN'"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="pagination-container">
        <Pagination
          :total="total"
          :page.sync="listQuery.currentPage"
          :limit.sync="listQuery.pageSize"
          @pagination="fetchData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      :modal="false"
      width="500px"
      class="user-dialog"
      :close-on-click-modal="false"
    >
      <el-form
        ref="dialogForm"
        :model="dialogForm"
        :rules="formRules"
        label-width="80px"
        class="dialog-form"
      >
        <div v-if="isEdit" class="avatar-upload">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img
              v-if="dialogForm.avatar"
              :src="imgBaseUrl + dialogForm.avatar"
              class="avatar"
            />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
          <div class="upload-tip">点击上传头像</div>
        </div>

        <el-form-item label="用户名" prop="username">
          <el-input v-model="dialogForm.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item
          v-if="userInfo.role == 'SUPER_ADMIN'"
          label="角色"
          prop="role"
        >
          <el-select
            v-model="dialogForm.role"
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option label="普通用户" value="USER"></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input v-model="dialogForm.name" placeholder="请输入姓名" />
        </el-form-item>

        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="dialogForm.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="电话" prop="phone">
          <el-input v-model="dialogForm.phone" placeholder="请输入电话" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="dialogForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeDialog">取 消</el-button>
        <el-button type="primary" :disabled="isSubmit" @click="handleSubmit"
          >确 定</el-button
        >
      </div>
    </el-dialog>

    <!-- 导出数据对话框 -->
    <el-dialog
      title="导出数据"
      :visible.sync="exportVisible"
      width="500px"
      class="export-dialog"
    >
      <div class="export-options">
        <el-button
          type="primary"
          icon="el-icon-document"
          @click="exportTable('xlsx')"
          >EXCEL格式</el-button
        >
        <el-button
          type="success"
          icon="el-icon-document"
          @click="exportTable('csv')"
          >CSV格式</el-button
        >
        <el-button
          type="info"
          icon="el-icon-document"
          @click="exportTable('txt')"
          >TXT格式</el-button
        >
      </div>
      <div class="export-tip">请选择要导出数据的格式</div>
    </el-dialog>
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
  name: 'UserManager',
  components: { Pagination, Upload, Hints },
  data() {
    return {
      uploadUrl: '/api/file/upload',
      imgBaseUrl: this.HOST,
      // 数据列表加载动画
      listLoading: true,
      // 查询列表参数对象
      listQuery: {
        name: undefined,
        username: undefined,
        sex: undefined,
        role: undefined,
        currentPage: 1,
        pageSize: 10,
      },
      // 新增/编辑提交表单对象
      dialogForm: {
        id: undefined, // 如果是编辑用户，可能需要这个字段来标识用户
        username: undefined, // 用户名
        password: undefined, // 密码
        name: undefined, // 姓名
        avatar: undefined, // 头像URL
        role: undefined, // 角色
        sex: undefined, // 性别
        phone: undefined, // 电话
        email: undefined, // 邮箱
      },

      // 数据总条数
      total: 0,
      // 表格数据数组
      tableData: [],
      // 多选数据暂存数组
      multipleSelection: [],
      // 新增/编辑 弹出框显示/隐藏
      dialogVisible: false,
      isEdit: false,
      categoryList: [],
      // 导入数据 弹出框 关闭事件
      // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
      formRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          {
            min: 3,
            max: 20,
            message: '长度在 3 到 20 个字符',
            trigger: 'blur',
          },
        ],
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }],
        sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
        phone: [{ validator: this.validatePhone, trigger: 'blur' }],
        email: [
          {
            type: 'email',
            message: '请输入有效的邮箱地址',
            trigger: ['blur', 'change'],
          },
        ],
      },
      // 防止多次连续提交表单
      isSubmit: false,

      // 导出文件格式
      filesFormat: '.txt, .csv, .xls, .xlsx',
      // 导出数据 弹出框显示/隐藏
      exportVisible: false,
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '修改用户信息' : '添加用户'
    },
  },
  created() {
    this.fetchData()
  },
  methods: {
    handleAvatarSuccess(res, file) {
      // 处理头像上传成功后的逻辑
      this.dialogForm.avatar = res.data
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
    onUploadSuccess(res) {
      this.dialogForm.img = res.data
    },
    getCompleteImgUrl(url) {
      return this.HOST + url
    },
    // 多选操作
    handleSelectionChange(val) {
      this.multipleSelection = val.map((v) => v.id)
    },
    // 新建数据
    handleCreate() {
      this.isEdit = false
      this.dialogForm = {}
      this.dialogVisible = true
    },
    getRoleName(role) {
      switch (role) {
        case 'USER':
          return '普通用户'
        case 'ADMIN':
          return '管理员'
        default:
          return '未知'
      }
    },
    // 编辑数据
    handleEdit(index, row) {
      this.isEdit = true
      this.dialogForm = JSON.parse(JSON.stringify(row))
      this.dialogVisible = true
    },
    closeDialog() {
      this.dialogVisible = false
      this.dialogForm = {}
    },
    handleSubmit() {
      if (this.isEdit) {
        this.handleEditSave('dialogForm')
      } else {
        this.handleAdd('dialogForm')
      }
    },
    handleAdd(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          Request.post('/user', this.dialogForm).then((response) => {
            if (response.code == 0) {
              this.$message({
                showClose: true,
                message: '用户添加成功，默认密码为' + response.data.password,
                type: 'success',
              })
              this.fetchData()
            } else {
              this.$message({
                showClose: true,
                message: response.msg,
                type: 'error',
              })
            }
          })
          this.closeDialog()
          return true
        } else {
          this.isSubmit = false
          return false
        }
      })
    },
    // 删除数据
    handleDelete(index, row) {
      this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          Request.delete('/user/' + row.id).then((response) => {
            if (response.code == 0) {
              this.$message({
                type: 'success',
                message: '删除成功!',
              })
              this.onReset()
            } else {
              this.$message({
                type: 'success',
                error: '删除失败!',
              })
            }
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除',
          })
        })
    },
    // 批量删除
    batchDelete() {
      if (this.multipleSelection.length < 1) {
        this.$message({
          message: '请先选择要删除的数据！',
          type: 'warning',
        })
      } else {
        Request.delete(
          `/user/deleteBatch?ids=${this.multipleSelection.join(',')}`
        ).then((res) => {
          if (res.code === '0') {
            this.$message({
              showClose: true,
              message: '批量删除成功',
              type: 'success',
            })
            this.onReset()
          } else {
            this.$message({
              showClose: true,
              message: res.msg,
              type: 'error',
            })
          }
        })
      }
    },

    // 获取数据列表
    fetchData() {
      this.listLoading = true
      Request.get('/user/page', {
        params: {
          username: this.listQuery.username,
          name: this.listQuery.name,
          sex: this.listQuery.sex,
          role: this.listQuery.role,
          currentRole: this.userInfo.role,
          currentPage: this.listQuery.currentPage,
          size: this.listQuery.pageSize,
        },
      }).then((response) => {
        const data = response.data
        if (response.code === '0') {
          this.total = data.total
          this.tableData = data.records
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
      this.listQuery.pageSize = 10
      this.listQuery.name = ''
      this.listQuery.username = ''
      this.listQuery.sex = ''
      this.listQuery.role = ''
      this.fetchData()
    },

    handleEditSave(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          Request.put('/user/' + this.dialogForm.id, this.dialogForm).then(
            (response) => {
              if (response.code == 0) {
                this.$message({
                  showClose: true,
                  message: '更新成功',
                  type: 'success',
                })
                this.closeDialog()
                this.fetchData()
              } else {
                this.$message({
                  showClose: true,
                  message: '更新失败',
                  type: 'error',
                })
              }
            }
          )
        } else {
        }
      })
    },

    // 导出数据--excle格式
    exportTable(type) {
      if (this.tableData.length) {
        const params = {
          header: [
            '编号',
            '用户名',
            '姓名',
            '头像',
            '角色',
            '性别',
            '电话',
            '邮箱',
          ],
          key: [
            'id',
            'username',
            'name',
            'avatar',
            'role',
            'sex',
            'phone',
            'email',
          ],
          data: this.tableData,
          autoWidth: true,
          fileName: '用户数据表',
          bookType: type,
        }
        excel.exportDataToExcel(params)
        this.exportVisible = false
      }
    },
  },
}
</script>

<style lang="less">
.user-manager {
  padding: 20px;
  background-color: #ffffff;
  min-height: 100vh;

  .main-card {
    border-radius: 8px;
    background-color: #ffffff;
  }

  .action-bar {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .right-actions {
      .el-button {
        margin-left: 10px;
      }
    }
  }

  .search-form {
    background-color: #ffffff;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    .el-form-item {
      margin-bottom: 0;
    }
  }

  .user-table {
    margin-bottom: 20px;
    background-color: #ffffff;

    .el-table__header-wrapper {
      th {
        background-color: #f2f3f7;
        color: #606266;
        font-weight: 600;
      }
    }

    .delete-btn {
      color: #f56c6c;
    }
  }

  .pagination-container {
    text-align: right;
    margin-top: 20px;
  }

  .user-dialog {
    .avatar-upload {
      text-align: center;
      margin-bottom: 20px;

      .avatar-uploader {
        .el-upload {
          border: 1px dashed #d9d9d9;
          border-radius: 50%;
          cursor: pointer;
          position: relative;
          overflow: hidden;
          transition: all 0.3s;

          &:hover {
            border-color: #409eff;
          }
        }

        .avatar-uploader-icon {
          font-size: 28px;
          color: #8c939d;
          width: 100px;
          height: 100px;
          line-height: 100px;
          text-align: center;
        }

        .avatar {
          width: 100px;
          height: 100px;
          display: block;
          border-radius: 50%;
        }
      }

      .upload-tip {
        font-size: 12px;
        color: #909399;
        margin-top: 5px;
      }
    }

    .dialog-form {
      .el-input {
        width: 100%;
      }
    }
  }

  .export-dialog {
    .export-options {
      display: flex;
      justify-content: space-around;
      margin-bottom: 20px;
    }

    .export-tip {
      text-align: center;
      color: #909399;
      font-size: 12px;
    }
  }
}
</style>
