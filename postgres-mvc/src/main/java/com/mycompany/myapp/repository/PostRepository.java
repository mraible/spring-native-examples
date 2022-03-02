package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Post;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Post entity.
 */
@Repository
public interface PostRepository extends PostRepositoryWithBagRelationships, JpaRepository<Post, Long> {
    default Optional<Post> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findOneWithToOneRelationships(id));
    }

    default List<Post> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships());
    }

    default Page<Post> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships(pageable));
    }

    @Query(
        value = "select distinct post from Post post left join fetch post.blog",
        countQuery = "select count(distinct post) from Post post"
    )
    Page<Post> findAllWithToOneRelationships(Pageable pageable);

    @Query("select distinct post from Post post left join fetch post.blog")
    List<Post> findAllWithToOneRelationships();

    @Query("select post from Post post left join fetch post.blog where post.id =:id")
    Optional<Post> findOneWithToOneRelationships(@Param("id") Long id);
}
