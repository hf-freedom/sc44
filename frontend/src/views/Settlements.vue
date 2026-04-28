<template>
  <div class="settlements-page">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="header-row">
              <span>日结记录</span>
              <el-button type="primary" @click="createTodaySettlement">生成今日日结</el-button>
            </div>
          </template>

          <el-table :data="settlements" style="width: 100%" v-loading="loading">
            <el-table-column prop="settlementDate" label="日期" width="120" />
            <el-table-column prop="totalOrders" label="订单数" width="80" />
            <el-table-column prop="totalSales" label="营业额" width="120">
              <template #default="scope">¥{{ scope.row.totalSales }}</template>
            </el-table-column>
            <el-table-column prop="totalRefund" label="退款额" width="100">
              <template #default="scope">
                <span style="color: #f56c6c;">¥{{ scope.row.totalRefund }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="netSales" label="净销售额" width="100">
              <template #default="scope">¥{{ scope.row.netSales }}</template>
            </el-table-column>
            <el-table-column prop="totalIngredientCost" label="食材成本" width="100">
              <template #default="scope">¥{{ scope.row.totalIngredientCost }}</template>
            </el-table-column>
            <el-table-column prop="totalWasteAmount" label="损耗金额" width="100">
              <template #default="scope">
                <span style="color: #f56c6c;">¥{{ scope.row.totalWasteAmount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="grossProfit" label="毛利" width="100">
              <template #default="scope">
                <span :style="{ color: scope.row.grossProfit >= 0 ? '#67C23A' : '#f56c6c' }">
                  ¥{{ scope.row.grossProfit }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="grossProfitRate" label="毛利率" width="100">
              <template #default="scope">
                <span :style="{ color: scope.row.grossProfitRate >= 0 ? '#67C23A' : '#f56c6c' }">
                  {{ scope.row.grossProfitRate }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="confirmed" label="状态" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.confirmed ? 'success' : 'warning'" size="small">
                  {{ scope.row.confirmed ? '已确认' : '待确认' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button 
                  v-if="!scope.row.confirmed" 
                  type="success" 
                  size="small" 
                  link
                  @click="confirmSettlement(scope.row)"
                >
                  确认日结
                </el-button>
                <el-button type="info" size="small" link @click="showDetail(scope.row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="header-row">
              <span>日结调整单</span>
              <el-button type="primary" size="small" @click="loadAdjustments">刷新</el-button>
            </div>
          </template>

          <div v-if="adjustments.length > 0">
            <div v-for="adj in adjustments" :key="adj.id" class="adjustment-item">
              <div class="adjustment-header">
                <span class="adjustment-type">{{ getAdjustmentTypeText(adj.type) }}</span>
                <span class="adjustment-time">{{ adj.createTime }}</span>
              </div>
              <div class="adjustment-info">
                <div>相关订单: {{ adj.relatedOrderNo }}</div>
                <div>调整金额: <span style="color: #f56c6c;">-¥{{ adj.amount }}</span></div>
                <div>原因: {{ adj.reason || '无' }}</div>
                <div v-if="adj.originalSettlementDate">原日结日期: {{ adj.originalSettlementDate }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无调整单" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="detailDialogVisible" title="日结详情" width="700px">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="日期">{{ currentSettlement.settlementDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentSettlement.confirmed ? 'success' : 'warning'">
            {{ currentSettlement.confirmed ? '已确认' : '待确认' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentSettlement.createTime }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">销售统计</el-divider>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="总订单数" :value="currentSettlement.totalOrders" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="取消订单" :value="currentSettlement.cancelledOrders" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="营业额">
            <template #default>
              <span class="el-statistic__number">¥{{ currentSettlement.totalSales }}</span>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="净销售额">
            <template #default>
              <span class="el-statistic__number">¥{{ currentSettlement.netSales }}</span>
            </template>
          </el-statistic>
        </el-col>
      </el-row>

      <el-divider content-position="left">成本与利润</el-divider>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-statistic title="食材成本">
            <template #default>
              <span class="el-statistic__number" style="color: #f56c6c;">¥{{ currentSettlement.totalIngredientCost }}</span>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="8">
          <el-statistic title="损耗金额">
            <template #default>
              <span class="el-statistic__number" style="color: #f56c6c;">¥{{ currentSettlement.totalWasteAmount }}</span>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="8">
          <el-statistic title="毛利">
            <template #default>
              <span class="el-statistic__number" :style="{ color: currentSettlement.grossProfit >= 0 ? '#67C23A' : '#f56c6c' }">
                ¥{{ currentSettlement.grossProfit }}
              </span>
            </template>
          </el-statistic>
        </el-col>
      </el-row>

      <el-divider content-position="left">毛利率</el-divider>
      <el-progress 
        :percentage="currentSettlement.grossProfitRate" 
        :color="currentSettlement.grossProfitRate >= 0 ? '#67C23A' : '#f56c6c'"
        :stroke-width="20"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { settlementApi } from '../utils/api'

const loading = ref(false)
const settlements = ref([])
const adjustments = ref([])
const detailDialogVisible = ref(false)
const currentSettlement = ref({})

const getAdjustmentTypeText = (type) => {
  const texts = {
    'REFUND_AFTER_SETTLEMENT': '日结后退款',
    'OTHER': '其他调整'
  }
  return texts[type] || type
}

const loadSettlements = async () => {
  loading.value = true
  try {
    const res = await settlementApi.getAll()
    settlements.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadAdjustments = async () => {
  try {
    const res = await settlementApi.getAdjustments()
    adjustments.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const createTodaySettlement = async () => {
  try {
    await ElMessageBox.confirm('确定要生成今日日结吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    await settlementApi.createToday()
    ElMessage.success('今日日结已生成')
    loadSettlements()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const confirmSettlement = async (settlement) => {
  try {
    await ElMessageBox.confirm(`确定要确认 ${settlement.settlementDate} 的日结吗？确认后历史订单退款将生成调整单。`, '确认日结', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await settlementApi.confirm(settlement.id)
    ElMessage.success('日结已确认')
    loadSettlements()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const showDetail = (settlement) => {
  currentSettlement.value = JSON.parse(JSON.stringify(settlement))
  detailDialogVisible.value = true
}

onMounted(() => {
  loadSettlements()
  loadAdjustments()
})
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.adjustment-item {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 10px;
}

.adjustment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.adjustment-type {
  font-weight: bold;
  color: #f56c6c;
}

.adjustment-time {
  font-size: 12px;
  color: #999;
}

.adjustment-info {
  font-size: 13px;
  color: #666;
  line-height: 1.8;
}
</style>