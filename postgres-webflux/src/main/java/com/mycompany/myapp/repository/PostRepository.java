package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Post entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PostRepository extends R2dbcRepository<Post, Long>, PostRepositoryInternal {
    Flux<Post> findAllBy(Pageable pageable);

    @Override
    Mono<Post> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Post> findAllWithEagerRelationships();

    @Override
    Flux<Post> findAllWithEagerRelationships(Pageable page);

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

interface PostRepositoryInternal {
    <S extends Post> Mono<S> insert(S entity);
    <S extends Post> Mono<S> save(S entity);
    Mono<Integer> update(Post entity);

    Flux<Post> findAll();
    Mono<Post> findById(Long id);
    Flux<Post> findAllBy(Pageable pageable);
    Flux<Post> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Post> findOneWithEagerRelationships(Long id);

    Flux<Post> findAllWithEagerRelationships();

    Flux<Post> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
