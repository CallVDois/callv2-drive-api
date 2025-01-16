package com.callv2.drive.infrastructure.filter.adapter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.callv2.drive.domain.pagination.SearchQuery;

public interface QueryAdapter {

    static PageRequest of(final SearchQuery searchQuery) {
        return PageRequest.of(searchQuery.page(), searchQuery.perPage(), of(searchQuery.order()));
    }

    static Sort of(final SearchQuery.Order order) {

        if (order == null)
            return Sort.unsorted();

        return Sort.by(of(order.direction()), order.field());
    }

    static Direction of(final SearchQuery.Order.Direction direction) {
        return Direction.fromString(direction.name());
    }

    static SearchQuery.Filter of(final String source) {

        final Map<String, String> map = source == null ? Map.of()
                : List.of(source.split(";"))
                        .stream()
                        .map(s -> s.split("="))
                        .collect(Collectors.toMap(s -> getSafeArrayElement(s, 0), s -> getSafeArrayElement(s, 1)));

        return new SearchQuery.Filter(
                map.get("field"),
                map.get("value"),
                map.get("valueToCompare"),
                SearchQuery.Filter.Type.of(map.get("type")).orElse(null));
    }

    private static String getSafeArrayElement(String[] array, int index) {
        return index >= 0 && index < array.length ? array[index] : "";
    }

}
