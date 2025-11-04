<template>
  <div class="goods-wrapper">
    <el-card shadow="hover" class="main-card">
      <!-- 操作栏 -->
      <div class="action-bar">
        <div class="left-actions">
          <el-popconfirm title="确认删除?" @confirm="batchDelete">
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
            >添加商品</el-button
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
        label-width="90px"
        class="search-form"
      >
        <el-form-item label="商品名称">
          <el-input
            v-model="listQuery.name"
            placeholder="请输入商品名称"
            prefix-icon="el-icon-goods"
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
        class="goods-table"
        border
        stripe
      >
        <el-table-column type="selection" width="60" align="center" />
        <el-table-column
          prop="id"
          label="编号"
          width="80"
          sortable
          align="center"
        />
        <el-table-column
          prop="name"
          label="商品名称"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column
          prop="price"
          label="商品价格"
          width="100"
          align="center"
        />
        <el-table-column prop="img" label="商品图片" width="100" align="center">
          <template slot-scope="scope">
            <el-image
              :src="imgBaseUrl + scope.row.img"
              alt="商品图片"
              :preview-src-list="[imgBaseUrl + scope.row.img]"
              class="product-image"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="num"
          label="剩余数量"
          width="100"
          align="center"
        />
        <el-table-column
          prop="desrc"
          label="商品描述"
          min-width="200"
          show-overflow-tooltip
        />
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

      <!-- 新增 弹出栏 -->
      <el-dialog
        title="添加商品"
        :visible.sync="formVisibleAdd"
        width="500px"
        class="dialog-form"
        :modal="false"
      >
        <el-form
          ref="dialogForm"
          :model="dialogForm"
          :rules="formRules"
          label-width="100px"
        >
          <div class="avatar-upload">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
            >
              <el-image
                v-if="dialogForm.img"
                :src="imgBaseUrl + dialogForm.img"
                class="avatar"
              />
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <div class="upload-tip">点击上传商品图片</div>
          </div>

          <el-form-item prop="name" label="商品名称">
            <el-input
              v-model="dialogForm.name"
              placeholder="请输入商品名称"
            ></el-input>
          </el-form-item>
          <el-form-item prop="price" label="商品价格">
            <el-input
              v-model="dialogForm.price"
              placeholder="请输入商品价格"
              @input="handleInput('num')"
            />
          </el-form-item>
          <el-form-item prop="num" label="剩余数量">
            <el-input
              v-model.number="dialogForm.num"
              placeholder="请输入商品数量"
              @input="handleInput('num')"
            />
          </el-form-item>
          <el-form-item prop="desrc" label="商品描述">
            <el-input
              resize="false"
              type="textarea"
              :rows="4"
              v-model="dialogForm.desrc"
              placeholder="请输入商品描述"
            ></el-input>
          </el-form-item>

          <div class="dialog-footer">
            <el-button @click="formVisibleAdd = false">取消</el-button>
            <el-button
              type="primary"
              :disabled="isSubmit"
              @click="handleAdd('dialogForm')"
              >确定</el-button
            >
          </div>
        </el-form>
      </el-dialog>

      <!-- 编辑 弹出栏 -->
      <el-dialog
        title="修改商品信息"
        :visible.sync="formVisibleEdit"
        width="500px"
        class="dialog-form"
      >
        <el-form
          ref="dialogForm"
          :model="dialogForm"
          :rules="formRules"
          label-width="100px"
        >
          <div class="avatar-upload">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
            >
              <el-image
                v-if="dialogForm.img"
                :src="imgBaseUrl + dialogForm.img"
                class="avatar"
              />
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <div class="upload-tip">点击上传商品图片</div>
          </div>

          <el-form-item prop="name" label="商品名称">
            <el-input
              v-model="dialogForm.name"
              placeholder="请输入商品名称"
            ></el-input>
          </el-form-item>
          <el-form-item prop="price" label="商品价格">
            <el-input
              v-model="dialogForm.price"
              placeholder="请输入商品价格"
              @input="handleInput('num')"
            />
          </el-form-item>
          <el-form-item prop="num" label="剩余数量">
            <el-input
              v-model.number="dialogForm.num"
              min="0"
              placeholder="请输入商品数量"
              @input="handleInput('num')"
            />
          </el-form-item>
          <el-form-item prop="desrc" label="商品描述">
            <el-input
              resize="false"
              type="textarea"
              :rows="4"
              v-model="dialogForm.desrc"
              placeholder="请输入商品描述"
            ></el-input>
          </el-form-item>
          <div class="dialog-footer">
            <el-button @click="formVisibleEdit = false">取消</el-button>
            <el-button
              type="primary"
              :disabled="isSubmit"
              @click="handleEditSave('dialogForm')"
              >确定</el-button
            >
          </div>
        </el-form>
      </el-dialog>

      <!-- 导出数据 弹出栏 -->
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
    </el-card>
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

        currentPage: 1,
        pageSize: 10,
      },
      // 新增/编辑提交表单对象
      dialogForm: {
        name: undefined,
        price: undefined,
        num: undefined,
        desrc: undefined,
        img: undefined,
      },

      // 数据总条数
      total: 0,
      // 表格数据数组
      tableData: [],
      // 多选数据暂存数组
      multipleSelection: [],
      // 新增/编辑 弹出框显示/隐藏
      formVisible: false,
      formVisibleAdd: false,
      formVisibleEdit: false,
      categoryList: [],
      // 导入数据 弹出框 关闭事件
      // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
      formRules: {
        name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        price: [
          { required: true, message: '请输入商品价格', trigger: 'blur' },
          // 必须为数字
          { validator: this.validatePrice, trigger: 'blur' },
        ],
        num: [
          { required: true, message: '请输入商品数量', trigger: 'blur' },

          //必须为数字
          { type: 'number', message: '商品数量必须为数字', trigger: 'blur' },
        ],
        desrc: [{ required: true, message: '请输入商品描述', trigger: 'blur' }],
      },
      // 防止多次连续提交表单
      isSubmit: false,

      // 导出文件格式
      filesFormat: '.txt, .csv, .xls, .xlsx',
      // 导出数据 弹出框显示/隐藏
      exportVisible: false,
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    validatePrice(rule, value, callback) {
      if (!value || isNaN(value) || value < 0) {
        callback(new Error('商品价格必须为非负数且为有效数字'))
      } else {
        callback()
      }
    },
    handleInput(field) {
      // 确保输入的数字不为负数
      if (this.dialogForm[field] < 0) {
        this.dialogForm[field] = 0
      }
    },
    handleAvatarSuccess(res, file) {
      // 处理上传成功后的逻辑
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
      this.formVisibleAdd = true
      this.dialogForm = {}
    },

    // 编辑数据
    handleEdit(index, row) {
      this.dialogForm = JSON.parse(JSON.stringify(row))
      this.formVisibleEdit = true
    },
    handleAdd(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          Request.post('/goods', this.dialogForm).then((response) => {
            if (response.code == 0) {
              this.$message({
                showClose: true,
                message: '商品添加成功',
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
          this.formVisibleAdd = false
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
          Request.delete('/goods/' + row.id).then((response) => {
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
          `/goods/deleteBatch?ids=${this.multipleSelection.join(',')}`
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
      Request.get('/goods/page', {
        params: {
          name: this.listQuery.name,

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

      this.fetchData()
    },

    handleEditSave(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          Request.put('/goods/' + this.dialogForm.id, this.dialogForm).then(
            (response) => {
              if (response.code == 0) {
                this.$message({
                  showClose: true,
                  message: '更新成功',
                  type: 'success',
                })
                this.formVisibleEdit = false
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

    exportTable(type) {
      if (this.tableData.length) {
        const params = {
          header: [
            '编号',
            '商品名称',
            '商品价格',
            '商品图片',
            '剩余数量',
            '商品描述',
          ],
          key: ['id', 'name', 'price', 'img', 'num', 'desrc'],
          data: this.tableData,
          autoWidth: true,
          fileName: '商品信息表',
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
.goods-wrapper {
  padding: 20px;
  background-color: #ffffff;
  min-height: 100vh;

  .main-card {
    border-radius: 8px;
    background-color: #ffffff;
    min-height: 656px;
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
    background-color: #f7f8fb;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.03);

    .el-form-item {
      margin-bottom: 0;
    }
  }

  .goods-table {
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

    .product-image {
      height: 50px;
      width: 50px;
      border-radius: 5px;
      object-fit: cover;
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
      transition: transform 0.3s;

      &:hover {
        transform: scale(1.05);
      }
    }
  }

  .pagination-container {
    text-align: right;
    margin-top: 20px;
  }

  .dialog-form {
    .el-input {
      width: 100%;
    }

    .dialog-footer {
      margin-top: 30px;
      text-align: right;
    }

    .avatar-upload {
      text-align: center;
      margin-bottom: 25px;

      .upload-tip {
        font-size: 12px;
        color: #909399;
        margin-top: 5px;
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

  .avatar-uploader .el-upload {
    cursor: pointer;
    position: relative;
    overflow: hidden;
    border-radius: 50%;
    border: 1px dashed #d9d9d9;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }

  .avatar-uploader-icon {
    font-size: 28px;
    color: #409eff;
    width: 120px;
    height: 120px;
    line-height: 120px;
    text-align: center;
    border-radius: 50%;
  }

  .avatar {
    width: 120px;
    height: 120px;
    display: block;
    border-radius: 50%;
    object-fit: cover;
  }
}
</style>
