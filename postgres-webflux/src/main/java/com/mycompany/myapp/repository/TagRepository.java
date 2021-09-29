package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Tag entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TagRepository extends R2dbcRepository<Tag, Long> {
    Flux<Tag> findAllBy(Pageable pageable);

    // just to avoid having unambigous methods
    @Override
    Flux<Tag> findAll();

    @Override
    Mono<Tag> findById(Long id);

    @Override
    <S extends Tag> Mono<S> save(S entity);
}
