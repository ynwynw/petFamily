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
            <el-table ref="multipleTable" v-loading="listLoading" :data="tableData"
                @selection-change="handleSelectionChange">
 
                <el-table-column prop="id" label="序号" width="50" align="center" sortable></el-table-column>
                <el-table-column  prop="img" label="上报图片" >
                    <template slot-scope="scope">
                        <el-button plain type="success"  size="mini" @click="showImgDialog(scope.$index, scope.row)">
                            查看上报图片
                        </el-button>
                    </template>

                </el-table-column>
                <el-table-column prop="name" label="上报说明" show-overflow-tooltip></el-table-column>
                <el-table-column prop="time" label="上报时间"></el-table-column>
                <el-table-column prop="status" label="处理状态">
                    <template slot-scope="scope">
                        <el-tag size="small" :type="scope.row.status === '已处理'?'success' : 'warning'">
                            {{ scope.row.status }}
                        </el-tag>
                        </template> 

                </el-table-column>
                <el-table-column label="操作" >
                    <template slot-scope="scope">
                        <el-button plain size="mini"  type="primary" v-if="scope.row.status === '待处理'" @click="handleDeal(scope.$index, scope.row)"
                          >去处理</el-button>
                        <el-button size="mini"  v-if="userInfo.role=='USER'&&scope.row.status === '待处理'" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
                        <el-button size="mini" type="warning" @click="handleDelete(scope.$index, scope.row)"
                            v-if="userInfo.role!='USER'&&scope.row.status === '已处理'">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <!-- 分页栏 -->
            <Pagination :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize"
                @pagination="fetchData" />
            <!-- 新增/编辑 弹出栏 -->
            

            <el-dialog title="修改上报信息" :visible.sync="formVisibleEdit" width="50%" class="dialog-form"
                :close-on-click-modal="false" :modal="false">
                <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="100px">
                    <div style="margin: 5px;text-align: center">
                        <el-upload action="#" list-type="picture-card" :on-preview="handlePreview"
                            :on-remove="handleRemove" :on-change="handleChange" :file-list="fileList" :limit="6"
                            :on-exceed="handleExceed" :auto-upload="false">
                            <i class="el-icon-plus"></i>
                        </el-upload>
                    </div>
                    <el-form-item prop="name" label="上报说明">
                        <el-input type="textarea" :rows="4" v-model="dialogForm.name" autocomplete="off"></el-input>
                    </el-form-item>
                    <div class="footer-item">
                        <el-button @click="formVisibleEdit = false;">取 消</el-button>
                        <el-button type="primary" :disabled="isSubmit" @click="handleEditSave('dialogForm')">确
                            定</el-button>
                    </div>
                </el-form>
            </el-dialog>

            <el-dialog title="图片信息" :visible.sync="imgDialogVisible" width="50%">
                <div v-if="imgsList.length === 0" style="text-align: center; padding: 20px;">
                    暂无信息
                </div>
            

                <div v-else class="img-list" style="text-align: center;width: 100%;margin:10px">
                    <img v-for="(img, index) in fileList" :key="index" :src="img.url" alt="" 
                        style="width: 150px; height: 150px;">
                </div>
                <span slot="footer" class="dialog-footer">
                    <el-button @click="closeImgDialog">关闭</el-button>
                </span>
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
    name: 'Submit',
    components: { Pagination, Upload, Hints },
    data() {
        return {
            uploadUrl: '/api/file/upload',
            imgBaseUrl: this.HOST,
            fileList: [],
            imgsList: [],
            // 数据列表加载动画
            listLoading: true,
            // 查询列表参数对象
            listQuery: {
                name: undefined,

                currentPage: 1,
                pageSize: 10

            },
            // 新增/编辑提交表单对象
            dialogForm: {
                id: undefined,
                name: undefined,
            },

            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            // 多选数据暂存数组
            multipleSelection: [],
            deletedImgs: [],
            newImgs: [],
            // 新增/编辑 弹出框显示/隐藏
    
            formVisibleEdit: false,
            imgDialogVisible: false,
            categoryList: [],

            // 导入数据 弹出框 关闭事件
            // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
            formRules: {
                name: [
                    { required: true, message: '请输入上报说明', trigger: 'blur' }]
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
        handlePreview(file) {
            // 处理图片预览逻辑
        },
      
        handleRemove(file, fileList) {
            // 移除图片时的逻辑
            this.fileList = fileList;
            this.deletedImgs.push(file.url)
        },
        handleChange(file, fileList) {

            this.newImgs.push(file)
            // 处理图片变更逻辑
            this.fileList = fileList;


        },
        handleExceed() {
            this.$message({
                type: 'warning',
                message: '最多上传6张图片'
            });
        },

        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },

        showImgDialog(index, row) {
            this.loadImgs(row.id);


            this.imgDialogVisible = true
        },
        closeImgDialog() {
            this.imgDialogVisible = false
        },
        handleDeal(index,row){
            Request.put("/submit/updateStatus/"+row.id).then(res=>{
                if(res.code==0){
                    this.$message({
                        message: "处理成功",
                        type: 'success',
                    })
                    this.fetchData();
                }else{
                    this.$message({
                        message: res.msg,
                        type: 'error',
                    })
                }
            })
        },
        // 编辑数据
        handleEdit(index, row) {
            this.fileList = [];
            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.loadImgs(row.id)
            this.formVisibleEdit = true

        },
        handleDelete(index, row) {
            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/submit/" + row.id).then(response => {
                    if (response.code == 0) {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        })
                        this.onReset()
                        this.fetchData()
                    } else {
                        this.$message({
                            type: 'success',
                            errpr: '删除失败!'
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


        // 获取数据列表
        fetchData() {
            this.listLoading = true
            Request.get("/submit/page", {
                params: {

                    name: this.listQuery.name,

                    currentPage: this.listQuery.currentPage,
                    size: this.listQuery.pageSize,
                }
            }).then(response => {
                const data = response.data;
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
            this.listQuery.pageSize = 10;
            this.listQuery.name = "";
            this.fetchData()
        },

        loadImgs(submitId) {
            var imgUrlList = [];
            Request.get("/submitImg/" + submitId).then(response => {
                if (response.code == 0) {
                    imgUrlList = response.data;
                    if (imgUrlList) {
                        this.imgsList = imgUrlList.map(item => {
                            this.imgBaseUrl + "/img/" + item.url
                        })
                        this.fileList = imgUrlList.map((item) => {

                            item.url = this.imgBaseUrl + "/img/" + item.url;
                            return item;

                        });

                    }

                }
            });
        },
        handleEditSave(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {


                    const formData = new FormData();
                    this.newImgs.forEach((file) => {
                        formData.append('files[]', file.raw);
                    });
                    formData.append('deletedImgs', JSON.stringify(this.deletedImgs));
                    const submitJson = JSON.stringify(this.dialogForm);

                    formData.append('submit', submitJson);
                    Request.post("/submit/update", formData).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '更新成功',
                                type: 'success',
                            });
                            this.deletedImgs = [];
                            this.newImgs = [];
                            this.fileList = [];
                            this.formVisibleEdit = false
                            this.fetchData()
                        } else {
                            this.$message({
                                showClose: true,
                                message: '更新失败',
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
      header: ['序号', '上报图片', '上报说明', '上报时间', '处理状态'],
      key: ['id', 'img', 'name', 'time','status'],
      data: this.tableData,
      autoWidth: true,
      fileName: '上报信息表',
      bookType: type
    }
    excel.exportDataToExcel(params)
    this.exportVisible = false
  }
},
    }
}
</script>

<style lang="less">
.category-wrapper {
    .el-card {
        min-height: 656px;
    }

    .control-btns {

        margin-bottom: 50px;
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



.avatar {
    width: 178px;
    height: 178px;
    display: block;
    border-radius: 50%;
}

// .el-upload-list__item.is-ready {
//   display: none;
// }
.el-upload-list__item {

    transition: none !important;

}
</style>