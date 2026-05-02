package util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO phân trang, tương thích format Spring Data Page.
 */
@Getter
@Builder
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number; // page number (0-indexed)
    private boolean first;
    private boolean last;

    public static <T> PageResponse<T> of(List<T> allItems, int page, int size) {
        int total = allItems.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<T> content = allItems.subList(fromIndex, toIndex);

        return PageResponse.<T>builder()
                .content(content)
                .totalPages(totalPages)
                .totalElements(total)
                .size(size)
                .number(page)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }
}
