<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 搜索区域 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="产品编号">
                <a-input v-model="queryParam.id" placeholder="请输入产品编号"/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="产品名称">
                <a-input v-model="queryParam.productName" placeholder="请输入产品名称"/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="工程阶段">
                <a-select v-model="queryParam.productStage" placeholder="请选择工程阶段" clearable>
                  <a-select-option v-for="stage in engineeringStages" :key="stage.value" :value="stage.value">{{ stage.label }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="部件名称">
                <a-input v-model="queryParam.partName" placeholder="请输入部件名称"/>
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
        <a-button type="primary" icon="plus" @click="handleAdd">新增产品</a-button>
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
      >
        <template slot="productStage" slot-scope="text">
          <a-tag :color="getStageColor(text)">{{ text }}</a-tag>
        </template>
        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">编辑</a>
          <a-divider type="vertical" />
          <a @click="handleDetail(record)">查看详情</a>
          <a-divider type="vertical" />
          <a-popconfirm
            title="确定要删除这个产品吗？"
            @confirm="() => handleDelete(record)"
          >
            <a style="color: red">删除</a>
          </a-popconfirm>
        </span>
      </s-table>

      <!-- 新增/编辑表单弹窗 -->
      <a-modal
        :title="mdl ? '编辑产品' : '新增产品'"
        :visible="visible"
        :confirmLoading="confirmLoading"
        @ok="handleOk"
        @cancel="handleCancel"
        width="700px"
      >
        <a-form-model
          ref="form"
          :model="form"
          :rules="rules"
          :label-col="{ span: 4 }"
          :wrapper-col="{ span: 18 }"
        >
          <a-form-model-item label="产品名称" prop="productName">
            <div>产品名称</div>
            <a-input v-model="form.productName" placeholder="请输入产品名称" />
          </a-form-model-item>
          <a-form-model-item label="基本信息" prop="productInformation">
            <div>基本信息</div>
            <a-textarea v-model="form.productInformation" :rows="4" placeholder="请输入基本信息" />
          </a-form-model-item>
          <a-form-model-item label="负责人" prop="productOwner">
            <div>负责人</div>
            <a-input v-model="form.productOwner" placeholder="请输入负责人" />
          </a-form-model-item>
          <a-form-model-item label="产品阶段" prop="productStage">
            <div>产品阶段</div>
            <a-select v-model="form.productStage" placeholder="请选择产品阶段" style="width: 100%">
              <a-select-option v-for="stage in engineeringStages" :key="stage.value" :value="stage.value">{{ stage.label }}</a-select-option>
            </a-select>
          </a-form-model-item>
        </a-form-model>
        <a-form-model-item label="部件名称" prop="partName">
          <div>部件名称</div>
          <a-input v-model="form.partName" placeholder="请输入部件名称" />
        </a-form-model-item>
      </a-modal>

      <!-- 详情弹窗 -->
      <a-modal
        title="产品详情"
        :visible="detailVisible"
        :footer="null"
        @cancel="handleDetailCancel"
        width="800px"
      >
        <a-descriptions bordered :column="2">
          <a-descriptions-item label="产品编号">
            {{ detailData?.id }}
          </a-descriptions-item>
          <a-descriptions-item label="产品名称">
            {{ detailData?.productName }}
          </a-descriptions-item>
          <a-descriptions-item label="基本信息" :span="2">
            {{ detailData?.productInformation }}
          </a-descriptions-item>
          <a-descriptions-item label="负责人">
            {{ detailData?.productOwner }}
          </a-descriptions-item>
          <a-descriptions-item label="产品阶段">
            <a-tag :color="getStageColor(detailData?.productStage)">
              {{ detailData?.productStage }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="部件名称">
            {{ detailData?.partName }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ detailData?.createTime }}
          </a-descriptions-item>
        </a-descriptions>
      </a-modal>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import { STable } from '@/components'
import { getProductDetail, createProduct, updateProduct, deleteProduct, getProductList } from '@/api/product'
const engineeringStages = [
  { value: 'InitialStage', label: '初始阶段' },
  { value: 'DesignStage', label: '概念化和设计阶段' },
  { value: 'DevelopmentStage', label: '原型开发阶段' },
  { value: 'PartStage', label: '部件采购和制造阶段' },
  { value: 'SaleStage', label: '产品销售阶段' }
]
const columns = [
  {
    title: '产品编号',
    dataIndex: 'id',
    width: '180px'
  },
  {
    title: '产品名称',
    dataIndex: 'productName'
  },
  {
    title: '基本信息',
    dataIndex: 'productInformation',
    ellipsis: true
  },
  {
    title: '负责人',
    dataIndex: 'productOwner'
  },
  {
    title: '产品阶段',
    dataIndex: 'productStage',
    scopedSlots: { customRender: 'productStage' }
  },
  {
    title: '部件名称',
    dataIndex: 'partName'
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '200px',
    scopedSlots: { customRender: 'action' }
  }
]

export default {
  name: 'ProductList',
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
        productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
        productInformation: [{ required: true, message: '请输入基本信息', trigger: 'blur' }],
        productOwner: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
        productStage: [{ required: true, message: '请选择产品阶段', trigger: 'change' }],
        partName: [{ required: true, message: '请输入部件名称', trigger: 'blur' }]
      },
      engineeringStages,
      queryParam: {},
      loadData: parameter => {
        return getProductList(Object.assign(parameter, this.queryParam)).then(res => {
          console.log(res)
          return {
            data: res.result.data || [],
            pageSize: res.result.pageSize,
            pageNo: res.result.pageNo,
            totalCount: res.result.totalCount,
            totalPage: res.result.totalPage
          }
        })
      },
      detailVisible: false,
      detailData: null
    }
  },
  methods: {
    getEmptyForm () {
      return {
        id: undefined,
        productName: '',
        productInformation: '',
        productOwner: '',
        productStage: 'InitialStage'
      }
    },
    getStageColor (stage) {
      const stageColors = {
        'InitialStage': 'blue',
        'DesignStage': 'green',
        'DevelopmentStage': 'orange',
        'PartStage': 'purple',
        'SaleStage': 'red'
      }
      return stageColors[stage] || 'default'
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
        const response = await getProductDetail(record.id)
        this.detailData = response.result
        this.detailVisible = true
      } catch (error) {
        this.$message.error('获取详情失败：' + error.message)
      }
    },
    async handleDelete (record) {
      await deleteProduct(record.id)
      this.$message.success('删除成功')
      this.$refs.table.refresh()
    },
    async handleOk () {
      try {
        this.confirmLoading = true

        if (this.mdl) {
          await updateProduct(this.mdl.id, this.form)
          this.$message.success('修改成功')
        } else {
          await createProduct(this.form)
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
