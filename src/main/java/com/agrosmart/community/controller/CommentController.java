package com.agrosmart.community.controller;

import com.agrosmart.common.dto.ApiResponse;
import com.agrosmart.community.dto.CommentRequest;
import com.agrosmart.community.dto.CommentResponse;
import com.agrosmart.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommunityService communityService;

    @PostMapping("/{postId}")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody CommentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Comment added",
                communityService.addComment(postId, userDetails.getUsername(), request)
        ));
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> getComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Comment fetched",
                communityService.getComment(commentId)
        ));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId,
            @RequestBody CommentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Comment updated",
                communityService.updateComment(commentId, userDetails.getUsername(), request)
        ));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId) {
        communityService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully"));
    }
}