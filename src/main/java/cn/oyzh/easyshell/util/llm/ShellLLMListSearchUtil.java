package cn.oyzh.easyshell.util.llm;

import cn.oyzh.common.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Lightweight in-memory search for LLM management lists.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class ShellLLMListSearchUtil {

    private ShellLLMListSearchUtil() {
    }

    @SafeVarargs
    public static <T> List<T> filter(Collection<T> items,
                                     String keyword,
                                     Function<T, String>... extractors) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        if (StringUtil.isBlank(keyword)) {
            return List.copyOf(items);
        }
        String query = keyword.trim();
        List<T> result = new ArrayList<>();
        for (T item : items) {
            if (item == null) {
                continue;
            }
            for (Function<T, String> extractor : extractors) {
                String value = extractor == null ? null : extractor.apply(item);
                if (StringUtil.containsIgnoreCase(value, query)) {
                    result.add(item);
                    break;
                }
            }
        }
        return result;
    }
}
