<script setup>
// 弹幕播放器组件
// 功能:
//   1) HTML5 <video> 播放视频
//   2) 弹幕层覆盖在视频上,从右向左滚动
//   3) 监听 timeupdate 事件,根据当前播放时间(danmuFrame)显示对应弹幕
//   4) 弹幕按轨道(行)分布,避免重叠
//   5) 底部弹幕输入框: 发送弹幕(emit 事件给父组件)
//   6) 弹幕开关 / 暂停时弹幕也暂停
//
// 实现要点:
//   - 使用 Vue 响应式数组 activeList 管理当前显示的弹幕
//   - 已显示弹幕集合 playedSet 避免重复渲染
//   - 弹幕通过 CSS @keyframes scroll-left 实现从右向左滚动
//   - 容错: 视频seek(进度跳转)时重置 playedSet,重新渲染对应时间段
import { ref, reactive, computed, onBeforeUnmount, nextTick } from 'vue'

const props = defineProps({
  // 视频URL
  src: { type: String, default: '' },
  // 视频封面
  poster: { type: String, default: '' },
  // 全部弹幕列表 [{ did, danmuContent, danmuFrame, color }]
  danmuList: { type: Array, default: () => [] }
})

// 向父组件发送弹幕事件
const emit = defineEmits(['send-danmu', 'play'])

const videoEl = ref(null)            // <video> DOM 引用
const containerEl = ref(null)         // 弹幕容器 DOM 引用

const isPlaying = ref(false)          // 是否播放中
const currentTime = ref(0)            // 当前播放时间(秒)
const duration = ref(0)                // 视频总时长
const danmuSwitch = ref(true)          // 弹幕开关
const activeList = ref([])             // 当前显示中的弹幕 [{id, text, color, track, duration}]
const playedSet = new Set()            // 已播放弹幕did集合(避免重复)
const TRACK_HEIGHT = 32               // 每条弹幕轨道高度
const MAX_TRACK = 8                   // 最大轨道数
const DANMU_DURATION = 8000           // 单条弹幕滚动时长(ms)
let trackUseUntil = Array(MAX_TRACK).fill(0) // 每条轨道下一次可用的时刻

// 计算容器可用高度,用于轨道布局
const containerHeight = computed(() => MAX_TRACK * TRACK_HEIGHT)

// ============= 视频控制 =============
const togglePlay = () => {
  if (!videoEl.value) return
  if (videoEl.value.paused) {
    videoEl.value.play()
  } else {
    videoEl.value.pause()
  }
}

const onPlay = () => {
  isPlaying.value = true
  emit('play')
}
const onPause = () => { isPlaying.value = false }
const onTimeUpdate = () => {
  if (!videoEl.value) return
  currentTime.value = videoEl.value.currentTime
  // 触发弹幕渲染
  flushDanmu(currentTime.value)
}
const onLoadedMetadata = () => {
  duration.value = videoEl.value.duration
}
// 视频seek(跳转进度)时,清空已播放集合,允许重新渲染
const onSeeking = () => {
  playedSet.clear()
  activeList.value = []
  trackUseUntil = Array(MAX_TRACK).fill(0)
}

// ============= 弹幕渲染逻辑 =============
// 取出当前时间点应该显示的弹幕(在 [currentTime-0.5, currentTime] 区间)
const flushDanmu = (t) => {
  if (!danmuSwitch.value) return
  // 当前时间前后0.5秒内的弹幕(留出容差)
  const start = t - 0.5
  const end = t + 0.5
  for (const d of props.danmuList) {
    if (d.danmuFrame >= start && d.danmuFrame <= end && !playedSet.has(d.did)) {
      playedSet.add(d.did)
      spawnDanmu(d)
    }
  }
}

// 创建一条弹幕
const spawnDanmu = (d) => {
  const track = pickTrack()
  if (track === -1) return // 所有轨道都被占用,跳过(避免重叠)
  const id = Date.now() + '-' + Math.random()
  activeList.value.push({
    id,
    text: d.danmuContent,
    color: d.color || '#FFFFFF',
    track,
    duration: DANMU_DURATION
  })
  // 8秒后清理
  setTimeout(() => {
    const idx = activeList.value.findIndex(item => item.id === id)
    if (idx >= 0) activeList.value.splice(idx, 1)
  }, DANMU_DURATION + 200)
}

// 选择可用轨道(简单的轨道选择算法)
const pickTrack = () => {
  const now = Date.now()
  for (let i = 0; i < MAX_TRACK; i++) {
    if (trackUseUntil[i] <= now) {
      // 该轨道空闲,占用,标记下一次可用时刻(让弹幕有滚动距离后才可复用)
      trackUseUntil[i] = now + 3000
      return i
    }
  }
  return -1
}

// ============= 弹幕发送 =============
const danmuInput = ref('')
const danmuColor = ref('#FFFFFF')

const sendDanmu = () => {
  const text = danmuInput.value.trim()
  if (!text) return
  // emit 给父组件,父组件调用接口保存
  emit('send-danmu', {
    danmuContent: text,
    danmuFrame: Math.floor(currentTime.value),
    color: danmuColor.value
  })
  // 同时本地立即显示(乐观更新)
  spawnDanmu({
    did: 'temp-' + Date.now(),
    danmuContent: text,
    danmuFrame: currentTime.value,
    color: danmuColor.value
  })
  danmuInput.value = ''
}

// 时间格式化 mm:ss
const formatTime = (s) => {
  if (!s || isNaN(s)) return '00:00'
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
}

// 组件卸载时清理
onBeforeUnmount(() => {
  activeList.value = []
  playedSet.clear()
})

// 暴露方法给父组件(可手动播放)
defineExpose({
  play: () => videoEl.value?.play(),
  pause: () => videoEl.value?.pause()
})
</script>

<template>
  <div class="danmu-player" ref="containerEl">
    <!-- 视频元素 -->
    <video
      ref="videoEl"
      class="video"
      :src="src"
      :poster="poster"
      controls
      @click="togglePlay"
      @play="onPlay"
      @pause="onPause"
      @timeupdate="onTimeUpdate"
      @loadedmetadata="onLoadedMetadata"
      @seeking="onSeeking"
    />

    <!-- 弹幕层 -->
    <div
      v-show="danmuSwitch"
      class="danmu-layer"
      :style="{ height: containerHeight + 'px' }"
    >
      <div
        v-for="item in activeList"
        :key="item.id"
        class="danmu-item"
        :style="{
          top: item.track * TRACK_HEIGHT + 'px',
          color: item.color,
          animationDuration: item.duration + 'ms'
        }"
      >
        {{ item.text }}
      </div>
    </div>

    <!-- 控制条 -->
    <div class="control-bar">
      <div class="left-controls">
        <el-button text class="ctrl-btn" @click="togglePlay">
          <el-icon size="18">
            <VideoPlay v-if="!isPlaying" />
            <VideoPause v-else />
          </el-icon>
        </el-button>
        <span class="time">
          {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
        </span>
      </div>

      <div class="right-controls">
        <el-tooltip :content="danmuSwitch ? '关闭弹幕' : '开启弹幕'">
          <el-button text class="ctrl-btn" @click="danmuSwitch = !danmuSwitch">
            <el-icon size="18"><ChatLineSquare /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 弹幕输入框 -->
    <div class="danmu-input-bar">
      <el-color-picker v-model="danmuColor" size="small" />
      <el-input
        v-model="danmuInput"
        placeholder="发个弹幕见证当下"
        maxlength="100"
        @keyup.enter="sendDanmu"
      />
      <el-button type="primary" @click="sendDanmu">发送</el-button>
    </div>
  </div>
</template>

<style scoped>
.danmu-player {
  position: relative;
  width: 100%;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}
.video {
  display: block;
  width: 100%;
  max-height: 540px;
  background: #000;
}
/* 弹幕层绝对覆盖在视频上,只占视频上半部分避免遮挡字幕 */
.danmu-layer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  pointer-events: none; /* 不阻挡视频控件 */
  overflow: hidden;
}
.danmu-item {
  position: absolute;
  left: 100%;
  white-space: nowrap;
  font-size: 18px;
  font-weight: bold;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
  animation-name: scroll-left;
  animation-timing-function: linear;
  animation-fill-mode: forwards;
}
@keyframes scroll-left {
  from { transform: translateX(0); }
  to   { transform: translateX(-150vw); }
}
.control-bar {
  position: absolute;
  bottom: 60px; /* 给原生 controls 留位置 */
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  padding: 0 12px;
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
}
.danmu-player:hover .control-bar {
  opacity: 1;
}
.left-controls, .right-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}
.ctrl-btn {
  color: #fff;
  pointer-events: auto;
}
.time {
  color: #fff;
  font-size: 13px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.6);
}
.danmu-input-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  background: #f1f2f3;
}
</style>
