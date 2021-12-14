package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Post;
import com.mycompany.myapp.domain.Tag;
import com.mycompany.myapp.repository.rowmapper.BlogRowMapper;
import com.mycompany.myapp.repository.rowmapper.PostRowMapper;
import com.mycompany.myapp.service.EntityManager;
import com.mycompany.myapp.service.EntityManager.LinkTable;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.time.Instant;
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
 * Spring Data SQL reactive custom repository implementation for the Post entity.
 */
@SuppressWarnings("unused")
@Component
class PostRepositoryInternalImpl extends SimpleR2dbcRepository<Post, Long> implements PostRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final BlogRowMapper blogMapper;
    private final PostRowMapper postMapper;

    private static final Table entityTable = Table.aliased("post", EntityManager.ENTITY_ALIAS);
    private static final Table blogTable = Table.aliased("blog", "blog");

    private static final EntityManager.LinkTable tagLink = new LinkTable("rel_post__tag", "post_id", "tag_id");

    public PostRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        BlogRowMapper blogMapper,
        PostRowMapper postMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Post.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.blogMapper = blogMapper;
        this.postMapper = postMapper;
    }

    @Override
    public Flux<Post> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<Post> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<Post> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = PostSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(BlogSqlHelper.getColumns(blogTable, "blog"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(blogTable)
            .on(Column.create("blog_id", entityTable))
            .equals(Column.create("id", blogTable));

        String select = entityManager.createSelect(selectFrom, Post.class, pageable, criteria);
        return db.sql(select).map(this::process);
    }

    @Override
    public Mono<Post> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Post> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Post> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Post process(Row row, RowMetadata metadata) {
        Post entity = postMapper.apply(row, "e");
        entity.setBlog(blogMapper.apply(row, "blog"));
        return entity;
    }

    protected <S extends Post> Mono<S> updateRelations(S entity) {
        Mono<Void> result = entityManager.updateLinkTable(tagLink, entity.getId(), entity.getTags().stream().map(Tag::getId)).then();
        return result.thenReturn(entity);
    }

    protected Mono<Void> deleteRelations(Long entityId) {
        return entityManager.deleteFromLinkTable(tagLink, entityId);
    }
}
