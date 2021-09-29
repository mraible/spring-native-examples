package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Post entity.
 */
@SuppressWarnings("unused")
@Repository
interface PostRepository extends R2dbcRepository<Post, Long> {
    Flux<Post> findAllBy(Pageable pageable);

    @Override
    Mono<Void> deleteById(Long id);

    @Query("SELECT * FROM post entity WHERE entity.blog_id = :id")
    Flux<Post> findByBlog(Long id);

    @Query("SELECT * FROM post entity WHERE entity.blog_id IS NULL")
    Flux<Post> findAllWhereBlogIsNull();

    @Query("SELECT entity.* FROM post entity JOIN rel_post__tag joinTable ON entity.id = joinTable.post_id WHERE joinTable.tag_id = :id")
    Flux<Post> findByTag(Long id);

    // just to avoid having unambigous methods
    @Override
    Flux<Post> findAll();

    @Override
    Mono<Post> findById(Long id);

    @Override
    <S extends Post> Mono<S> save(S entity);
}
