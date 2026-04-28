<template>
  <div class="dashboard">
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409EFF;">
              <span class="stat-icon-text">订</span>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ todayOrders }}</div>
              <div class="stat-label">今日订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67C23A;">
              <span class="stat-icon-text">¥</span>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ todaySales }}</div>
              <div class="stat-label">今日营业额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #E6A23C;">
              <span class="stat-icon-text">!</span>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ lowInventoryCount }}</div>
              <div class="stat-label">库存预警</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #F56C6C;">
              <span class="stat-icon-text">损</span>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ todayWaste }}</div>
              <div class="stat-label">今日损耗</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>今日订单</span>
            <el-button type="primary" size="small" style="float: right;" @click="refreshOrders">刷新</el-button>
          </template>
          <el-table :data="recentOrders" style="width: 100%" size="small">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="totalAmount" label="金额" width="100">
              <template #default="scope">¥{{ scope.row.totalAmount }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>库存预警</span>
          </template>
          <el-table :data="lowInventoryItems" style="width: 100%" size="small">
            <el-table-column prop="name" label="原材料" />
            <el-table-column prop="inventory" label="当前库存" width="100">
              <template #default="scope">{{ scope.row.inventory }} {{ scope.row.unit }}</template>
            </el-table-column>
            <el-table-column prop="safeInventory" label="安全库存" width="100">
              <template #default="scope">{{ scope.row.safeInventory }} {{ scope.row.unit }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default>
                <el-tag type="warning">库存不足</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi, ingredientApi, wasteApi } from '../utils/api'

const todayOrders = ref(0)
const todaySales = ref('0.00')
const lowInventoryCount = ref(0)
const todayWaste = ref('0.00')
const recentOrders = ref([])
const lowInventoryItems = ref([])

const getStatusType = (status) => {
  const types = {
    'PENDING': 'info',
    'PAID': 'primary',
    'DELIVERED': 'success',
    'CANCELLED': 'danger',
    'REFUNDED': 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'DELIVERED': '已出餐',
    'CANCELLED': '已取消',
    'REFUNDED': '已退款'
  }
  return texts[status] || status
}

const loadData = async () => {
  try {
    const ordersRes = await orderApi.getToday()
    recentOrders.value = ordersRes.data.slice(0, 10)
    todayOrders.value = ordersRes.data.length
    todaySales.value = ordersRes.data.reduce((sum, order) => {
      return sum + (order.paidAmount || 0)
    }, 0).toFixed(2)
  } catch (e) {
    console.error(e)
  }

  try {
    const inventoryRes = await ingredientApi.getLowInventory()
    lowInventoryItems.value = inventoryRes.data
    lowInventoryCount.value = inventoryRes.data.length
  } catch (e) {
    console.error(e)
  }

  try {
    const wasteRes = await wasteApi.getToday()
    todayWaste.value = wasteRes.data.reduce((sum, order) => {
      return sum + (order.totalAmount || 0)
    }, 0).toFixed(2)
  } catch (e) {
    console.error(e)
  }
}

const refreshOrders = () => {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  font-weight: bold;
}

.stat-icon-text {
  font-size: 24px;
  font-weight: bold;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}
</style>