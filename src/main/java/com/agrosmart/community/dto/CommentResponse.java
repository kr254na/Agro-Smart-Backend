package com.agrosmart.community.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String authorName;
    private String authorEmail;
    private LocalDateTime createdAt;
}