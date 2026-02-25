package com.agrosmart.community.service;

import com.agrosmart.common.exception.NotAllowedException;
import com.agrosmart.community.dto.*;
import com.agrosmart.community.enums.PostCategory;
import com.agrosmart.community.exception.CommentNotFoundException;
import com.agrosmart.community.exception.PostNotFoundException;
import com.agrosmart.community.model.*;
import com.agrosmart.community.repository.CommentRepo;
import com.agrosmart.community.repository.PostRepo;
import com.agrosmart.identity.exception.UserNotFoundException;
import com.agrosmart.identity.model.User;
import com.agrosmart.identity.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final PostRepo postRepo;
    private final UserRepo userRepo;
    private final CommentRepo commentRepo;

    @Transactional
    public PostResponse createPost(String email, PostRequest request) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .author(user)
                .createdAt(LocalDateTime.now())
                .likesCount(0)
                .build();

        return mapToPostResponse(postRepo.save(post),email);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPosts(String currentUserEmail, PostCategory category, String search) {
        List<Post> posts;
        if (category != null && search != null) {
            posts = postRepo.findByCategoryAndSearch(category,search);
        } else if (category != null) {
            posts = postRepo.findByCategory(category);
        } else if (search != null) {
            posts = postRepo.searchPosts(search);
        } else {
            posts = postRepo.findAll();
        }

        return posts.stream()
                .map(post -> mapToPostResponse(post, currentUserEmail))
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleLike(Long postId, String email) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (post.getLikedBy().contains(user)) {
            post.getLikedBy().remove(user);
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
        } else {
            post.getLikedBy().add(user);
            post.setLikesCount(post.getLikesCount() + 1);
        }
        postRepo.save(post);
    }

    @Transactional
    public void deletePost(String currentUserEmail, Long postId){
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));
        User user = userRepo.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!post.getAuthor().getEmail().equals(currentUserEmail)) {
            throw new NotAllowedException("Not authorized to delete this post");
        }
        postRepo.deleteById(postId);
    }

    @Transactional
    public CommentResponse addComment(Long postId, String email, CommentRequest request) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with ID: " + postId));

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .author(user)
                .createdAt(LocalDateTime.now())
                .build();

        return mapToCommentResponse(commentRepo.save(comment));
    }

    @Transactional(readOnly = true)
    public CommentResponse getComment(Long commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));
        return mapToCommentResponse(comment);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, String email, CommentRequest request) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        if (!comment.getAuthor().getEmail().equals(email)) {
            throw new NotAllowedException("You are not authorized to edit this comment");
        }

        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        return mapToCommentResponse(commentRepo.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId, String email) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        if (!comment.getAuthor().getEmail().equals(email)) {
            throw new NotAllowedException("You are not authorized to delete this comment");
        }

        commentRepo.delete(comment);
    }

    private PostResponse mapToPostResponse(Post post, String currentUserEmail) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .authorName(post.getAuthor().getProfile()
                        .getFirstName() + " " + post.getAuthor().getProfile().getLastName())
                .authorEmail(post.getAuthor().getEmail())
                .createdAt(post.getCreatedAt())
                .likesCount(post.getLikesCount())
                .isLikedByCurrentUser(post.getLikedBy().stream()
                        .anyMatch(u -> u.getEmail().equals(currentUserEmail)))
                .comments(post.getComments() != null ? post.getComments().stream()
                        .map(this::mapToCommentResponse).collect(Collectors.toList()) : List.of())
                .build();
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorName(comment.getAuthor().getProfile().getFirstName()
                        + " " + comment.getAuthor().getProfile().getLastName())
                .authorEmail(comment.getAuthor().getEmail())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}