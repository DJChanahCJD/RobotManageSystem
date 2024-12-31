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
              <a-form-item label="蓝图说明">
                <a-input v-model="queryParam.blueprintDescription" placeholder="请输入蓝图说明"/>
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
        <template slot="bluePrintImage" slot-scope="text, record">
          <img
            v-if="record.bluePrint"
            :src="record.bluePrint"
            style="max-width: 100px; max-height: 100px; object-fit: contain"
            @click="handlePreview(record)"
          />
          <span v-else>暂无图片</span>
        </template>
        <template slot="action" slot-scope="text, record">
          <a-space>
            <a @click="handleDetail(record)">查看</a>
            <a-upload
              :showUploadList="false"
              :beforeUpload="file => handleUpload(file, record)"
            >
              <a>上传</a>
            </a-upload>
            <a @click="handleEdit(record)">修改</a>
            <a-popconfirm
              title="确定删除吗？"
              @confirm="() => handleDelete(record)"
            >
              <a style="color: #ff4d4f">删除</a>
            </a-popconfirm>
          </a-space>
        </template>
      </s-table>

      <!-- 新增/修改蓝图表单弹窗 -->
      <a-modal
        :title="mdl ? '修改蓝图' : '新增蓝图'"
        :visible="visible"
        :confirmLoading="confirmLoading"
        @ok="handleOk"
        @cancel="handleCancel"
      >
        <a-form
          ref="form"
          :model="form"
          :rules="rules"
        >
          <a-form-item label="蓝图说明">
            <a-textarea
              v-model="form.blueprintDescription"
              :rows="4"
              placeholder="请输入蓝图说明"
            />
          </a-form-item>
        </a-form>
      </a-modal>

      <!-- 添加详情弹窗 -->
      <a-modal
        title="蓝图详情"
        :visible="detailVisible"
        :footer="null"
        @cancel="handleDetailCancel"
        width="800px"
      >
        <a-spin :spinning="detailLoading">
          <template v-if="detailData">
            <a-descriptions bordered :column="2">
              <a-descriptions-item label="蓝图编号">
                {{ detailData.id }}
              </a-descriptions-item>
              <a-descriptions-item label="创建人">
                {{ detailData.creator }}
              </a-descriptions-item>
              <a-descriptions-item label="创建时间">
                {{ new Date(detailData.createTime).toLocaleString() }}
              </a-descriptions-item>
              <a-descriptions-item label="更新时间">
                {{ new Date(detailData.lastUpdateTime).toLocaleString() }}
              </a-descriptions-item>
              <a-descriptions-item label="说明" :span="2">
                {{ detailData.blueprintDescription }}
              </a-descriptions-item>
              <a-descriptions-item label="预览图" :span="2">
                <img :src="detailData.bluePrint" alt="蓝图预览" style="max-width: 100%;" v-if="detailData.bluePrint"/>
                <span v-else>暂无预览图</span>
              </a-descriptions-item>
            </a-descriptions>
          </template>
        </a-spin>
      </a-modal>

      <!-- 添加图片预览弹窗 -->
      <a-modal
        :visible="previewVisible"
        :footer="null"
        @cancel="previewVisible = false"
      >
        <img :src="previewImage" style="width: 100%" />
      </a-modal>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import { STable } from '@/components'
import {
  getBlueprintList,
  createBlueprint,
  updateBlueprint,
  deleteBlueprint,
  getBlueprintDetail,
  uploadBlueprint
} from '@/api/blueprint'
import {
  Form,
  Upload,
  Icon,
  Button,
  Input,
  Modal,
  Descriptions
} from 'ant-design-vue'

const columns = [
  {
    title: '蓝图编号',
    dataIndex: 'id'
  },
  {
    title: '预览图',
    dataIndex: 'bluePrint',
    scopedSlots: { customRender: 'bluePrintImage' },
    width: 120
  },
  {
    title: '蓝图说明',
    dataIndex: 'blueprintDescription'
  },
  {
    title: '创建人',
    dataIndex: 'creator'
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    sorter: true,
    customRender: (text) => {
      return new Date(text).toLocaleString()
    }
  },
  {
    title: '更新时间',
    dataIndex: 'lastUpdateTime',
    customRender: (text) => {
      return new Date(text).toLocaleString()
    }
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
    STable,
    [Form.name]: Form,
    [Form.Item.name]: Form.Item,
    [Upload.name]: Upload,
    [Icon.name]: Icon,
    [Button.name]: Button,
    [Input.name]: Input,
    [Input.TextArea.name]: Input.TextArea,
    [Modal.name]: Modal,
    [Descriptions.name]: Descriptions,
    [Descriptions.Item.name]: Descriptions.Item
  },
  data () {
    return {
      columns,
      visible: false,
      confirmLoading: false,
      mdl: null,
      form: {
        id: undefined,
        blueprintDescription: '',
        bluePrint: undefined
      },
      rules: {
        blueprintDescription: [{ required: true, message: '请输入蓝图说明', trigger: 'blur' }],
        bluePrint: [{ required: true, message: '请上传蓝图文件', trigger: 'change' }]
      },
      queryParam: {
        id: undefined,
        blueprintDescription: ''
      },
      loadData: parameter => {
        return getBlueprintList(this.queryParam.id, this.queryParam.blueprintDescription, parameter.pageNo, parameter.pageSize)
          .then(res => {
            return {
              data: res.result.data || [],
              pageSize: res.result.pageSize,
              pageNo: res.result.pageNo,
              totalCount: res.result.totalCount,
              totalPage: res.result.totalPage
            }
          })
          .catch(error => {
            this.$message.error('获取蓝图列表失败：' + error.message)
            return []
          })
      },
      selectedRowKeys: [],
      selectedRows: [],
      detailVisible: false,
      detailData: null,
      detailLoading: false,
      previewVisible: false,
      previewImage: ''
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
      this.form = {
        id: undefined,
        blueprintDescription: '',
        bluePrint: undefined
      }
      this.visible = true
    },
    handleEdit (record) {
      this.mdl = { ...record }
      this.form = {
        id: record.id,
        blueprintDescription: record.blueprintDescription
        // bluePrint: record.bluePrint
      }
      this.visible = true
    },
    async handleDetail (record) {
      this.detailVisible = true
      this.detailLoading = true
      try {
        const response = await getBlueprintDetail(record.id)
        this.detailData = response.result
      } catch (error) {
        this.$message.error('获取详情失败：' + error.message)
      } finally {
        this.detailLoading = false
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
      const isImage = file.type.startsWith('image/')
      if (!isImage) {
        this.$message.error('只能上传图片格式的蓝图文件!')
        return false
      }
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isLt5M) {
        this.$message.error('蓝图文件必须小于5MB!')
        return false
      }
      return false // 阻止自动上传
    },
    handleFileChange (info) {
      if (info.file.status !== 'uploading') {
        this.form.bluePrint = info.file
      }
    },
    async handleOk () {
      try {
        if (!this.form.blueprintDescription) {
          this.$message.warning('请输入蓝图说明')
          return
        }

        this.confirmLoading = true

        // 统一使用 JSON 格式，只更新说明
        const updateData = {
          id: this.mdl.id,
          blueprintDescription: this.form.blueprintDescription
        }

        if (this.mdl) {
          // 修改
          await updateBlueprint(this.mdl.id, updateData)
          this.$message.success('修改成功')
        } else {
          // 新增
          await createBlueprint(updateData)
          this.$message.success('新增成功')
        }

        this.visible = false
        this.$refs.table.refresh()
      } catch (error) {
        this.$message.error('操作失败：' + (error.message || '未知错误'))
      } finally {
        this.confirmLoading = false
      }
    },
    handleCancel () {
      this.visible = false
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.resetFields()
        }
      })
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
    },
    handlePreview (record) {
      if (record.bluePrint) {
        this.previewImage = record.bluePrint
        this.previewVisible = true
      }
    },
    async handleUpload (file, record) {
      this.$message.info('上传中...')
      try {
        const formData = new FormData()
        formData.append('bluePrint', file)
        await uploadBlueprint(record.id, formData)
        this.$message.success('上传成功')
        this.$refs.table.refresh()
      } catch (error) {
        this.$message.error('上传失败：' + (error.message || '未知错误'))
      }
      return false // 阻止自动上传
    }
  }
}
</script>

<style scoped>
.ant-space {
  display: flex;
  gap: 8px;
}
/* 操作按钮样式 */
a {
  color: #1890ff;
}
</style>
