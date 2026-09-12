<script setup>
/**
 * 汇率换算器：多币种换算 + 人民币参考汇率表
 */
import { computed, onMounted, ref } from 'vue'
import { Icon, showToast } from 'vant'
import { convertCurrency } from '../api/exchange'

/** 可选货币（代码 → 中文名，与后端保持一致） */
const CURRENCIES = [
  { code: 'CNY', name: '人民币' },
  { code: 'USD', name: '美元' },
  { code: 'EUR', name: '欧元' },
  { code: 'JPY', name: '日元' },
  { code: 'KRW', name: '韩元' },
  { code: 'HKD', name: '港币' },
  { code: 'TWD', name: '新台币' },
  { code: 'THB', name: '泰铢' },
  { code: 'SGD', name: '新加坡元' },
  { code: 'MYR', name: '马来西亚林吉特' },
  { code: 'GBP', name: '英镑' },
  { code: 'AUD', name: '澳元' },
  { code: 'CAD', name: '加元' },
  { code: 'CHF', name: '瑞士法郎' },
  { code: 'RUB', name: '卢布' },
  { code: 'VND', name: '越南盾' },
  { code: 'AED', name: '迪拉姆' }
]

const from = ref('CNY')
const to = ref('USD')
const amount = ref(1000)
const result = ref(null)
const loading = ref(false)
const error = ref('')
const showPicker = ref(false)
const picking = ref('from')

onMounted(load)

async function load() {
  const amt = Number(amount.value)
  if (!amt || amt <= 0) {
    result.value = null
    return
  }
  loading.value = true
  error.value = ''
  try {
    result.value = await convertCurrency({ from: from.value, to: to.value, amount: amt })
  } catch (e) {
    error.value = e.message || '汇率获取失败'
    result.value = null
  } finally {
    loading.value = false
  }
}

function swap() {
  const f = from.value
  from.value = to.value
  to.value = f
  load()
}

function openPicker(which) {
  picking.value = which
  showPicker.value = true
}

function pick(code) {
  if (picking.value === 'from') from.value = code
  else to.value = code
  showPicker.value = false
  load()
}

const fromName = computed(() => CURRENCIES.find((c) => c.code === from.value)?.name || '')
const toName = computed(() => CURRENCIES.find((c) => c.code === to.value)?.name || '')
</script>

<template>
  <div class="exchange-page">
    <van-nav-bar title="汇率换算" left-arrow @click-left="$router.back()" />

    <div class="converter">
      <div class="currency-row">
        <span class="currency-label">从</span>
        <div class="currency-pick" @click="openPicker('from')">
          <span class="code">{{ from }}</span>
          <span class="name">{{ fromName }}</span>
          <Icon name="arrow-down" size="12" />
        </div>
      </div>
      <van-field v-model="amount" type="number" class="amount-field" placeholder="输入金额" />

      <div class="swap-row">
        <div class="swap-btn" @click="swap"><Icon name="exchange" size="18" /></div>
      </div>

      <div class="currency-row">
        <span class="currency-label">到</span>
        <div class="currency-pick" @click="openPicker('to')">
          <span class="code">{{ to }}</span>
          <span class="name">{{ toName }}</span>
          <Icon name="arrow-down" size="12" />
        </div>
      </div>
    </div>

    <!-- 结果 -->
    <div class="result-card">
      <van-loading v-if="loading" size="22" vertical>换算中...</van-loading>
      <template v-else-if="result">
        <div class="result-main">
          {{ amount }} {{ result.from }} ≈
          <span class="result-amount">{{ result.result }}</span>
          {{ result.to }}
        </div>
        <div class="result-rate">1 {{ result.from }} = {{ result.rate }} {{ result.to }}</div>
        <div class="result-time">更新时间：{{ result.updateTime }}</div>
        <van-button size="mini" round plain type="primary" @click="load">刷新汇率</van-button>
      </template>
      <div v-else-if="error" class="result-error">{{ error }}</div>
      <div v-else class="result-hint">输入金额后自动换算</div>
    </div>

    <!-- 参考汇率 -->
    <div v-if="result && result.commonRates && result.commonRates.length" class="rate-table">
      <div class="table-title">人民币参考汇率</div>
      <div class="rate-row" v-for="r in result.commonRates" :key="r.code">
        <span class="rate-code">{{ r.code }}</span>
        <span class="rate-name">{{ r.name }}</span>
        <span class="rate-value">1 CNY = {{ r.rate }}</span>
      </div>
    </div>

    <div class="tip">汇率数据来源：open.er-api.com（免费接口，仅供参考，实际以银行为准）</div>

    <!-- 货币选择 -->
    <van-popup v-model:show="showPicker" position="bottom" round>
      <div class="picker">
        <div class="picker-title">选择货币</div>
        <div class="picker-list">
          <div
            v-for="c in CURRENCIES"
            :key="c.code"
            class="picker-item"
            :class="{ 'picker-item--active': (picking === 'from' ? from : to) === c.code }"
            @click="pick(c.code)"
          >
            <span class="code">{{ c.code }}</span>
            <span class="name">{{ c.name }}</span>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.exchange-page {
  min-height: 100vh;
  background: var(--bg);
}

.converter {
  margin: 12px;
  background: var(--surface);
  border-radius: 16px;
  padding: 6px 14px 16px;
}

.currency-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
}

.currency-label {
  width: 30px;
  font-size: 13px;
  color: var(--text-2);
}

.currency-pick {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.code {
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
}

.name {
  font-size: 12px;
  color: var(--text-2);
}

.amount-field {
  background: var(--bg);
  border-radius: 10px;
}

.amount-field :deep(.van-field__body) {
  padding: 4px 0;
}

.amount-field :deep(input) {
  font-size: 20px;
  font-weight: 600;
}

.swap-row {
  display: flex;
  justify-content: center;
  margin: 6px 0;
}

.swap-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--tint);
  color: var(--brand-deep);
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-card {
  margin: 0 12px 12px;
  border-radius: 14px;
  padding: 22px 18px;
  background: var(--grad-dark);
  color: #fff;
  text-align: center;
  min-height: 110px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.result-main {
  font-size: 15px;
}

.result-amount {
  font-size: 26px;
  font-weight: 700;
}

.result-rate {
  font-size: 12px;
  opacity: 0.9;
}

.result-time {
  font-size: 11px;
  opacity: 0.75;
}

.result-error,
.result-hint {
  font-size: 13px;
  opacity: 0.9;
}

.rate-table {
  margin: 0 12px;
  background: var(--surface);
  border-radius: 16px;
  padding: 6px 14px;
}

.table-title {
  font-size: 14px;
  font-weight: 600;
  padding: 10px 0;
}

.rate-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 0;
  border-top: 1px solid var(--line);
  font-size: 13px;
}

.rate-code {
  width: 42px;
  font-weight: 600;
}

.rate-name {
  flex: 1;
  color: var(--text-2);
  font-size: 12px;
}

.rate-value {
  color: var(--brand-deep);
  font-weight: 600;
}

.tip {
  text-align: center;
  font-size: 11px;
  color: var(--text-2);
  margin: 14px 20px 30px;
  line-height: 1.6;
}

.picker {
  padding: 18px 16px 28px;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}

.picker-title {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 12px;
}

.picker-list {
  overflow-y: auto;
}

.picker-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 4px;
  border-bottom: 1px solid var(--line);
}

.picker-item--active .code {
  color: var(--brand-deep);
}
</style>
