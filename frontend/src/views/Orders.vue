<template>
  <div class="orders-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>订单管理</span>
          <el-button type="primary" @click="loadOrders">刷新</el-button>
        </div>
      </template>

      <el-table :data="orders" style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="scope">¥{{ scope.row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="paidAmount" label="已支付" width="100">
          <template #default="scope">¥{{ scope.row.paidAmount }}</template>
        </el-table-column>
        <el-table-column prop="refundAmount" label="已退款" width="100">
          <template #default="scope">
            <span v-if="scope.row.refundAmount > 0" style="color: #f56c6c;">
              ¥{{ scope.row.refundAmount }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否已日结" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.settled ? 'info' : 'warning'" size="small">
              {{ scope.row.settled ? '已日结' : '未日结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'PENDING'" 
              type="primary" 
              size="small" 
              link
              @click="payOrder(scope.row)"
            >
              支付
            </el-button>
            <el-button 
              v-if="scope.row.status === 'PAID'" 
              type="success" 
              size="small" 
              link
              @click="deliverOrder(scope.row)"
            >
              出餐
            </el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING' || scope.row.status === 'PAID'" 
              type="danger" 
              size="small" 
              link
              @click="cancelOrder(scope.row)"
            >
              取消
            </el-button>
            <el-button 
              v-if="scope.row.status === 'DELIVERED' && hasUnrefundedItems(scope.row)" 
              type="warning" 
              size="small" 
              link
              @click="showRefundDialog(scope.row)"
            >
              退款
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              link
              @click="showDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="已支付">¥{{ currentOrder.paidAmount }}</el-descriptions-item>
        <el-descriptions-item label="已退款">¥{{ currentOrder.refundAmount }}</el-descriptions-item>
        <el-descriptions-item label="是否已日结">
          <el-tag :type="currentOrder.settled ? 'info' : 'warning'">
            {{ currentOrder.settled ? '已日结' : '未日结' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">订单项</el-divider>
      <el-table :data="currentOrder.items" size="small">
        <el-table-column prop="dishName" label="菜品" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="amount" label="小计" width="100">
          <template #default="scope">¥{{ scope.row.amount }}</template>
        </el-table-column>
        <el-table-column label="退款状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.refunded" type="danger" size="small">已全部退款</el-tag>
            <el-tag v-else-if="scope.row.refundedQuantity > 0" type="warning" size="small">
              已退{{ scope.row.refundedQuantity }}份
            </el-tag>
            <el-tag v-else type="success" size="small">未退款</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="refundDialogVisible" title="订单退款" width="500px">
      <el-form label-width="100px">
        <el-form-item label="订单号">
          <span>{{ refundOrder.orderNo }}</span>
        </el-form-item>
        <el-form-item label="选择订单项">
          <el-select v-model="refundItemId" placeholder="请选择要退款的订单项" style="width: 100%;">
            <el-option
              v-for="item in refundOrder.items"
              :key="item.id"
              :label="`${item.dishName} (剩余${item.quantity - item.refundedQuantity}份)`"
              :value="item.id"
              :disabled="item.refunded"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="退款数量" v-if="refundItemId">
          <el-input-number v-model="refundQuantity" :min="1" :max="maxRefundQuantity" />
        </el-form-item>
        <el-form-item label="退款原因">
          <el-input v-model="refundReason" type="textarea" placeholder="请输入退款原因" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '../utils/api'

const loading = ref(false)
const orders = ref([])
const detailDialogVisible = ref(false)
const refundDialogVisible = ref(false)
const currentOrder = ref({})
const refundOrder = ref({})
const refundItemId = ref('')
const refundQuantity = ref(1)
const refundReason = ref('')

const maxRefundQuantity = computed(() => {
  if (!refundItemId.value || !refundOrder.value.items) return 1
  const item = refundOrder.value.items.find(i => i.id === refundItemId.value)
  if (!item) return 1
  return item.quantity - item.refundedQuantity
})

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

const hasUnrefundedItems = (order) => {
  if (!order.items) return false
  return order.items.some(item => !item.refunded && item.refundedQuantity < item.quantity)
}

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await orderApi.getAll()
    orders.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const payOrder = async (order) => {
  try {
    await ElMessageBox.confirm(`确认支付订单 ${order.orderNo}？金额：¥${order.totalAmount}`, '支付确认', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'info'
    })

    await orderApi.pay(order.id)
    ElMessage.success('支付成功')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const deliverOrder = async (order) => {
  try {
    await ElMessageBox.confirm(`确认出餐？订单号：${order.orderNo}`, '出餐确认', {
      confirmButtonText: '确认出餐',
      cancelButtonText: '取消',
      type: 'success'
    })

    await orderApi.deliver(order.id)
    ElMessage.success('出餐成功')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm(`确认取消订单 ${order.orderNo}？`, '取消确认', {
      confirmButtonText: '确认取消',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await orderApi.cancel(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const showDetail = (order) => {
  currentOrder.value = JSON.parse(JSON.stringify(order))
  detailDialogVisible.value = true
}

const showRefundDialog = (order) => {
  refundOrder.value = JSON.parse(JSON.stringify(order))
  refundItemId.value = ''
  refundQuantity.value = 1
  refundReason.value = ''
  refundDialogVisible.value = true
}

const submitRefund = async () => {
  if (!refundItemId.value) {
    ElMessage.warning('请选择要退款的订单项')
    return
  }

  try {
    const res = await orderApi.refundItem(refundOrder.value.id, refundItemId.value, {
      quantity: refundQuantity.value,
      reason: refundReason.value
    })

    if (res.data.refunded) {
      ElMessage.success('退款成功')
    } else {
      ElMessage.warning(res.data.message || '订单已日结，已生成调整单')
    }

    refundDialogVisible.value = false
    loadOrders()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>