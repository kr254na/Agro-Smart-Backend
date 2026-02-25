package com.agrosmart.community.dto;

import com.agrosmart.community.enums.PostCategory;
import lombok.Data;

@Data
public class PostRequest {
    private String title;
    private String content;
    private PostCategory category;
}