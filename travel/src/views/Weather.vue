<script setup>
/**
 * 目的地天气：选择城市查看未来 7 天预报
 */
import { onMounted, ref } from 'vue'
import { Icon, showToast } from 'vant'
import { weatherByCity } from '../api/weather'
import { listCities } from '../api/dest'

const cities = ref([])
const currentCity = ref('')
const weather = ref(null)
const loading = ref(false)
const error = ref('')

onMounted(async () => {
  try {
    cities.value = await listCities()
    const first = cities.value.find((c) => c.name === '北京') || cities.value[0]
    if (first) selectCity(first.name)
  } catch (e) {
    // ignore
  }
})

async function selectCity(name) {
  currentCity.value = name
  loading.value = true
  error.value = ''
  try {
    weather.value = await weatherByCity(name, 7)
  } catch (e) {
    error.value = e.message || '天气获取失败'
    weather.value = null
  } finally {
    loading.value = false
  }
}

function isToday(i) {
  return i === 0
}
</script>

<template>
  <div class="weather-page">
    <van-nav-bar title="目的地天气" left-arrow @click-left="$router.back()" />

    <!-- 城市选择 -->
    <div class="city-bar">
      <span
        v-for="c in cities"
        :key="c.id"
        class="city-chip"
        :class="{ 'city-chip--active': currentCity === c.name }"
        @click="selectCity(c.name)"
      >{{ c.name }}</span>
    </div>

    <div v-if="loading" class="state-wrap"><van-loading size="28" vertical>加载天气...</van-loading></div>

    <div v-else-if="error" class="state-wrap">
      <van-empty image="error" :description="error" />
      <van-button round type="primary" size="small" @click="selectCity(currentCity)">重试</van-button>
    </div>

    <template v-else-if="weather">
      <!-- 今日概览 -->
      <div v-if="weather.daily && weather.daily.length" class="today-card">
        <div class="today-city"><Icon name="location-o" /> {{ weather.city }}</div>
        <div class="today-main">
          <span class="today-icon">{{ weather.daily[0].icon }}</span>
          <div class="today-text">
            <div class="today-desc">{{ weather.daily[0].text }}</div>
            <div class="today-temp">
              {{ weather.daily[0].tempMin }}° ~ {{ weather.daily[0].tempMax }}°
            </div>
          </div>
          <div class="today-extra">
            <div>降水 {{ weather.daily[0].precipProb }}%</div>
            <div>风速 {{ weather.daily[0].windMax }}km/h</div>
          </div>
        </div>
      </div>

      <!-- 未来预报 -->
      <div class="forecast">
        <div class="forecast-title">未来 7 天</div>
        <div
          v-for="(d, i) in weather.daily"
          :key="d.date"
          class="forecast-item"
          :class="{ 'forecast-item--today': isToday(i) }"
        >
          <div class="forecast-day">
            <span class="forecast-week">{{ isToday(i) ? '今天' : d.weekday }}</span>
            <span class="forecast-date">{{ d.date.slice(5) }}</span>
          </div>
          <span class="forecast-icon">{{ d.icon }}</span>
          <span class="forecast-text">{{ d.text }}</span>
          <div class="forecast-temp">
            <span class="temp-min">{{ d.tempMin }}°</span>
            <div class="temp-bar">
              <div class="temp-bar__inner"></div>
            </div>
            <span class="temp-max">{{ d.tempMax }}°</span>
          </div>
        </div>
      </div>

      <div class="tip">数据来源：Open-Meteo（免费预报，仅供参考）</div>
    </template>

    <div style="height: 30px"></div>
  </div>
</template>

<style scoped>
.weather-page {
  min-height: 100vh;
  background: var(--bg);
}

.city-bar {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 10px 12px;
}

.city-chip {
  flex-shrink: 0;
  padding: 4px 12px;
  border-radius: 13px;
  background: var(--surface);
  border: 1px solid var(--line);
  font-size: 12px;
  color: var(--text);
}

.city-chip--active {
  background: var(--grad-brand);
  border-color: transparent;
  color: var(--brand-ink);
  font-weight: 600;
}

.state-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  padding: 70px 0;
}

.today-card {
  margin: 0 12px 12px;
  border-radius: 14px;
  padding: 18px;
  background: var(--grad-dark);
  color: #fff;
}

.today-city {
  font-size: 14px;
  opacity: 0.95;
}

.today-main {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 12px;
}

.today-icon {
  font-size: 42px;
}

.today-text {
  flex: 1;
}

.today-desc {
  font-size: 16px;
  font-weight: 600;
}

.today-temp {
  font-size: 22px;
  font-weight: 700;
  margin-top: 2px;
}

.today-extra {
  text-align: right;
  font-size: 12px;
  opacity: 0.9;
  line-height: 1.7;
}

.forecast {
  margin: 0 12px;
  background: var(--surface);
  border-radius: 16px;
  padding: 6px 14px;
}

.forecast-title {
  font-size: 14px;
  font-weight: 600;
  padding: 10px 0;
}

.forecast-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-top: 1px solid var(--line);
}

.forecast-item--today {
  background: var(--tint);
  margin: 0 -14px;
  padding-left: 14px;
  padding-right: 14px;
}

.forecast-day {
  width: 52px;
  flex-shrink: 0;
}

.forecast-week {
  font-size: 13px;
  font-weight: 600;
  display: block;
}

.forecast-date {
  font-size: 11px;
  color: var(--text-2);
}

.forecast-icon {
  font-size: 20px;
}

.forecast-text {
  width: 64px;
  font-size: 12px;
  color: var(--text-2);
  flex-shrink: 0;
}

.forecast-temp {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: flex-end;
}

.temp-min {
  font-size: 12px;
  color: var(--text-2);
  width: 30px;
  text-align: right;
}

.temp-max {
  font-size: 13px;
  font-weight: 600;
  width: 30px;
}

.temp-bar {
  width: 54px;
  height: 5px;
  border-radius: 3px;
  background: linear-gradient(90deg, #04dc9c, #4cccf4);
  opacity: 0.75;
}

.tip {
  text-align: center;
  font-size: 11px;
  color: var(--text-2);
  margin-top: 14px;
}
</style>
