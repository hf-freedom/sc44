<template>
  <div class="dishes-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>菜品管理</span>
          <el-button type="primary" @click="handleAdd">新增菜品</el-button>
        </div>
      </template>

      <el-table :data="dishes" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="菜品名称" />
        <el-table-column prop="price" label="售价" width="120">
          <template #default="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'AVAILABLE' ? 'success' : 'danger'">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="配方" min-width="300">
          <template #default="scope">
            <div v-if="scope.row.recipe && scope.row.recipe.length > 0">
              <div v-for="(item, index) in scope.row.recipe" :key="index" style="margin-bottom: 5px;">
                {{ item.ingredientName }}: {{ item.quantity }} {{ getIngredientUnit(item.ingredientId) }}
              </div>
            </div>
            <span v-else style="color: #999;">暂无配方</span>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="配方">
          <div style="margin-bottom: 10px;">
            <el-button type="primary" size="small" @click="addRecipeItem">添加配方项</el-button>
          </div>
          <div v-for="(item, index) in form.recipe" :key="index" style="display: flex; gap: 10px; margin-bottom: 10px; align-items: center;">
            <el-select v-model="item.ingredientId" placeholder="选择原材料" style="width: 200px;" @change="(val) => onIngredientChange(val, index)">
              <el-option v-for="ing in ingredients" :key="ing.id" :label="ing.name" :value="ing.id" />
            </el-select>
            <el-input-number v-model="item.quantity" :min="0.01" :precision="3" placeholder="数量" style="width: 120px;" />
            <span style="color: #999;">{{ item.unit || '' }}</span>
            <el-button type="danger" size="small" link @click="removeRecipeItem(index)">删除</el-button>
          </div>
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
import { dishApi, ingredientApi } from '../utils/api'

const loading = ref(false)
const dishes = ref([])
const ingredients = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜品')
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  price: 0,
  recipe: []
})

const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

const getStatusText = (status) => {
  const texts = {
    'AVAILABLE': '可售',
    'UNAVAILABLE': '不可售',
    'SOLD_OUT': '售罄'
  }
  return texts[status] || status
}

const getIngredientUnit = (ingredientId) => {
  const ing = ingredients.value.find(i => i.id === ingredientId)
  return ing ? ing.unit : ''
}

const loadDishes = async () => {
  loading.value = true
  try {
    const res = await dishApi.getAll()
    dishes.value = res.data
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
  isEdit.value = false
  dialogTitle.value = '新增菜品'
  form.id = null
  form.name = ''
  form.price = 0
  form.recipe = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑菜品'
  form.id = row.id
  form.name = row.name
  form.price = row.price
  form.recipe = row.recipe ? JSON.parse(JSON.stringify(row.recipe)) : []
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除菜品「${row.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await dishApi.delete(row.id)
    ElMessage.success('删除成功')
    loadDishes()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const addRecipeItem = () => {
  form.recipe.push({
    ingredientId: '',
    ingredientName: '',
    quantity: 0,
    unit: ''
  })
}

const removeRecipeItem = (index) => {
  form.recipe.splice(index, 1)
}

const onIngredientChange = (id, index) => {
  const ing = ingredients.value.find(i => i.id === id)
  if (ing) {
    form.recipe[index].ingredientName = ing.name
    form.recipe[index].unit = ing.unit
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    const submitData = {
      name: form.name,
      price: form.price,
      recipe: form.recipe.filter(r => r.ingredientId && r.quantity > 0)
    }
    
    if (isEdit.value) {
      await dishApi.update(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await dishApi.create(submitData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadDishes()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadDishes()
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