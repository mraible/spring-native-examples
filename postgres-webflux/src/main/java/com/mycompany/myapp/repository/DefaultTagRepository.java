package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Tag;
import com.mycompany.myapp.repository.rowmapper.TagRowMapper;
import com.mycompany.myapp.service.EntityManager;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoin;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.relational.core.query.Criteria.where;

/**
 * Spring Data SQL reactive custom repository implementation for the Tag entity.
 */
@Repository
public class DefaultTagRepository {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;
    private final TagRowMapper tagMapper;
    private final TagRepository repository;

    private static final Table entityTable = Table.aliased("tag", EntityManager.ENTITY_ALIAS);

    public DefaultTagRepository(TagRepository repository, R2dbcEntityTemplate template, EntityManager entityManager, TagRowMapper tagMapper) {
        this.repository = repository;
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.tagMapper = tagMapper;
    }

    public Flux<Tag> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    public Flux<Tag> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<Tag> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = TagSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);

        String select = entityManager.createSelect(selectFrom, Tag.class, pageable, criteria);
        String alias = entityTable.getReferenceName().getReference();
        String selectWhere = Optional
            .ofNullable(criteria)
            .map(crit ->
                new StringBuilder(select)
                    .append(" ")
                    .append("WHERE")
                    .append(" ")
                    .append(alias)
                    .append(".")
                    .append(crit.toString())
                    .toString()
            )
            .orElse(select); // TODO remove once https://github.com/spring-projects/spring-data-jdbc/issues/907 will be fixed
        return db.sql(selectWhere).map(this::process);
    }

    public Flux<Tag> findAll() {
        return findAllBy(null, null);
    }

    public Mono<Tag> findById(Long id) {
        return createQuery(null, where("id").is(id)).one();
    }

    private Tag process(Row row, RowMetadata metadata) {
        Tag entity = tagMapper.apply(row, "e");
        return entity;
    }

    public <S extends Tag> Mono<S> insert(S entity) {
        return entityManager.insert(entity);
    }

    public <S extends Tag> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            return insert(entity);
        } else {
            return update(entity)
                .map(numberOfUpdates -> {
                    if (numberOfUpdates.intValue() <= 0) {
                        throw new IllegalStateException("Unable to update Tag with id = " + entity.getId());
                    }
                    return entity;
                });
        }
    }

    public Mono<Integer> update(Tag entity) {
        //fixme is this the proper way?
        return r2dbcEntityTemplate.update(entity).thenReturn(1);
    }

    public Mono<Boolean> existsById(Long id) {
        return repository.existsById(id);
    }

    public Mono<Long> count() {
        return repository.count();
    }

    public Mono<Void> deleteById(Long id) {
        return repository.deleteById(id);
    }
}

class TagSqlHelper {

    static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("name", table, columnPrefix + "_name"));

        return columns;
    }
}
