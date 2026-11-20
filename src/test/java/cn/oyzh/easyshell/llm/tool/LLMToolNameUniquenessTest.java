package cn.oyzh.easyshell.llm.tool;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

public class LLMToolNameUniquenessTest {

    private static final Pattern TOOL_NAME = Pattern.compile(
            "@Tool\\(name\\s*=\\s*\"([^\"]+)\"");

    @Test
    public void allAnnotatedToolNamesAreGloballyUnique() throws Exception {
        Path root = Path.of("src/main/java");
        Map<String, List<String>> sources = new LinkedHashMap<>();
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .toList()) {
                String source = Files.readString(path, StandardCharsets.UTF_8);
                Matcher matcher = TOOL_NAME.matcher(source);
                while (matcher.find()) {
                    sources.computeIfAbsent(matcher.group(1),
                            ignored -> new ArrayList<>()).add(root.relativize(path).toString());
                }
            }
        }
        List<String> duplicates = sources.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> entry.getKey() + " <- " + String.join(", ", entry.getValue()))
                .toList();
        assertTrue("Duplicate LLM tool names: " + String.join("; ", duplicates),
                duplicates.isEmpty());
    }
}
