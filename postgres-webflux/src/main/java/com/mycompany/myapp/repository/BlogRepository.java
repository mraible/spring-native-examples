package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Blog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Blog entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BlogRepository extends ReactiveCrudRepository<Blog, Long>, BlogRepositoryInternal {
    @Override
    Mono<Blog> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Blog> findAllWithEagerRelationships();

    @Override
    Flux<Blog> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM blog entity WHERE entity.user_id = :id")
    Flux<Blog> findByUser(Long id);

    @Query("SELECT * FROM blog entity WHERE entity.user_id IS NULL")
    Flux<Blog> findAllWhereUserIsNull();

    @Override
    <S extends Blog> Mono<S> save(S entity);

    @Override
    Flux<Blog> findAll();

    @Override
    Mono<Blog> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface BlogRepositoryInternal {
    <S extends Blog> Mono<S> save(S entity);

    Flux<Blog> findAllBy(Pageable pageable);

    Flux<Blog> findAll();

    Mono<Blog> findById(Long id);

    Flux<Blog> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Blog> findOneWithEagerRelationships(Long id);

    Flux<Blog> findAllWithEagerRelationships();

    Flux<Blog> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
