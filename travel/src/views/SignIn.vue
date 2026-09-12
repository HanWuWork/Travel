<script setup>
/**
 * 每日签到：本月签到日历 + 连续天数 + 积分等级
 */
import { computed, onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { doSignIn, signInStatus } from '../api/signin'
import { isLoggedIn } from '../utils/auth'
import { useRouter } from 'vue-router'

const router = useRouter()

const status = ref(null)
const signing = ref(false)
const loading = ref(true)

const today = new Date()
const year = today.getFullYear()
const month = today.getMonth() + 1
const todayDay = today.getDate()

onMounted(async () => {
  if (!isLoggedIn()) {
    showToast('请先登录')
    router.replace('/login')
    return
  }
  await load()
})

async function load() {
  loading.value = true
  try {
    status.value = await signInStatus()
  } catch (e) {
    showToast(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function onSign() {
  signing.value = true
  try {
    status.value = await doSignIn()
    showToast(`签到成功，+${status.value.todayPoints} 积分`)
  } catch (e) {
    showToast(e.message || '签到失败')
  } finally {
    signing.value = false
  }
}

/** 本月日历单元格：先补空白对齐星期，再排日期 */
const cells = computed(() => {
  const firstWeekday = new Date(year, month - 1, 1).getDay() // 0=周日
  const daysInMonth = new Date(year, month, 0).getDate()
  const signed = new Set(status.value?.monthSignedDates || [])
  const list = []
  for (let i = 0; i < firstWeekday; i++) {
    list.push({ empty: true, key: 'e' + i })
  }
  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    list.push({
      day: d,
      key: dateStr,
      signed: signed.has(dateStr),
      isToday: d === todayDay
    })
  }
  return list
})

const levelProgress = computed(() => {
  if (!status.value || status.value.nextLevelPoints < 0) return 100
  const from = status.value.levelMinPoints
  const to = status.value.nextLevelPoints
  return Math.min(100, Math.round(((status.value.points - from) / (to - from)) * 100))
})
</script>

<template>
  <div class="signin-page">
    <van-nav-bar title="每日签到" left-arrow @click-left="$router.back()" />

    <template v-if="status">
      <!-- 积分与等级 -->
      <div class="points-card">
        <div class="points-main">
          <div class="points-num">{{ status.points }}</div>
          <div class="points-label">当前积分</div>
        </div>
        <div class="level-box">
          <div class="level-name">{{ status.levelName }}</div>
          <div class="level-bar">
            <div class="level-fill" :style="{ width: levelProgress + '%' }"></div>
          </div>
          <div class="level-next">
            <template v-if="status.nextLevelPoints > 0">
              再得 {{ status.nextLevelPoints - status.points }} 分升级
            </template>
            <template v-else>已达最高等级</template>
          </div>
        </div>
      </div>

      <!-- 签到按钮 -->
      <div class="sign-card">
        <div class="sign-stats">
          <div class="sign-stat">
            <div class="sign-stat__num">{{ status.consecutiveDays }}</div>
            <div class="sign-stat__label">连续签到</div>
          </div>
          <div class="sign-stat">
            <div class="sign-stat__num">{{ status.totalDays }}</div>
            <div class="sign-stat__label">累计签到</div>
          </div>
          <div class="sign-stat">
            <div class="sign-stat__num">+{{ status.todayPoints }}</div>
            <div class="sign-stat__label">{{ status.todaySigned ? '今日已得' : '今日可得' }}</div>
          </div>
        </div>
        <van-button
          :type="status.todaySigned ? 'default' : 'primary'"
          block
          round
          :disabled="status.todaySigned"
          :loading="signing"
          @click="onSign"
        >
          {{ status.todaySigned ? '今日已签到' : '立即签到' }}
        </van-button>
      </div>

      <!-- 本月日历 -->
      <div class="calendar-card">
        <div class="calendar-title">{{ year }} 年 {{ month }} 月</div>
        <div class="weekdays">
          <span v-for="w in ['日', '一', '二', '三', '四', '五', '六']" :key="w">{{ w }}</span>
        </div>
        <div class="calendar-grid">
          <div
            v-for="c in cells"
            :key="c.key"
            class="day-cell"
            :class="{ 'day-cell--empty': c.empty, 'day-cell--signed': c.signed, 'day-cell--today': c.isToday }"
          >
            <template v-if="!c.empty">{{ c.day }}</template>
          </div>
        </div>
        <div class="rule">{{ status.rule }}</div>
      </div>
    </template>

    <div v-else-if="loading" class="state-wrap"><van-loading size="28" vertical>加载中...</van-loading></div>
  </div>
</template>

<style scoped>
.signin-page {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: 30px;
}

.state-wrap {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.points-card {
  display: flex;
  align-items: center;
  gap: 18px;
  margin: 12px;
  padding: 20px 18px;
  border-radius: 14px;
  background: var(--grad-dark);
  color: #fff;
}

.points-main {
  text-align: center;
  flex-shrink: 0;
}

.points-num {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.1;
}

.points-label {
  font-size: 12px;
  opacity: 0.85;
}

.level-box {
  flex: 1;
}

.level-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}

.level-bar {
  height: 7px;
  background: rgba(255, 255, 255, 0.28);
  border-radius: 4px;
  overflow: hidden;
}

.level-fill {
  height: 100%;
  background: #ffd666;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.level-next {
  font-size: 11px;
  opacity: 0.85;
  margin-top: 6px;
}

.sign-card {
  margin: 0 12px 12px;
  background: var(--surface);
  border-radius: 16px;
  padding: 16px;
}

.sign-stats {
  display: flex;
  margin-bottom: 14px;
}

.sign-stat {
  flex: 1;
  text-align: center;
}

.sign-stat__num {
  font-size: 20px;
  font-weight: 700;
  color: var(--brand-deep);
}

.sign-stat__label {
  font-size: 11px;
  color: var(--text-2);
  margin-top: 2px;
}

.calendar-card {
  margin: 0 12px;
  background: var(--surface);
  border-radius: 16px;
  padding: 16px;
}

.calendar-title {
  font-size: 15px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 12px;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  font-size: 12px;
  color: var(--text-2);
  text-align: center;
  margin-bottom: 6px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.day-cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  border-radius: 50%;
  color: var(--text);
}

.day-cell--empty {
  visibility: hidden;
}

.day-cell--signed {
  background: #04dc9c;
  color: #fff;
  font-weight: 600;
}

.day-cell--today {
  border: 1.5px solid #0c0c0c;
}

.rule {
  font-size: 11px;
  color: var(--text-2);
  line-height: 1.6;
  margin-top: 14px;
  padding-top: 10px;
  border-top: 1px solid var(--line);
}
</style>
