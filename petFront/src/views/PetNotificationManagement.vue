<template>
  <div class="notification-wrapper">
    <el-card shadow="always">
      <!-- 操作栏 -->
      <div class="control-btns">
        <el-button type="primary" @click="handleSendNotification">发送通知</el-button>
        <el-button type="danger" @click="batchDelete">批量删除</el-button>
      </div>

      <!-- 查询栏 -->
      <el-form ref="searchForm" :inline="true" :model="listQuery" label-width="80px" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="listQuery.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="listQuery.status" placeholder="请选择状态">
            <el-option label="未读" value="unread" />
            <el-option label="已读" value="read" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 通知列表 -->
      <el-table :data="tableData" style="width: 100%" @selection-change="handleSelectionChange" v-loading="listLoading">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="message" label="消息内容" show-overflow-tooltip />
        <el-table-column prop="timestamp" label="发送时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'unread' ? 'warning' : 'success'">
              {{ scope.row.status === 'unread' ? '未读' : '已读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button 
              size="mini" 
              type="primary" 
              @click="markAsRead(scope.row)"
              v-if="scope.row.status === 'unread'"
            >标记已读</el-button>
            <el-button 
              size="mini" 
              type="danger" 
              @click="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination 
        :total="total" 
        :page.sync="listQuery.currentPage" 
        :limit.sync="listQuery.size" 
        @pagination="fetchData" 
      />

      <!-- 发送通知对话框 -->
      <el-dialog title="发送通知" :visible.sync="dialogVisible" width="500px" :modal="false">
        <el-form :model="notificationForm" :rules="rules" ref="notificationForm" label-width="100px">
          <el-form-item label="发送对象" prop="sendType">
            <el-radio-group v-model="notificationForm.sendType" @change="handleSendTypeChange">
              <el-radio label="role">按角色发送</el-radio>
              <el-radio label="user">按用户发送</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="选择角色" v-if="notificationForm.sendType === 'role'" prop="role">
            <el-select v-model="notificationForm.role" placeholder="请选择角色">
              <el-option label="普通用户" value="USER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </el-form-item>

          <el-form-item label="选择用户" v-if="notificationForm.sendType === 'user'" prop="userId">
            <el-select
              v-model="notificationForm.userId"
              placeholder="请输入用户名搜索"
              filterable
              remote
              reserve-keyword
              :remote-method="remoteSearchUsers"
              :loading="userLoading"
            >
              <el-option
                v-for="user in userList"
                :key="user.id"
                :label="user.username"
                :value="user.id"
              >
                <span style="float: left">{{ user.username }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ user.email }}</span>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="消息内容" prop="message">
            <el-input 
              type="textarea" 
              v-model="notificationForm.message" 
              :rows="4" 
              placeholder="请输入消息内容"
            />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitNotification">确 定</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import Pagination from '../components/Pagination/index.vue'
import Request from '../utils/request.js'

export default {
  name: 'NotificationManagement',
  components: { Pagination },
  data() {
    return {
      listLoading: true,
      listQuery: {
        username: '',
        status: '',
        currentPage: 1,
        size: 10
      },
      total: 0,
      tableData: [],
      multipleSelection: [],
      dialogVisible: false,
      userList: [],
      userLoading: false,
      userQuery: '',
      timeout: null,
      notificationForm: {
        sendType: 'role',
        role: null,
        userId: null,
        message: ''
      },
      rules: {
        sendType: [{ required: true, message: '请选择发送对象', trigger: 'change' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }],
        userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
        message: [{ required: true, message: '请输入消息内容', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.fetchData()
  },
  methods: {
    formatDateTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },

    fetchData() {
      this.listLoading = true
      Request.get('/pet-notification/page', {
        params: this.listQuery
      }).then(response => {
        if (response.code === '0' && response.data) {
          this.total = response.data.total
          this.tableData = (response.data.records || []).map(item => ({
            ...item,
            timestamp: this.formatDateTime(item.timestamp)
          }))
        }
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },

    handleSendNotification() {
      this.dialogVisible = true
      this.notificationForm = {
        sendType: 'role',
        role: null,
        userId: null,
        message: ''
      }
      this.userList = []
    },

    submitNotification() {
      this.$refs.notificationForm.validate(valid => {
        if (valid) {
          const url = this.notificationForm.sendType === 'role' 
            ? `/pet-notification/role/${this.notificationForm.role}`
            : `/pet-notification/user/${this.notificationForm.userId}`

          Request.post(url, {
            message: this.notificationForm.message
          }).then(response => {
            if (response.code === '0') {
              this.$message.success('发送成功')
              this.dialogVisible = false
              this.fetchData()
            }else{
              this.$message.error(response.message||"发送失败")
            }
          })
        }
      })
    },

    handleSelectionChange(val) {
      this.multipleSelection = val.map(item => item.id)
    },

    markAsRead(row) {
      Request.put(`/pet-notification/markAsRead/${row.id}`).then(response => {
        if (response.code === '0') {
          this.$message.success('标记成功')
          this.fetchData()
        }
      })
    },

    handleDelete(row) {
      this.$confirm('确认删除该通知?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete(`/pet-notification/${row.id}`).then(response => {
          if (response.code === '0') {
            this.$message.success('删除成功')
            this.fetchData()
          }
        })
      })
    },

    batchDelete() {
      if (this.multipleSelection.length === 0) {
        this.$message.warning('请选择要删除的记录')
        return
      }

      this.$confirm('确认删除选中的通知?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete(`/pet-notification/deleteBatch?ids=${this.multipleSelection.join(',')}`).then(response => {
          if (response.code === '0') {
            this.$message.success('批量删除成功')
            this.fetchData()
          }
        })
      })
    },

    onSubmit() {
      this.listQuery.currentPage = 1
      this.fetchData()
    },

    onReset() {
      this.listQuery = {
        username: '',
        status: '',
        currentPage: 1,
        size: 10
      }
      this.fetchData()
    },

    remoteSearchUsers(query) {
      if (query !== '') {
        this.userLoading = true
        clearTimeout(this.timeout)
        this.timeout = setTimeout(() => {
          Request.get('/user/search', {
            params: {
              username: query,
              limit: 10
            }
          }).then(response => {
            this.userLoading = false
            if (response.code === '0') {
              this.userList = response.data || []
            }
          }).catch(() => {
            this.userLoading = false
          })
        }, 300)
      } else {
        this.userList = []
      }
    },

    handleSendTypeChange() {
      this.notificationForm.userId = null
      this.notificationForm.role = null
      this.userList = []
    }
  }
}
</script>

<style lang="less">
.notification-wrapper {
  .el-card {
    min-height: 656px;
  }

  .control-btns {
    margin-bottom: 20px;
  }

  .search-form {
    padding: 10px !important;
    background-color: #f7f8fb;
    margin-bottom: 20px;
  }

  .el-table {
    margin-top: 20px;
  }
}
</style> 