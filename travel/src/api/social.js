import { request } from '../utils/auth'

/** 游记列表（公开；传 token 会返回是否已点赞；分页返回 { list, total, hasMore }） */
export function listPosts({ city, keyword, mine, sort, page = 0, size = 10 } = {}) {
  const params = new URLSearchParams()
  if (city) params.set('city', city)
  if (keyword) params.set('keyword', keyword)
  if (mine) params.set('mine', 'true')
  if (sort) params.set('sort', sort)
  params.set('page', String(page))
  params.set('size', String(size))
  return request(`/api/social/posts?${params.toString()}`)
}

/** 我点赞过的游记（需登录） */
export function listLikedPosts() {
  return request('/api/social/posts/liked')
}

/** 游记详情（浏览量+1） */
export function getPost(id) {
  return request(`/api/social/posts/${id}`)
}

/** 发布游记（需登录） */
export function publishPost(data) {
  return request('/api/social/posts', { method: 'POST', body: JSON.stringify(data) })
}

/** 编辑游记（需登录） */
export function updatePost(id, data) {
  return request(`/api/social/posts/${id}`, { method: 'POST', body: JSON.stringify(data) })
}

/** 删除游记（需登录） */
export function deletePost(id) {
  return request(`/api/social/posts/${id}`, { method: 'DELETE' })
}

/** 点赞/取消点赞（需登录） */
export function toggleLike(id) {
  return request(`/api/social/posts/${id}/like`, { method: 'POST' })
}

/** 评论列表（公开） */
export function listComments(postId) {
  return request(`/api/social/posts/${postId}/comments`)
}

/** 发表评论（需登录） */
export function addComment({ postId, content, replyTo }) {
  return request('/api/social/comments', {
    method: 'POST',
    body: JSON.stringify({ postId, content, replyTo })
  })
}

/** 删除评论（需登录） */
export function deleteComment(id) {
  return request(`/api/social/comments/${id}`, { method: 'DELETE' })
}
