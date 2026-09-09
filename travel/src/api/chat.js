/**
 * AI 对话流式接口封装
 *
 * 后端 SSE 事件协议（data 均为 JSON）：
 *  - { "type": "delta", "content": "片段" }   正文增量，可多次
 *  - { "type": "done",  "sessionId": "..." } 回复结束
 *  - { "type": "error", "message": "..." }   发生错误
 */

/**
 * 发送一条消息并以流式方式接收 AI 回复
 *
 * @param {Object}   options
 * @param {string}   options.message  用户消息
 * @param {string}   [options.sessionId] 会话 ID（首轮不传，由 done 事件返回后续复用）
 * @param {AbortSignal} [options.signal] 中断信号（用于“停止生成”）
 * @param {(text: string) => void} [options.onDelta] 正文增量回调
 * @param {(payload: object) => void} [options.onDone] 流结束回调（含 sessionId）
 * @param {(message: string) => void} [options.onError] 错误回调
 */
export async function streamChat({ message, sessionId, signal, onDelta, onDone, onError }) {
  let response
  try {
    response = await fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message, sessionId }),
      signal
    })
  } catch (e) {
    if (e.name === 'AbortError') return
    onError && onError('网络连接失败，请确认后端服务已启动（端口 1200）')
    return
  }

  // 非 2xx：后端在建立 SSE 前抛出的业务异常，返回的是 Result JSON
  if (!response.ok || !response.body) {
    let msg = `请求失败（HTTP ${response.status}）`
    try {
      const data = await response.json()
      if (data && data.message) msg = data.message
    } catch (e) {
      // 忽略 JSON 解析失败，使用默认提示
    }
    onError && onError(msg)
    return
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let finished = false
  let lastSessionId = sessionId || ''

  const handleEvent = (rawEvent) => {
    // 一个事件块可能含多行，只取 data: 开头的行
    const dataLine = rawEvent.split('\n').find((l) => l.startsWith('data:'))
    if (!dataLine) return
    let payload
    try {
      payload = JSON.parse(dataLine.slice(5).trim())
    } catch (e) {
      return
    }
    if (payload.type === 'delta' && payload.content) {
      onDelta && onDelta(payload.content)
    } else if (payload.type === 'done') {
      finished = true
      if (payload.sessionId) lastSessionId = payload.sessionId
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
      // SSE 事件以空行（\n\n）分隔
      const events = buffer.split('\n\n')
      buffer = events.pop()
      for (const evt of events) {
        if (evt.trim()) handleEvent(evt)
      }
    }
    // 冲刷缓冲区中残留的最后一个事件
    if (buffer.trim()) handleEvent(buffer)
  } catch (e) {
    if (e.name === 'AbortError') return
    onError && onError('读取 AI 回复时连接中断，请重试')
    return
  }

  // 兜底：连接已正常关闭但没收到 done 事件（如代理提前断开），
  // 也要通知调用方结束，避免按钮一直停留在“停止”状态
  if (!finished) {
    onDone && onDone({ sessionId: lastSessionId, provider: 'unknown' })
  }
}
