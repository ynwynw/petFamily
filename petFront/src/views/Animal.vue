<template>
  <div class="animal-manager">
    <el-card shadow="hover" class="main-card">
      <!-- 操作栏 -->
      <div class="action-bar">
        <div class="left-actions">
          <el-popconfirm title="确认删除选中宠物?" @confirm="batchDelete">
            <template #reference>
              <el-button type="danger" icon="el-icon-delete" size="medium"
                >批量删除</el-button
              >
            </template>
          </el-popconfirm>
        </div>
        <div class="right-actions">
          <el-button
            v-if="userInfo.role != 'USER'"
            type="primary"
            icon="el-icon-plus"
            size="medium"
            @click="handleCreate"
            >添加宠物</el-button
          >
          <el-button
            v-if="userInfo.role != 'USER'"
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
        <el-form-item label="宠物名字">
          <el-input
            v-model="listQuery.name"
            placeholder="请输入宠物名字"
            prefix-icon="el-icon-search"
          />
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
        class="animal-table"
        border
        stripe
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="img" label="宠物头像" width="100" align="center">
          <template #default="scope">
            <el-image
              :src="imgBaseUrl + scope.row.img"
              alt="宠物头像"
              :preview-src-list="[imgBaseUrl + scope.row.img]"
              style="height: 50px; width: 50px; border-radius: 5px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="宠物名字" min-width="120" />
        <el-table-column prop="type" label="宠物种类" min-width="100" />
        <el-table-column
          prop="sex"
          label="宠物性别"
          width="80"
          align="center"
        />
        <el-table-column prop="age" label="宠物年龄" width="100" align="center">
          <template #default="scope"> {{ scope.row.age }}岁 </template>
        </el-table-column>
        <el-table-column
          prop="status"
          label="宠物状态"
          width="100"
          align="center"
        >
          <template #default="scope">
            <el-tag
              :type="scope.row.status === '待领养' ? 'warning' : 'success'"
              size="small"
            >
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="breed" label="宠物品种" min-width="120" />
        <el-table-column
          prop="registrationDate"
          label="注册日期"
          width="120"
          align="center"
        >
          <template #default="scope">
            {{ scope.row.registrationDate }}
          </template>
        </el-table-column>
        <el-table-column
          prop="behaviorTraits"
          label="行为特征"
          min-width="120"
        />
        <el-table-column
          prop="sterilizationStatus"
          label="绝育状况"
          width="100"
          align="center"
        />
        <el-table-column prop="color" label="颜色" width="100" align="center" />
        <el-table-column
          prop="trainingLevel"
          label="训练状况"
          width="100"
          align="center"
        />
        <el-table-column prop="specialNeeds" label="特殊要求" min-width="120" />
        <el-table-column
          prop="descr"
          label="宠物介绍"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          label="操作"
          width="180"
          align="center"
          fixed="right"
          v-if="userInfo.role != 'USER'"
        >
          <template #default="scope">
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
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="pagination-container">
        <Pagination
          :total="total"
          :page="listQuery.currentPage"
          :limit="listQuery.pageSize"
          @pagination="fetchData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="500px"
      class="animal-dialog"
      :close-on-click-modal="false"
      :modal="false"
    >
      <el-form
        ref="dialogForm"
        :model="dialogForm"
        :rules="formRules"
        label-width="100px"
        class="dialog-form"
      >
        <div class="avatar-upload">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img
              v-if="dialogForm.img"
              :src="imgBaseUrl + dialogForm.img"
              class="avatar"
            />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
          <div class="upload-tip">点击上传宠物头像</div>
        </div>

        <el-form-item prop="name" label="宠物名字">
          <el-input v-model="dialogForm.name" placeholder="请输入宠物名字" />
        </el-form-item>
        <el-form-item prop="sex" label="宠物性别">
          <el-select
            v-model="dialogForm.sex"
            placeholder="请选择性别"
            style="width: 100%"
          >
            <el-option label="公" value="公"></el-option>
            <el-option label="母" value="母"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="age" label="宠物年龄">
          <el-input v-model.number="dialogForm.age" placeholder="请输入年龄" />
        </el-form-item>
        <el-form-item prop="type" label="宠物种类">
          <el-input v-model="dialogForm.type" placeholder="请输入宠物种类" />
        </el-form-item>
        <el-form-item prop="status" label="宠物状态">
          <el-select
            v-model="dialogForm.status"
            placeholder="请选择状态"
            style="width: 100%"
          >
            <el-option label="待领养" value="待领养"></el-option>
            <el-option label="已领养" value="已领养"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="breed" label="宠物品种">
          <el-select
            v-model="dialogForm.breed"
            placeholder="请选择品种"
            style="width: 100%"
          >
            <el-option
              v-for="breed in breedList"
              :key="breed.breedId"
              :label="breed.breedName"
              :value="breed.breedName"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="descr" label="宠物介绍">
          <el-input
            type="textarea"
            :rows="4"
            v-model="dialogForm.descr"
            placeholder="请输入宠物介绍"
          />
        </el-form-item>
        <el-form-item prop="registrationDate" label="注册日期">
          <el-date-picker
            v-model="dialogForm.registrationDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item prop="behaviorTraits" label="行为特征">
          <el-input
            v-model="dialogForm.behaviorTraits"
            placeholder="请输入行为特征"
          />
        </el-form-item>
        <el-form-item prop="sterilizationStatus" label="绝育状况">
          <el-select
            v-model="dialogForm.sterilizationStatus"
            placeholder="请选择绝育状况"
            style="width: 100%"
          >
            <el-option label="无需绝育" value="无需绝育"></el-option>
            <el-option label="已绝育" value="已绝育"></el-option>
            <el-option label="未绝育" value="未绝育"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="color" label="颜色">
          <el-input v-model="dialogForm.color" placeholder="请输入颜色" />
        </el-form-item>
        <el-form-item prop="trainingLevel" label="训练状况">
          <el-select
            v-model="dialogForm.trainingLevel"
            placeholder="请选择训练状况"
            style="width: 100%"
          >
            <el-option label="未知" value="未知"></el-option>
            <el-option label="未训练" value="未训练"></el-option>
            <el-option label="基础训练" value="基础训练"></el-option>
            <el-option label="终极训练" value="终极训练"></el-option>
            <el-option label="高级训练" value="高级训练"></el-option>
            <el-option label="专业训练" value="专业训练"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="specialNeeds" label="特殊要求">
          <el-input
            v-model="dialogForm.specialNeeds"
            placeholder="请输入特殊要求"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button
            type="primary"
            :disabled="isSubmit"
            @click="
              isEdit ? handleEditSave('dialogForm') : handleAdd('dialogForm')
            "
            >确 定</el-button
          >
        </div>
      </template>
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
  name: 'Animal',
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
        currentPage: 1,
        pageSize: 10,
      },
      // 新增/编辑提交表单对象
      dialogForm: {
        name: '',
        sex: '',
        age: '',
        type: '',
        breed: '',
        status: '',
        registrationDate: '',
        behaviorTraits: '',
        sterilizationStatus: '',
        color: '',
        trainingLevel: '',
        specialNeeds: '',
        descr: '',
      },
      // 数据总条数
      total: 0,
      // 表格数据数组
      tableData: [],
      breedList: [],
      // 多选数据暂存数组
      multipleSelection: [],
      // 新增/编辑 弹出框显示/隐藏
      dialogVisible: false,
      isEdit: false,
      categoryList: [],
      // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
      formRules: {
        name: [
          { required: true, message: '宠物名字不能为空', trigger: 'blur' },
          {
            min: 1,
            max: 255,
            message: '宠物名字长度必须在1到255个字符之间',
            trigger: 'blur',
          },
        ],
        sex: [{ required: true, message: '请选择宠物性别', trigger: 'change' }],
        age: [
          { required: true, message: '宠物年龄不能为空', trigger: 'blur' },
          { type: 'number', message: '宠物年龄必须为数字', trigger: 'blur' },
        ],
        type: [
          { required: true, message: '宠物种类不能为空', trigger: 'blur' },
          {
            min: 1,
            max: 255,
            message: '宠物种类长度必须在1到255个字符之间',
            trigger: 'blur',
          },
        ],
        breed: [
          { required: true, message: '请选择宠物品种', trigger: 'change' },
        ],
        descr: [
          { required: true, message: '宠物介绍不能为空', trigger: 'blur' },
          {
            min: 1,
            max: 255,
            message: '宠物介绍长度必须在1到255个字符之间',
            trigger: 'blur',
          },
        ],
        registrationDate: [
          { required: true, message: '请选择注册日期', trigger: 'change' },
          {
            validator: (rule, value, callback) => {
              const today = new Date()
              if (value > today) {
                callback(new Error('注册日期不得大于今天'))
              } else {
                callback()
              }
            },
            trigger: 'change',
          },
        ],
        behaviorTraits: [
          { required: true, message: '请输入行为特征', trigger: 'blur' },
          {
            min: 2,
            max: 50,
            message: '长度在 2 到 50 个字符',
            trigger: 'blur',
          },
        ],
        sterilizationStatus: [
          { required: true, message: '请选择绝育状况', trigger: 'change' },
        ],
        color: [
          { required: true, message: '请输入颜色', trigger: 'blur' },
          {
            min: 2,
            max: 20,
            message: '长度在 2 到 20 个字符',
            trigger: 'blur',
          },
        ],
        trainingLevel: [
          { required: true, message: '请选择训练状况', trigger: 'change' },
        ],
        specialNeeds: [
          { required: true, message: '请输入特殊要求', trigger: 'blur' },
          {
            min: 2,
            max: 50,
            message: '长度在 2 到 50 个字符',
            trigger: 'blur',
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
      return this.isEdit ? '修改宠物信息' : '添加宠物'
    },
  },
  created() {
    this.fetchData()
  },
  methods: {
    handleAvatarSuccess(res, file) {
      // 处理头像上传成功后的逻辑
      this.dialogForm.img = res.data
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
      console.log('handleCreate called')
      this.loadBreed()
      this.isEdit = false
      this.dialogForm = {
        name: '',
        sex: '',
        age: '',
        type: '',
        breed: '',
        status: '待领养',
        registrationDate: new Date(),
        behaviorTraits: '',
        sterilizationStatus: '',
        color: '',
        trainingLevel: '',
        specialNeeds: '',
        descr: '',
      }
      this.dialogVisible = true
      console.log('dialogVisible set to true:', this.dialogVisible)
    },
    // 编辑数据
    handleEdit(index, row) {
      console.log('handleEdit called with row:', row)
      this.loadBreed()
      this.isEdit = true
      this.dialogForm = JSON.parse(JSON.stringify(row))
      this.dialogVisible = true
      console.log('dialogVisible set to true:', this.dialogVisible)
    },
    loadBreed() {
      Request.get('/breed/selectAll').then((response) => {
        if (response.code == 0) {
          this.breedList = response.data
        } else {
          this.$message({
            showClose: true,
            message: response.msg,
            type: 'error',
          })
        }
      })
    },
    handleAdd(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.isSubmit = true
          Request.post('/animal', this.dialogForm)
            .then((response) => {
              if (response.code == 0) {
                this.$message({
                  showClose: true,
                  message: '添加成功',
                  type: 'success',
                })
                this.dialogVisible = false
                this.fetchData()
              } else {
                this.$message({
                  showClose: true,
                  message: response.msg,
                  type: 'error',
                })
              }
              this.isSubmit = false
            })
            .catch(() => {
              this.isSubmit = false
            })
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
          Request.delete('/animal/' + row.id).then((response) => {
            if (response.code == 0) {
              this.$message({
                type: 'success',
                message: '删除成功!',
              })
              this.onReset()
            } else {
              this.$message({
                type: 'success',
                errpr: '删除失败!',
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
          `/animal/deleteBatch?ids=${this.multipleSelection.join(',')}`
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
      console.log('page:' + this.listQuery.currentPage)
      Request.get('/animal/page', {
        params: {
          name: this.listQuery.name,
          pageNum: this.listQuery.currentPage,
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
      this.fetchData()
    },
    handleEditSave(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.isSubmit = true
          Request.put('/animal/' + this.dialogForm.id, this.dialogForm)
            .then((response) => {
              if (response.code == 0) {
                this.$message({
                  showClose: true,
                  message: '更新成功',
                  type: 'success',
                })
                this.dialogVisible = false
                this.fetchData()
              } else {
                this.$message({
                  showClose: true,
                  message: '更新失败',
                  type: 'error',
                })
              }
              this.isSubmit = false
            })
            .catch(() => {
              this.isSubmit = false
            })
        } else {
          this.isSubmit = false
        }
      })
    },
    exportTable(type) {
      if (this.tableData.length) {
        const params = {
          header: [
            '宠物头像',
            '宠物名字',
            '宠物种类',
            '宠物性别',
            '宠物年龄',
            '宠物状态',
            '宠物品种',
            '宠物介绍',
          ],
          key: [
            'img',
            'name',
            'type',
            'sex',
            'age',
            'status',
            'breed',
            'descr',
          ],
          data: this.tableData.map((item) => {
            return {
              img: item.img,
              name: item.name,
              type: item.type,
              sex: item.sex,
              age: `${item.age}岁`,
              status: item.status,
              breed: item.breed,
              descr: item.descr,
            }
          }),
          autoWidth: true,
          fileName: '宠物信息表',
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
.animal-manager {
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

  .animal-table {
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

  .animal-dialog {
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
