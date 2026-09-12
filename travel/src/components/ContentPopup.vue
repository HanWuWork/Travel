<script setup>
/**
 * 通用内容弹层：用于「常见问题 / 使用指南 / 用户协议 / 隐私政策」等纯说明类条目，
 * 保证点击后展示的内容与该条目名称一致。
 */
import { Popup } from 'vant'

defineProps({
  show: { type: Boolean, default: false },
  title: { type: String, default: '' },
  /** [{ t: '文本', h: true 表示小标题 }] */
  items: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:show'])
</script>

<template>
  <Popup
    :show="show"
    position="bottom"
    round
    :style="{ maxHeight: '78vh' }"
    @update:show="(v) => emit('update:show', v)"
  >
    <div class="cp">
      <div class="cp__head">
        <span class="cp__title">{{ title }}</span>
        <van-icon name="cross" class="cp__close" @click="emit('update:show', false)" />
      </div>
      <div class="cp__body">
        <template v-for="(it, i) in items" :key="i">
          <div v-if="it.h" class="cp__h">{{ it.t }}</div>
          <p v-else class="cp__p">{{ it.t }}</p>
        </template>
      </div>
      <div class="cp__foot">
        <van-button block round type="primary" @click="emit('update:show', false)">知道了</van-button>
      </div>
    </div>
  </Popup>
</template>

<style scoped>
.cp {
  display: flex;
  flex-direction: column;
  max-height: 78vh;
}

.cp__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 10px;
  flex-shrink: 0;
}

.cp__title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
}

.cp__close {
  font-size: 16px;
  color: var(--text-3);
}

.cp__body {
  padding: 0 16px 4px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.cp__h {
  margin: 12px 0 4px;
  font-size: 14px;
  font-weight: 700;
  color: var(--brand-deep);
}

.cp__h:first-child {
  margin-top: 2px;
}

.cp__p {
  margin: 0 0 8px;
  font-size: 13.5px;
  line-height: 1.75;
  color: var(--text-2);
}

.cp__foot {
  padding: 8px 16px calc(16px + env(safe-area-inset-bottom));
  flex-shrink: 0;
}
</style>
