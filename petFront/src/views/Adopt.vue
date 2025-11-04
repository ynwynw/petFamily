<template>
  <div class="category-wrapper">
    <el-card shadow="always">
      <!-- 操作栏 -->
      <div class="control-btns">
        <el-popconfirm title="确认删除?" @confirm="batchDelete">
          <template #reference>
            <el-button
              type="danger"
              size="medium"
              style="background-color: red; border-color: red"
              >批量删除</el-button
            >
          </template>
        </el-popconfirm>

        <el-button
          type="primary"
          size="medium"
          @click="exportVisible = true"
          style="
            float: right;
            margin-right: 10px;
            background-color: blue;
            border-color: blue;
          "
          >导出数据</el-button
        >
      </div>
      <!-- 查询栏 -->
      <el-form
        ref="searchForm"
        :inline="true"
        :model="listQuery"
        label-width="90px"
        class="search-form"
      >
        <el-form-item label="宠物名字">
          <el-input v-model="listQuery.name" placeholder="请输入宠物名字" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="onSubmit">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="danger" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格栏 -->
      <el-table
        ref="multipleTable"
        v-loading="listLoading"
        :data="tableData"
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="55"
          align="center"
        ></el-table-column>
        <el-table-column
          prop="id"
          label="序号"
          width="80"
          align="center"
          sortable
        ></el-table-column>
        <el-table-column prop="animalImg" label="宠物头像" width="100">
          <template slot-scope="scope">
            <el-image
              :src="imgBaseUrl + scope.row.animalImg"
              alt="宠物头像"
              width="100"
              :preview-src-list="[imgBaseUrl + scope.row.animalImg]"
              style="height: 50px; width: 50px; border-radius: 5px"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="animalName"
          label="宠物姓名"
          show-overflow-tooltip
        ></el-table-column>
        <el-table-column
          prop="userName"
          label="领养人"
          show-overflow-tooltip
        ></el-table-column>
        <el-table-column prop="time" label="领养时间"></el-table-column>
        <el-table-column prop="status" label="领养状态">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{
              scope.row.status
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="reviewComment"
          label="审核意见"
          show-overflow-tooltip
        ></el-table-column>
        <el-table-column
          prop="reviewTime"
          label="审核时间"
          show-overflow-tooltip
        ></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="handleReview(scope.row)"
              v-if="
                scope.row.status === '审核中' &&
                (userInfo.role === 'SUPER_ADMIN' || userInfo.role === 'ADMIN')
              "
              >审核</el-button
            >
            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(scope.$index, scope.row)"
              v-if="
                scope.row.role === 'SUPER_ADMIN' || scope.row.role === 'ADMIN'
              "
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页栏 -->
      <Pagination
        :total="total"
        :page.sync="listQuery.currentPage"
        :limit.sync="listQuery.pageSize"
        @pagination="fetchData"
      />

      <!-- 审核弹出框 -->
      <el-dialog
        title="审核领养申请"
        :visible.sync="reviewFormVisible"
        width="40%"
        :modal="false"
      >
        <el-form
          ref="reviewForm"
          :model="reviewForm"
          label-width="100px"
          class="dialog-form"
        >
          <el-form-item label="宠物姓名">
            <el-input v-model="reviewForm.animalName" disabled></el-input>
          </el-form-item>
          <el-form-item label="领养人">
            <el-input v-model="reviewForm.userName" disabled></el-input>
          </el-form-item>
          <el-form-item label="领养时间">
            <el-input v-model="reviewForm.time" disabled></el-input>
          </el-form-item>
          <el-form-item label="审核结果">
            <el-radio-group v-model="reviewForm.status">
              <el-radio label="领养中">通过</el-radio>
              <el-radio label="审核不通过">不通过</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审核意见">
            <el-input
              v-model="reviewForm.reviewComment"
              type="textarea"
              rows="3"
              placeholder="请输入审核意见"
            ></el-input>
          </el-form-item>
          <el-form-item class="footer-item">
            <el-button @click="reviewFormVisible = false">取消</el-button>
            <el-button type="primary" @click="submitReview">确定</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>

      <!-- 导出数据 弹出栏 -->
      <el-dialog title="导出数据" :visible.sync="exportVisible" width="500px">
        <div
          class="export-data"
          style="
            display: flex;
            flex-direction: row;
            justify-content: space-between;
          "
        >
          <el-button type="primary" @click="exportTable('xlsx')"
            >EXCEL格式</el-button
          >
          <el-button type="primary" @click="exportTable('csv')"
            >CSV格式</el-button
          >
          <el-button type="primary" @click="exportTable('txt')"
            >TXT格式</el-button
          >
        </div>
        <div class="hints">TIP：请选择要导出数据的格式。</div>
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

      // 数据总条数
      total: 0,
      // 表格数据数组
      tableData: [],
      // 多选数据暂存数组
      multipleSelection: [],

      // 审核表单显示状态
      reviewFormVisible: false,
      // 审核表单数据
      reviewForm: {
        id: null,
        animalName: '',
        userName: '',
        time: '',
        status: '领养中',
        reviewComment: '',
        reviewerId: null,
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
    // 获取状态对应的类型
    getStatusType(status) {
      const statusMap = {
        领养中: 'success',
        待领养: 'info',
        审核中: 'warning',
        审核不通过: 'danger',
      }
      return statusMap[status] || 'info'
    },

    // 处理审核
    handleReview(row) {
      this.reviewForm = {
        id: row.id,
        animalName: row.animalName,
        userName: row.userName,
        time: row.time,
        status: '领养中',
        reviewComment: '',
        reviewerId: this.userInfo.id,
      }
      this.reviewFormVisible = true
    },

    // 提交审核
    submitReview() {
      if (!this.reviewForm.reviewComment) {
        this.$message.warning('请填写审核意见')
        return
      }

      Request.put(`/adopt/review/${this.reviewForm.id}`, this.reviewForm)
        .then((response) => {
          if (response.code == 0) {
            this.$message({
              showClose: true,
              message: '审核成功',
              type: 'success',
            })
            this.reviewFormVisible = false
            this.fetchData()
          } else {
            this.$message({
              showClose: true,
              message: response.msg || '审核失败',
              type: 'error',
            })
          }
        })
        .catch((error) => {
          console.error(error)
          this.$message({
            showClose: true,
            message: '审核失败，请稍后重试',
            type: 'error',
          })
        })
    },

    // 多选操作
    handleSelectionChange(val) {
      this.multipleSelection = val.map((v) => v.id)
    },

    // 删除数据
    handleDelete(index, row) {
      this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          Request.delete('/adopt/' + row.id).then((response) => {
            if (response.code == 0) {
              this.$message({
                type: 'success',
                message: '删除成功!',
              })
              this.onReset()
            } else {
              this.$message({
                type: 'error',
                message: '删除失败!',
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
          `/adopt/deleteBatch?ids=${this.multipleSelection.join(',')}`
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
      Request.get('/adopt/selectPage', {
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

    exportTable(type) {
      if (this.tableData.length) {
        const params = {
          header: [
            '序号',
            '宠物头像',
            '宠物姓名',
            '领养人',
            '领养时间',
            '领养状态',
            '审核意见',
            '审核时间',
          ],
          key: [
            'id',
            'animalImg',
            'animalName',
            'userName',
            'time',
            'status',
            'reviewComment',
            'reviewTime',
          ],
          data: this.tableData.map((item) => {
            return {
              id: item.id,
              animalImg: item.animalImg,
              animalName: item.animalName,
              userName: item.userName,
              time: item.time,
              status: item.status,
              reviewComment: item.reviewComment || '无',
              reviewTime: item.reviewTime || '无',
            }
          }),
          autoWidth: true,
          fileName: '宠物领养数据表',
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
.category-wrapper {
  .el-card {
    min-height: 656px;
  }

  .control-btns {
    margin-bottom: 20px;
  }

  .search-form {
    padding: 10px !important;
    background-color: #f7f8fb;
    height: 40px !important;
    line-height: 40px !important;
  }

  .el-table thead {
    font-weight: 600;

    th {
      background-color: #f2f3f7;
    }
  }

  .dialog-form {
    .el-input {
      width: 90%;
    }

    .footer-item {
      margin-top: 50px;
      text-align: right;
    }
  }

  .upload-box,
  .export-data {
    width: 300px;
    margin: 0 auto 30px;
  }

  .upload-box {
    width: 156px;

    .files-upload {
      color: #20a0ff;
    }
  }

  .hints {
    font-size: 12px;
    color: #aaa;
    text-align: center;
  }
}
</style>
