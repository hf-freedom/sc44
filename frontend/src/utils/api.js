import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.success) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default api

export const ingredientApi = {
  getAll: () => api.get('/ingredients'),
  getById: (id) => api.get(`/ingredients/${id}`),
  create: (data) => api.post('/ingredients', data),
  update: (id, data) => api.put(`/ingredients/${id}`, data),
  delete: (id) => api.delete(`/ingredients/${id}`),
  getLowInventory: () => api.get('/ingredients/low-inventory'),
  getExpired: () => api.get('/ingredients/expired')
}

export const dishApi = {
  getAll: () => api.get('/dishes'),
  getById: (id) => api.get(`/dishes/${id}`),
  create: (data) => api.post('/dishes', data),
  update: (id, data) => api.put(`/dishes/${id}`, data),
  delete: (id) => api.delete(`/dishes/${id}`),
  getCost: (id) => api.get(`/dishes/${id}/cost`),
  canMake: (id, quantity) => api.get(`/dishes/${id}/can-make`, { params: { quantity } }),
  updateAvailability: () => api.post('/dishes/update-availability')
}

export const orderApi = {
  getAll: () => api.get('/orders'),
  getToday: () => api.get('/orders/today'),
  getById: (id) => api.get(`/orders/${id}`),
  create: (data) => api.post('/orders', data),
  pay: (id) => api.post(`/orders/${id}/pay`),
  deliver: (id) => api.post(`/orders/${id}/deliver`),
  cancel: (id) => api.post(`/orders/${id}/cancel`),
  refundItem: (orderId, itemId, data) => api.post(`/orders/${orderId}/items/${itemId}/refund`, data)
}

export const purchaseApi = {
  getAll: () => api.get('/purchases'),
  getById: (id) => api.get(`/purchases/${id}`),
  create: (data) => api.post('/purchases', data),
  receive: (id) => api.post(`/purchases/${id}/receive`),
  getSuggestions: () => api.get('/purchases/suggestions'),
  generateSuggestions: () => api.post('/purchases/suggestions/generate'),
  convertSuggestions: (data) => api.post('/purchases/suggestions/convert', data)
}

export const wasteApi = {
  getAll: () => api.get('/wastes'),
  getToday: () => api.get('/wastes/today'),
  getById: (id) => api.get(`/wastes/${id}`),
  create: (data) => api.post('/wastes', data)
}

export const settlementApi = {
  getAll: () => api.get('/settlements'),
  getToday: () => api.get('/settlements/today'),
  getById: (id) => api.get(`/settlements/${id}`),
  createToday: () => api.post('/settlements/create-today'),
  confirm: (id) => api.post(`/settlements/${id}/confirm`),
  getAdjustments: () => api.get('/settlements/adjustments')
}