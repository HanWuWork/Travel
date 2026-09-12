package com.example.travelserver.repository;

import com.example.travelserver.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    // ---------- 最新排序（派生查询 + 分页） ----------

    Page<Post> findAllByOrderByCreateTimeDesc(Pageable pageable);

    Page<Post> findByCityOrderByCreateTimeDesc(String city, Pageable pageable);

    Page<Post> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    Page<Post> findByTitleContainingOrContentContainingOrderByCreateTimeDesc(String title, String content, Pageable pageable);

    // ---------- 最热排序（热度 = 点赞 + 评论*2，需自定义查询下推到数据库） ----------

    @Query("select p from Post p order by (coalesce(p.likeCount, 0) + coalesce(p.commentCount, 0) * 2) desc, p.createTime desc")
    Page<Post> findAllOrderByHotDesc(Pageable pageable);

    @Query("select p from Post p where p.city = :city order by (coalesce(p.likeCount, 0) + coalesce(p.commentCount, 0) * 2) desc, p.createTime desc")
    Page<Post> findByCityOrderByHotDesc(@Param("city") String city, Pageable pageable);

    @Query("select p from Post p where p.userId = :userId order by (coalesce(p.likeCount, 0) + coalesce(p.commentCount, 0) * 2) desc, p.createTime desc")
    Page<Post> findByUserIdOrderByHotDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("select p from Post p where p.title like :kw or p.content like :kw order by (coalesce(p.likeCount, 0) + coalesce(p.commentCount, 0) * 2) desc, p.createTime desc")
    Page<Post> searchOrderByHotDesc(@Param("kw") String kw, Pageable pageable);

    // ---------- 计数字段原子更新（避免并发读写丢失更新，也免去 count 重算） ----------

    @Modifying
    @Query("update Post p set p.viewCount = coalesce(p.viewCount, 0) + 1 where p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("update Post p set p.likeCount = coalesce(p.likeCount, 0) + 1 where p.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("update Post p set p.likeCount = greatest(coalesce(p.likeCount, 0) - 1, 0) where p.id = :id")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("update Post p set p.commentCount = coalesce(p.commentCount, 0) + 1 where p.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    @Modifying
    @Query("update Post p set p.commentCount = greatest(coalesce(p.commentCount, 0) - 1, 0) where p.id = :id")
    void decrementCommentCount(@Param("id") Long id);
}