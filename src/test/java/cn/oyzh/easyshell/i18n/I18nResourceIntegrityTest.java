package cn.oyzh.easyshell.i18n;

import cn.oyzh.i18n.I18nResourceBundle;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class I18nResourceIntegrityTest {

    private static final List<String> LOCALES = List.of(
            "zh_CN", "zh_TW", "en", "ja_JP", "de", "ko_KR", "fr_FR", "it_IT"
    );

    private static final List<String> ZH_TW_LEGACY_TERMS = List.of(
            "錶", "配寘", "分辯率", "分辨率", "組織秒", "組織毫秒", "組織分鐘", "組織位元組",
            "默認", "加載", "蒐索", "許可權", "數據", "消息", "網絡", "內存", "導入", "删",
            "用戶名", "連接", "文件夾", "信息", "服務器", "字體", "保存", "添加",
            "支持", "創建", "當前", "打開", "查看", "异常", "粘貼", "剪切", "重命名",
            "尅隆", "引導", "不能空", "磁盘", "进程", "线程", "队列", "协议", "宽带",
            "代码", "视频", "音频", "日志", "登录", "使用者端"
    );

    @Test
    public void easyShellLocalesHaveSameKeysAndNoEmptyValues() throws IOException {
        assertLocaleFiles("i18n_%s.properties", "src/main/resources");
    }

    @Test
    public void baseI18nLocalesHaveSameKeysAndNoEmptyValues() throws IOException {
        assertLocaleFiles("base_i18n_%s.properties", "../base/base-i18n/src/main/resources");
    }

    @Test
    public void baseCompoundLocalesHaveSameKeysAndNoEmptyValues() throws IOException {
        assertLocaleFiles("base_compound_%s.properties", "../base/base-i18n/src/main/resources");
    }

    @Test
    public void compoundKeysMatchBaseResources() throws IOException {
        Properties compound = loadProperties("base_compound_zh_CN.properties");
        assertEquals("base.compound.add.connect", I18nResourceBundle.compoundKey("base.add", "base.connect"));
        assertTrue(compound.containsKey("base.compound.add.connect"));
        assertEquals("添加连接", compound.getProperty("base.compound.add.connect"));
    }

    @Test
    public void placeholdersAreConsistentAcrossLocales() throws IOException {
        assertPlaceholders("i18n_%s.properties");
        assertPlaceholders("base_i18n_%s.properties");
        assertPlaceholders("base_compound_%s.properties");
    }

    @Test
    public void zhTwDoesNotContainKnownLegacyTerminology() throws IOException {
        for (String name : List.of(
                "i18n_zh_TW.properties",
                "base_i18n_zh_TW.properties",
                "base_compound_zh_TW.properties")) {
            Properties properties = loadProperties(name);
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                for (String term : ZH_TW_LEGACY_TERMS) {
                    assertFalse(name + " contains legacy term '" + term + "' at " + key + ": " + value,
                            value.contains(term));
                }
            }
        }
    }

    @Test
    public void shellHelperKeysExistInEveryLocale() throws IOException {
        Path helperSource = projectDir().resolve("src/main/java/cn/oyzh/easyshell/util/ShellI18nHelper.java");
        String source = Files.readString(helperSource, StandardCharsets.UTF_8);
        Set<String> keys = new TreeSet<>();
        Matcher literalMatcher = Pattern.compile("(?:text|i18nString)\\(\"([^\"]+)\"(?:\\s*\\)|\\s*,)")
                .matcher(source);
        while (literalMatcher.find()) {
            keys.add(literalMatcher.group(1));
        }
        Matcher confirmMatcher = Pattern.compile("ShellI18nHelper\\.confirm\\(\"([^\"]+)\"")
                .matcher(sourceTree());
        while (confirmMatcher.find()) {
            keys.add("shell.confirm." + confirmMatcher.group(1));
        }
        assertFalse(keys.isEmpty());
        for (String locale : LOCALES) {
            Properties properties = loadProperties("i18n_" + locale + ".properties");
            Set<String> missing = keys.stream()
                    .filter(key -> !properties.containsKey(key))
                    .collect(Collectors.toCollection(TreeSet::new));
            assertTrue(locale + " missing keys: " + missing, missing.isEmpty());
        }
    }

    @Test
    public void sourceHasNoCompositeI18nHelperCalls() throws IOException {
        Pattern composite = Pattern.compile(
                "I18nHelper\\.\\w+\\(\\)\\s*\\+\\s*(?:[^;]{0,160}?\\+\\s*)?I18nHelper\\.\\w+\\(\\)");
        Pattern formatted = Pattern.compile("String\\.format\\([^;]*I18nHelper\\.");
        Path sourceRoot = projectDir().resolve("src/main/java");
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("//") || trimmed.startsWith("*")) {
                        continue;
                    }
                    assertFalse(file + " contains composite i18n source: " + line,
                            composite.matcher(line).find());
                    assertFalse(file + " contains formatted i18n source: " + line,
                            formatted.matcher(line).find());
                }
            }
        }
    }

    @Test
    public void onlySupportedLocalesRemainInRuntimeResources() throws IOException {
        Path resources = projectDir().resolve("src/main/resources");
        Set<String> easyShellLocales;
        try (Stream<Path> files = Files.list(resources)) {
            easyShellLocales = files
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith("i18n_") && name.endsWith(".properties"))
                    .map(name -> name.substring("i18n_".length(), name.length() - ".properties".length()))
                    .collect(Collectors.toCollection(TreeSet::new));
        }
        assertEquals(new TreeSet<>(LOCALES), easyShellLocales);

        Path baseResources = projectDir().resolve("../base/base-i18n/src/main/resources");
        Set<String> baseLocales;
        try (Stream<Path> files = Files.list(baseResources)) {
            baseLocales = files
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith("base_i18n_") && name.endsWith(".properties"))
                    .map(name -> name.substring("base_i18n_".length(), name.length() - ".properties".length()))
                    .collect(Collectors.toCollection(TreeSet::new));
        }
        assertEquals(new TreeSet<>(LOCALES), baseLocales);
    }

    @Test
    public void legacyResourcesAreArchivedButNotLoaded() throws IOException {
        Path archive = projectDir().resolve("resource/i18n-legacy");
        assertEquals(22, countProperties(archive.resolve("easyshell")));
        assertEquals(22, countProperties(archive.resolve("base-i18n")));
        assertTrue(Files.exists(archive.resolve("README.md")));
    }

    private void assertLocaleFiles(String pattern, String directory) throws IOException {
        Properties reference = loadProperties(String.format(pattern, LOCALES.getFirst()));
        Set<String> expectedKeys = new TreeSet<>(reference.stringPropertyNames());
        assertFalse(expectedKeys.isEmpty());
        for (String locale : LOCALES) {
            Properties properties = loadProperties(String.format(pattern, locale));
            Set<String> keys = new TreeSet<>(properties.stringPropertyNames());
            assertEquals(locale + " key set differs", expectedKeys, keys);
            Set<String> empty = properties.stringPropertyNames().stream()
                    .filter(key -> properties.getProperty(key)
                            .replace("\\n", "")
                            .replace("\\r", "")
                            .isBlank())
                    .collect(Collectors.toCollection(TreeSet::new));
            assertTrue(locale + " contains empty values: " + empty, empty.isEmpty());
        }
        assertNotNull(directory);
    }

    private void assertPlaceholders(String pattern) throws IOException {
        Properties reference = loadProperties(String.format(pattern, LOCALES.getFirst()));
        for (String locale : LOCALES) {
            Properties properties = loadProperties(String.format(pattern, locale));
            for (String key : reference.stringPropertyNames()) {
                assertEquals(locale + " placeholder mismatch at " + key,
                        placeholders(reference.getProperty(key)),
                        placeholders(properties.getProperty(key)));
            }
        }
    }

    private List<String> placeholders(String value) {
        Matcher matcher = Pattern.compile("%(?:\\d+\\$)?[sd]|\\{\\d+}").matcher(value);
        List<String> placeholders = new ArrayList<>();
        while (matcher.find()) {
            placeholders.add(matcher.group());
        }
        return placeholders.stream().sorted().toList();
    }

    private long countProperties(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(path -> path.toString().endsWith(".properties")).count();
        }
    }

    private Properties loadProperties(String name) throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(name)) {
            assertNotNull("Missing resource: " + name, stream);
            properties.load(stream);
        }
        return properties;
    }

    private String sourceTree() throws IOException {
        Path sourceRoot = projectDir().resolve("src/main/java");
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            StringBuilder source = new StringBuilder();
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                source.append(Files.readString(file, StandardCharsets.UTF_8)).append('\n');
            }
            return source.toString();
        }
    }

    private Path projectDir() {
        return Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }
}
