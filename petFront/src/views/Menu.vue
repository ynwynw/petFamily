<template>
    <div class="category-wrapper">
        <el-card shadow="always">
            <!-- 操作栏 -->
            <div class="control-btns">
                <el-popconfirm title="确认删除?" @confirm="batchDelete">
                    <template #reference>
                        <el-button type="danger" size="medium"
                            style="background-color: red;border-color: red;">批量删除</el-button>

                    </template>
                </el-popconfirm>

                <el-button type="primary" size="medium" @click="handleCreate"
                    style="float:right;background-color: blue;border-color: blue;">新增菜单项</el-button>

                <el-button type="primary" size="medium" @click="exportVisible = true"
                    style="float:right;margin-right: 10px;background-color: blue;border-color: blue;">导出数据</el-button>

            </div>
            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" label-width="90px" class="search-form">
                <el-form-item label="菜单标题">
                    <el-input v-model="listQuery.name" placeholder="菜单标题" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="medium" style="background-color: blue;border-color: blue;"
                        @click="onSubmit">查询</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="danger" size="medium" style="background-color: red;border-color: red;"
                        @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>
            <!-- 表格栏 -->
            <el-table ref="multipleTable" v-loading="listLoading" :data="tableData" tooltip-effect="dark" row-key="id"
                style="width: 100%" size="medium" @selection-change="handleSelectionChange" border default-expand-all>
                <el-table-column type="selection" width="50"></el-table-column>
                <el-table-column prop="id" label="id"></el-table-column>
                <el-table-column prop="name" label="菜单名称"></el-table-column>
                <el-table-column prop="path" label="菜单路径"></el-table-column>
                <el-table-column label="菜单图标">
                    <template slot-scope="scope">
                        <i :class="scope.row.icon" />
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="菜单描述"></el-table-column>
                <el-table-column prop="pagePath" label="页面路径"></el-table-column>
                <el-table-column prop="role" label="菜单分配">
                    <template slot-scope="scope">
                        <div>{{ getMenuRole(scope.row.role) }}</div>
                    </template>
                </el-table-column>
                <el-table-column prop="sortNum" label="排序"></el-table-column>
                <el-table-column label="操作" width="350">
                    <template slot-scope="scope">
                        <el-button size="small" type="success" class="el-icon-circle-plus"
                            @click="addChildMenu(scope.row.id)"
                            v-if="!scope.row.pid && !scope.row.path">新增子菜单</el-button>
                        <el-button size="small" type="primary" class="el-icon-edit"
                            @click="handleEdit(scope.row)">编辑</el-button>
                        <el-button size="mini" type="danger"
                            @click="handleDelete(scope.$index, scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 新增/编辑 弹出栏 -->


            <el-dialog title="修改菜单项" :visible.sync="formVisibleEdit" width="30%" :modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm">
                    <el-form-item label="菜单名称" :label-width="formLabelWidth" prop="name">
                        <el-input v-model="dialogForm.name" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item label="菜单路径" :label-width="formLabelWidth">
                        <el-input v-model="dialogForm.path" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item label="菜单图标" :label-width="formLabelWidth" prop="icon">
                        <el-select v-model="dialogForm.icon" filterable placeholder="请选择图标">
                            <el-option v-for="dict in iconDict" :key="dict.itemKey" :label="dict.itemKey"
                                :value="dict.itemValue">
                                <i :class="dict.itemValue"></i>&nbsp;&nbsp;{{ dict.itemKey }}
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="页面路径" :label-width="formLabelWidth">
                        <el-input v-model="dialogForm.pagePath" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item label="排序" :label-width="formLabelWidth" prop="sortNum">
                        <el-input v-model="dialogForm.sortNum" autocomplete="off" type="number"></el-input>
                    </el-form-item>
                    <el-form-item label="菜单分配" :label-width="formLabelWidth" prop="role">
                        <el-select v-model="dialogForm.role" placeholder="请选择">
                            <el-option label="暂不分配" :value="0"></el-option>
                            <el-option label="普通用户" :value="2"></el-option>
                            <el-option label="普通管理员" :value="1"></el-option>
                            <el-option label="全部" :value="3"></el-option>

                        </el-select>
                    </el-form-item>
                    <el-form-item label="描述" :label-width="formLabelWidth">
                        <el-input v-model="dialogForm.description" autocomplete="off"></el-input>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="formVisibleEdit = false">取 消</el-button>
                    <el-button type="primary" :disabled="isSubmit" @click="handleEditSave('dialogForm')">确 定</el-button>
                </div>
            </el-dialog>

            <el-dialog title="新增菜单项" :visible.sync="formVisibleAdd" width="30%">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm">
                    <el-form-item label="菜单名称" :label-width="formLabelWidth" prop="name">
                        <el-input v-model="dialogForm.name" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item label="菜单路径" :label-width="formLabelWidth">
                        <el-input v-model="dialogForm.path" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item label="菜单图标" :label-width="formLabelWidth" prop="icon">
                        <el-select v-model="dialogForm.icon" filterable placeholder="请选择图标">
                            <el-option v-for="dict in iconDict" :key="dict.itemKey" :label="dict.itemKey"
                                :value="dict.itemValue">
                                <i :class="dict.itemValue"></i>&nbsp;&nbsp;{{ dict.itemKey }}
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="页面路径" :label-width="formLabelWidth">
                        <el-input v-model="dialogForm.pagePath" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item label="排序" :label-width="formLabelWidth" prop="sortNum">
                        <el-input v-model="dialogForm.sortNum" autocomplete="off" type="number"></el-input>
                    </el-form-item>
                    <el-form-item label="菜单分配" :label-width="formLabelWidth" prop="role">
                        <el-select v-model="dialogForm.role" placeholder="请选择">
                            <el-option label="暂不分配" :value="0"></el-option>
                            <el-option label="普通用户" :value="2"></el-option>
                            <el-option label="普通管理员" :value="1"></el-option>
                            <el-option label="全部" :value="3"></el-option>

                        </el-select>
                    </el-form-item>
                    <el-form-item label="描述" :label-width="formLabelWidth">
                        <el-input v-model="dialogForm.description" autocomplete="off"></el-input>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="formVisibleAdd = false">取 消</el-button>
                    <el-button type="primary" :disabled="isSubmit" @click="handleAdd('dialogForm')">确 定</el-button>
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
    name: 'Menu',
    components: { Pagination, Upload, Hints },
    data() {
        return {
            // 数据列表加载动画
            listLoading: true,
            formLabelWidth: '80px',
            iconDict: {},
            // 查询列表参数对象
            listQuery: {
                name: undefined
            },
            // 新增/编辑提交表单对象
            dialogForm: {
                name: undefined,
                description: undefined,
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
            // 导入数据 弹出框 关闭事件
            // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
            formRules: {
                // 验证规则
                name: [
                    { required: true, message: '请填写菜单名称', trigger: 'blur' }
                ],
                sortNum: [
                    { required: true, message: '排序不能为空', trigger: 'blur' },

                ],
                role: [
                    { required: true, message: '请选择菜单分配', trigger: 'change' }
                ],
                icon: [
                    { required: true, message: '请选择菜单图标', trigger: 'change' }
                ]
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
        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },
        // 新建数据
        handleCreate() {
            this.formVisibleAdd = true
            this.dialogForm = {}

        },
        // 编辑数据
        handleEdit(row) {
            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.formVisibleEdit = true

        },
        cancel() {
            this.$message.success('取消操作成功');
        },
        // 删除数据
        handleDelete(index, row) {
            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/menu/deleteById/" + row.id).then(response => {
                    if (response.code == 0) {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        })
                        this.onReset()
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
        addChildMenu(pid) {
            this.formVisibleAdd = true;
            this.dialogForm = {};
            if (pid) {
                this.dialogForm.pid = pid;
            }
        },
        // 批量删除
        batchDelete() {
            if (this.multipleSelection.length < 1) {
                this.$message({
                    message: '请先选择要删除的数据！',
                    type: 'warning'
                })
            } else {
                Request.delete(`/menu/deleteBatch?ids=${this.multipleSelection.join(',')}`).then(res => {
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
        loadIconList() {

            Request.get("/icon-dict/all").then(response => {
                if (response.code === '0') {
                    this.iconDict = response.data;

                } else {
                    this.$message({
                        type: 'error',
                        message: '图标列表失败!'
                    })
                }
            });

        },
        // 获取数据列表
        fetchData() {
            this.listLoading = true
            this.loadIconList();
            Request.get("/menu/findAll", {
                params: {
                    name: this.listQuery.name
                }
            }).then(response => {
                const data = response.data;
                this.tableData = data;
                this.total = data.length;
            })
            this.listLoading = false
        },
        // 查询数据
        onSubmit() {
            this.fetchData()
        },
        onReset() {
            this.listQuery.name = "";
            this.fetchData()
        },


        handleAdd(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.post("/menu/save", this.dialogForm).then(response => {
                        if (response.code == 0) {
                            this.$message({
                                showClose: true,
                                message: '添加成功',
                                type: 'success',
                            });
                        } else {
                            this.$message({
                                showClose: true,
                                message: '添加失败',
                                type: 'error',
                            });
                        }

                    });
                    this.formVisibleAdd = false
                    return true;
                } else {
                    this.isSubmit = false
                    return false
                }
            })
        },
        handleEditSave(formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    Request.put("/menu/" + this.dialogForm.id, this.dialogForm).then(response => {
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
                                message: '更新失败',
                                type: 'error',
                            });
                        }
                    });

                } else {

                }
            });
        },

        // 导出数据--excle格式
        exportTable(type) {
            if (this.tableData.length) {
                const params = {
                    header: ['菜单ID', '菜单名', '菜单路径', '菜单图标', '描述', '父级菜单ID', '页面路径', '排序', '菜单所属角色'], // 修改表头以匹配类属性
                    key: ['id', 'name', 'path', 'icon', 'description', 'pid', 'pagePath', 'sortNum', 'role'], // 修改key以匹配类属性
                    data: this.tableData, // 假定this.tableData已经是格式化好的数组，每个元素是一个菜单对象
                    autoWidth: true,
                    fileName: '菜单数据表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            }
        },
        getMenuRole(role) {
            switch (role) {
                case 0:
                    return '未分配';
                case 2:
                    return '普通用户';
                case 1:
                    return '普通管理员';
                case 3:
                    return '全部';
                default: return '未分配';
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
        margin-bottom: 20px;
    }

    .search-form {


        padding-bottom: 20px !important;
        margin-bottom: 15px;
        background-color: #f7f8fb;
        height: 35px !important;
        line-height: 35px !important;
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