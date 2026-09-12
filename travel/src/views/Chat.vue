<template>
  <div class="chat">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      title="AI旅游助手"
      left-text="返回"
      left-arrow
      @click-left="onBack"
    />

    <!-- 主体区域 -->
    <div class="chat-body">
      <!-- 空状态：尚未开始对话 -->
      <div v-if="!hasChat" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 64 64" width="80" height="80" fill="none">
            <rect x="14" y="8" width="36" height="48" rx="4" fill="#e8e9ec" />
            <rect x="20" y="16" width="24" height="4" rx="2" fill="#c8cacd" />
            <rect x="20" y="24" width="24" height="4" rx="2" fill="#c8cacd" />
            <rect x="20" y="32" width="16" height="4" rx="2" fill="#c8cacd" />
            <circle cx="32" cy="48" r="4" fill="#d8d9dc" />
          </svg>
        </div>
        <p class="empty-tip">开始和AI旅游助手 对话</p>

        <div class="faq">
          <div class="faq-title">常见问题</div>
          <div class="faq-list">
            <div
              v-for="q in faqList"
              :key="q"
              class="faq-item"
              @click="sendFaq(q)"
            >{{ q }}</div>
          </div>
        </div>
      </div>

      <!-- 对话消息列表 -->
      <div v-else class="chat-list" ref="listRef">
        <div
          v-for="item in messages"
          :key="item.id"
          class="msg-row"
          :class="item.role === 'user' ? 'msg-row--user' : 'msg-row--ai'"
        >
          <div class="avatar">
            <template v-if="item.role === 'user'">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M12 12a5 5 0 1 0 0-10 5 5 0 0 0 0 10Zm0 2c-4.42 0-8 2.69-8 6v2h16v-2c0-3.31-3.58-6-8-6Z" />
              </svg>
            </template>
            <template v-else>
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M12 2a2 2 0 0 1 2 2v1h3a2 2 0 0 1 2 2v2h1a1 1 0 0 1 1 1v2.5a2.5 2.5 0 0 1-2.5 2.5H19a3 3 0 0 1-3-3v-1h-2v4h1.5a1.5 1.5 0 0 1 1.5 1.5v.5a4 4 0 0 1-4 4 4 4 0 0 1-4-4v-.5A1.5 1.5 0 0 1 9.5 16H11v-4H9v1a3 3 0 0 1-3 3h-.5A2.5 2.5 0 0 1 3 10.5V8a1 1 0 0 1 1-1h1V5a2 2 0 0 1 2-2h3V4a2 2 0 0 1 2-2ZM9 9v2H7V9h2Zm8 0v2h-2V9h2Z" />
              </svg>
            </template>
          </div>
          <div class="bubble" :class="{ 'bubble--error': item.error }">
            <!-- AI 回复：渲染为可读排版（加粗/列表/代码），不显示 Markdown 符号 -->
            <template v-if="item.role === 'assistant' && item.text">
              <span class="bubble-rich" v-html="renderAiText(item.text)"></span>
              <span v-if="item.streaming" class="cursor"></span>
            </template>
            <template v-else-if="item.text">{{ item.text }}</template>
            <span v-else-if="item.pending" class="thinking">
              正在思考<span class="thinking-dots"><i>.</i><i>.</i><i>.</i></span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部输入栏 -->
    <div class="chat-input">
      <input
        v-model="inputText"
        class="input-box"
        type="text"
        maxlength="1000"
        placeholder="请输入你想咨询的问题"
        :disabled="streaming"
        @keyup.enter="sendMessage"
      />
      <button
        v-if="!streaming"
        class="send-btn"
        :disabled="!inputText.trim()"
        @click="sendMessage"
      >
        <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
          <path d="M3.4 20.4 21 12 3.4 3.6l-.2 7.2L15 12 3.2 13.2z" />
        </svg>
      </button>
      <button v-else class="stop-btn" @click="stopGenerate" title="停止">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
          <rect x="6" y="6" width="12" height="12" rx="2" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { streamChat } from '../api/chat'
import { renderAiText } from '../utils/aiText'

const router = useRouter()
const onBack = () => router.push('/')

let msgId = 0
const nextId = () => ++msgId

const listRef = ref(null)
const inputText = ref('')
const streaming = ref(false)
const sessionId = ref('')
let abortController = null

const faqList = [
  '北京有哪些必去的景点？',
  '上海美食推荐',
  '成都三日游攻略',
  '如何选择旅行保险？'
]

// 初始不预置欢迎语，直接进入空状态；用户首次发送后再展示对话
const messages = ref([])

// 是否已经开始对话（存在用户消息）
const hasChat = computed(() =>
  messages.value.some((m) => m.role === 'user')
)

const scrollToBottom = () => {
  nextTick(() => {
    const el = listRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

const sendFaq = (q) => {
  if (streaming.value) return
  inputText.value = q
  sendMessage()
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || streaming.value) return

  messages.value.push({ id: nextId(), role: 'user', text, pending: false, error: false })
  const aiMsg = { id: nextId(), role: 'assistant', text: '', pending: true, streaming: false, error: false }
  messages.value.push(aiMsg)
  inputText.value = ''
  streaming.value = true
  scrollToBottom()

  abortController = new AbortController()

  await streamChat({
    message: text,
    sessionId: sessionId.value,
    signal: abortController.signal,
    onMeta: (payload) => {
      // 流建立即拿到 sessionId，后续轮次可直接复用
      if (payload.sessionId) sessionId.value = payload.sessionId
    },
    onDelta: (chunk) => {
      aiMsg.pending = false
      aiMsg.streaming = true
      aiMsg.text += chunk
      scrollToBottom()
    },
    onDone: (payload) => {
      aiMsg.pending = false
      aiMsg.streaming = false
      if (payload.sessionId) sessionId.value = payload.sessionId
      if (!aiMsg.text) aiMsg.text = '（回复为空，换个问法试试吧～）'
      finishStream()
      scrollToBottom()
    },
    onError: (msg) => {
      aiMsg.pending = false
      aiMsg.streaming = false
      aiMsg.error = true
      aiMsg.text = msg
      finishStream()
      scrollToBottom()
    }
  })
}

const finishStream = () => {
  streaming.value = false
  abortController = null
}

const stopGenerate = () => {
  if (abortController) abortController.abort()
  const last = messages.value[messages.value.length - 1]
  if (last && last.role === 'assistant') {
    last.pending = false
    last.streaming = false
    if (!last.text) last.text = '已停止生成。'
  }
  finishStream()
}

onUnmounted(() => {
  if (abortController) abortController.abort()
})
</script>

<style scoped>
.chat {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  background-color: var(--bg);
}

.chat-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 空状态 */
.empty-state {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 24px 24px;
  overflow-y: auto;
}

.empty-icon {
  margin-top: 20px;
  opacity: 0.9;
}

.empty-tip {
  margin: 16px 0 32px;
  font-size: 14px;
  color: var(--text-2);
  letter-spacing: 0.5px;
}

.faq {
  width: 100%;
  max-width: 360px;
}

.faq-title {
  text-align: center;
  font-size: 13px;
  color: var(--text-2);
  margin-bottom: 14px;
}

.faq-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 12px;
}

.faq-item {
  padding: 10px 12px;
  font-size: 13px;
  color: var(--text);
  text-align: center;
  background-color: var(--line);
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.15s;
  line-height: 1.4;
}

.faq-item:active {
  background-color: var(--line);
}

/* 消息列表 */
.chat-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 14px 12px 12px;
  -webkit-overflow-scrolling: touch;
  background-color: var(--surface-2);
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 14px;
}

.msg-row--user {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--brand);
  background-color: var(--ink);
}

.msg-row--ai .avatar {
  background: var(--grad-brand);
  color: var(--brand-ink);
}

.bubble {
  max-width: 76%;
  padding: 9px 12px;
  border-radius: 10px;
  background-color: var(--surface);
  color: var(--text);
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  box-shadow: var(--shadow);
}

.msg-row--user .bubble {
  background: var(--grad-brand);
  color: var(--brand-ink);
  font-weight: 600;
}

.bubble--error {
  background-color: var(--surface-2);
  color: var(--danger);
  border: 1px solid var(--line);
}

/* AI 富文本内容（去 Markdown 符号后的排版） */
.bubble-rich :deep(strong) {
  font-weight: 700;
}

.bubble-rich :deep(.ai-code),
.bubble-rich :deep(.ai-code-inline) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12.5px;
  background: var(--surface-2);
  border-radius: 6px;
}

.bubble-rich :deep(.ai-code) {
  display: block;
  padding: 8px 10px;
  margin: 6px 0;
  overflow-x: auto;
  white-space: pre;
}

.bubble-rich :deep(.ai-code-inline) {
  padding: 1px 5px;
}

/* 流式输出光标 */
.cursor {
  display: inline-block;
  width: 2px;
  height: 14px;
  margin-left: 2px;
  vertical-align: -2px;
  background: var(--brand-deep);
  animation: caret 0.9s steps(1) infinite;
}

@keyframes caret {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.thinking {
  color: var(--text-2);
}

.thinking-dots i {
  font-style: normal;
  animation: blink 1.2s infinite;
}
.thinking-dots i:nth-child(2) { animation-delay: 0.2s; }
.thinking-dots i:nth-child(3) { animation-delay: 0.4s; }

@keyframes blink {
  0%, 80%, 100% { opacity: 0.2; }
  40% { opacity: 1; }
}

/* 底部输入栏 */
.chat-input {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px calc(8px + env(safe-area-inset-bottom));
  border-top: 1px solid var(--line);
  background-color: var(--surface);
}

.input-box {
  flex: 1;
  height: 36px;
  padding: 0 14px;
  font-size: 14px;
  color: var(--text);
  background-color: var(--surface-2);
  border: none;
  border-radius: 18px;
  outline: none;
}

.input-box::placeholder {
  color: var(--text-2);
}

.input-box:disabled {
  opacity: 0.6;
}

.send-btn {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: var(--grad-brand);
  color: var(--brand-ink);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: opacity 0.15s;
}

.send-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.send-btn:active:not(:disabled) {
  opacity: 0.8;
}

.stop-btn {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background-color: var(--danger-fill);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
</style>
