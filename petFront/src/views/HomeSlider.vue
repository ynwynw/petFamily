<template>
    <div class="slider-wrapper">
        <el-card shadow="always">
            <!-- 操作栏 -->
            <div class="control-btns">

                <el-button type="primary" size="medium" @click="handleCreate">新增轮播图</el-button>

            </div>

            <!-- 表格栏 -->
            <el-table ref="multipleTable" v-loading="listLoading" :data="tableData" tooltip-effect="dark" row-key="id"
                style="width: 100%" size="medium" border>
                <el-table-column prop="name" label="标题"></el-table-column>
                <el-table-column label="图片" align="center">
                    <template slot-scope="scope">
                        <img :src="HOST+'/img/'+scope.row.img" alt="轮播图" class="slider-img" />
                    </template>
                </el-table-column>
                <el-table-column prop="desrc" label="描述"></el-table-column>
          
                <el-table-column label="操作" width="200">
                    <template slot-scope="scope">
                        <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
                        <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <!-- 分页栏 -->
            <Pagination :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit"
                @pagination="fetchData" />
            <!-- 新增/编辑 弹出栏 -->
            <el-dialog :title="dialogTitle" :visible.sync="formVisible" width="30%" :close-on-click-modal="false" :modal="false">   
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" enctype="multipart/form-data">
                    <el-form-item label="轮播图标题" prop="name">
                        <el-input v-model="dialogForm.name" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item label="轮播图描述" prop="desrc">
                        <el-input maxlength="20" resize="false" :show-word-limit="true" type="textarea" v-model="dialogForm.desrc" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item label="上传图片" prop="img">
                        <div style="margin: 5px;text-align: center">
                        <el-upload  :class="{hide:hideUpload}"  action="#" list-type="picture-card" :on-preview="handlePreview" :on-change="handleFileChange"  :on-remove="handleRemove"  :on-exceed="handleExceed"  :limit="limitCount"
                            :file-list="fileList" :auto-upload="false">
                            <i class="el-icon-plus"></i>
                            <!-- <i class="el-icon-upload"></i>
                            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                            <div class="el-upload__tip" slot="tip">只能上传jpg/png文件，且不超过500KB</div> -->
                        </el-upload>
                    </div>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="formVisible = false">取消</el-button>
                    <el-button type="primary" :disabled="isSubmit" @click="saveSlider('dialogForm')">确定</el-button>
                </div>
            </el-dialog>

        </el-card>
    </div>
</template>

<script>
import excel from '../utils/excel.js'
import Pagination from '../components/Pagination/index.vue'
import Upload from '../components/Upload/index.vue'
import Request from '../utils/request.js'

export default {
    name: 'HomeSlider',
    components: { Pagination, Upload },
    data() {
        return {
            listLoading: true,
            formLabelWidth: '120px',
            listQuery: {

                page: 1,
                limit: 10
            },
            dialogForm: {
                id: undefined,
                name: '',
                desrc: '',
                img: ''
            },
            total: 0,
            tableData: [],
            formVisible: false,

            isSubmit: false,
            dialogTitle: '',
            deletedImgs:[],
            fileList: [],
            hideUpload: false,

limitCount:1,
            formRules: {
                name: [
                    { required: true, message: '请输入轮播图标题', trigger: 'blur' },
                    { min: 1, max: 50, message: '标题长度在 1 到 50 个字符', trigger: 'blur' }
                ],
                description: [
                    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
                ],
                img: [
                    { required: true, message: '请上传图片', trigger: 'change' }
                ]
            }
        }
    },
    created() {
        this.fetchData()
    },
    methods: {
        handleCreate() {
            this.dialogTitle = '新增轮播图';
            this.formVisible = true
            this.dialogForm = { name: '', description: '', img: '' }
            this.fileList = []
            this.hideUpload=false;
            this.deletedImgs = []
        },
        handleEdit(row) {
            this.dialogTitle = '编辑轮播图';
            this.formVisible = true
            this.dialogForm = { ...row }
            this.fileList = [{ name: 'Image', url: row.img }]
            this.fileList[0].url=this.HOST + "/img/" + this.dialogForm.img;
            this.deletedImgs = []
            this.hideUpload = this.fileList.length >= this.limitCount;
            console.log("--------------------------------")
            console.log(this.dialogForm.img)
            console.log("--------------------------------")
        },
        handlePreview(file) {
            // 处理图片预览逻辑
        },
      
        handleExceed() {
            this.$message({
                type: 'warning',
                message: '最多上传1张图片'
            });
        },
        handleDelete(id) {
            this.$confirm('此操作将删除该轮播图, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/slider/" + id).then(response => {
                    if (response.code === '0') {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        });
                        this.fetchData();
                    } else {
                        this.$message({
                            type: 'error',
                            message: '删除失败!'
                        });
                    }
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });
        },
        handleFileChange(file, fileList) {
            
            this.fileList = fileList
            if (file.raw) {
                this.dialogForm.img = file.raw
            }
            this.hideUpload = this.fileList.length >= this.limitCount;
        },
        handleRemove(file, fileList) {
          
            // 移除图片时的逻辑
            this.fileList = fileList;
            this.deletedImgs.push(file.url)
            console.log(  this.deletedImgs[0])
            this.hideUpload = this.fileList.length >= this.limitCount;
        },
        saveSlider(formName) {
            this.$refs[formName].validate((valid) => {
                if (valid) {
                    const formData = new FormData();
                    formData.append('homeSlider', JSON.stringify(this.dialogForm));
                    
                    // 如果是更新操作
                    if (this.dialogForm.id) {
                        // 如果有新图片，添加新图片
                        if (this.fileList.length > 0 && this.fileList[0].raw) {
                            formData.append('files', this.fileList[0].raw);
                        } else {
                            // 如果没有新图片，添加一个空文件
                            formData.append('files', new Blob([]));
                        }
                        
                        // 如果有删除的图片，添加删除图片的路径
                        if (this.deletedImgs.length > 0) {
                            formData.append('deletedImg', this.deletedImgs[0]);
                        }
                        
                        Request.post("/slider/update/", formData).then(response => {
                            if (response.code === '0') {
                                this.$message({
                                    type: 'success',
                                    message: '更新成功!'
                                });
                                this.formVisible = false;
                                this.fetchData();
                            } else {
                                this.$message({
                                    type: 'error',
                                    message: '更新失败!'
                                });
                            }
                        });
                    } else {
                        // 新增操作
                        if (this.fileList.length > 0 && this.fileList[0].raw) {
                            formData.append('files', this.fileList[0].raw);
                            Request.post("/slider", formData, {
                                transformRequest: [(data, headers) => {
                                    headers['Content-Type'] = 'multipart/form-data; boundary=----WebKitFormBoundaryrq9eYSZPFTlYOBey';
                                    return data;
                                }]
                            }).then(response => {
                                if (response.code === '0') {
                                    this.$message({
                                        type: 'success',
                                        message: '添加成功!'
                                    });
                                    this.formVisible = false;
                                    this.fetchData();
                                } else {
                                    this.$message({
                                        type: 'error',
                                        message: '添加失败!'
                                    });
                                }
                            });
                        } else {
                            this.$message({
                                type: 'warning',
                                message: '请选择要上传的图片!'
                            });
                        }
                    }
                } else {
                    this.isSubmit = false;
                    return false;
                }
            });
        },
        fetchData() {
            this.listLoading = true;
            Request.get("/slider/findAll", {
                params: {
                    page: this.listQuery.page,
                    limit: this.listQuery.limit
                }
            }).then(response => {
                if (response.code === '0') {
                    console.log(response.data.records)
                    this.tableData = response.data.records;
                    this.total = response.data.total;
                } else {
                    this.$message.error('数据加载失败');
                }
            }).finally(() => {
                this.listLoading = false;
            });
        },

    }
}
</script>

<style lang="less" scoped>
.slider-wrapper {
    .el-card {
        min-height: 656px;
    }

    .control-btns {
        margin-bottom: 20px;
    }

    .search-form {
        padding-bottom: 20px;
        margin-bottom: 15px;
        background-color: #f7f8fb;
        height: 35px;
        line-height: 35px;
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

    .slider-img {
        width: 50px;
        height: 50px;
        object-fit: cover;
    }

    .hints {
        font-size: 12px;
        color: #aaa;
        text-align: center;
    }
}
/deep/  .el-upload-list__item {

    transition: none !important;

}
/deep/ .hide .el-upload--picture-card {
  display: none!important;
}
</style>
