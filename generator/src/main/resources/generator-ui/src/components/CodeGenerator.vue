<template>
  <el-container>
    <el-header>
      <div style="float: left">
        <img src="~@/assets/logo.svg" alt="logo"/>
        <span style="margin-left: 20px;color: #fff;">代码生成器</span>
      </div>
    </el-header>
    <el-main>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="数据库" prop="dbName">
              <el-select v-model="form.dbName" placeholder="请选择数据库" filterable :clearable="true" @change="selDatabase" style="float: left;width: 100%;">
                <el-option v-for="(name, idx) in databases" :key="idx" :label="name" :value="name"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="18">
            <el-form-item label="数据表" prop="includeTableNames">
              <el-select v-model="form.includeTableNames" placeholder="请选择数据表(为空则全选)" multiple filterable style="float: left;width: 100%;">
                <el-option v-for="(item,idx) in tableNames" :key="idx" :value="item.name">
                  {{ item.name }}{{ item.comment }}
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="服务名" prop="serverName">
              <el-tooltip content="默认为数据库名,如果自定义则只需填写关键单词即可,系统会默认添加前缀cunw和后缀server">
                <el-input v-model="form.serverName" placeholder="请输入服务名"/>
              </el-tooltip>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="模块名" prop="moduleName">
              <el-tooltip content="默认实现为正则表达式去取表名第一个符合的单词,如遇到Java关键字则按下划线拆解符合的下个单词;如修改为固定单词则去修改的为模块名">
                <el-input v-model="form.moduleName" placeholder="请输入模块名"/>
              </el-tooltip>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="表名规则" prop="tableNameRuleRegex">
              <el-tooltip content="通过正则表达式获取表名的规则,为空则表示直接使用表名">
                <el-input v-model="form.tableNameRuleRegex"/>
              </el-tooltip>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="基础包名" prop="basePackageName">
          <el-input v-model="form.basePackageName" placeholder="请输入基础包名"/>
        </el-form-item>
        <!-- 分组 -->
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="生成分类" prop="includeGroup">
              <el-radio-group v-model="form.includeGroup" @change="handGroupChange">
                <el-radio-button v-for="(item,idx) in exportFileGroups" :key="idx" :label="item.val">{{item.title}}</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="fileCustomSelected">
          <el-col :span="24">
            <el-form-item label="生成文件" prop="includeFileTypes">
              <el-select v-model="form.includeFileTypes" placeholder="请选择生成文件类型(为空则全选)" multiple filterable style="float: left;width: 100%;">
                <el-option v-for="(item,idx) in exportFileTypes" :key="idx" :value="item" :label="item"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="3">
            <el-form-item label="是否服务化" prop="hasProvideService">
              <el-switch v-model="form.hasProvideService"/>
            </el-form-item>
          </el-col>
          <el-col :span="3">
            <el-form-item label="是否BaseApi" prop="hasBaseApi">
              <el-switch v-model="form.hasBaseApi"/>
            </el-form-item>
          </el-col>
          <el-col :span="3">
            <el-form-item label="是否ORM" prop="hasOrm">
              <el-switch v-model="form.hasOrm"/>
            </el-form-item>
          </el-col>
          <el-col :span="3">
            <el-form-item label="是否微服务" prop="hasMicro">
              <el-switch v-model="form.hasMicro"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="success" @click="codePreview">代码预览</el-button>
          <el-button type="info" v-if="!alone" @click="codeLocal">本地生成</el-button>
          <el-button type="primary" @click="codeDownload">立即下载</el-button>
          <el-button type="danger" @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      <el-dialog title="代码预览" width="80%" :visible.sync="previewVisible" v-if="previewVisible" :destroy-on-close="true" append-to-body>
        <CodePreview :form="form"/>
        <div slot="footer" class="dialog-footer">
          <el-button @click="previewVisible=false">取消</el-button>
          <el-button type="info" v-if="!alone" @click="codeLocal">本地生成</el-button>
          <el-button type="primary" @click="codeDownload">立即下载</el-button>
        </div>
      </el-dialog>
    </el-main>
  </el-container>
</template>

<script>
import {getAllTables, getAlone, getExportFileTypes, getDatabases, download, buildLocal} from '@/api/codegen'
import CodePreview from "@/components/CodePreview";

export default {
  name: "CodeGenerator",
  components: {
    CodePreview
  },
  data() {
    return {
      alone: false,
      previewVisible: false,
      databases: [],
      tableNames: [],
      exportFileTypes: [],
      exportFileGroups: [{
        val: "Api,Common,Service",
        title: "模块"
      },{
        val: "",
        title: "项目"
      },{
        val: "A1",
        title: "自定义"
      }],
      fileCustomSelected: false,
      form: {
        serverName: "",
        moduleName: "^([a-z]+)",
        basePackageName: "",
        dbName: "",
        tableNameRuleRegex: "([a-z]+)$",
        includeTableNames: [],
        includeGroup: 'Api,Common,Service',
        includeFileTypes: [],
        hasProvideService: true,
        hasBaseApi: true,
        hasOrm: true,
        hasMicro: true
      },
      rules: {
        serverName: [{required: true, message: "请输入服务名", trigger: "blur"}],
        moduleName: [{required: true, message: "请输入模块名", trigger: "blur"}],
        dbName: [{required: true, message: "请输入数据库名", trigger: "blur"}],
        basePackageName: [{required: true, message: "请输入基础包名", trigger: "blur"}],
      }
    }
  },
  created() {
    //获取是否独立部署
    getAlone().then(res => {
      this.alone = res || false
    })
    //获取导出文件类型
    getExportFileTypes().then(res => {
      this.exportFileTypes = res || []
    })
    //获取数据库集合
    getDatabases().then(res => {
      this.databases = res || []
    })
  },
  methods: {
    //选择数据库
    selDatabase(val) {
      this.tableNames = []
      if (val !== "") {
        if (this.form.serverName === "") {
          this.form.serverName = val
        }
        if (this.form.moduleName === "") {
          this.form.moduleName = val
        }
        if (this.form.basePackageName === "") {
          this.form.basePackageName = `top.zenyoung.cloud.${val}`
        }
        getAllTables(val).then(res => {
          this.tableNames = res || []
        })
      }
    },
    //分组选中事件
    handGroupChange(val) {
      if(val === "" || /^[A-Z|a-z|,]+$/.test(val)){
        this.fileCustomSelected = false
        this.form.includeFileTypes = []
      }else {
        this.fileCustomSelected = true
      }
    },
    //代码预览
    codePreview() {
      this.submitForm(() => {
        this.previewVisible = true
      })
    },
    //代码本地生成
    codeLocal() {
      this.submitForm(() => {
        buildLocal(this.form)
      })
    },
    //代码下载
    codeDownload() {
      this.submitForm(() => {
        download(this.form, `zy-${this.form.serverName}.zip`)
      })
    },
    //验证表单
    submitForm(fn) {
      this.$refs.form.validate((valid) => {
        if (valid) {
          fn()
        } else {
          console.log("error submit!")
          return false
        }
      })
    },
    //
    resetForm() {
      this.$refs.form.resetFields()
    }
  }
}
</script>

<style lang="scss" scoped>
.el-header {
  line-height: 64px;
  height: 64px;
  background-color: #334eb4;
}
</style>
