<script setup>
// 视频详情页
// 功能:
//   1) DanmuPlayer 播放视频(弹幕层)
//   2) 视频信息: 标题、UP主、播放量、点赞、收藏按钮
//   3) 简介
//   4) 评论区: 列表 / 发送 / 点赞
//   5) 进入页面自增播放量(仅首次进入)
//   6) 右侧侧边栏: 视频选集(分P,仅多P时显示) + 推荐列表(首页随机,排除当前视频)
import { ref, onMounted, onBeforeUnmount, computed, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Navbar from '@/components/Navbar.vue'
import DanmuPlayer from '@/components/DanmuPlayer.vue'
import { useUserStore } from '@/stores/user'
import {
  getVideoDetail,
  getVideoList,
  incrPlay,
  toggleLike,
  toggleFavorite
} from '@/api/video'
import { getDanmuList, sendDanmu } from '@/api/danmu'
import { getCommentList, sendComment, likeComment } from '@/api/comment'
// 观看历史接口(登录用户观看视频时自动记录,后端去重)
import { recordHistory } from '@/api/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 视频详情
const video = ref(null)
// 弹幕列表
const danmuList = ref([])
// 评论列表
const commentList = ref([])
// 加载状态
const loading = ref(false)
// 已自增播放量(避免重复)
let playIncrd = false

// ============= 右侧侧边栏: 选集(分P) + 推荐 =============
// 全部首页视频(用于推荐)
const allVideos = ref([])
// 推荐区域可显示的条数(随详情页高度动态变化)
const recommendCount = ref(5)
// 推荐容器 DOM 引用(用于 ResizeObserver 测量高度)
const recommendBoxRef = ref(null)
// 每条推荐项的预估高度(px): 封面68 + 上下padding16
const RECOMMEND_ITEM_HEIGHT = 84
let resizeObserver = null

// 当前播放的分P序号(从0开始)
const currentPartIndex = ref(0)

// 选集列表: 当前视频的分P
// 从 video.videoUrl 按换行符 \n 拆分得到分P数组
// 单P视频(无换行)→ 数组长度1 → 不显示选集列表
// 多P视频(有换行)→ 数组长度>1 → 显示选集列表
const partList = computed(() => {
  if (!video.value || !video.value.videoUrl) return []
  return video.value.videoUrl
    .split('\n')
    .map(url => url.trim())
    .filter(url => url.length > 0)
})

// 是否显示选集列表(仅多P时显示)
const showSelection = computed(() => partList.value.length > 1)

// 当前播放的分P的URL
const currentPartUrl = computed(() => {
  if (partList.value.length === 0) return ''
  return partList.value[currentPartIndex.value] || ''
})

// 推荐候选池: 首页视频随机打乱,仅排除当前视频(不会包含本视频的分P)
// 不循环补齐: 候选不足时推荐显示到最多为止,不出现重复视频
const recommendPool = computed(() => {
  if (!video.value) return []
  const pool = allVideos.value.filter(v => v.vId !== route.params.vId)
  // Fisher-Yates 随机洗牌
  for (let i = pool.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[pool[i], pool[j]] = [pool[j], pool[i]]
  }
  return pool
})

// 切换分P: 点击选集项时调用,更新播放器src
const switchPart = (index) => {
  if (index === currentPartIndex.value) return
  currentPartIndex.value = index
}

// 实际展示的推荐列表(数量随详情页高度变化)
const recommendList = computed(() =>
  recommendPool.value.slice(0, recommendCount.value)
)

// 加载首页视频(用于选集与推荐)
const loadAllVideos = async () => {
  try {
    const res = await getVideoList({ page: 1, size: 100 })
    allVideos.value = res.data.records || []
  } catch (e) {}
}

// 根据推荐容器可用高度计算可显示条数
const updateRecommendCount = () => {
  const box = recommendBoxRef.value
  if (!box) return
  const h = box.clientHeight
  if (h > 0) {
    recommendCount.value = Math.max(1, Math.floor(h / RECOMMEND_ITEM_HEIGHT))
  }
}

// 初始化 ResizeObserver,监听推荐容器高度变化(详情页变长则多显示)
const initResizeObserver = async () => {
  await nextTick()
  if (recommendBoxRef.value) {
    resizeObserver = new ResizeObserver(() => updateRecommendCount())
    resizeObserver.observe(recommendBoxRef.value)
    updateRecommendCount()
  }
}

// ============= 加载视频详情 =============
const loadVideo = async () => {
  loading.value = true
  try {
    const res = await getVideoDetail(route.params.vId)
    video.value = res.data
    // 进入页面 +1 播放量(只调一次)
    if (!playIncrd) {
      playIncrd = true
      try { await incrPlay(route.params.vId) } catch (e) {}
    }
    // 记录观看历史(仅登录用户,后端利用 UNIQUE(user_id,v_id) 去重,重复观看只更新时间)
    if (userStore.isLoggedIn) {
      try { await recordHistory(route.params.vId) } catch (e) {}
    }
  } finally {
    loading.value = false
  }
}

// ============= 加载弹幕 =============
const loadDanmu = async () => {
  const res = await getDanmuList(route.params.vId)
  danmuList.value = res.data || []
}

// ============= 加载评论 =============
const loadComments = async () => {
  const res = await getCommentList(route.params.vId)
  commentList.value = res.data || []
}

// ============= 点赞 / 收藏 =============
const onLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    const res = await toggleLike(route.params.vId)
    video.value.liked = res.data.liked
    // 本地同步数字
    video.value.likeNum += res.data.liked ? 1 : -1
  } catch (e) {}
}

const onFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    const res = await toggleFavorite(route.params.vId)
    video.value.favorited = res.data.favorited
  } catch (e) {}
}

// ============= 弹幕发送 =============
const onSendDanmu = async (payload) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后发送弹幕')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await sendDanmu({
      vId: route.params.vId,
      ...payload
    })
    // 同步加入弹幕列表(避免重新请求)
    danmuList.value.push({
      did: 'temp-' + Date.now(),
      danmuContent: payload.danmuContent,
      danmuFrame: payload.danmuFrame,
      color: payload.color
    })
    ElMessage.success('弹幕已发送')
  } catch (e) {}
}

// ============= 评论 =============
const commentInput = ref('')
const sending = ref(false)

const onSendComment = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后评论')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const text = commentInput.value.trim()
  if (!text) return
  sending.value = true
  try {
    await sendComment({
      vId: route.params.vId,
      commentContent: text
    })
    ElMessage.success('评论成功')
    commentInput.value = ''
    await loadComments()
  } finally {
    sending.value = false
  }
}

// 评论点赞
const onCommentLike = async (c) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await likeComment(c.cid)
    c.likeNum += 1
  } catch (e) {}
}

// ============= 工具方法 =============
const formatNum = (n) => {
  if (!n) return '0'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return String(n)
}

const formatDate = (s) => {
  if (!s) return ''
  return String(s).replace('T', ' ').substring(0, 16)
}

// 初始化
onMounted(async () => {
  await loadVideo()
  await Promise.all([loadDanmu(), loadComments(), loadAllVideos()])
  await initResizeObserver()
})

// 监听路由参数变化(从推荐/选集跳转其他视频): 重置分P序号并重新加载
watch(() => route.params.vId, async (newId, oldId) => {
  if (newId && newId !== oldId) {
    currentPartIndex.value = 0
    playIncrd = false
    await loadVideo()
    await Promise.all([loadDanmu(), loadComments()])
  }
})

// 组件卸载前断开 ResizeObserver,避免内存泄漏
onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
})
</script>

<template>
  <div class="detail-page">
    <Navbar />

    <div v-loading="loading" class="container detail-content">
      <div v-if="video" class="detail-layout">
        <!-- 左侧主内容 -->
        <div class="main">
          <!-- 播放器(多P时src随选集切换,key变化强制重新挂载以刷新视频) -->
        <DanmuPlayer
          :key="currentPartIndex"
          :src="currentPartUrl || video.videoUrl"
          :poster="video.coverUrl"
          :danmu-list="danmuList"
          @send-danmu="onSendDanmu"
        />

          <!-- 视频标题 -->
          <h1 class="video-title">{{ video.title }}</h1>

          <!-- 信息条: UP主 / 点赞 / 收藏 / 分享 -->
          <div class="info-bar">
            <div class="left-info">
              <el-avatar :size="40" class="up-avatar">
                {{ video.authorName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="up-name">UP主: {{ video.authorName || video.id }}</span>
            </div>

            <div class="right-actions">
              <!-- 点赞 -->
              <el-button
                :type="video.liked ? 'primary' : 'default'"
                round
                @click="onLike"
              >
                <el-icon><Star /></el-icon>
                <span>{{ formatNum(video.likeNum) }}</span>
              </el-button>

              <!-- 收藏 -->
              <el-button
                :type="video.favorited ? 'primary' : 'default'"
                round
                @click="onFavorite"
              >
                <el-icon><CollectionTag /></el-icon>
                <span>收藏</span>
              </el-button>

              <!-- 分享(简化版) -->
              <el-button round>
                <el-icon><Share /></el-icon>
                <span>{{ formatNum(video.shareNum) }}</span>
              </el-button>
            </div>
          </div>

          <!-- 简介 -->
          <div class="description">
            <div class="desc-header">
              <span class="play-count">{{ formatNum(video.playNum) }} 观看</span>
              <span class="tags" v-if="video.tags">标签: {{ video.tags }}</span>
            </div>
            <p class="desc-text">{{ video.description || '暂无简介' }}</p>
          </div>

          <!-- 评论区 -->
          <div class="comments-section">
            <h2 class="section-title">
              评论
              <span class="comment-count">{{ commentList.length }}</span>
            </h2>

            <!-- 发送评论 -->
            <div class="comment-input">
              <el-avatar :size="36" class="user-avatar">
                {{ userStore.userInfo?.userName?.charAt(0) || '?' }}
              </el-avatar>
              <el-input
                v-model="commentInput"
                type="textarea"
                :rows="2"
                placeholder="发一条友善的弹幕 / 评论吧~"
                maxlength="100"
                show-word-limit
                resize="none"
              />
              <el-button
                type="primary"
                :loading="sending"
                :disabled="!commentInput.trim()"
                @click="onSendComment"
              >
                发送
              </el-button>
            </div>

            <!-- 评论列表 -->
            <div class="comment-list">
              <div v-for="c in commentList" :key="c.cid" class="comment-item">
                <el-avatar :size="36" class="user-avatar">
                  {{ c.userName?.charAt(0) || 'U' }}
                </el-avatar>
                <div class="comment-body">
                  <div class="comment-meta">
                    <span class="comment-user">{{ c.userName || '匿名用户' }}</span>
                    <span class="comment-time">{{ formatDate(c.commentFrame) }}</span>
                  </div>
                  <p class="comment-content">{{ c.commentContent }}</p>
                  <div class="comment-actions">
                    <el-button text size="small" @click="onCommentLike(c)">
                      <el-icon><Star /></el-icon>
                      <span>{{ c.likeNum || 0 }}</span>
                    </el-button>
                  </div>
                </div>
              </div>

              <el-empty
                v-if="commentList.length === 0"
                description="还没有评论,快来抢沙发~"
              />
            </div>
          </div>
        </div>

        <!-- 右侧侧边栏: 选集(仅多P) + 推荐 -->
        <aside class="sidebar">
          <!-- 视频选集(当前视频的分P,仅多P时显示) -->
          <section v-if="showSelection" class="panel selection-panel">
            <h3 class="panel-title">
              选集(P{{ currentPartIndex + 1 }}/{{ partList.length }})
            </h3>
            <div class="panel-body selection-list">
              <div
                v-for="(url, i) in partList"
                :key="i"
                class="side-item part-item"
                :class="{ active: i === currentPartIndex }"
                @click="switchPart(i)"
              >
                <span class="side-index">P{{ i + 1 }}</span>
                <div class="side-info">
                  <p class="side-title" :title="url">分P{{ i + 1 }}</p>
                </div>
              </div>
            </div>
          </section>

          <!-- 推荐(首页视频随机排列,排除当前视频,数量随详情页高度变化) -->
          <section class="panel recommend-panel">
            <h3 class="panel-title">
              推荐
              <span class="panel-count">{{ recommendList.length }}</span>
            </h3>
            <!-- ref 绑定供 ResizeObserver 测量高度 -->
            <div ref="recommendBoxRef" class="panel-body recommend-box">
              <div
                v-for="v in recommendList"
                :key="v.vId"
                class="side-item"
                @click="router.push(`/video/${v.vId}`)"
              >
                <img
                  class="side-cover"
                  :src="v.coverUrl"
                  :alt="v.title"
                  loading="lazy"
                />
                <div class="side-info">
                  <p class="side-title" :title="v.title">{{ v.title }}</p>
                  <span class="side-stat">
                    <el-icon><VideoPlay /></el-icon>{{ formatNum(v.playNum) }}
                  </span>
                </div>
              </div>
            </div>
          </section>
        </aside>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page {
  min-height: 100vh;
  padding-bottom: 40px;
}
.detail-content {
  padding-top: 20px;
}

/* 双栏布局: 主内容 + 侧边栏 */
/* align-items: stretch(默认),使侧边栏高度随主内容(评论区)拉伸 */
/* 主内容越高(评论越多) -> 侧边栏越高 -> 推荐显示条数越多 */
.detail-layout {
  display: flex;
  gap: 20px;
  align-items: stretch;
}
.main {
  flex: 1;
  min-width: 0; /* 防止 flex 子项溢出 */
}
.video-title {
  font-size: 20px;
  font-weight: 500;
  margin: 16px 0;
  line-height: 1.4;
}
.info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f1f2f3;
}
.left-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.up-avatar {
  background: #7388ff;
  color: #fff;
}
.up-name {
  font-size: 15px;
  font-weight: 500;
}
.right-actions {
  display: flex;
  gap: 8px;
}
.description {
  margin: 16px 0;
  padding: 14px;
  background: #fff;
  border-radius: 8px;
}
.desc-header {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #9499a0;
  margin-bottom: 8px;
}
.desc-text {
  color: #18191c;
  line-height: 1.6;
}
.comments-section {
  margin-top: 24px;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
.section-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.comment-count {
  font-size: 14px;
  color: #9499a0;
  font-weight: normal;
}
.comment-input {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: flex-start;
}
.comment-input :deep(.el-textarea) {
  flex: 1;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.comment-item {
  display: flex;
  gap: 12px;
}
.user-avatar {
  background: #6CA4F9;
  color: #fff;
  flex-shrink: 0;
}
.comment-body {
  flex: 1;
}
.comment-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 6px;
}
.comment-user {
  font-size: 14px;
  color: #61666d;
  font-weight: 500;
}
.comment-time {
  font-size: 12px;
  color: #9499a0;
}
.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #18191c;
  margin-bottom: 6px;
}
.comment-actions {
  display: flex;
  gap: 16px;
}

/* ============= 右侧侧边栏 ============= */
.sidebar {
  width: 320px;
  flex-shrink: 0;
  /* 高度由 align-items: stretch 拉伸至与主内容等高(评论区越多越高) */
  display: flex;
  flex-direction: column;
  gap: 16px;
  /* 吸顶: 滚动详情页时侧边栏跟随,下方推荐随滚动可见 */
  position: sticky;
  top: 76px;
}
.panel {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.panel-title {
  font-size: 15px;
  font-weight: 500;
  padding: 12px 14px;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  border-bottom: 1px solid #f1f2f3;
}
.panel-count {
  font-size: 12px;
  color: #9499a0;
  font-weight: normal;
}
.panel-body {
  padding: 8px;
  overflow-y: auto;
}
/* 选集列表: 内容多时内部滚动,但优先展示 */
.selection-list {
  max-height: 260px;
}
/* 推荐区: 填充侧边栏剩余高度,数量随详情页高度变化 */
.recommend-panel {
  flex: 1;
  min-height: 0;
}
.recommend-box {
  flex: 1;
  overflow-y: auto;
}
/* 侧边栏单个视频项(横向紧凑布局) */
.side-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}
.side-item:hover {
  background: #f1f2f3;
}
/* 当前播放的分P高亮 */
.part-item.active {
  background: #ecf5ff;
  color: var(--el-color-primary, #409eff);
}
.part-item.active .side-index,
.part-item.active .side-title {
  color: var(--el-color-primary, #409eff);
  font-weight: 500;
}
.side-index {
  width: 20px;
  text-align: center;
  font-size: 13px;
  color: #9499a0;
  flex-shrink: 0;
}
.side-cover {
  width: 120px;
  height: 68px;
  object-fit: cover;
  border-radius: 4px;
  background: #f1f2f3;
  flex-shrink: 0;
}
.side-info {
  flex: 1;
  min-width: 0;
}
.side-title {
  font-size: 13px;
  line-height: 1.4;
  margin: 0 0 4px;
  /* 单行省略 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #18191c;
}
.side-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9499a0;
}

/* 响应式: 窄屏隐藏侧边栏 */
@media (max-width: 1100px) {
  .detail-layout {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
    position: static;
    max-height: none;
  }
  .side-cover {
    width: 160px;
  }
}
</style>
