<template>
  <div class="purchases-page">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="header-row">
              <span>采购单列表</span>
              <el-button type="primary" @click="handleAdd">新增采购单</el-button>
            </div>
          </template>

          <el-table :data="purchaseOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNo" label="采购单号" width="180" />
            <el-table-column prop="totalAmount" label="采购金额" width="120">
              <template #default="scope">¥{{ scope.row.totalAmount }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'PENDING' ? 'warning' : 'success'" size="small">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column prop="receiveTime" label="到货时间" width="180">
              <template #default="scope">
                <span v-if="scope.row.receiveTime">{{ scope.row.receiveTime }}</span>
                <span v-else style="color: #999;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button 
                  v-if="scope.row.status === 'PENDING'" 
                  type="success" 
                  size="small" 
                  link
                  @click="receiveOrder(scope.row)"
                >
                  确认到货
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
              <span>采购建议</span>
              <div>
                <el-button type="warning" size="small" @click="generateSuggestions" style="margin-right: 10px;">生成采购建议</el-button>
                <el-button type="primary" size="small" @click="loadSuggestions">刷新</el-button>
              </div>
            </div>
          </template>

          <el-checkbox-group v-model="selectedSuggestions" v-if="suggestions.length > 0">
            <div v-for="sug in suggestions" :key="sug.id" class="suggestion-item">
              <el-checkbox :value="sug.id" :disabled="sug.converted">
                <div class="suggestion-content">
                  <div class="suggestion-name">{{ sug.ingredientName }}</div>
                  <div class="suggestion-info">
                    当前库存: {{ sug.currentInventory }} {{ sug.unit }} | 
                    安全库存: {{ sug.safeInventory }} {{ sug.unit }} | 
                    建议采购: {{ sug.suggestedQuantity }} {{ sug.unit }}
                  </div>
                  <el-tag v-if="sug.converted" type="info" size="small">已转为采购单</el-tag>
                </div>
              </el-checkbox>
            </div>
          </el-checkbox-group>
          <el-empty v-else description="暂无采购建议" :image-size="60" />

          <div style="margin-top: 20px;">
            <el-button 
              type="primary" 
              style="width: 100%;"
              :disabled="selectedSuggestions.length === 0"
              @click="convertSuggestions"
            >
              生成采购单 (已选{{ selectedSuggestions.length }}项)
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" :rows="2" />
        </el-form-item>
        <el-form-item label="采购明细">
          <div style="margin-bottom: 10px;">
            <el-button type="primary" size="small" @click="addItem">添加明细</el-button>
          </div>
          <div v-for="(item, index) in form.items" :key="index" class="form-item-row">
            <el-select v-model="item.ingredientId" placeholder="选择原材料" style="width: 150px;" @change="(val) => onIngredientSelect(val, index)">
              <el-option v-for="ing in ingredients" :key="ing.id" :label="ing.name" :value="ing.id" />
            </el-select>
            <el-input-number v-model="item.quantity" :min="0.01" :precision="2" placeholder="数量" style="width: 100px;" />
            <span>{{ item.unit }}</span>
            <el-input-number v-model="item.unitPrice" :min="0" :precision="2" placeholder="单价" style="width: 100px;" />
            <el-button type="danger" size="small" link @click="removeItem(index)">删除</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="采购单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="采购单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentOrder.status === 'PENDING' ? 'warning' : 'success'">
            {{ getStatusText(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="到货时间" :span="2">
          <span v-if="currentOrder.receiveTime">{{ currentOrder.receiveTime }}</span>
          <span v-else style="color: #999;">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">采购明细</el-divider>
      <el-table :data="currentOrder.items" size="small">
        <el-table-column prop="ingredientName" label="原材料" />
        <el-table-column prop="quantity" label="数量" width="100">
          <template #default="scope">{{ scope.row.quantity }} {{ scope.row.unit }}</template>
        </el-table-column>
        <el-table-column prop="unitPrice" label="单价" width="100">
          <template #default="scope">¥{{ scope.row.unitPrice }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="小计" width="100">
          <template #default="scope">¥{{ scope.row.amount }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { purchaseApi, ingredientApi } from '../utils/api'

const loading = ref(false)
const purchaseOrders = ref([])
const suggestions = ref([])
const ingredients = ref([])
const selectedSuggestions = ref([])
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('新增采购单')
const formRef = ref(null)
const currentOrder = ref({})

const form = reactive({
  remark: '',
  items: []
})

const getStatusText = (status) => {
  return status === 'PENDING' ? '待到货' : '已到货'
}

const loadPurchaseOrders = async () => {
  loading.value = true
  try {
    const res = await purchaseApi.getAll()
    purchaseOrders.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadSuggestions = async () => {
  try {
    const res = await purchaseApi.getSuggestions()
    suggestions.value = res.data
    selectedSuggestions.value = []
  } catch (e) {
    console.error(e)
  }
}

const generateSuggestions = async () => {
  try {
    const res = await purchaseApi.generateSuggestions()
    if (res.data && res.data.length > 0) {
      ElMessage.success(`生成了 ${res.data.length} 条采购建议`)
    } else {
      ElMessage.info('所有原材料库存充足，无需生成采购建议')
    }
    loadSuggestions()
  } catch (e) {
    console.error(e)
  }
}

const loadIngredients = async () => {
  try {
    const res = await ingredientApi.getAll()
    ingredients.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增采购单'
  form.remark = ''
  form.items = []
  dialogVisible.value = true
}

const addItem = () => {
  form.items.push({
    ingredientId: '',
    ingredientName: '',
    quantity: 0,
    unitPrice: 0,
    unit: ''
  })
}

const removeItem = (index) => {
  form.items.splice(index, 1)
}

const onIngredientSelect = (id, index) => {
  const ing = ingredients.value.find(i => i.id === id)
  if (ing) {
    form.items[index].ingredientName = ing.name
    form.items[index].unit = ing.unit
    if (ing.unitPrice) {
      form.items[index].unitPrice = ing.unitPrice
    }
  }
}

const handleSubmit = async () => {
  try {
    const submitData = {
      items: form.items.filter(i => i.ingredientId && i.quantity > 0),
      remark: form.remark
    }
    
    await purchaseApi.create(submitData)
    ElMessage.success('采购单创建成功')
    dialogVisible.value = false
    loadPurchaseOrders()
  } catch (e) {
    console.error(e)
  }
}

const receiveOrder = async (order) => {
  try {
    await ElMessageBox.confirm(`确认采购单 ${order.orderNo} 已到货？`, '确认到货', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    await purchaseApi.receive(order.id)
    ElMessage.success('确认到货成功，库存已更新')
    loadPurchaseOrders()
    loadSuggestions()
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

const convertSuggestions = async () => {
  if (selectedSuggestions.value.length === 0) {
    ElMessage.warning('请选择要转换的采购建议')
    return
  }

  try {
    await ElMessageBox.confirm(`确认将选中的 ${selectedSuggestions.value.length} 项采购建议转为采购单？`, '确认转换', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await purchaseApi.convertSuggestions({ suggestionIds: selectedSuggestions.value })
    ElMessage.success('采购单生成成功')
    loadPurchaseOrders()
    loadSuggestions()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  loadPurchaseOrders()
  loadSuggestions()
  loadIngredients()
})
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.suggestion-item {
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 10px;
}

.suggestion-content {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.suggestion-name {
  font-weight: bold;
}

.suggestion-info {
  font-size: 12px;
  color: #666;
}

.form-item-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
</style>