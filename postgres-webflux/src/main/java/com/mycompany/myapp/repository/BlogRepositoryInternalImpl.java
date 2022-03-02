package com.mycompany.myapp.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import com.mycompany.myapp.domain.Blog;
import com.mycompany.myapp.repository.rowmapper.BlogRowMapper;
import com.mycompany.myapp.repository.rowmapper.UserRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive custom repository implementation for the Blog entity.
 */
@SuppressWarnings("unused")
@Component
class BlogRepositoryInternalImpl extends SimpleR2dbcRepository<Blog, Long> implements BlogRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final UserRowMapper userMapper;
    private final BlogRowMapper blogMapper;

    private static final Table entityTable = Table.aliased("blog", EntityManager.ENTITY_ALIAS);
    private static final Table userTable = Table.aliased("jhi_user", "e_user");

    public BlogRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        UserRowMapper userMapper,
        BlogRowMapper blogMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Blog.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.userMapper = userMapper;
        this.blogMapper = blogMapper;
    }

    @Override
    public Flux<Blog> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<Blog> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<Blog> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = BlogSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(UserSqlHelper.getColumns(userTable, "user"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(userTable)
            .on(Column.create("user_id", entityTable))
            .equals(Column.create("id", userTable));

        String select = entityManager.createSelect(selectFrom, Blog.class, pageable, criteria);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Blog> findAll() {
        return findAllBy(null, null);
    }

    @Override
    public Mono<Blog> findById(Long id) {
        return createQuery(null, where(EntityManager.ENTITY_ALIAS + ".id").is(id)).one();
    }

    @Override
    public Mono<Blog> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Blog> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Blog> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Blog process(Row row, RowMetadata metadata) {
        Blog entity = blogMapper.apply(row, "e");
        entity.setUser(userMapper.apply(row, "user"));
        return entity;
    }

    @Override
    public <S extends Blog> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
