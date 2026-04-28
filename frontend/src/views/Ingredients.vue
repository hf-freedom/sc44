<template>
  <div class="ingredients-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>原材料库存</span>
          <el-button type="primary" @click="handleAdd">新增原材料</el-button>
        </div>
      </template>

      <el-table :data="ingredients" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="inventory" label="当前库存" width="120">
          <template #default="scope">
            {{ scope.row.inventory }}
            <span v-if="scope.row.lockedInventory > 0" style="color: #999; font-size: 12px;">
              (锁定: {{ scope.row.lockedInventory }})
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="safeInventory" label="安全库存" width="100" />
        <el-table-column prop="unitPrice" label="单价" width="100">
          <template #default="scope">¥{{ scope.row.unitPrice }}</template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="保质期" width="120" />
        <el-table-column label="库存状态" width="120">
          <template #default="scope">
            <el-tag v-if="isExpired(scope.row)" type="danger">已过期</el-tag>
            <el-tag v-else-if="isLowInventory(scope.row)" type="warning">库存不足</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入原材料名称" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入单位，如：份、克、个" />
        </el-form-item>
        <el-form-item label="当前库存" prop="inventory">
          <el-input-number v-model="form.inventory" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="安全库存" prop="safeInventory">
          <el-input-number v-model="form.safeInventory" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="保质期" prop="expiryDate">
          <el-date-picker
            v-model="form.expiryDate"
            type="date"
            placeholder="选择保质期"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ingredientApi } from '../utils/api'

const loading = ref(false)
const ingredients = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增原材料')
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  unit: '',
  inventory: 0,
  safeInventory: 0,
  unitPrice: 0,
  expiryDate: null
})

const rules = {
  name: [{ required: true, message: '请输入原材料名称', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }]
}

const isExpired = (row) => {
  if (!row.expiryDate) return false
  const today = new Date().toISOString().split('T')[0]
  return row.expiryDate < today
}

const isLowInventory = (row) => {
  return row.inventory < row.safeInventory
}

const loadIngredients = async () => {
  loading.value = true
  try {
    const res = await ingredientApi.getAll()
    ingredients.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增原材料'
  form.id = null
  form.name = ''
  form.unit = ''
  form.inventory = 0
  form.safeInventory = 0
  form.unitPrice = 0
  form.expiryDate = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑原材料'
  form.id = row.id
  form.name = row.name
  form.unit = row.unit
  form.inventory = row.inventory
  form.safeInventory = row.safeInventory
  form.unitPrice = row.unitPrice
  form.expiryDate = row.expiryDate
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除原材料「${row.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await ingredientApi.delete(row.id)
    ElMessage.success('删除成功')
    loadIngredients()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (isEdit.value) {
      await ingredientApi.update(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await ingredientApi.create(form)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadIngredients()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadIngredients()
})
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>