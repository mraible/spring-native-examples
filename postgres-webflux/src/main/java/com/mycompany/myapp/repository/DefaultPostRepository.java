package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Post;
import com.mycompany.myapp.domain.Tag;
import com.mycompany.myapp.repository.rowmapper.BlogRowMapper;
import com.mycompany.myapp.repository.rowmapper.PostRowMapper;
import com.mycompany.myapp.service.EntityManager;
import com.mycompany.myapp.service.EntityManager.LinkTable;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
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
import static org.springframework.data.relational.core.query.Query.query;

/**
 * Spring Data SQL reactive custom repository implementation for the Post entity.
 */
@Repository
public class DefaultPostRepository {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;
    private final PostRepository repository;

    private final BlogRowMapper blogMapper;
    private final PostRowMapper postMapper;

    private static final Table entityTable = Table.aliased("post", EntityManager.ENTITY_ALIAS);
    private static final Table blogTable = Table.aliased("blog", "blog");

    private static final EntityManager.LinkTable tagLink = new LinkTable("rel_post__tag", "post_id", "tag_id");

    public DefaultPostRepository(
        PostRepository repository,
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        BlogRowMapper blogMapper,
        PostRowMapper postMapper
    ) {
        this.repository = repository;
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.blogMapper = blogMapper;
        this.postMapper = postMapper;
    }

    public Flux<Post> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

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

    public Flux<Post> findAll() {
        return findAllBy(null, null);
    }

    public Mono<Post> findById(Long id) {
        return createQuery(null, where("id").is(id)).one();
    }

    public Mono<Post> findOneWithEagerRelationships(Long id) {
        return repository.findById(id);
    }

    public Flux<Post> findAllWithEagerRelationships(Pageable page) {
        return repository.findAllBy(page);
    }

    private Post process(Row row, RowMetadata metadata) {
        Post entity = postMapper.apply(row, "e");
        entity.setBlog(blogMapper.apply(row, "blog"));
        return entity;
    }

    public <S extends Post> Mono<S> insert(S entity) {
        return entityManager.insert(entity);
    }

    public <S extends Post> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            return insert(entity).flatMap(savedEntity -> updateRelations(savedEntity));
        } else {
            return update(entity)
                .map(numberOfUpdates -> {
                    if (numberOfUpdates.intValue() <= 0) {
                        throw new IllegalStateException("Unable to update Post with id = " + entity.getId());
                    }
                    return entity;
                })
                .then(updateRelations(entity));
        }
    }

    public Mono<Integer> update(Post entity) {
        //fixme is this the proper way?
        return r2dbcEntityTemplate.update(entity).thenReturn(1);
    }

    public Mono<Void> deleteById(Long entityId) {
        return deleteRelations(entityId)
            .then(r2dbcEntityTemplate.delete(Post.class).matching(query(where("id").is(entityId))).all().then());
    }

    protected <S extends Post> Mono<S> updateRelations(S entity) {
        Mono<Void> result = entityManager.updateLinkTable(tagLink, entity.getId(), entity.getTags().stream().map(Tag::getId)).then();
        return result.thenReturn(entity);
    }

    protected Mono<Void> deleteRelations(Long entityId) {
        return entityManager.deleteFromLinkTable(tagLink, entityId);
    }

    public Mono<Boolean> existsById(Long id) {
        return repository.existsById(id);
    }

    public Mono<Long> count() {
        return repository.count();
    }
}

class PostSqlHelper {

    static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("title", table, columnPrefix + "_title"));
        columns.add(Column.aliased("content", table, columnPrefix + "_content"));
        columns.add(Column.aliased("date", table, columnPrefix + "_date"));

        columns.add(Column.aliased("blog_id", table, columnPrefix + "_blog_id"));
        return columns;
    }
}
