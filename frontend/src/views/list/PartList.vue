<!-- 部件列表 -->
<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 搜索区域 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="部件编号">
                <a-input v-model="queryParam.id" placeholder="请输入部件编号"/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="部件名称">
                <a-input v-model="queryParam.name" placeholder="请输入部件名称"/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
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
        <a-button type="primary" icon="plus" @click="handleAdd">新增部件</a-button>
      </div>

      <!-- 表格区域 -->
      <s-table
        ref="table"
        size="default"
        rowKey="id"
        :columns="columns"
        :data="loadData"
        showPagination="auto"
      >
        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">编辑</a>
          <a-divider type="vertical" />
          <a @click="handleDetail(record)">查看详情</a>
          <a-divider type="vertical" />
          <a-popconfirm
            title="确定要删除这个部件吗？"
            @confirm="() => handleDelete(record)"
          >
            <a>删除</a>
          </a-popconfirm>
        </span>
      </s-table>

      <!-- 新增/编辑表单弹窗 -->
      <a-modal
        :title="mdl ? '编辑部件' : '新增部件'"
        :visible="visible"
        :confirmLoading="confirmLoading"
        @ok="handleOk"
        @cancel="handleCancel"
      >
        <a-form-model
          ref="form"
          :model="form"
          :rules="rules"
        >
          <a-form-model-item label="部件名称" prop="name">
            <div>部件名称</div>
            <a-input v-model="form.name" placeholder="请输入部件名称" />
          </a-form-model-item>
          <a-form-model-item label="具体名称" prop="specificName">
            <div>具体名称</div>
            <a-input v-model="form.specificName" placeholder="请输入具体名称" />
          </a-form-model-item>
          <a-form-model-item label="版本号" prop="version">
            <div>版本号</div>
            <a-input v-model="form.version" placeholder="请输入版本号" />
          </a-form-model-item>
          <a-form-model-item label="说明" prop="description">
            <div>说明</div>
            <a-textarea v-model="form.description" :rows="4" placeholder="请输入说明" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>

      <!-- 详情弹窗 -->
      <a-modal
        title="部件详情"
        :visible="detailVisible"
        :footer="null"
        @cancel="handleDetailCancel"
      >
        <a-descriptions bordered :column="2">
          <a-descriptions-item label="部件编号">
            {{ detailData?.id }}
          </a-descriptions-item>
          <a-descriptions-item label="部件名称">
            {{ detailData?.name }}
          </a-descriptions-item>
          <a-descriptions-item label="具体名称">
            {{ detailData?.specificName }}
          </a-descriptions-item>
          <a-descriptions-item label="版本号">
            {{ detailData?.version }}
          </a-descriptions-item>
          <a-descriptions-item label="说明" :span="2">
            {{ detailData?.description }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ detailData?.createTime | moment('YYYY-MM-DD HH:mm:ss') }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ detailData?.updateTime | moment('YYYY-MM-DD HH:mm:ss') }}
          </a-descriptions-item>
        </a-descriptions>
      </a-modal>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import { STable } from '@/components'

const columns = [
  {
    title: '部件编号',
    dataIndex: 'id'
  },
  {
    title: '部件名称',
    dataIndex: 'name'
  },
  {
    title: '具体名称',
    dataIndex: 'specificName'
  },
  {
    title: '版本号',
    dataIndex: 'version'
  },
  {
    title: '说明',
    dataIndex: 'description',
    ellipsis: true
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '200px',
    scopedSlots: { customRender: 'action' }
  }
]

export default {
  name: 'PartList',
  components: {
    STable
  },
  data () {
    return {
      columns,
      visible: false,
      confirmLoading: false,
      detailVisible: false,
      mdl: null,
      detailData: null,
      form: this.getEmptyForm(),
      rules: {
        name: [{ required: true, message: '请输入部件名称', trigger: 'blur' }],
        specificName: [{ required: true, message: '请输入具体名称', trigger: 'blur' }],
        version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
        description: [{ required: true, message: '请输入说明', trigger: 'blur' }]
      },
      queryParam: {},
      loadData: parameter => {
        return this.$http.get('/part/list', {
          params: Object.assign(parameter, this.queryParam)
        }).then(res => {
          return {
            data: res.result.data || [],
            pageSize: res.result.pageSize,
            pageNo: res.result.pageNo,
            totalCount: res.result.totalCount,
            totalPage: res.result.totalPage
          }
        })
      }
    }
  },
  methods: {
    getEmptyForm () {
      return {
        id: undefined,
        name: '',
        specificName: '',
        version: '',
        description: ''
      }
    },
    handleAdd () {
      this.mdl = null
      this.form = this.getEmptyForm()
      this.visible = true
    },
    handleEdit (record) {
      this.mdl = { ...record }
      this.form = { ...record }
      this.visible = true
    },
    async handleDetail (record) {
      try {
        const response = await this.$http.get(`/part/detail/${record.id}`)
        this.detailData = response.result
        this.detailVisible = true
      } catch (error) {
        this.$message.error('获取详情失败：' + error.message)
      }
    },
    async handleDelete (record) {
      await this.$http.delete(`/part/delete/${record.id}`)
      this.$message.success('删除成功')
      this.$refs.table.refresh()
    },
    async handleOk () {
      try {
        await this.$refs.form.validate()
        this.confirmLoading = true

        if (this.mdl) {
          await this.$http.post('/part/update', this.form)
          this.$message.success('修改成功')
        } else {
          await this.$http.post('/part/create', this.form)
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
    },
    handleDetailCancel () {
      this.detailVisible = false
      this.detailData = null
    }
  }
}
</script>
