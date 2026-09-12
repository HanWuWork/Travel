package com.example.travelserver.service.social;

import com.example.travelserver.dto.social.CommentRequest;
import com.example.travelserver.dto.social.PostRequest;
import com.example.travelserver.vo.PageVO;
import com.example.travelserver.vo.social.CommentVO;
import com.example.travelserver.vo.social.PostVO;

import java.util.List;

/**
 * 游记社区服务：发布、浏览、点赞、评论
 */
public interface PostService {

    /** 游记列表（分页，支持 city/keyword/只看我的） */
    PageVO<PostVO> list(Long userId, String city, String keyword, boolean mine, String sort, int page, int size);

    /** 游记详情（浏览量 +1） */
    PostVO detail(Long userId, Long postId);

    /** 发布游记 */
    PostVO publish(Long userId, PostRequest request);

    /** 编辑游记（仅作者） */
    PostVO update(Long userId, Long postId, PostRequest request);

    /** 删除游记（仅作者） */
    void delete(Long userId, Long postId);

    /** 点赞/取消点赞，返回最新点赞数 */
    PostVO toggleLike(Long userId, Long postId);

    /** 评论列表 */
    List<CommentVO> comments(Long postId);

    /** 发表评论 */
    CommentVO comment(Long userId, CommentRequest request);

    /** 删除评论（仅本人） */
    void deleteComment(Long userId, Long commentId);

    /** 我点赞过的游记 */
    List<PostVO> likedPosts(Long userId);
}
