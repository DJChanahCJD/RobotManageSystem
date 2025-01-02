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
              <a-form-item label="说明">
                <a-input v-model="queryParam.description" placeholder="请输入说明"/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="版本号">
                <a-input v-model="queryParam.version" placeholder="请输入版本号"/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="分类">
                <a-select v-model="queryParam.classification" placeholder="请选择分类" allowClear>
                  <a-select-option v-for="item in classificationList" :key="item.id" :value="item.id">
                    {{ item.name }} ({{ item.nameEn }})
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
                <a-button style="margin-left: 8px" @click="resetQuery">重置</a-button>
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
        <!-- 分类列 -->
        <template slot="classification" slot-scope="text, record">
          <span v-if="record.extAttrs">
            <a-badge :color="getClassificationColor(record.extAttrs)" :text="getClassificationName(record.extAttrs)" />
          </span>
        </template>

        <!-- 属性列 -->
        <template slot="attributes" slot-scope="text, record">
          <div v-if="record.clsAttrs && record.clsAttrs[0]">
            <div v-for="(value, key) in record.clsAttrs[0].Classification" :key="key">
              {{ key }}: {{ value }}
            </div>
          </div>
        </template>

        <!-- 操作列 -->
        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">编辑</a>
          <a-divider type="vertical" />
          <a @click="handleDetail(record)">查看详情</a>
          <a-divider type="vertical" />
          <a-popconfirm
            title="确定要删除这个部件吗？"
            @confirm="() => handleDelete(record)"
          >
            <a style="color: red">删除</a>
          </a-popconfirm>
        </span>
      </s-table>

      <!-- 新增/编辑表单弹窗 -->
      <a-modal
        :title="modalTitle"
        :visible="visible"
        :confirmLoading="confirmLoading"
        @ok="handleOk"
        @cancel="handleCancel"
        width="800px"
      >
        <a-form-model
          ref="form"
          :model="form"
          :rules="rules"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 14 }"
        >
          <a-form-model-item label="部件名称" prop="name">
            <div>部件名称</div>
            <a-input v-model="form.name" placeholder="请输入部件名称" />
          </a-form-model-item>

          <a-form-model-item label="说明" prop="description">
            <div>说明</div>
            <a-textarea v-model="form.description" :rows="4" placeholder="请输入说明" />
          </a-form-model-item>

          <a-form-model-item label="分类" prop="classification">
            <div>分类</div>
            <a-select
            v-model="form.classification"
            placeholder="请选择分类"
            @change="handleClassificationChange"
            style="width: 40%"
          >
              <a-select-option v-for="item in classificationList" :key="item.id" :value="item.id">
                <a-badge :color="getClassificationColor([{name: 'Classification', value: item}])" />
                {{ item.name }} ({{ item.nameEn }})
              </a-select-option>
            </a-select>
          </a-form-model-item>

          <!-- 动态属性表单 -->
          <template v-if="form.classification && classificationAttributes.length">
            <a-divider>分类属性</a-divider>
            <a-row :gutter="24">
              <a-col :span="12" v-for="attr in classificationAttributes" :key="attr.id">
                <a-form-model-item
                  :label="attr.name"
                  :prop="'attributes.' + attr.name"
                  :label-col="{ span: 8 }"
                  :wrapper-col="{ span: 16 }"
                >
                  <a-input
                    v-model="form.attributes[attr.name]"
                    :placeholder="`请输入${attr.name}`"
                  />
                </a-form-model-item>
              </a-col>
            </a-row>
          </template>
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
            {{ detailData?.lastUpdateTime | moment('YYYY-MM-DD HH:mm:ss') }}
          </a-descriptions-item>
        </a-descriptions>
      </a-modal>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import { STable } from '@/components'
import { getPartList, createPart, updatePart, deletePart, getPartDetail, getClassificationList, getPartAttributes } from '@/api/part'

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
    title: '版本号',
    dataIndex: 'version'
  },
  {
    title: '说明',
    dataIndex: 'description',
    ellipsis: true
  },
  {
    title: '分类',
    dataIndex: 'classification',
    scopedSlots: { customRender: 'classification' }
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    scopedSlots: { customRender: 'createTime' }
  },
  {
    title: '更新时间',
    dataIndex: 'lastUpdateTime',
    scopedSlots: { customRender: 'lastUpdateTime' }
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
      form: {
        id: undefined,
        masterId: undefined,
        name: '',
        description: '',
        classification: undefined,
        attributes: {}
      },
      rules: {
        name: [{ required: true, message: '请输入部件名称', trigger: 'blur' }],
        classification: [{ required: true, message: '请选择分类', trigger: 'change' }]
      },
      queryParam: {},
      classificationList: [],
      classificationAttributes: [],
      modalTitle: '新增部件',
      loadData: parameter => {
        const params = Object.assign(parameter, this.queryParam)
        return getPartList(params).then(res => {
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
  created () {
    this.loadClassificationList()
  },
  methods: {
    handleAdd () {
      this.form = {
        id: undefined,
        masterId: undefined,
        name: '',
        description: '',
        classification: undefined,
        attributes: {}
      }
      this.modalTitle = '新增部件'
      this.visible = true
    },
    handleEdit (record) {
      try {
        const classification = record.extAttrs?.find(attr => attr.name === 'Classification')?.value
        const attributes = record.clsAttrs?.[0]?.Classification || {}

        this.form = {
          id: record.id,
          masterId: record.master?.id,
          name: record.name,
          description: record.description,
          classification: classification?.id,
          attributes: JSON.parse(JSON.stringify(attributes))
        }

        this.modalTitle = '编辑部件'
        this.visible = true
      } catch (error) {
        console.error('Edit error:', error)
        this.$message.error('加载编辑数据失败')
      }
    },
    async handleDetail (record) {
      try {
        const response = await getPartDetail(record.id)
        console.log('response from getPartDetail: ', response)
        this.detailData = response
        this.detailVisible = true
      } catch (error) {
        this.$message.error('获取详情失败：' + error.message)
      }
    },
    async handleDelete (record) {
      await deletePart(record.master.id)
      this.$message.success('删除成功')
      this.$refs.table.refresh()
    },
    async handleOk () {
      try {
        this.confirmLoading = true

        const formData = {
          id: this.form.id,
          name: this.form.name,
          description: this.form.description,
          partName: this.form.name
        }

        if (this.form.id) {
          formData.master = {
            id: this.form.masterId
          }
        }

        if (this.form.classification) {
          const classification = JSON.parse(JSON.stringify(
            this.classificationList.find(c => c.id === this.form.classification)
          ))

          if (classification) {
            formData.extAttrs = [
              {
                name: 'Classification',
                value: classification.id
              }
            ]

            const attributes = JSON.parse(JSON.stringify(this.form.attributes || {}))
            // 将 clsAttrs 转换为字符串后再解析，确保是纯对象
            formData.clsAttrs = JSON.parse(JSON.stringify([
              {
                Classification: attributes
              }
            ]))
          }
        }

        console.log('Creating/Updating part with data:', formData)

        if (this.form.id) {
          await updatePart(formData.master.id, formData)
          this.$message.success('修改成功')
        } else {
          const res = await createPart(formData)
          if (res.error) {
            throw new Error(res.error)
          }
          this.$message.success('新增成功')
        }

        this.visible = false
        this.$refs.table.refresh()
      } catch (error) {
        console.error('Error:', error)
        this.$message.error(error.message || '操作失败')
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
    },
    async loadClassificationList () {
      try {
        const res = await getClassificationList()
        this.classificationList = res.result || []
      } catch (error) {
        this.$message.error('获取分类列表失败：' + error.message)
      }
    },
    getClassificationName (extAttrs) {
      const classification = extAttrs.find(attr => attr.name === 'Classification')
      if (classification && classification.value) {
        return `${classification.value.name}`
      }
      return '未分类'
    },
    resetQuery () {
      this.queryParam = {}
      this.$refs.table.refresh(true)
    },
    getClassificationColor (extAttrs) {
      const colors = {
        '控制部件': 'purple',
        '驱动部件': 'green',
        '电器部件': 'orange',
        '传感器部件': 'red',
        '执行器部件': 'blue',
        '其他辅助部件': 'cyan'
      }
      const classification = extAttrs.find(attr => attr.name === 'Classification')
      if (classification && classification.value) {
        return colors[classification.value.name] || 'grey'
      }
      return 'grey'
    },
    async handleClassificationChange (value) {
      if (!value) {
        this.classificationAttributes = []
        this.$set(this.form, 'attributes', {})
        return
      }

      try {
        const res = await getPartAttributes(value)
        this.classificationAttributes = res.result

        const newAttributes = {}
        this.classificationAttributes.forEach(attr => {
          newAttributes[attr.name] = this.form.attributes[attr.name] || null
        })

        this.$set(this.form, 'attributes', newAttributes)
      } catch (error) {
        this.$message.error('获取分类属性失败：' + error.message)
      }
    },
    resetForm () {
      this.$refs.form.resetFields()
      this.$set(this.form, 'attributes', {})
    }
  }
}
</script>

<style lang="less" scoped>
.table-operator {
  margin-bottom: 18px;
}
.table-page-search-wrapper {
  .ant-form-inline {
    .ant-form-item {
      display: flex;
      margin-bottom: 24px;
      margin-right: 0;
      > .ant-form-item-label {
        width: auto;
        line-height: 32px;
        padding-right: 8px;
      }
    }
  }
}
</style>
