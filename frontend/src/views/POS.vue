<template>
  <div class="pos-page">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="header-row">
              <span>菜品列表</span>
              <el-button type="primary" size="small" @click="loadDishes">刷新菜品</el-button>
            </div>
          </template>
          <div class="dish-grid">
            <div 
              v-for="dish in dishes" 
              :key="dish.id" 
              class="dish-card"
              :class="{ 'disabled': dish.status !== 'AVAILABLE' }"
              @click="addDish(dish)"
            >
              <div class="dish-name">{{ dish.name }}</div>
              <div class="dish-price">¥{{ dish.price }}</div>
              <div class="dish-status">
                <el-tag :type="dish.status === 'AVAILABLE' ? 'success' : 'danger'" size="small">
                  {{ getDishStatusText(dish.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>当前订单</span>
          </template>
          
          <div class="order-items" v-if="currentOrder.length > 0">
            <div v-for="(item, index) in currentOrder" :key="index" class="order-item">
              <div class="item-info">
                <span class="item-name">{{ item.dishName }}</span>
                <span class="item-price">¥{{ item.price }}</span>
              </div>
              <div class="item-quantity">
                <el-button size="small" circle @click="decreaseQuantity(index)">-</el-button>
                <span class="quantity">{{ item.quantity }}</span>
                <el-button size="small" circle @click="increaseQuantity(index)">+</el-button>
              </div>
              <div class="item-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
              <el-button type="danger" size="small" link @click="removeItem(index)">删除</el-button>
            </div>
          </div>
          
          <el-empty v-else description="请选择菜品" :image-size="80" />

          <el-divider />

          <div class="order-summary">
            <div class="summary-row">
              <span>合计:</span>
              <span class="total-amount">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
          </div>

          <div style="margin-top: 20px;">
            <el-button 
              type="primary" 
              size="large" 
              style="width: 100%;"
              :disabled="currentOrder.length === 0"
              @click="createOrder"
            >
              下单（¥{{ totalAmount.toFixed(2) }}）
            </el-button>
            <el-button 
              type="danger" 
              style="width: 100%; margin-top: 10px;"
              :disabled="currentOrder.length === 0"
              @click="clearOrder"
            >
              清空订单
            </el-button>
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>待处理订单</span>
            <el-button type="primary" size="small" style="float: right;" @click="loadPendingOrders">刷新</el-button>
          </template>
          <el-table :data="pendingOrders" size="small" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="150" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="small">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="金额" width="80">
              <template #default="scope">¥{{ scope.row.totalAmount }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120">
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
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dishApi, orderApi } from '../utils/api'

const dishes = ref([])
const currentOrder = ref([])
const pendingOrders = ref([])

const totalAmount = computed(() => {
  return currentOrder.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const getDishStatusText = (status) => {
  const texts = {
    'AVAILABLE': '可售',
    'UNAVAILABLE': '不可售',
    'SOLD_OUT': '售罄'
  }
  return texts[status] || status
}

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

const loadDishes = async () => {
  try {
    const res = await dishApi.getAll()
    dishes.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const loadPendingOrders = async () => {
  try {
    const res = await orderApi.getToday()
    pendingOrders.value = res.data.filter(order => 
      order.status === 'PENDING' || order.status === 'PAID'
    )
  } catch (e) {
    console.error(e)
  }
}

const addDish = (dish) => {
  if (dish.status !== 'AVAILABLE') {
    ElMessage.warning('该菜品不可售')
    return
  }

  const existing = currentOrder.value.find(item => item.dishId === dish.id)
  if (existing) {
    existing.quantity++
  } else {
    currentOrder.value.push({
      dishId: dish.id,
      dishName: dish.name,
      price: dish.price,
      quantity: 1
    })
  }
}

const removeItem = (index) => {
  currentOrder.value.splice(index, 1)
}

const increaseQuantity = (index) => {
  currentOrder.value[index].quantity++
}

const decreaseQuantity = (index) => {
  if (currentOrder.value[index].quantity > 1) {
    currentOrder.value[index].quantity--
  } else {
    currentOrder.value.splice(index, 1)
  }
}

const clearOrder = () => {
  currentOrder.value = []
}

const createOrder = async () => {
  try {
    await ElMessageBox.confirm(`确认下单？金额：¥${totalAmount.value.toFixed(2)}`, '确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })

    const orderData = {
      items: currentOrder.value.map(item => ({
        dishId: item.dishId,
        quantity: item.quantity
      })),
      remark: ''
    }

    const res = await orderApi.create(orderData)
    ElMessage.success('订单创建成功')
    currentOrder.value = []
    loadPendingOrders()
    loadDishes()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
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
    loadPendingOrders()
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
    loadPendingOrders()
    loadDishes()
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
    loadPendingOrders()
    loadDishes()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  loadDishes()
  loadPendingOrders()
})
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
}

.dish-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.dish-card:hover:not(.disabled) {
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.dish-card.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.dish-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}

.dish-price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 10px;
}

.order-items {
  max-height: 300px;
  overflow-y: auto;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: bold;
  display: block;
}

.item-price {
  color: #999;
  font-size: 12px;
}

.item-quantity {
  display: flex;
  align-items: center;
  gap: 5px;
  margin: 0 10px;
}

.quantity {
  min-width: 30px;
  text-align: center;
  font-weight: bold;
}

.item-total {
  min-width: 60px;
  text-align: right;
  font-weight: bold;
  color: #f56c6c;
}

.order-summary {
  text-align: right;
}

.summary-row {
  font-size: 18px;
}

.total-amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 24px;
}
</style>