package fi.vm.yti.common.opensearch;

import fi.vm.yti.common.enums.Status;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOptionsBuilders;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.mapping.FieldType;
import org.opensearch.client.opensearch._types.query_dsl.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.vm.yti.common.opensearch.OpenSearchClientWrapper.MAX_NGRAM_LENGTH;

public class QueryFactoryUtils {

    private QueryFactoryUtils() {
        // Utility class
    }

    public static final int DEFAULT_PAGE_FROM = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int INTERNAL_SEARCH_PAGE_SIZE = 10000;
    public static final String DEFAULT_SORT_LANG = "fi";
    public static final Set<Character> WILDCARD_SPECIAL_CHARS = Set.of(
            '*', '?', '\\',           // actual wildcard query special characters
            '+', '-', '=', '/',       // arithmetic/regex
            '&', '|', '!',            // logical operators
            '(', ')', '{', '}', '[', ']',  // grouping characters
            '^', '"', '~', ':', '<', '>' ); // boosting, phrases, ranges

    public static String escapeWildcard(String value) {
        var sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (WILDCARD_SPECIAL_CHARS.contains(c)) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static int pageFrom(Integer pageFrom) {
        if (pageFrom == null || pageFrom <= 0) {
            return DEFAULT_PAGE_FROM;
        } else {
            return pageFrom;
        }
    }

    public static int pageFrom(BaseSearchRequest request) {
        var pageFrom = request.getPageFrom();
        var pageSize = pageSize(request.getPageSize());

        if (pageFrom == null || pageFrom <= 0) {
            return DEFAULT_PAGE_FROM;
        } else {
            return (pageFrom - 1) * pageSize;
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
                .field("label." + QueryFactoryUtils.getSortLang(sortLang) + ".sortKey")
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

    public static Query labelQuery(String query, String... fields) {
        List<String> baseFields = fields.length == 0
                ? List.of("label.*")
                : Arrays.stream(fields).toList();

        List<String> edgeFields = baseFields.stream()
                .map(f -> f + ".edge")
                .toList();

        List<String> ngramFields = baseFields.stream()
                .map(f -> f + ".ngram")
                .toList();


        var trimmed = query.trim();
        boolean needsWildcard = trimmed.length() > MAX_NGRAM_LENGTH;

        return Query.of(q -> q
                .bool(b -> {
                    b.should(s -> s
                                    .multiMatch(m -> m
                                            .query(trimmed)
                                            .fields(baseFields)
                                            .operator(Operator.And)
                                            .boost(3.0f)
                                    )
                            )
                            .should(s -> s
                                    .multiMatch(m -> m
                                            .query(trimmed)
                                            .fields(edgeFields)
                                            .operator(Operator.And)
                                            .boost(2.0f)
                                    )
                            )
                            .should(s -> s
                                    .multiMatch(m -> m
                                            .query(trimmed)
                                            .fields(ngramFields)
                                            .operator(Operator.Or)
                                    )
                            )
                            .minimumShouldMatch("1");

                    if (needsWildcard) {
                        var words = trimmed.split("\\s+");
                        var wildcardQuery = Arrays.stream(words)
                                .map(word -> "*" + escapeWildcard(word.toLowerCase()) + "*")
                                .collect(Collectors.joining(" "));

                        b.should(s -> s
                                .queryString(qs -> qs
                                        .query(wildcardQuery)
                                        .fields(baseFields)
                                        .defaultOperator(Operator.And)
                                )
                        );
                    }

                    return b;
                })
        );
    }

    public static Query wildcardLabelQuery (String query, String... fields) {
        List<String> searchFields = fields.length == 0
                ? List.of("label.*")
                : Arrays.stream(fields).toList();

        var trimmed = query.trim();
        final var qs = trimmed.contains(" ")
                ? Arrays.stream(trimmed.split("\\s+"))
                .map(q -> String.format("*%s*", q))
                .collect(Collectors.joining(" "))
                : String.format("%s~1 *%s*", trimmed, trimmed);
        return QueryStringQuery.of(q-> q
                .query(qs)
                .defaultOperator(trimmed.contains(" ")
                        ? Operator.And
                        : Operator.Or)
                .fields(searchFields)
        ).toQuery();
    }

}
