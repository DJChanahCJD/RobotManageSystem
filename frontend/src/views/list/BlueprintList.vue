<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 搜索区域 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="蓝图编号">
                <a-input v-model="queryParam.id" placeholder="请输入蓝图编号"/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="蓝图名称">
                <a-input v-model="queryParam.name" placeholder="请输入蓝图名称"/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="handleSearch">查询</a-button>
                <a-button style="margin-left: 8px" @click="() => this.queryParam = {}">重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <!-- 操作按钮区域 -->
      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="handleAdd">新增蓝图</a-button>
        <a-button type="danger" icon="delete" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">
          批量删除
        </a-button>
      </div>

      <!-- 表格区域 -->
      <s-table
        ref="table"
        size="default"
        rowKey="id"
        :columns="columns"
        :data="loadData"
        :alert="true"
        :rowSelection="rowSelection"
        showPagination="auto"
      >
        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">修改</a>
          <a-divider type="vertical" />
          <a @click="handleDetail(record)">查看详情</a>
          <a-divider type="vertical" />
          <a-popconfirm
            title="确定要删除这条蓝图吗？"
            @confirm="() => handleDelete(record)"
          >
            <a>删除</a>
          </a-popconfirm>
        </span>
      </s-table>

      <!-- 新增/修改蓝图表单弹窗 -->
      <a-modal
        :title="mdl ? '修改蓝图' : '新增蓝图'"
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
          <a-form-model-item label="蓝图名称" prop="name">
            <div>蓝图名称</div>
            <a-input v-model="form.name" placeholder="请输入蓝图名称" />
          </a-form-model-item>
          <a-form-model-item label="蓝图文件" prop="blueprint">
            <div>上传蓝图</div>
            <a-upload
              name="file"
              :multiple="false"
              :beforeUpload="beforeUpload"
              @change="handleFileChange"
            >
              <a-button>
                <a-icon type="upload" /> 选择文件
              </a-button>
            </a-upload>
          </a-form-model-item>
          <a-form-model-item label="说明" prop="description">
            <div>蓝图说明</div>
            <a-textarea v-model="form.description" :rows="4" placeholder="请输入蓝图说明" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>

      <!-- 添加详情弹窗 -->
      <a-modal
        title="蓝图详情"
        :visible="detailVisible"
        :footer="null"
        @cancel="handleDetailCancel"
        width="800px"
      >
        <template v-if="detailData">
          <a-descriptions bordered :column="2">
            <a-descriptions-item label="蓝图编号">
              {{ detailData.id }}
            </a-descriptions-item>
            <a-descriptions-item label="蓝图名称">
              {{ detailData.name }}
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">
              {{ detailData.createTime }}
            </a-descriptions-item>
            <a-descriptions-item label="更新时间">
              {{ detailData.updateTime }}
            </a-descriptions-item>
            <a-descriptions-item label="创建者">
              {{ detailData.creator }}
            </a-descriptions-item>
            <a-descriptions-item label="说明" :span="2">
              {{ detailData.description }}
            </a-descriptions-item>
            <a-descriptions-item label="预览图" :span="2">
              <img :src="detailData.blueprint" alt="蓝图预览" style="max-width: 100%;" />
            </a-descriptions-item>
          </a-descriptions>
        </template>
      </a-modal>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import { STable } from '@/components'
import { getBlueprintList, createBlueprint, updateBlueprint, deleteBlueprint, getBlueprintDetail } from '@/api/blueprint'

const columns = [
  {
    title: '蓝图编号',
    dataIndex: 'id'
  },
  {
    title: '蓝图名称',
    dataIndex: 'name'
  },
  {
    title: '说明',
    dataIndex: 'description'
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    sorter: true
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '200px',
    scopedSlots: { customRender: 'action' }
  }
]

export default {
  name: 'BlueprintList',
  components: {
    STable
  },
  data () {
    return {
      columns,
      visible: false,
      confirmLoading: false,
      mdl: null,
      form: {
        id: undefined,
        name: '',
        blueprint: undefined,
        description: ''
      },
      rules: {
        name: [{ required: true, message: '请输入蓝图名称', trigger: 'blur' }],
        blueprint: [{ required: true, message: '请上传蓝图文件', trigger: 'change' }]
      },
      queryParam: {},
      loadData: parameter => {
        return getBlueprintList(Object.assign(parameter, this.queryParam))
          .then(res => {
            return res.result
          })
      },
      selectedRowKeys: [],
      selectedRows: [],
      detailVisible: false,
      detailData: null
    }
  },
  computed: {
    rowSelection () {
      return {
        selectedRowKeys: this.selectedRowKeys,
        onChange: this.onSelectChange
      }
    }
  },
  methods: {
    handleAdd () {
      this.mdl = null
      this.form = { id: undefined, name: '', blueprint: undefined, description: '' }
      this.visible = true
    },
    handleEdit (record) {
      this.mdl = { ...record }
      this.form = { ...record }
      this.visible = true
    },
    async handleDetail (record) {
      try {
        const response = await getBlueprintDetail(record.id)
        this.detailData = response.result
        this.detailVisible = true
      } catch (error) {
        this.$message.error('获取详情失败：' + error.message)
      }
    },
    async handleDelete (record) {
      await deleteBlueprint(record.id)
      this.$message.success('删除成功')
      this.$refs.table.refresh()
    },
    async handleBatchDelete () {
      await Promise.all(this.selectedRowKeys.map(id => deleteBlueprint(id)))
      this.$message.success('批量删除成功')
      this.selectedRowKeys = []
      this.$refs.table.refresh()
    },
    beforeUpload (file) {
      // 限制文件类型和大小
      const isImage = file.type.startsWith('image/')
      if (!isImage) {
        this.$message.error('只能上传图片格式的蓝图文件!')
      }
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isLt5M) {
        this.$message.error('蓝图文件必须小于5MB!')
      }
      return false
    },
    handleFileChange (info) {
      this.form.blueprint = info.file
    },
    async handleOk () {
      try {
        await this.$refs.form.validate()
        this.confirmLoading = true

        const formData = new FormData()
        Object.keys(this.form).forEach(key => {
          formData.append(key, this.form[key])
        })

        if (this.mdl) {
          await updateBlueprint(formData)
          this.$message.success('修改成功')
        } else {
          await createBlueprint(formData)
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
    onSelectChange (selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
    },
    handleSearch () {
      this.$refs.table.refresh(true)
    },
    handleDetailCancel () {
      this.detailVisible = false
      this.detailData = null
    }
  }
}
</script>
