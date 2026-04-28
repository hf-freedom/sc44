<template>
  <div class="wastes-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>损耗记录</span>
          <el-button type="primary" @click="handleAdd">新增损耗单</el-button>
        </div>
      </template>

      <el-table :data="wasteOrders" style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="损耗单号" width="180" />
        <el-table-column prop="reason" label="损耗原因" width="120">
          <template #default="scope">
            <el-tag :type="getReasonType(scope.row.reason)" size="small">
              {{ getReasonText(scope.row.reason) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="损耗金额" width="120">
          <template #default="scope">¥{{ scope.row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200">
          <template #default="scope">{{ scope.row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="info" size="small" link @click="showDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增损耗单" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="损耗原因" prop="reason">
          <el-select v-model="form.reason" placeholder="请选择损耗原因" style="width: 100%;">
            <el-option label="过期" value="EXPIRED" />
            <el-option label="损坏" value="DAMAGED" />
            <el-option label="操作失误" value="MISTAKE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="损耗明细">
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
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="损耗单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="损耗单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="损耗原因">
          <el-tag :type="getReasonType(currentOrder.reason)">{{ getReasonText(currentOrder.reason) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="损耗金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">损耗明细</el-divider>
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
import { wasteApi, ingredientApi } from '../utils/api'

const loading = ref(false)
const wasteOrders = ref([])
const ingredients = ref([])
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const formRef = ref(null)
const currentOrder = ref({})

const form = reactive({
  reason: '',
  remark: '',
  items: []
})

const rules = {
  reason: [{ required: true, message: '请选择损耗原因', trigger: 'change' }]
}

const getReasonType = (reason) => {
  const types = {
    'EXPIRED': 'danger',
    'DAMAGED': 'warning',
    'MISTAKE': 'info',
    'OTHER': ''
  }
  return types[reason] || ''
}

const getReasonText = (reason) => {
  const texts = {
    'EXPIRED': '过期',
    'DAMAGED': '损坏',
    'MISTAKE': '操作失误',
    'OTHER': '其他'
  }
  return texts[reason] || reason
}

const loadWasteOrders = async () => {
  loading.value = true
  try {
    const res = await wasteApi.getAll()
    wasteOrders.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
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
  form.reason = ''
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
    await formRef.value.validate()
    
    if (form.items.length === 0) {
      ElMessage.warning('请添加损耗明细')
      return
    }
    
    const submitData = {
      reason: form.reason,
      items: form.items.filter(i => i.ingredientId && i.quantity > 0),
      remark: form.remark
    }
    
    await wasteApi.create(submitData)
    ElMessage.success('损耗单创建成功')
    dialogVisible.value = false
    loadWasteOrders()
  } catch (e) {
    console.error(e)
  }
}

const showDetail = (order) => {
  currentOrder.value = JSON.parse(JSON.stringify(order))
  detailDialogVisible.value = true
}

onMounted(() => {
  loadWasteOrders()
  loadIngredients()
})
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-item-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
</style>