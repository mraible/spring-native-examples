package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Blog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Blog entity.
 */
@SuppressWarnings("unused")
interface BlogRepository extends R2dbcRepository<Blog, Long> {
    @Query("SELECT * FROM blog entity WHERE entity.user_id = :id")
    Flux<Blog> findByUser(Long id);

    @Query("SELECT * FROM blog entity WHERE entity.user_id IS NULL")
    Flux<Blog> findAllWhereUserIsNull();

    // just to avoid having unambigous methods
    @Override
    Flux<Blog> findAll();

    @Override
    Mono<Blog> findById(Long id);

    @Override
    <S extends Blog> Mono<S> save(S entity);
}
