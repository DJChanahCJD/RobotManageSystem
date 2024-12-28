<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 搜索区域 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="6" :sm="24">
              <a-form-item label="用户名">
                <a-input v-model="queryParam.name" placeholder="请输入用户名"/>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="手机号">
                <a-input v-model="queryParam.phone" placeholder="请输入手机号"/>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="权限">
                <a-select v-model="queryParam.authority" placeholder="请选择权限" allowClear>
                  <a-select-option value="admin">管理员</a-select-option>
                  <a-select-option value="user">普通用户</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
                <a-button style="margin-left: 8px" @click="() => this.queryParam = {}">重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <!-- 操作按钮区域 -->
      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="handleAdd">新增用户</a-button>
      </div>

      <!-- 表格区域 -->
      <s-table
        ref="table"
        size="default"
        rowKey="id"
        :columns="columns"
        :data="loadData"
        :pagination="{
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 10,
          showTotal: total => `共 ${total} 条`
        }"
        :alert="false"
      >
        <template slot="authority" slot-scope="text">
          <a-tag :color="text === 'admin' ? 'red' : 'blue'">
            {{ text === 'admin' ? '管理员' : '普通用户' }}
          </a-tag>
        </template>
        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">编辑</a>
          <a-divider type="vertical" />
          <a-popconfirm
            title="确定要删除这个用户吗？"
            @confirm="() => handleDelete(record)"
          >
            <a>删除</a>
          </a-popconfirm>
        </span>
      </s-table>

      <!-- 新增/编辑表单弹窗 -->
      <a-modal
        :title="mdl ? '编辑用户' : '新增用户'"
        :visible="visible"
        :confirmLoading="confirmLoading"
        @ok="handleOk"
        @cancel="handleCancel"
      >
        <a-form-model
          ref="form"
          :model="form"
          :rules="rules"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 14 }"
        >
          <a-form-model-item label="用户名" prop="name">
            <div>用户名</div>
            <a-input v-model="form.name" placeholder="请输入用户名" />
          </a-form-model-item>
          <a-form-model-item label="手机号" prop="phone">
            <div>手机号</div>
            <a-input v-model="form.phone" placeholder="请输入手机号" />
          </a-form-model-item>
          <a-form-model-item label="权限" prop="authority">
            <div>权限</div>
            <a-select v-model="form.authority" placeholder="请选择权限">
              <a-select-option value="admin">管理员</a-select-option>
              <a-select-option value="user">普通用户</a-select-option>
            </a-select>
          </a-form-model-item>
          <a-form-model-item
            label="密码"
            prop="password"
            :required="!mdl"
          >
            <div>密码</div>
            <a-input-password
              v-model="form.password"
              placeholder="请输入密码"
              :disabled="!!mdl"
            />
          </a-form-model-item>
        </a-form-model>
      </a-modal>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import { STable } from '@/components'
import { getUserList, createUser, updateUser, deleteUser } from '@/api/user'

const columns = [
  {
    title: '用户名',
    dataIndex: 'name'
  },
  {
    title: '手机号',
    dataIndex: 'phone'
  },
  {
    title: '权限',
    dataIndex: 'authority',
    scopedSlots: { customRender: 'authority' }
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    sorter: true
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '150px',
    scopedSlots: { customRender: 'action' }
  }
]

export default {
  name: 'UserList',
  components: {
    STable
  },
  data () {
    return {
      columns,
      visible: false,
      confirmLoading: false,
      mdl: null,
      form: this.getEmptyForm(),
      rules: {
        name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        authority: [{ required: true, message: '请选择权限', trigger: 'change' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      queryParam: {},
      loadData: parameter => {
        console.log('loadData parameter:', parameter)
        return getUserList(Object.assign(parameter, this.queryParam))
          .then(res => {
            console.log('loadData response:', res)
            return {
              pageSize: res.result.pageSize,
              pageNo: res.result.pageNo,
              totalCount: res.result.totalCount,
              data: res.result.data || []
            }
          })
      }
    }
  },
  methods: {
    getEmptyForm () {
      return {
        name: '',
        phone: '',
        authority: 'user',
        password: ''
      }
    },
    handleAdd () {
      this.mdl = null
      this.form = this.getEmptyForm()
      this.visible = true
    },
    handleEdit (record) {
      this.mdl = { ...record }
      this.form = {
        ...record,
        password: '******' // 编辑时不显示真实密码
      }
      this.visible = true
    },
    async handleDelete (record) {
      await deleteUser(record.id)
      this.$message.success('删除成功')
      this.$refs.table.refresh()
    },
    async handleOk () {
      try {
        await this.$refs.form.validate()
        this.confirmLoading = true

        if (this.mdl) {
          const data = { ...this.form }
          delete data.password // 编辑时不提交密码
          await updateUser(data)
          this.$message.success('修改成功')
        } else {
          await createUser(this.form)
          this.$message.success('新增成功')
        }

        this.visible = false
        this.$refs.table.refresh()
      } catch (error) {
        console.error(error)
      } finally {
        this.confirmLoading = false
      }
    },
    handleCancel () {
      this.visible = false
      this.$refs.form.resetFields()
    }
  }
}
</script>
