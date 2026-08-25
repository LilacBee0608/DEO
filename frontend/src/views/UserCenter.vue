<script setup>
// 用户主页
// 功能:
//   1) 顶部用户信息卡片(头像 + 用户名)
//   2) 双 Tab 切换: 观看历史 / 收藏夹
//   3) 视频卡片网格展示(复用首页卡片样式)
//   4) 观看历史支持清空
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navbar from '@/components/Navbar.vue'
import { useUserStore } from '@/stores/user'
import { getHistoryList, clearHistory, getFavoriteList } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

// 当前激活的 tab
const activeTab = ref('history')
// 观看历史列表
const historyList = ref([])
// 收藏夹列表
const favoriteList = ref([])
// 加载状态
const loading = ref(false)

// 加载观看历史
const loadHistory = async () => {
  loading.value = true
  try {
    const res = await getHistoryList()
    historyList.value = res.data || []
  } catch (e) {
    historyList.value = []
  } finally {
    loading.value = false
  }
}

// 加载收藏夹
const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getFavoriteList()
    favoriteList.value = res.data || []
  } catch (e) {
    favoriteList.value = []
  } finally {
    loading.value = false
  }
}

// 切换 tab 时加载对应数据
const onTabChange = (tab) => {
  if (tab === 'history' && historyList.value.length === 0) {
    loadHistory()
  } else if (tab === 'favorites' && favoriteList.value.length === 0) {
    loadFavorites()
  }
}

// 清空观看历史
const onClearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有观看历史吗？此操作不可撤销', '清空确认', {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await clearHistory()
    ElMessage.success('观看历史已清空')
    historyList.value = []
  } catch (e) {
    // 用户取消则不做任何操作
  }
}

// 工具方法: 格式化数字
const formatNum = (n) => {
  if (!n) return '0'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return String(n)
}

// 工具方法: 格式化时间
const formatDate = (s) => {
  if (!s) return ''
  return String(s).replace('T', ' ').substring(0, 16)
}

// 初始化: 默认加载观看历史
onMounted(() => {
  loadHistory()
})
</script>

<template>
  <div class="user-center">
    <Navbar />

    <!-- 用户信息卡片 -->
    <div class="user-header">
      <div class="container user-info">
        <el-avatar :size="80" class="user-avatar-lg">
          {{ userStore.userInfo?.userName?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="user-meta">
          <h1 class="user-name-lg">{{ userStore.userInfo?.userName || '未知用户' }}</h1>
          <p class="user-stats">
            <span>{{ historyList.length }} 观看记录</span>
            <span class="divider">|</span>
            <span>{{ favoriteList.length }} 收藏</span>
          </p>
        </div>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="container tab-content">
      <el-tabs v-model="activeTab" @tab-change="onTabChange" v-loading="loading">
        <!-- 观看历史 -->
        <el-tab-pane label="观看历史" name="history">
          <div class="tab-header" v-if="historyList.length > 0">
            <span class="tab-count">共 {{ historyList.length }} 条记录</span>
            <el-button type="danger" text size="small" @click="onClearHistory">
              清空历史
            </el-button>
          </div>
          <div v-if="historyList.length > 0" class="video-grid">
            <div
              v-for="v in historyList"
              :key="v.v_id"
              class="video-card"
              @click="router.push(`/video/${v.v_id}`)"
            >
              <div class="card-cover">
                <img :src="v.cover_url" :alt="v.title" loading="lazy" />
                <span class="watch-time">{{ formatDate(v.watch_time) }}</span>
              </div>
              <div class="card-body">
                <p class="card-title" :title="v.title">{{ v.title }}</p>
                <div class="card-meta">
                  <span class="card-author">{{ v.authorName || '匿名' }}</span>
                  <span class="card-play">
                    <el-icon><VideoPlay /></el-icon>{{ formatNum(v.play_num) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="还没有观看记录" />
        </el-tab-pane>

        <!-- 收藏夹 -->
        <el-tab-pane label="收藏夹" name="favorites">
          <div class="tab-header" v-if="favoriteList.length > 0">
            <span class="tab-count">共 {{ favoriteList.length }} 个收藏</span>
          </div>
          <div v-if="favoriteList.length > 0" class="video-grid">
            <div
              v-for="v in favoriteList"
              :key="v.v_id"
              class="video-card"
              @click="router.push(`/video/${v.v_id}`)"
            >
              <div class="card-cover">
                <img :src="v.cover_url" :alt="v.title" loading="lazy" />
              </div>
              <div class="card-body">
                <p class="card-title" :title="v.title">{{ v.title }}</p>
                <div class="card-meta">
                  <span class="card-author">{{ v.authorName || '匿名' }}</span>
                  <span class="card-play">
                    <el-icon><VideoPlay /></el-icon>{{ formatNum(v.play_num) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="还没有收藏任何视频" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.user-center {
  min-height: 100vh;
  padding-bottom: 40px;
  background: #f4f5f7;
}

/* 用户信息卡片 */
.user-header {
  background: linear-gradient(135deg, #7388ff 0%, #5a6ed6 100%);
  padding: 30px 0;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}
.user-avatar-lg {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
  font-size: 32px;
  font-weight: bold;
  border: 3px solid rgba(255, 255, 255, 0.5);
}
.user-meta {
  color: #fff;
}
.user-name-lg {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px;
}
.user-stats {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}
.user-stats .divider {
  margin: 0 10px;
  opacity: 0.5;
}

/* Tab 内容区 */
.tab-content {
  margin-top: 20px;
}
.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.tab-count {
  font-size: 14px;
  color: #9499a0;
}

/* 视频卡片网格 */
.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.video-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.video-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}
.card-cover {
  position: relative;
  width: 100%;
  padding-top: 56.25%;
  background: #f1f2f3;
  overflow: hidden;
}
.card-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.watch-time {
  position: absolute;
  bottom: 6px;
  right: 6px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 3px;
}
.card-body {
  padding: 10px 12px;
}
.card-title {
  font-size: 14px;
  line-height: 1.4;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: #18191c;
}
.card-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #9499a0;
}
.card-author {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-play {
  display: flex;
  align-items: center;
  gap: 2px;
}

/* 响应式 */
@media (max-width: 768px) {
  .video-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 10px;
  }
}
</style>
