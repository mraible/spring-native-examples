package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Post;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.hibernate.annotations.QueryHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class PostRepositoryWithBagRelationshipsImpl implements PostRepositoryWithBagRelationships {

    @Autowired
    private EntityManager entityManager;

    @Override
    public Optional<Post> fetchBagRelationships(Optional<Post> post) {
        return post.map(this::fetchTags);
    }

    @Override
    public Page<Post> fetchBagRelationships(Page<Post> posts) {
        return new PageImpl<>(fetchBagRelationships(posts.getContent()), posts.getPageable(), posts.getTotalElements());
    }

    @Override
    public List<Post> fetchBagRelationships(List<Post> posts) {
        return Optional.of(posts).map(this::fetchTags).get();
    }

    Post fetchTags(Post result) {
        return entityManager
            .createQuery("select post from Post post left join fetch post.tags where post is :post", Post.class)
            .setParameter("post", result)
            .setHint(QueryHints.PASS_DISTINCT_THROUGH, false)
            .getSingleResult();
    }

    List<Post> fetchTags(List<Post> posts) {
        return entityManager
            .createQuery("select distinct post from Post post left join fetch post.tags where post in :posts", Post.class)
            .setParameter("posts", posts)
            .setHint(QueryHints.PASS_DISTINCT_THROUGH, false)
            .getResultList();
    }
}
