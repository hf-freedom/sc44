<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background-color: #2f3541;">
      <div class="logo">
        <h2 style="color: #fff; margin: 0; padding: 20px; text-align: center;">餐饮管理系统</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#2f3541"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/dashboard">
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/pos">
          <span>前台售卖</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/ingredients">
          <span>原材料管理</span>
        </el-menu-item>
        <el-menu-item index="/dishes">
          <span>菜品管理</span>
        </el-menu-item>
        <el-menu-item index="/purchases">
          <span>采购管理</span>
        </el-menu-item>
        <el-menu-item index="/wastes">
          <span>损耗管理</span>
        </el-menu-item>
        <el-menu-item index="/settlements">
          <span>日结管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="text-align: right; font-size: 12px; border-bottom: 1px solid #ddd; background: #fff;">
        <span style="cursor: pointer;">管理员</span>
      </el-header>
      <el-main style="background-color: #f5f7fa;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
}

.el-menu {
  border-right: none;
}

.el-main {
  padding: 20px;
}
</style>