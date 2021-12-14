package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Tag entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TagRepository extends ReactiveCrudRepository<Tag, Long>, TagRepositoryInternal {
    Flux<Tag> findAllBy(Pageable pageable);

    // just to avoid having unambigous methods
    @Override
    Flux<Tag> findAll();

    @Override
    Mono<Tag> findById(Long id);

    @Override
    <S extends Tag> Mono<S> save(S entity);
}

interface TagRepositoryInternal {
    <S extends Tag> Mono<S> save(S entity);
    Flux<Tag> findAllBy(Pageable pageable);
    Flux<Tag> findAllBy(Pageable pageable, Criteria criteria);
}
