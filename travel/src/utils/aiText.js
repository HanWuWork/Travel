/**
 * AI 回复文本渲染（轻量 Markdown → 安全 HTML）
 *
 * 目标：让 AI 输出在气泡里可直接阅读，且**不出现 Markdown 符号**（尤其是 `*`）。
 * 做法：先转义 HTML，再把常见 Markdown 语义转成真实排版（加粗、斜体、标题、列表、代码），
 *      最后清理残留符号。因此输出的 DOM 文本中不会再有 `*`、`#` 等分隔符。
 *
 * 安全：先对原文做 HTML 转义，转换只引入受控标签，可安全用于 v-html。
 */

const ESCAPE_MAP = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#39;'
}

function escapeHtml(text) {
  return String(text).replace(/[&<>"']/g, (ch) => ESCAPE_MAP[ch])
}

/**
 * 把 AI 回复文本渲染为可读 HTML
 * @param {string} raw 原始回复（可能含 Markdown 符号）
 * @returns {string} 安全 HTML（可配合 v-html 使用）
 */
export function renderAiText(raw) {
  if (raw === null || raw === undefined) return ''
  let t = escapeHtml(raw)

  // 1) 代码块 ```...``` （含语言标识行）
  t = t.replace(/```[a-zA-Z0-9_-]*\n?([\s\S]*?)```/g, (m, code) => {
    return `\n<pre class="ai-code">${code.replace(/\n$/, '')}</pre>\n`
  })

  // 2) 行内代码 `x`
  t = t.replace(/`([^`\n]+)`/g, '<code class="ai-code-inline">$1</code>')

  // 3) 加粗 **x** / __x__ （先于斜体，避免被吃掉）
  t = t.replace(/\*\*([^*\n]+)\*\*/g, '<strong>$1</strong>')
  t = t.replace(/__([^_\n]+)__/g, '<strong>$1</strong>')

  // 4) 斜体 *x* / _x_
  t = t.replace(/(^|[^*\w])\*([^*\n]+)\*(?!\*)/g, '$1<em>$2</em>')
  t = t.replace(/(^|[^_\w])_([^_\n]+)_(?!_)/g, '$1<em>$2</em>')

  // 5) 标题 ###### x → 整行加粗
  t = t.replace(/^[ \t]*#{1,6}[ \t]*(.+)$/gm, '<strong>$1</strong>')

  // 6) 无序列表行首 * / - / + → 圆点符号
  t = t.replace(/^[ \t]*[*\-+][ \t]+/gm, '• ')

  // 7) 有序列表：统一为 "1. " 形式
  t = t.replace(/^[ \t]*(\d+)[.)][ \t]+/gm, '$1. ')

  // 8) 引用 > 去掉符号，保留内容
  t = t.replace(/^[ \t]*&gt;[ \t]?/gm, '')

  // 9) 分隔线 ---/***/___ → 去掉（气泡里不需要）
  t = t.replace(/^[ \t]*([-*_])[ \t]*\1[ \t]*\1[ \t]*$/gm, '')

  // 10) 链接 [文字](url) → 文字（避免在气泡里出现括号与符号）
  t = t.replace(/\[([^\]]+)\]\((https?:\/\/[^)\s]+)\)/g, '$1')

  // 11) 兜底清理：删除残留的 Markdown 星号（需求：输出中不要 * 分隔符）
  t = t.replace(/\*/g, '')

  // 12) 压缩多余空行，并去掉首尾空白
  t = t.replace(/\n{3,}/g, '\n\n').trim()

  return t
}

/**
 * 纯文本形式（用于标题/摘要等不能放 HTML 的场景）：去除所有 Markdown 符号
 */
export function toPlainText(raw) {
  if (!raw) return ''
  return String(raw)
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/`([^`]*)`/g, '$1')
    .replace(/\*\*([^*]*)\*\*/g, '$1')
    .replace(/[*_#>]/g, '')
    .replace(/^[ \t]*[-+][ \t]+/gm, '')
    .replace(/\s+/g, ' ')
    .trim()
}
