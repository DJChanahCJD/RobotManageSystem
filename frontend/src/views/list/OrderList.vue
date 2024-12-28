<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 搜索区域 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="6" :sm="24">
              <a-form-item label="订单日期">
                <a-range-picker v-model="queryParam.dateRange" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="物品名称">
                <a-input v-model="queryParam.name" placeholder="请输入物品名称"/>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="类型">
                <a-select v-model="queryParam.type" placeholder="请选择类型" allowClear>
                  <a-select-option value="1">类型一</a-select-option>
                  <a-select-option value="2">类型二</a-select-option>
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
        <a-button type="primary" icon="plus" @click="handleAdd">新增订单</a-button>
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
        <span slot="orderDate" slot-scope="text">
          {{ text | moment('YYYY-MM-DD') }}
        </span>
        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">修改</a>
          <a-divider type="vertical" />
          <a @click="handleDetail(record)">查看详情</a>
          <a-divider type="vertical" />
          <a-popconfirm
            title="确定要删除这条订单吗？"
            @confirm="() => handleDelete(record)"
          >
            <a>删除</a>
          </a-popconfirm>
        </span>
      </s-table>

      <!-- 新增/修改订单表单弹窗 -->
      <a-modal
        :title="mdl ? '修改订单' : '新增订单'"
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
          <a-form-model-item label="物品名称" prop="name">
            <div>物品名称</div>
            <a-input v-model="form.name" placeholder="请输入物品名称" />
          </a-form-model-item>
          <a-form-model-item label="数量" prop="quantity">
            <div>数量</div>
            <a-input-number v-model="form.quantity" :min="1" style="width: 100%" />
          </a-form-model-item>
          <a-form-model-item label="类型" prop="type">
            <div>类型</div>
            <a-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
              <a-select-option value="1">类型一</a-select-option>
              <a-select-option value="2">类型二</a-select-option>
            </a-select>
          </a-form-model-item>
          <a-form-model-item label="订单日期" prop="orderDate">
            <div>订单日期</div>
            <a-date-picker v-model="form.orderDate" style="width: 100%" />
          </a-form-model-item>
        </a-form-model>
      </a-modal>

      <!-- 详情弹窗 -->
      <a-modal
        title="订单详情"
        :visible="detailVisible"
        :footer="null"
        @cancel="handleDetailCancel"
      >
        <a-descriptions bordered :column="2">
          <a-descriptions-item label="订单编号">
            {{ detailData?.id }}
          </a-descriptions-item>
          <a-descriptions-item label="物品名称">
            {{ detailData?.name }}
          </a-descriptions-item>
          <a-descriptions-item label="数量">
            {{ detailData?.quantity }}
          </a-descriptions-item>
          <a-descriptions-item label="类型">
            {{ detailData?.type }}
          </a-descriptions-item>
          <a-descriptions-item label="订单日期">
            {{ detailData?.orderDate | moment('YYYY-MM-DD') }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ detailData?.createTime | moment('YYYY-MM-DD HH:mm:ss') }}
          </a-descriptions-item>
        </a-descriptions>
      </a-modal>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import moment from 'moment'
import { STable } from '@/components'
import { getOrderList, createOrder, updateOrder, deleteOrder, getOrderDetail } from '@/api/order'

const columns = [
  {
    title: '订单编号',
    dataIndex: 'id'
  },
  {
    title: '物品名称',
    dataIndex: 'name'
  },
  {
    title: '数量',
    dataIndex: 'quantity'
  },
  {
    title: '类型',
    dataIndex: 'type'
  },
  {
    title: '订单日期',
    dataIndex: 'orderDate',
    scopedSlots: { customRender: 'orderDate' }
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '200px',
    scopedSlots: { customRender: 'action' }
  }
]

export default {
  name: 'OrderList',
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
        name: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
        quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
        type: [{ required: true, message: '请选择类型', trigger: 'change' }],
        orderDate: [{ required: true, message: '请选择订单日期', trigger: 'change' }]
      },
      queryParam: {},
      loadData: parameter => {
        const params = Object.assign(parameter, this.queryParam)
        if (params.dateRange) {
          params.startDate = params.dateRange[0].format('YYYY-MM-DD')
          params.endDate = params.dateRange[1].format('YYYY-MM-DD')
          delete params.dateRange
        }
        return getOrderList(params).then(res => {
          return res.result
        })
      }
    }
  },
  methods: {
    getEmptyForm () {
      return {
        id: undefined,
        name: '',
        quantity: 1,
        type: undefined,
        orderDate: moment()
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
        orderDate: moment(record.orderDate)
      }
      this.visible = true
    },
    async handleDetail (record) {
      try {
        const response = await getOrderDetail(record.id)
        this.detailData = response.result
        this.detailVisible = true
      } catch (error) {
        this.$message.error('获取详情失败：' + error.message)
      }
    },
    async handleDelete (record) {
      await deleteOrder(record.id)
      this.$message.success('删除成功')
      this.$refs.table.refresh()
    },
    async handleOk () {
      try {
        await this.$refs.form.validate()
        this.confirmLoading = true

        const formData = {
          ...this.form,
          orderDate: this.form.orderDate.format('YYYY-MM-DD')
        }

        if (this.mdl) {
          await updateOrder(formData)
          this.$message.success('修改成功')
        } else {
          await createOrder(formData)
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
