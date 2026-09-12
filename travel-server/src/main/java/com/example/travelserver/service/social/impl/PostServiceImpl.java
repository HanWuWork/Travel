package com.example.travelserver.service.social.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.dto.social.CommentRequest;
import com.example.travelserver.dto.social.PostRequest;
import com.example.travelserver.entity.Post;
import com.example.travelserver.entity.PostComment;
import com.example.travelserver.entity.PostLike;
import com.example.travelserver.entity.User;
import com.example.travelserver.repository.PostCommentRepository;
import com.example.travelserver.repository.PostLikeRepository;
import com.example.travelserver.repository.PostRepository;
import com.example.travelserver.repository.UserRepository;
import com.example.travelserver.service.social.PostService;
import com.example.travelserver.vo.PageVO;
import com.example.travelserver.vo.social.CommentVO;
import com.example.travelserver.vo.social.PostVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PostRepository postRepository;
    private final PostLikeRepository likeRepository;
    private final PostCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final com.example.travelserver.service.user.NotificationService notificationService;

    public PostServiceImpl(PostRepository postRepository,
                           PostLikeRepository likeRepository,
                           PostCommentRepository commentRepository,
                           UserRepository userRepository,
                           com.example.travelserver.service.user.NotificationService notificationService) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public PageVO<PostVO> list(Long userId, String city, String keyword, boolean mine, String sort, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        boolean hot = "hot".equals(sort);

        Page<Post> postPage;
        if (mine && userId != null) {
            postPage = hot ? postRepository.findByUserIdOrderByHotDesc(userId, pageable)
                    : postRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            postPage = hot ? postRepository.searchOrderByHotDesc("%" + kw + "%", pageable)
                    : postRepository.findByTitleContainingOrContentContainingOrderByCreateTimeDesc(kw, kw, pageable);
        } else if (city != null && !city.isBlank()) {
            postPage = hot ? postRepository.findByCityOrderByHotDesc(city, pageable)
                    : postRepository.findByCityOrderByCreateTimeDesc(city, pageable);
        } else {
            postPage = hot ? postRepository.findAllOrderByHotDesc(pageable)
                    : postRepository.findAllByOrderByCreateTimeDesc(pageable);
        }

        List<Post> posts = postPage.getContent();
        // 批量加载作者，避免逐条查询造成 N+1
        Map<Long, User> users = loadUsers(posts.stream().map(Post::getUserId));
        List<PostVO> list = posts.stream().map(p -> toVO(p, userId, false, users)).collect(Collectors.toList());
        return new PageVO<>(list, postPage.getTotalElements(), postPage.hasNext());
    }

    @Override
    @Transactional
    public PostVO detail(Long userId, Long postId) {
        Post post = mustGet(postId);
        // 原子自增，避免并发读改写丢失浏览数
        postRepository.incrementViewCount(postId);
        post.setViewCount(nz(post.getViewCount()) + 1);
        return toVO(post, userId, true, null);
    }

    @Override
    @Transactional
    public PostVO publish(Long userId, PostRequest request) {
        validate(request);
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent());
        post.setCity(request.getCity());
        post.setTags(request.getTags());
        post.setCover(request.getCover());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreateTime(LocalDateTime.now());
        post = postRepository.save(post);
        return toVO(post, userId, true, null);
    }

    @Override
    @Transactional
    public PostVO update(Long userId, Long postId, PostRequest request) {
        Post post = mustGet(postId);
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能编辑自己的游记");
        }
        validate(request);
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent());
        post.setCity(request.getCity());
        post.setTags(request.getTags());
        if (request.getCover() != null) {
            post.setCover(request.getCover());
        }
        return toVO(postRepository.save(post), userId, true, null);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long postId) {
        Post post = mustGet(postId);
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的游记");
        }
        postRepository.delete(post);
    }

    @Override
    @Transactional
    public PostVO toggleLike(Long userId, Long postId) {
        Post post = mustGet(postId);
        boolean nowLiked;
        var existing = likeRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            nowLiked = false;
            postRepository.decrementLikeCount(postId);
        } else {
            likeRepository.save(new PostLike(postId, userId));
            nowLiked = true;
            postRepository.incrementLikeCount(postId);
        }
        post.setLikeCount(Math.max(0, nz(post.getLikeCount()) + (nowLiked ? 1 : -1)));
        if (nowLiked) {
            notificationService.notify(post.getUserId(), userId, com.example.travelserver.entity.Notification.LIKE,
                    "你的游记收到了一个赞", post.getTitle(), "/posts/" + postId);
        }
        return toVO(post, userId, false, null);
    }

    @Override
    public List<CommentVO> comments(Long postId) {
        List<PostComment> comments = commentRepository.findByPostIdOrderByCreateTimeAsc(postId);
        Map<Long, User> users = loadUsers(comments.stream().map(PostComment::getUserId));
        Map<Long, PostComment> byId = comments.stream()
                .collect(Collectors.toMap(PostComment::getId, Function.identity(), (a, b) -> a));
        List<CommentVO> result = new ArrayList<>();
        for (PostComment c : comments) {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setPostId(c.getPostId());
            vo.setUserId(c.getUserId());
            User u = users.get(c.getUserId());
            vo.setAuthorName(u == null ? "用户" : (u.getNickname() == null ? u.getUsername() : u.getNickname()));
            vo.setAuthorAvatar(u == null ? null : u.getAvatar());
            vo.setContent(c.getContent());
            vo.setReplyTo(c.getReplyTo());
            if (c.getReplyTo() != null && byId.containsKey(c.getReplyTo())) {
                User ru = users.get(byId.get(c.getReplyTo()).getUserId());
                vo.setReplyToName(ru == null ? null
                        : (ru.getNickname() == null ? ru.getUsername() : ru.getNickname()));
            }
            vo.setCreateTime(c.getCreateTime() == null ? null : c.getCreateTime().format(FMT));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional
    public CommentVO comment(Long userId, CommentRequest request) {
        if (request.getPostId() == null) {
            throw new BusinessException(400, "缺少游记 ID");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BusinessException(400, "评论内容不能为空");
        }
        if (request.getContent().length() > 500) {
            throw new BusinessException(400, "评论最多 500 字");
        }
        Post post = mustGet(request.getPostId());
        PostComment c = new PostComment();
        c.setPostId(post.getId());
        c.setUserId(userId);
        c.setContent(request.getContent().trim());
        c.setReplyTo(request.getReplyTo());
        c.setCreateTime(LocalDateTime.now());
        PostComment saved = commentRepository.save(c);
        postRepository.incrementCommentCount(post.getId());


        // 通知游记作者
        notificationService.notify(post.getUserId(), userId,
                com.example.travelserver.entity.Notification.COMMENT,
                "你的游记收到了新评论",
                abbreviate(request.getContent().trim()), "/posts/" + post.getId());
        // 若为回复，同时通知被回复者
        if (c.getReplyTo() != null) {
            commentRepository.findById(c.getReplyTo()).ifPresent(parent -> {
                if (!parent.getUserId().equals(post.getUserId())) {
                    notificationService.notify(parent.getUserId(), userId,
                            com.example.travelserver.entity.Notification.COMMENT,
                            "有人回复了你的评论",
                            abbreviate(request.getContent().trim()), "/posts/" + post.getId());
                }
            });
        }

        Long savedId = saved.getId();
        return comments(post.getId()).stream()
                .filter(v -> v.getId().equals(savedId))
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        PostComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(404, "评论不存在"));
        if (!c.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的评论");
        }
        commentRepository.delete(c);
        postRepository.decrementCommentCount(c.getPostId());
    }

    @Override
    public List<PostVO> likedPosts(Long userId) {
        List<Long> ids = likeRepository.findByUserId(userId).stream()
                .map(PostLike::getPostId)
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return List.of();
        }
        // 一次 IN 查询批量取游记，再批量取作者，避免 N+1
        List<Post> posts = new ArrayList<>(postRepository.findAllById(ids));
        posts.sort(Comparator.comparing(Post::getCreateTime).reversed());
        Map<Long, User> users = loadUsers(posts.stream().map(Post::getUserId));
        return posts.stream().map(p -> toVO(p, userId, false, users)).collect(Collectors.toList());
    }

    private void validate(PostRequest request) {
        if (request == null || request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BusinessException(400, "标题不能为空");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BusinessException(400, "正文不能为空");
        }
        if (request.getTitle().length() > 100) {
            throw new BusinessException(400, "标题最多 100 字");
        }
    }

    private Post mustGet(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(404, "游记不存在"));
    }

    private Map<Long, User> loadUsers(java.util.stream.Stream<Long> ids) {
        List<Long> list = ids.distinct().collect(Collectors.toList());
        return userRepository.findAllById(list).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private PostVO toVO(Post post, Long userId, boolean withContent, Map<Long, User> userMap) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        User author = userMap != null ? userMap.get(post.getUserId()) : null;
        if (author == null) {
            // 单对象场景未预加载时回退为单次查询（不构成 N+1）
            author = userRepository.findById(post.getUserId()).orElse(null);
        }
        if (author != null) {
            vo.setAuthorName(author.getNickname() == null ? author.getUsername() : author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }
        vo.setTitle(post.getTitle());
        vo.setContent(withContent ? post.getContent() : null);
        vo.setSummary(summary(post.getContent()));
        vo.setCity(post.getCity());
        if (post.getTags() != null && !post.getTags().isBlank()) {
            vo.setTagList(Arrays.stream(post.getTags().split("[,，\\s]+"))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList()));
        } else {
            vo.setTagList(List.of());
        }
        vo.setCover(post.getCover());
        vo.setViewCount(nz(post.getViewCount()));
        vo.setLikeCount(nz(post.getLikeCount()));
        vo.setCommentCount(nz(post.getCommentCount()));
        vo.setLiked(userId != null && likeRepository.existsByPostIdAndUserId(post.getId(), userId));
        vo.setCreateTime(post.getCreateTime() == null ? null : post.getCreateTime().format(FMT));
        return vo;
    }

    private String summary(String content) {
        if (content == null) {
            return "";
        }
        String flat = content.replaceAll("\\s+", " ").trim();
        return flat.length() > 60 ? flat.substring(0, 60) + "..." : flat;
    }

    private String abbreviate(String content) {
        if (content == null) {
            return "";
        }
        String flat = content.replaceAll("\\s+", " ").trim();
        return flat.length() > 40 ? flat.substring(0, 40) + "..." : flat;
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
