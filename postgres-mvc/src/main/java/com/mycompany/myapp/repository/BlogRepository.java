package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Blog;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Blog entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    @Query("select blog from Blog blog where blog.user.login = ?#{principal.preferredUsername}")
    List<Blog> findByUserIsCurrentUser();
}
