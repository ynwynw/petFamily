<template>
    <div class="category-wrapper">
        <el-card shadow="always">
            <!-- 操作栏 -->
            <div class="control-btns">
      

  

                <el-button type="primary" size="medium" @click="exportVisible = true"
                    style="float:right;margin-right: 10px;background-color: blue;border-color: blue;">导出数据</el-button>

            </div>
            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" label-width="90px" class="search-form">
                <el-form-item label="宠物姓名">
                    <el-input v-model="listQuery.animalName" placeholder="请输入宠物姓名" />
                </el-form-item>
                <el-select v-model="listQuery.status" placeholder="请选择状态" style="width: 200px; margin-left: 5px">
                    <el-option label="待确认" value="待处理"></el-option>
                    <el-option label="寄养中" value="寄养中"></el-option>
                    <el-option label="已领回" value="已领回"></el-option>
                </el-select>


                <el-form-item>
                    <el-button type="primary" @click="onSubmit">查询</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="danger" @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 表格栏 -->
            <el-table ref="multipleTable" v-loading="listLoading" :data="tableData"
                @selection-change="handleSelectionChange">

                <el-table-column prop="userName" label="用户" show-overflow-tooltip></el-table-column>
                <el-table-column prop="name" label="宠物昵称" show-overflow-tooltip></el-table-column>
                <el-table-column prop="time" label="寄养时间"></el-table-column>
                <el-table-column prop="days" label="寄养天数"></el-table-column>
                <el-table-column prop="status" label="寄养状态"></el-table-column>
                <el-table-column prop="roomName" label="寄养房间"></el-table-column>
                <el-table-column v-if="userInfo.role!='USER'" label="操作" >
                    <template slot-scope="scope">
                        <el-button plain v-if="scope.row.status != '已领回'" type="primary" @click="handleEdit(scope.$index, scope.row)"
                            size="mini">修改</el-button>
                        <el-button plain size="small" type="warning" v-if="scope.row.status == '待处理'"
                            @click="handleOk(scope.$index, scope.row)">去处理</el-button>
                        <el-button v-if="scope.row.status == '已领回'" size="mini" type="danger"
                            @click="handleDelete(scope.$index, scope.row)">删除</el-button>

                            <el-button plain v-if="scope.row.status == '寄养中'" size="mini" type="success"
                            @click="handleGiveBack(scope.$index, scope.row)">领回处理</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <!-- 分页栏 -->
            <Pagination :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize"
                @pagination="fetchData" />
  

            <el-dialog title="修改寄养信息" :visible.sync="formVisibleEdit" width="30%" class="dialog-form"
                :close-on-click-modal="false" :modal="false">
                <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="100px">
                    <el-form-item prop="name" label="宠物昵称">
                        <el-input v-model="dialogForm.name" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="time" label="寄养时间">
                        <el-date-picker style="width: 100%" v-model="dialogForm.time" type="date"
                            value-format="yyyy-MM-dd" placeholder="选择日期">
                        </el-date-picker>
                    </el-form-item>
                    <el-form-item prop="days" label="寄养天数">
                        <el-input v-model.number="dialogForm.days" autocomplete="off" ></el-input>
                    </el-form-item>

                    <el-form-item v-if="dialogForm.status!='已领回'" prop="roomId" label="选择房间">
                        <el-select v-model="dialogForm.roomId" placeholder="请选择房间" style="width: 100%">
                            <el-option :disabled="item.status==='使用中'" v-for="item in roomData" :label="item.name" :value="item.id" :key="item.id"></el-option>
                        </el-select>
                    </el-form-item>


                    <div class="footer-item">
                        <el-button @click="formVisibleEdit = false">取 消</el-button>
                        <el-button type="primary" :disabled="isSubmit" @click="handleEditSave('dialogForm')">确
                            定</el-button>
                    </div>
                </el-form>
            </el-dialog>

            <el-dialog title="寄养处理" :visible.sync="roomVisible" width="40%" :close-on-click-modal="false"
                destroy-on-close>
                <el-form label-width="100px" style="padding-right: 50px" :model="dialogForm">
                    <el-form-item prop="roomId" label="选择房间">
                        <el-select v-model="dialogForm.roomId" placeholder="请选择房间" style="width: 100%">
                            <el-option v-for="item in roomData" :label="item.name" :value="item.id" :key="item.id"></el-option>
                        </el-select>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="roomVisible = false">取 消</el-button>
                    <el-button type="primary" @click="fosterSave">确 定</el-button>
                </div>
            </el-dialog>

            <!-- 导出数据 弹出栏 -->
            <el-dialog title="导出数据" :visible.sync="exportVisible" width="500px">
                <div class="export-data" style="display: flex;flex-direction: row;justify-content: space-between;">
                    <el-button type="primary" @click="exportTable('xlsx')">EXCEL格式</el-button>
                    <el-button type="primary" @click="exportTable('csv')">CSV格式</el-button>
                    <el-button type="primary" @click="exportTable('txt')">TXT格式</el-button>
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
    name: 'UserManager',
    components: { Pagination, Upload, Hints },
    data() {
        return {
            uploadUrl: '/api/file/upload',
            imgBaseUrl: this.HOST,
            // 数据列表加载动画
            listLoading: true,

            roomData: [],
            // 查询列表参数对象
            listQuery: {
                animalName: undefined,
                status: undefined,
                currentPage: 1,
                pageSize: 10

            },
            // 新增/编辑提交表单对象
            dialogForm: {
                userId:'',
                name: '',
                time: '',
                days: '',
                status: '',
                roomId: ''
            },

            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            // 多选数据暂存数组
            multipleSelection: [],
            // 新增/编辑 弹出框显示/隐藏
            roomVisible: false,
        
            formVisibleEdit: false,
            categoryList: [],
            // 导入数据 弹出框 关闭事件
            // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
            formRules: {
                name: [
                    { required: true, message: '请输入房间编号', trigger: 'blur' },
                ],
                time: [
                    { required: true, message: '请选择寄养时间', trigger: 'blur' },
                ],
                days: [
                    { required: true, message: '请填写寄养天数', trigger: 'blur' },
                    { type: 'number', message: '请填写数字', trigger: 'blur' },
                ],
            },
            // 防止多次连续提交表单
            isSubmit: false,

            // 导出文件格式
            filesFormat: '.txt, .csv, .xls, .xlsx',
            // 导出数据 弹出框显示/隐藏
            exportVisible: false
        }
    },
    created() {
        this.fetchData()
    },
    methods: {
        fosterSave(){
              this.dialogForm.status = '寄养中'

              Request.put("/foster/" + this.dialogForm.id, this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '分配成功',
                                type: 'success',
                            });
                            this.formVisibleEdit = false
                            this.fetchData()
                        } else {
                            this.$message({
                                showClose: true,
                                message: response.msg,
                                type: 'error',
                            });
                        }
                    });
        },
        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },

        handleGiveBack(index, row){
            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.dialogForm.status = '已领回'
            //确认领回对话框
            this.$confirm('确认领回宠物？', '提示', {
                confirmButtonText: '确认',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.put("/foster/" + this.dialogForm.id, this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '领回成功',
                                type: 'success',
                            });
                            this.formVisibleEdit = false
                            this.fetchData()
                        } else {
                            this.$message({
                                showClose: true,
                                message: response.msg,
                                type: 'error',
                            });
                        }
                    });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消'
                });
            });
       

 
        },

        // 编辑数据
        handleEdit(index, row) {
            this.loadRooms();
            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.formVisibleEdit = true

        },
loadRooms() {
    Request.get('/room/selectAll').then(res => {
        if (res.code === '0') {
            // 过滤出状态为“闲置”的房间数据
            this.roomData = res.data;
        } else {
            this.$message.error(res.msg);
        }
    });
},
        handleOk(index, row){
            this.loadRooms();
            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.roomVisible=true
        },
  
        // 删除数据
        handleDelete(index, row) {
            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/foster/" + row.id).then(response => {
                    if (response.code == 0) {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        })
                        this.onReset()
                    } else {
                        this.$message({
                            type: 'success',
                            error: response.msg
                        })
                    }
                });

            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                })
            })
        },
        // 批量删除
        batchDelete() {
            if (this.multipleSelection.length < 1) {
                this.$message({
                    message: '请先选择要删除的数据！',
                    type: 'warning'
                })
            } else {
                Request.delete(`/foster/deleteBatch?ids=${this.multipleSelection.join(',')}`).then(res => {
                    if (res.code === '0') {
                        this.$message({
                            showClose: true,
                            message: '批量删除成功',
                            type: 'success'
                        });
                        this.onReset()
                    }
                    else {
                        this.$message({
                            showClose: true,
                            message: res.msg,
                            type: 'error',
                        });
                    }
                })

            }
        },

        // 获取数据列表
        fetchData() {
            this.listLoading = true
            Request.get("/foster/selectPage", {
                params: {
                    userId:this.userInfo.role=='USER'?this.userInfo.id:-1,
                    animalName: this.listQuery.animalName,
                    status: this.listQuery.status,
                    currentPage: this.listQuery.currentPage,
                    size: this.listQuery.pageSize,
                }
            }).then(response => {
                const data = response.data;
                if (response.code === '0') {
                    this.total = data.total
                    this.tableData = data.records
                    // if(this.userInfo.role=='USER'){
                    //     console.log(this.userInfo)
                    //     console.log(this.tableData)
                    //     this.tableData = data.records.filter(record => record.userId === this.userInfo.id);
                    // }
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
            this.listQuery.pageSize = 10;
            this.listQuery.animalName = "";

            this.fetchData()
        },


        handleEditSave(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.put("/foster/" + this.dialogForm.id, this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '更新成功',
                                type: 'success',
                            });
                            this.formVisibleEdit = false
                            this.fetchData()
                        } else {
                            this.$message({
                                showClose: true,
                                message: response.msg,
                                type: 'error',
                            });
                        }
                    });

                } else {

                }
            });
        },

        exportTable(type) {
  if (this.tableData.length) {
    const params = {
      header: ['序号', '用户', '宠物昵称', '寄养时间', '寄养天数', '寄养状态', '寄养房间'],
      key: ['id', 'userName', 'name', 'time', 'days','status', 'roomName'],
      data: this.tableData.map(item => {
        return {
          id: item.id,
          userName: item.userName,
          name: item.name,
          time: item.time,
          days: item.days,
          status: item.status,
          roomName: item.roomName
        }
      }),
      autoWidth: true,
      fileName: '寄养信息表',
      bookType: type
    }
    excel.exportDataToExcel(params)
    this.exportVisible = false
  }
}
    }
}
</script>

<style lang="less">
.category-wrapper {
    .el-card {
        min-height: 656px;
    }

    .control-btns {
        margin-bottom: 45px;
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
</style>