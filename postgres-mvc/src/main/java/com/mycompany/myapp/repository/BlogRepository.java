package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Blog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Blog entity.
 */
@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    @Query("select blog from Blog blog where blog.user.login = ?#{principal.preferredUsername}")
    List<Blog> findByUserIsCurrentUser();

    default Optional<Blog> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Blog> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Blog> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct blog from Blog blog left join fetch blog.user",
        countQuery = "select count(distinct blog) from Blog blog"
    )
    Page<Blog> findAllWithToOneRelationships(Pageable pageable);

    @Query("select distinct blog from Blog blog left join fetch blog.user")
    List<Blog> findAllWithToOneRelationships();

    @Query("select blog from Blog blog left join fetch blog.user where blog.id =:id")
    Optional<Blog> findOneWithToOneRelationships(@Param("id") Long id);
}
