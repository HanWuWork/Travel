package com.example.travelserver.controller;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.common.UserContext;
import com.example.travelserver.dto.social.CommentRequest;
import com.example.travelserver.dto.social.PostRequest;
import com.example.travelserver.service.social.PostService;
import com.example.travelserver.vo.PageVO;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.social.CommentVO;
import com.example.travelserver.vo.social.PostVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 游记社区接口。
 * 浏览类接口公开（游客可看，登录用户会返回是否已点赞）；发布/点赞/评论需登录。
 */
@RestController
@RequestMapping("/api/social")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /** 游记列表（分页） */
    @GetMapping("/posts")
    public Result<PageVO<PostVO>> list(@RequestParam(required = false) String city,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "false") boolean mine,
                                       @RequestParam(defaultValue = "new") String sort,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return Result.ok(postService.list(UserContext.getUserId(), city, keyword, mine, sort, page, size));
    }

    /** 我点赞过的游记 */
    @GetMapping("/posts/liked")
    public Result<List<PostVO>> liked() {
        return Result.ok(postService.likedPosts(requireLogin()));
    }

    /** 游记详情 */
    @GetMapping("/posts/{id}")
    public Result<PostVO> detail(@PathVariable Long id) {
        return Result.ok(postService.detail(UserContext.getUserId(), id));
    }

    /** 发布游记 */
    @PostMapping("/posts")
    public Result<PostVO> publish(@RequestBody PostRequest request) {
        return Result.ok(postService.publish(requireLogin(), request));
    }

    /** 编辑游记 */
    @PostMapping("/posts/{id}")
    public Result<PostVO> update(@PathVariable Long id, @RequestBody PostRequest request) {
        return Result.ok(postService.update(requireLogin(), id, request));
    }

    /** 删除游记 */
    @DeleteMapping("/posts/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        postService.delete(requireLogin(), id);
        return Result.ok();
    }

    /** 点赞/取消点赞 */
    @PostMapping("/posts/{id}/like")
    public Result<PostVO> like(@PathVariable Long id) {
        return Result.ok(postService.toggleLike(requireLogin(), id));
    }

    /** 评论列表 */
    @GetMapping("/posts/{id}/comments")
    public Result<List<CommentVO>> comments(@PathVariable Long id) {
        return Result.ok(postService.comments(id));
    }

    /** 发表评论 */
    @PostMapping("/comments")
    public Result<CommentVO> comment(@RequestBody CommentRequest request) {
        return Result.ok(postService.comment(requireLogin(), request));
    }

    /** 删除评论 */
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        postService.deleteComment(requireLogin(), id);
        return Result.ok();
    }

    /** 写操作需要登录（浏览接口公开） */
    private Long requireLogin() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }
}
