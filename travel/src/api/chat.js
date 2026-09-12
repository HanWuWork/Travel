/**
 * AI 对话接口封装（统一契约）
 *
 * 两个能力：
 *  - streamChat：SSE 流式，事件名与 data.type 一致（meta / delta / done / error）
 *  - sendChat  ：非流式，一次性返回完整回复（流式不可用时的兜底）
 *
 * 请求体两者一致：{ message: 必填, sessionId?: 可选, history?: 可选 }
 */

/** 解析一个 SSE 事件块（可能含 event: / data: / :注释 多行） */
function parseEventBlock(block) {
  let eventName = ''
  const dataLines = []
  for (const rawLine of block.split('\n')) {
    const line = rawLine.replace(/\r$/, '')
    if (!line || line.startsWith(':')) continue // 空行或心跳注释，忽略
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trim())
    }
  }
  if (!dataLines.length) return null
  const raw = dataLines.join('\n')
  if (raw === '[DONE]') return { type: 'done' }
  try {
    const payload = JSON.parse(raw)
    // 兼容两种写法：有 event 名用 event 名，否则用 data.type
    if (!payload.type) payload.type = eventName || 'delta'
    return payload
  } catch (e) {
    return null
  }
}

/**
 * 发送一条消息并以流式方式接收 AI 回复
 *
 * @param {Object}   options
 * @param {string}   options.message    用户消息
 * @param {string}   [options.sessionId] 会话 ID（首轮不传，由 meta/done 事件返回）
 * @param {Array}    [options.history]   历史消息（可选，服务端按 sessionId 维护时可不传）
 * @param {AbortSignal} [options.signal] 中断信号（“停止生成”）
 * @param {(payload: object) => void} [options.onMeta]  流建立（含 sessionId/provider）
 * @param {(text: string) => void}    [options.onDelta] 正文增量
 * @param {(payload: object) => void} [options.onDone]  正常结束
 * @param {(message: string) => void} [options.onError] 错误
 */
export async function streamChat({ message, sessionId, history, signal, onMeta, onDelta, onDone, onError }) {
  let response
  try {
    response = await fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message, sessionId, history }),
      signal
    })
  } catch (e) {
    if (e.name === 'AbortError') return
    onError && onError('网络连接失败，请确认后端服务已启动（端口 1200）')
    return
  }

  // 非 2xx：参数校验/服务异常在建立 SSE 之前返回，body 是统一 Result JSON
  if (!response.ok || !response.body) {
    let msg = `请求失败（HTTP ${response.status}）`
    try {
      const data = await response.json()
      if (data && data.message) msg = data.message
    } catch (e) {
      // 忽略解析失败
    }
    onError && onError(msg)
    return
  }

  // 后端业务异常按项目约定返回 HTTP 200 + {code:400,message}，需要按 JSON 处理而非流
  const contentType = response.headers.get('content-type') || ''
  if (!contentType.includes('text/event-stream')) {
    let msg = 'AI 服务暂时不可用，请稍后再试'
    try {
      const data = await response.json()
      if (data && data.message) msg = data.message
    } catch (e) {
      // 忽略解析失败
    }
    onError && onError(msg)
    return
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let finished = false
  let lastSessionId = sessionId || ''

  const handleBlock = (block) => {
    const payload = parseEventBlock(block)
    if (!payload) return
    if (payload.sessionId) lastSessionId = payload.sessionId
    if (payload.type === 'meta') {
      onMeta && onMeta(payload)
    } else if (payload.type === 'delta') {
      if (payload.content) onDelta && onDelta(payload.content)
    } else if (payload.type === 'done') {
      finished = true
      onDone && onDone(payload)
    } else if (payload.type === 'error') {
      finished = true
      onError && onError(payload.message || 'AI 服务返回错误')
    }
  }

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      // SSE 事件以空行分隔
      const blocks = buffer.split('\n\n')
      buffer = blocks.pop()
      for (const b of blocks) {
        if (b.trim()) handleBlock(b)
      }
    }
    if (buffer.trim()) handleBlock(buffer)
  } catch (e) {
    if (e.name === 'AbortError') return
    onError && onError('读取 AI 回复时连接中断，请重试')
    return
  }

  // 连接正常关闭但未收到 done（如代理提前断开）时也要收尾，避免按钮卡在“停止”
  if (!finished) {
    onDone && onDone({ type: 'done', sessionId: lastSessionId, provider: 'unknown' })
  }
}

/**
 * 非流式对话（兜底）：一次性返回完整回复
 * @returns {Promise<{reply:string, sessionId:string, provider:string}>}
 */
export async function sendChat({ message, sessionId, history }) {
  const response = await fetch('/api/ai/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message, sessionId, history })
  })
  const data = await response.json().catch(() => null)
  if (!response.ok || !data || data.success === false) {
    throw new Error((data && data.message) || `请求失败（HTTP ${response.status}）`)
  }
  return data.data
}
