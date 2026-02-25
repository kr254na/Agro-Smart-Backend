package com.agrosmart.community.dto;

import com.agrosmart.community.enums.PostCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private PostCategory category;
    private String authorName;
    private String authorEmail;
    private LocalDateTime createdAt;
    private int likesCount;
    @JsonProperty("isLikedByCurrentUser")
    private boolean isLikedByCurrentUser;
    private List<CommentResponse> comments;
}