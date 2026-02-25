package com.agrosmart.community.controller;

import com.agrosmart.common.dto.ApiResponse;
import com.agrosmart.community.dto.PostRequest;
import com.agrosmart.community.dto.PostResponse;
import com.agrosmart.community.enums.PostCategory;
import com.agrosmart.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PostRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Post created successfully",
                communityService.createPost(userDetails.getUsername(), request)
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> getAllPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) PostCategory category,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(
                "Posts fetched successfully",
                communityService.getPosts(userDetails.getUsername(), category, search)
        ));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> toggleLike(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId) {
        communityService.toggleLike(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Like toggled successfully", null));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId
    ){
        communityService.deletePost(userDetails.getUsername(), postId);
        return ResponseEntity.ok(ApiResponse.success(
                "Post deleted successfully"
        ));
    }
}