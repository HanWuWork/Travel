<template>
  <div class="journey-card">
    <Card>
      <template #title>
        <div class="card-title">
          <Icon name="location-o" />
          <span>旅程规划</span>
        </div>
      </template>
      <template #desc>
        <div class="journey-form">
          <Field
            v-model="form.destination"
            label="旅游目的地"
            placeholder="请选择目的地"
            is-link
            readonly
            @click="showDestinationPicker = true"
          />
          <Field
            v-model="form.budget"
            label="预算"
            placeholder="请输入预算金额"
            type="number"
          >
            <template #button>
              <span class="field-suffix">元</span>
            </template>
          </Field>
          <Field
            v-model="form.days"
            label="天数"
            placeholder="请输入天数"
            type="number"
          >
            <template #button>
              <span class="field-suffix">天</span>
            </template>
          </Field>
        </div>
      </template>
      <template #footer>
        <div class="journey-actions">
          <Button class="action-btn" type="primary" round @click="onStartPlan">
            开始规划
          </Button>
          <Button class="action-btn" round plain @click="onReset">
            重置
          </Button>
        </div>
      </template>
    </Card>

    <Popup v-model:show="showDestinationPicker" position="bottom" class="destination-popup">
      <Picker
        title="选择目的地"
        :columns="destinationColumns"
        @confirm="onDestinationConfirm"
        @cancel="showDestinationPicker = false"
      />
    </Popup>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Card, Icon, Button, Field, Popup, Picker, showToast } from 'vant'

const props = defineProps({
  journey: {
    type: Object,
    default: () => ({
      destination: '',
      budget: '',
      days: ''
    })
  }
})

const emit = defineEmits(['view-plan', 'edit-plan', 'update:journey'])
const router = useRouter()

const showDestinationPicker = ref(false)

const form = ref({
  destination: props.journey.destination || '',
  budget: props.journey.budget || '',
  days: props.journey.days || ''
})

watch(
  () => props.journey,
  (newJourney) => {
    form.value = {
      destination: newJourney.destination || '',
      budget: newJourney.budget || '',
      days: newJourney.days || ''
    }
  },
  { deep: true }
)

const destinationColumns = [
  { text: '北京', value: '北京' },
  { text: '上海', value: '上海' },
  { text: '杭州', value: '杭州' },
  { text: '成都', value: '成都' },
  { text: '西安', value: '西安' },
  { text: '三亚', value: '三亚' },
  { text: '厦门', value: '厦门' },
  { text: '云南', value: '云南' },
  { text: '西藏', value: '西藏' },
  { text: '新疆', value: '新疆' },
  { text: '广州', value: '广州' },
  { text: '深圳', value: '深圳' },
  { text: '南京', value: '南京' },
  { text: '重庆', value: '重庆' },
  { text: '青岛', value: '青岛' }
]

const onDestinationConfirm = ({ selectedOptions }) => {
  form.value.destination = selectedOptions[0].text
  showDestinationPicker.value = false
  emit('update:journey', { ...form.value })
}

const onReset = () => {
  form.value = { destination: '', budget: '', days: '' }
  emit('update:journey', { ...form.value })
  emit('edit-plan')
}

const onStartPlan = () => {
  if (!form.value.destination) {
    showToast('请先选择旅游目的地')
    return
  }
  if (!form.value.budget) {
    showToast('请输入预算金额')
    return
  }
  if (!form.value.days) {
    showToast('请输入行程天数')
    return
  }
  emit('update:journey', { ...form.value })
  router.push({
    path: '/plan',
    query: {
      destination: form.value.destination,
      budget: form.value.budget,
      days: form.value.days
    }
  })
}
</script>

<style scoped>
.journey-card {
  margin: 12px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
}

.journey-form {
  padding: 8px 0;
}

.field-suffix {
  color: #969799;
  font-size: 14px;
}

.journey-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 4px 0;
}

.action-btn {
  flex: 1;
  height: 44px;
  font-size: 16px;
}
</style>
