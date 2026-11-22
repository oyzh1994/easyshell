package cn.oyzh.easyshell.trees;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TreeObjectCountCacheTest {

    private static final Pattern ADD_METHOD = Pattern.compile(
            "public void add(Table|View|Function|Procedure|Event|Sequence)\\([^)]*\\)\\s*\\{(.*?)\\n\\s{4}\\}",
            Pattern.DOTALL);

    @Test
    public void objectAddMethodsIncrementCachedCounts() throws Exception {
        AtomicInteger methods = new AtomicInteger();
        try (Stream<Path> paths = Files.walk(Path.of("src/main/java/cn/oyzh/easyshell/trees"))) {
            paths.filter(path -> path.getFileName().toString().matches("Shell.*sTreeItem\\.java"))
                    .forEach(path -> {
                        try {
                            Matcher matcher = ADD_METHOD.matcher(Files.readString(path));
                            while (matcher.find()) {
                                methods.incrementAndGet();
                                String body = matcher.group(2);
                                assertFalse(path + " clears count after add",
                                        body.contains("clear" + matcher.group(1) + "Size();"));
                                assertTrue(path + " does not update cached count",
                                        body.contains("incrementCount(")
                                                || body.contains("refreshCount(")
                                                || body.contains("set" + matcher.group(1) + "Size("));
                            }
                        } catch (Exception ex) {
                            throw new RuntimeException(path.toString(), ex);
                        }
                    });
        }
        assertTrue("Expected object add methods to be covered", methods.get() >= 30);
    }
}
