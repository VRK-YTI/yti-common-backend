package fi.vm.yti.common.opensearch;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOptionsBuilders;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.mapping.FieldType;
import org.opensearch.client.opensearch._types.query_dsl.*;

import fi.vm.yti.common.enums.Status;

import java.util.Collection;

public class QueryFactoryUtils {

    private QueryFactoryUtils() {
        // Utility class
    }

    public static final int DEFAULT_PAGE_FROM = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int INTERNAL_SEARCH_PAGE_SIZE = 10000;
    public static final String DEFAULT_SORT_LANG = "fi";

    public static int pageFrom(Integer pageFrom) {
        if (pageFrom == null || pageFrom <= 0) {
            return DEFAULT_PAGE_FROM;
        } else {
            return pageFrom;
        }
    }

    public static int pageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        } else {
            return pageSize;
        }
    }

    public static String getSortLang(String sortLang) {
        if (sortLang == null || sortLang.isBlank()) {
            return DEFAULT_SORT_LANG;
        } else {
            return sortLang;
        }
    }

    public static SortOptions getLangSortOptions(String sortLang) {
        var builder = SortOptionsBuilders.field()
                .field("label." + QueryFactoryUtils.getSortLang(sortLang) + ".keyword")
                .order(SortOrder.Asc)
                .unmappedType(FieldType.Keyword)
                .build();
        return SortOptions.of(s -> s.field(builder));
    }

    // COMMON QUERIES

    public static Query hideDraftStatusQuery() {
        var termQuery = TermQuery.of(q -> q
                .field("status")
                .value(FieldValue.of(Status.DRAFT.name()))).toQuery();
        return BoolQuery.of(q -> q.mustNot(termQuery)).toQuery();
    }

    public static Query termsQuery(String field, Collection<String> values) {
        return TermsQuery.of(q -> q
                .field(field)
                .terms(t -> t
                        .value(values.stream().map(FieldValue::of).toList())))
                .toQuery();
    }

    public static Query termQuery(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return TermQuery.of(q -> q
                .field(field)
                .value(FieldValue.of(value)))
                .toQuery();
    }

    public static Query existsQuery(String field, boolean notExists) {
        var exists = ExistsQuery.of(q -> q.field(field)).toQuery();
        if (notExists) {
            return BoolQuery.of(q -> q.mustNot(exists)).toQuery();
        }
        return exists;
    }

    public static Query labelQuery(String query) {
        return QueryStringQuery.of(q -> q
                .query("*" + query.trim() + "*")
                .fields("label.*")
                .fuzziness("2")).toQuery();
    }

}
