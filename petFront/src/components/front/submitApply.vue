<template>
    <div>
        <el-dialog title="流浪宠物上报" :visible.sync="visible" width="30%" class="dialog-form">
            <el-form ref="dialogForm" :model="dialogForm" :rules="formRules" label-width="100px">
                <el-form-item prop="name" label="上报说明">
                    <el-input type="textarea" :rows="4" v-model="dialogForm.name" autocomplete="off"></el-input>
                </el-form-item>
                <div style="margin: 15px;">
                    <el-upload action="#" list-type="picture-card" :on-preview="handlePreview" :on-remove="handleRemove"
                        :on-change="handleChange" :file-list="fileList" :limit="6" :on-exceed="handleExceed"
                        :auto-upload="false">
                        <i class="el-icon-plus"></i>
                    </el-upload>
                </div>
                <div class="footer-item">
                    <el-button @click="visible = false; fileList = []">取 消</el-button>
                    <el-button type="primary" :disabled="isSubmit" @click="handleAdd('dialogForm')">确 定</el-button>
                </div>
            </el-form>
            </el-dialog>
    </div>
</template>

<script>
import Request from '../../utils/request.js'
export default {
    data() {
        return {
            dialogForm: {
                id: undefined,
                name: undefined,
            },
            fileList: [],
            imgsList: [],
            newImgs: [],
     
            visible: false,
            isSubmit: false,
            formRules: {
                name: [
                    { required: true, message: '请输入上报说明', trigger: 'blur' }
                ]
            }
        }
    },
    methods: {
        handleCreate() {
            this.visible = true;
            this.dialogForm={};
            this.fileList=[];
        },
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

        handleAdd(formName) {
            this.$refs[formName].validate(valid => {

                const formData = new FormData();
                this.newImgs.forEach((file) => {
                    formData.append('files[]', file.raw);
                });
                const submitJson = JSON.stringify(this.dialogForm);
                formData.append('submit', submitJson);
                if (valid) {
                    Request.post("/submit", formData).then(response => {

                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '上报成功',
                                type: 'success',
                            });
                            this.fileList = null;
                            this.newImgs = [];
                            this.fetchData()
                        } else {
                            this.$message({
                                showClose: true,
                                message: response.msg,
                                type: 'error',
                            });
                        }

                    });
                    this.visible = false
                    this.fetchData();
                    return true;
                } else {
                    this.isSubmit = false
                    return false
                }
            })
        },
    }
}
</script>

<style scoped>
/deep/  .el-upload-list__item {

transition: none !important;

}

</style>