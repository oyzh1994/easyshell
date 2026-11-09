package cn.oyzh.easyshell.llm;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.ToolExecutionResultMessage;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

/**
 * Returns a corrective tool result when the model uses an unregistered tool name.
 *
 * @author oyzh
 * @since 2026-11-09
 */
public final class LLMToolNameStrategy {

    private LLMToolNameStrategy() {
    }

    /**
     * Creates a strategy containing the exact tool names registered in the current AI service.
     *
     * @param tools tool objects
     * @return hallucinated tool name strategy
     */
    public static Function<ToolExecutionRequest, ToolExecutionResultMessage> create(Collection<?> tools) {
        Set<String> names = new LinkedHashSet<>();
        for (Object tool : tools) {
            for (ToolSpecification specification : ToolSpecifications.toolSpecificationsFrom(tool)) {
                names.add(specification.name());
            }
        }
        return request -> result(request, names);
    }

    static ToolExecutionResultMessage result(ToolExecutionRequest request, Collection<String> toolNames) {
        String suggestion = suggest(toolNames, request.name());
        StringBuilder text = new StringBuilder()
                .append("Tool '").append(request.name()).append("' is not registered. ")
                .append("Tool names are case-sensitive and must be used exactly as defined. ");
        if (suggestion != null) {
            text.append("Did you mean '").append(suggestion).append("'? ")
                    .append("Retry the tool call with name '").append(suggestion)
                    .append("' and the same arguments.");
        } else {
            text.append("Use only a tool name explicitly defined in the current request; ")
                    .append("do not translate names to snake_case or kebab-case.");
        }
        return ToolExecutionResultMessage.builder()
                .id(request.id())
                .toolName(request.name())
                .text(text.toString())
                .isError(true)
                .build();
    }

    static String suggest(Collection<String> toolNames, String requestedName) {
        String requested = normalize(requestedName);
        if (requested.isEmpty()) {
            return null;
        }
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (String name : toolNames) {
            String normalized = normalize(name);
            if (requested.equals(normalized)) {
                return name;
            }
            int distance = distance(requested, normalized);
            if (distance < bestDistance) {
                best = name;
                bestDistance = distance;
            }
        }
        int threshold = Math.max(2, requested.length() / 3);
        return bestDistance <= threshold ? best : null;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder result = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (Character.isLetterOrDigit(character)) {
                result.append(Character.toLowerCase(character));
            }
        }
        return result.toString().toLowerCase(Locale.ROOT);
    }

    private static int distance(String first, String second) {
        int[] previous = new int[second.length() + 1];
        int[] current = new int[second.length() + 1];
        for (int index = 0; index <= second.length(); index++) {
            previous[index] = index;
        }
        for (int firstIndex = 1; firstIndex <= first.length(); firstIndex++) {
            current[0] = firstIndex;
            for (int secondIndex = 1; secondIndex <= second.length(); secondIndex++) {
                int substitution = previous[secondIndex - 1]
                        + (first.charAt(firstIndex - 1) == second.charAt(secondIndex - 1) ? 0 : 1);
                current[secondIndex] = Math.min(
                        Math.min(previous[secondIndex] + 1, current[secondIndex - 1] + 1),
                        substitution);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[second.length()];
    }
}
