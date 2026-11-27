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

    /**
     * 是否为懒加载数量模式：数量直接取自已加载的子节点，不再发起数量查询，因此无需失效缓存，也无需同步。
     *
     * @param source 源码
     * @return 结果
     */
    private static boolean isLazyCount(String source) {
        return source.contains("unfilteredChildrenSize()");
    }

    @Test
    public void objectAddMethodsInvalidateCachedCounts() throws Exception {
        AtomicInteger methods = new AtomicInteger();
        try (Stream<Path> paths = Files.walk(Path.of("src/main/java/cn/oyzh/easyshell/trees"))) {
            paths.filter(path -> path.getFileName().toString().matches("Shell.*sTreeItem\\.java"))
                    .forEach(path -> {
                        try {
                            String source = Files.readString(path);
                            Matcher matcher = ADD_METHOD.matcher(source);
                            while (matcher.find()) {
                                methods.incrementAndGet();
                                String body = matcher.group(2);
                                // 懒加载模式下数量随子节点自动变化，无需显式失效缓存
                                if (!isLazyCount(source)) {
                                    assertTrue(path + " does not invalidate count after add",
                                            body.contains("clear" + matcher.group(1) + "Size();")
                                                    || body.contains("refreshCount();"));
                                }
                                assertFalse(path + " increments count manually",
                                        body.contains("incrementCount("));
                            }
                        } catch (Exception ex) {
                            throw new RuntimeException(path.toString(), ex);
                        }
                    });
        }
        assertTrue("Expected object add methods to be covered", methods.get() >= 30);
    }

    @Test
    public void typeNodeExtraUsesUncachedCountGetter() throws Exception {
        AtomicInteger values = new AtomicInteger();
        try (Stream<Path> paths = Files.walk(Path.of("src/main/java/cn/oyzh/easyshell/trees"))) {
            paths.filter(path -> path.getFileName().toString().matches("Shell.*sTreeItemValue\\.java"))
                    .forEach(path -> {
                        try {
                            String source = Files.readString(path);
                            if (!source.contains("public String extra()")) {
                                return;
                            }
                            values.incrementAndGet();
                            assertFalse(path + " reads cached count",
                                    source.contains("getCachedTableSize()")
                                            || source.contains("getCachedViewSize()")
                                            || source.contains("getCachedFunctionSize()")
                                            || source.contains("getCachedProcedureSize()")
                                            || source.contains("getCachedEventSize()")
                                            || source.contains("getCachedSequenceSize()"));
                        } catch (Exception ex) {
                            throw new RuntimeException(path.toString(), ex);
                        }
                    });
        }
        assertTrue("Expected type node values to be covered", values.get() >= 30);
    }

    @Test
    public void countGettersAreSynchronizedAndCachedGettersAreRemoved() throws Exception {
        AtomicInteger getters = new AtomicInteger();
        Pattern getter = Pattern.compile(
                "public (?:synchronized )?(?:int|Integer) get(Table|View|Function|Procedure|Event|Sequence|Buckets|User|Collections)Size\\(\\)");
        Pattern sideEffectSetter = Pattern.compile(
                "public void set(?:Table|View|Function|Procedure|Event|Sequence|Buckets|User|Collection)Size\\(Integer ");
        try (Stream<Path> paths = Files.walk(Path.of("src/main/java/cn/oyzh/easyshell/trees"))) {
            paths.filter(path -> path.getFileName().toString().matches("Shell.*sTreeItem\\.java"))
                    .forEach(path -> {
                        try {
                            String source = Files.readString(path);
                            assertFalse(path + " still exposes a cached count getter",
                                    source.contains("getCachedTableSize()")
                                            || source.contains("getCachedViewSize()")
                                            || source.contains("getCachedFunctionSize()")
                                            || source.contains("getCachedProcedureSize()")
                                            || source.contains("getCachedEventSize()")
                                            || source.contains("getCachedSequenceSize()")
                                            || source.contains("getCachedBucketsSize()")
                                            || source.contains("getCachedUserSize()")
                                            || source.contains("getCachedCollectionsSize()"));
                            assertFalse(path + " still exposes a count setter",
                                    sideEffectSetter.matcher(source).find());
                            Matcher matcher = getter.matcher(source);
                            while (matcher.find()) {
                                getters.incrementAndGet();
                                // 懒加载模式的数量取值不再访问数据库，无需同步保护
                                if (!isLazyCount(source)) {
                                    assertTrue(path + " count getter is not synchronized",
                                            matcher.group().contains(" synchronized "));
                                }
                            }
                        } catch (Exception ex) {
                            throw new RuntimeException(path.toString(), ex);
                        }
                    });
        }
        assertTrue("Expected count getters to be covered", getters.get() >= 30);
    }
}
