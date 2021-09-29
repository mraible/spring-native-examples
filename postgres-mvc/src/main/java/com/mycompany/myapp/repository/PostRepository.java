package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data SQL repository for the Post entity.
 */
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(
        value = "select distinct post from Post post left join fetch post.tags",
        countQuery = "select count(distinct post) from Post post"
    )
    Page<Post> findAllWithEagerRelationships(Pageable pageable);

    @Query("select distinct post from Post post left join fetch post.tags")
    List<Post> findAllWithEagerRelationships();

    @Query("select post from Post post left join fetch post.tags where post.id =:id")
    Optional<Post> findOneWithEagerRelationships(@Param("id") Long id);
}
