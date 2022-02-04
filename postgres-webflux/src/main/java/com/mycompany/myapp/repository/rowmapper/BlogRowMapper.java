package com.mycompany.myapp.repository.rowmapper;

import com.mycompany.myapp.domain.Blog;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Blog}, with proper type conversions.
 */
@Service
public class BlogRowMapper implements BiFunction<Row, String, Blog> {

    private final ColumnConverter converter;

    public BlogRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Blog} stored in the database.
     */
    @Override
    public Blog apply(Row row, String prefix) {
        Blog entity = new Blog();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setHandle(converter.fromRow(row, prefix + "_handle", String.class));
        entity.setUserId(converter.fromRow(row, prefix + "_user_id", String.class));
        return entity;
    }
}
