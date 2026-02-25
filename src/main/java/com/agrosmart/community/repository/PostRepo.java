package com.agrosmart.community.repository;

import com.agrosmart.community.enums.PostCategory;
import com.agrosmart.community.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepo extends JpaRepository<Post,Long> {

    @Query("SELECT p FROM Post p WHERE p.category = :category AND (p.title LIKE %:search% OR p.content LIKE %:search%)")
    List<Post> findByCategoryAndSearch(
            @Param("category") PostCategory category,
            @Param("search") String search
    );

    List<Post> findByCategory(PostCategory category);

    @Query("SELECT p FROM Post p WHERE p.category = :category AND (p.title LIKE %:search% OR p.content LIKE %:search%)")
    List<Post> findByCategoryAndSearch(@Param("category") String category, @Param("search") String search);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:search% OR p.content LIKE %:search%")
    List<Post> searchPosts(@Param("search") String search);
}