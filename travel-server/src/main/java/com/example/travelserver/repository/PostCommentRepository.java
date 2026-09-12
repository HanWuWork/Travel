package com.example.travelserver.repository;

import com.example.travelserver.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostIdOrderByCreateTimeAsc(Long postId);

}
