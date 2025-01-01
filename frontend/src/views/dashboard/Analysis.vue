<template>
  <div>
    <p class="welcome-text">欢迎进入智能机器人工程数据管理系统！</p>
    <div class="antd-pro-pages-dashboard-analysis-twoColLayout" :class="!isMobile && 'desktop'">
      <a-row :gutter="24" type="flex" :style="{ marginTop: '24px' }">
        <a-col :xl="12" :lg="24" :md="24" :sm="24" :xs="24">
          <a-card class="antd-pro-pages-dashboard-analysis-salesCard" :loading="loading" :bordered="false" :title="$t('dashboard.analysis.the-order')" :style="{ height: '100%' }">
            <div slot="extra" style="height: inherit;">
              <span class="dashboard-analysis-iconGroup">
                <a-dropdown :trigger="['click']" placement="bottomLeft">
                  <a-icon type="ellipsis" class="ant-dropdown-link" />
                </a-dropdown>
              </span>
            </div>
            <h4>{{ $t('dashboard.analysis.order') }}</h4>
            <div>
              <div>
                <v-chart :force-fit="true" :height="405" :data="pieData" :scale="pieScale">
                  <v-tooltip :showTitle="false" dataKey="item*percent" />
                  <v-axis />
                  <v-legend dataKey="item"/>
                  <v-pie position="percent" color="item" :vStyle="pieStyle" />
                  <v-coord type="theta" :radius="0.75" :innerRadius="0.6" />
                </v-chart>
              </div>
            </div>
          </a-card>
        </a-col>

        <a-col :xl="12" :lg="24" :md="24" :sm="24" :xs="24">
          <a-card class="antd-pro-pages-dashboard-analysis-salesCard" :loading="loading" :bordered="false" :title="$t('dashboard.analysis.the-component')" :style="{ height: '100%' }">
            <div slot="extra" style="height: inherit;">
              <span class="dashboard-analysis-iconGroup">
                <a-dropdown :trigger="['click']" placement="bottomLeft">
                  <a-icon type="ellipsis" class="ant-dropdown-link" />
                </a-dropdown>
              </span>
            </div>
            <h4>{{ $t('dashboard.analysis.component') }}</h4>
            <div>
              <div>
                <v-chart :force-fit="true" :height="405" :data="partsData" :scale="pieScale">
                  <v-tooltip :showTitle="false" dataKey="item*percent" />
                  <v-axis />
                  <v-legend dataKey="item"/>
                  <v-pie position="percent" color="item" :vStyle="pieStyle" />
                  <v-coord type="theta" :radius="0.75" :innerRadius="0.6" />
                </v-chart>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script>
import { ChartCard, Bar, Trend } from '@/components'
import { baseMixin } from '@/store/app-mixin'

const pieScale = [{
  dataKey: 'percent',
  min: 0,
  formatter: '.0%'
}]

// 模拟数据
const pieData = [
  { item: '设计', count: Math.floor(Math.random() * 1000) + 200 },
  { item: '制造', count: Math.floor(Math.random() * 1000) + 200 },
  { item: '采购', count: Math.floor(Math.random() * 1000) + 200 }
]
// 部件数据
const partsData = [
  { item: '执行器零件', count: Math.floor(Math.random() * 1000) + 200 },
  { item: '控制零件', count: Math.floor(Math.random() * 1000) + 200 },
  { item: '驱动零件', count: Math.floor(Math.random() * 1000) + 200 },
  { item: '电器零件', count: Math.floor(Math.random() * 1000) + 200 },
  { item: '传感器零件', count: Math.floor(Math.random() * 1000) + 200 },
  { item: '其他辅助零件', count: Math.floor(Math.random() * 1000) + 200 }
]
// 数据转换为百分比形式
const DataSet = require('@antv/data-set')
const dv = new DataSet.View().source(pieData)
dv.transform({
  type: 'percent',
  field: 'count',
  dimension: 'item',
  as: 'percent'
})
const updatedPieData = dv.rows

const dvv = new DataSet.View().source(partsData)
dvv.transform({
  type: 'percent',
  field: 'count',
  dimension: 'item',
  as: 'percent'
})
const updatedPartsData = dvv.rows

export default {
  name: 'Analysis',
  mixins: [baseMixin],
  components: { ChartCard, Bar, Trend },
  data () {
    return {
      loading: true,
      pieData: updatedPieData,
      partsData: updatedPartsData,
      pieScale,
      pieStyle: {
        stroke: '#fff',
        lineWidth: 1
      }
    }
  },
  created () {
    setTimeout(() => {
      this.loading = !this.loading
    }, 1000)
  }
}
</script>

<style lang="less" scoped>
.welcome-text {
  font-size: 24px; /* 字体大小 */
  margin: 0;
}
</style>
