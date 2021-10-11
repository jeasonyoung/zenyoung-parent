<template>
  <el-container>
    <el-header>
      <i class="el-icon-coordinate"></i>代码生成器
    </el-header>
    <el-main>
      <el-collapse accordion>
        <!-- 数据库配置 -->
        <el-collapse-item>
          <template v-slot:title>
            <i class="el-icon-info"></i> 数据库配置:
            <el-tooltip effect="dark">
              <div v-html="connectTooltip" slot="content"></div>
              <small>{{ connectString }}</small>
            </el-tooltip>
          </template>
          <template v-slot:default>
            <el-form :model="form" :rules="rules" ref="form" label-width="120px">
              <el-form-item :required="true" label="服务器" prop="dbServer">
                <el-input v-model="form.dbServer" placeholder="请输入服务器地址(IP或域名)"/>
              </el-form-item>
              <el-form-item :required="true" label="数据库名" prop="dbName">
                <el-input v-model="form.dbName" placeholder="请输入数据库名称"/>
              </el-form-item>
              <el-form-item :required="true" label="数据库用户" prop="dbUser">
                <el-input v-model="form.dbUser" placeholder="请输入数据库访问用户名或账号名"/>
              </el-form-item>
              <el-form-item :required="true" label="数据库密码" prop="dbPasswd">
                <el-input v-model="form.dbPasswd" placeholder="请输入数据库访问密码"/>
              </el-form-item>
              <el-form-item :required="true" label="数据库端口" prop="dbPort">
                <el-input v-model.number="form.dbPort" placeholder="请输入数据库连接端口"/>
              </el-form-item>
              <el-form-item>
                <el-button icon="el-icon-refresh" type="primary" @click="onConnectTest('form')">测试连接</el-button>
                <el-button icon="el-icon-circle-check" @click="onConnectSave('form')">保存连接</el-button>
                <el-button icon="el-icon-refresh-left" @click="onConnectReset('form');">重置</el-button>
              </el-form-item>
            </el-form>
          </template>
        </el-collapse-item>
        <!-- 表查询 -->
        <el-collapse-item>
          <template v-slot:title>
            <i class="el-icon-s-data"></i>代码生成表数据:
            <el-button-group>
              <el-button type="primary" icon="el-icon-search" size="mini" @click.stop="queryRequest('queryForm')">搜索
              </el-button>
            </el-button-group>
          </template>
          <template v-slot:default>
            <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="100px">
              <el-form-item label="数据表名称">
                <el-input v-model="queryParams.tableName" placeholder="请求输入表名称/描述(支持模糊匹配)" clearable size="small"
                          @keyup.enter.native="queryRequest('queryForm')"/>
              </el-form-item>
            </el-form>
          </template>
        </el-collapse-item>
      </el-collapse>
      <!-- 表数据 -->
      <el-table :data="queryResult">
        <el-table-column type="selection" width="55" align="center"/>
        <el-table-column type="index" width="50" align="center"/>
        <el-table-column label="表名称" align="left" prop="tableName"/>
        <el-table-column label="表描述" align="left" prop="tableComment"/>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button type="text" size="small" icon="el-icon-view" @click="handlerPreview(scope.row)">预览</el-button>
            <el-button type="text" size="small" icon="el-icon-edit" @click="handlerEditTable(scope.row)">编辑</el-button>
            <el-button type="text" size="small" icon="el-icon-download" @click="handlerDownload(scope.row)">生成代码
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 预览界面 -->
      <el-dialog :title="preview.title" :visible.sync="preview.open" width="80%" top="5vh" append-to-body>
        <el-tabs v-model="preview.activeName">
          <el-tab-pane v-for="(val,key) in preview.data" :label="key.substring(key.lastIndexOf('/') + 1, key.indexOf(codeFileSuffix))" :name="key.substring(key.lastIndexOf('/') + 1, key.indexOf(codeFileSuffix))" :key="key">
            <pre><code class="hljs" v-html="highlightedCode(val,key)"/></pre>
          </el-tab-pane>
        </el-tabs>
      </el-dialog>
    </el-main>
  </el-container>
</template>

<script>
import codeHighlight from 'highlight.js/lib/core'
import 'highlight.js/styles/github.css'

codeHighlight.registerLanguage('java', require('highlight.js/lib/languages/java'));
codeHighlight.registerLanguage('html', require('highlight.js/lib/languages/xml'));
codeHighlight.registerLanguage('vue', require('highlight.js/lib/languages/xml'));
codeHighlight.registerLanguage('javascript', require('highlight.js/lib/languages/javascript'));
codeHighlight.registerLanguage('sql', require('highlight.js/lib/languages/sql'))

export default {
  name: "Generator",
  data() {
    return {
      form: {
        dbServer: "",//服务器
        dbName: "",//数据库名
        dbUser: "root",//用户
        dbPasswd: "",//密码
        dbPort: 3306,//端口
      },
      rules: {
        dbServer: [{require: true, message: '请输入数据库服务器地址', trigger: 'blur'}],
        dbName: [{require: true, message: '请输入数据库名称', trigger: 'blur'}],
        dbUser: [{require: true, message: '请输入数据库用户', trigger: 'blur'}],
        dbPasswd: [{require: true, message: '请输入数据库密码', trigger: 'blur'}],
        dbPort: [{require: true, type: 'number', message: '请输入数据库端口号', trigger: 'blur'}]
      },
      queryParams: {
        tableName: "",
      },
      queryResult: [],
      codeFileSuffix:'.ftlh',
      preview: {
        open: false,
        title: '代码预览',
        data: {},
        activeName: 'domain.java',
      }
    };
  },
  created() {

  },
  computed: {
    //数据库连接字符串
    connectString: function () {
      return `jdbc:mysql://${this.form.dbServer}:${this.form.dbPort}/${this.form.dbName}?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai`;
    },
    connectAccount: function () {
      return this.form.dbUser;
    },
    connectPasswd: function () {
      return this.form.dbPasswd;
    },
    connectTooltip: function () {
      return `连接字符串:${this.connectString}<br/>连接账号:${this.connectAccount}<br/>连接密码:${this.connectPasswd}`;
    }
  },
  methods: {
    //测试连接
    onConnectTest(formName) {
      this.formValidHandler(formName, () => {
        console.info('onConnectTest')
      })
    },
    //连接保存
    onConnectSave(formName) {
      this.formValidHandler(formName, () => {
        console.info('onConnectSave')
      })
    },
    //重置
    onConnectReset(formName) {
      if (this.$refs[formName]) {
        this.$refs[formName].resetFields();
      }
    },
    formValidHandler(formName, handler) {
      if (this.$refs[formName]) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            alert('submit');
            if (typeof handler === 'function') {
              handler();
            }
          } else {
            console.warn('valid fail!')
            return false;
          }
        })
      }
    },
    queryRequest(formName) {
      console.log('queryRequest:' + formName)
    },
    handlerPreview(row) {
      console.log(row);
    },
    handlerEditTable(row) {
      console.log(row);
    },
    handlerDownload(row) {
      console.log(row);
    },
    highlightedCode(code, key) {
      const ftlName = key.substring(key.lastIndexOf('/') + 1, key.indexOf(this.codeFileSuffix))
      let lang = ftlName.substring(ftlName.indexOf('.') + ftlName.length);
      const ret = codeHighlight.highlight(lang, code || '', true);
      return ret.value || '&nbsp;'
    }
  }
}
</script>

<style scoped>
.el-header, .el-footer {
  background-color: #b3c0d1;
  color: #333;
  text-align: center;
  line-height: 60px;
}

.el-header {
  text-align: left;
}

.el-main {
  background-color: #e9dce6;
  color: #333;
  text-align: center;
  line-height: 160px;
}

.el-button-group {
  width: 80%;
  margin-right: 20px;
  /* border: solid 1px red;*/
}

.el-button-group > .el-button {
  float: right;
  margin-right: 5px;
}
</style>
