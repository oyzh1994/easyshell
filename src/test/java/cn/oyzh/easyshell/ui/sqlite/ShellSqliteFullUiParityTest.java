package cn.oyzh.easyshell.ui.sqlite;

import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * SQLite 全部 FXML 页面与 MySQL、达梦的截图差异诊断。
 *
 * @since 2026-11-04
 */
public class ShellSqliteFullUiParityTest {

    private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

    private static final Path REPORT_ROOT = Path.of("target/ui-test-report/sqlite-full-ui");

    private static final int FALLBACK_WIDTH = 1200;

    private static final int FALLBACK_HEIGHT = 760;

    private static final Map<String, Integer> MYSQL_PAGE_BUDGET = Map.ofEntries(
            Map.entry("fxml-connect-shelladdsqliteconnect", 9000),
            Map.entry("fxml-connect-shellupdatesqliteconnect", 9000),
            Map.entry("fxml-function-functioninfo", 500),
            Map.entry("fxml-table-tableinfo", 3000),
            Map.entry("popups-indexfieldpopup", 400),
            Map.entry("tabs-function-functiondesigntab", 19000),
            Map.entry("tabs-home-hometab", 3000),
            Map.entry("tabs-tab", 3500),
            Map.entry("tabs-table-tabledesigntab", 2000),
            Map.entry("tabs-view-viewdesigntab", 700));

    private static final Map<String, Integer> DAMENG_PAGE_BUDGET = Map.ofEntries(
            Map.entry("fxml-connect-shelladdsqliteconnect", 9200),
            Map.entry("fxml-connect-shellupdatesqliteconnect", 9200),
            Map.entry("fxml-function-functioninfo", 500),
            Map.entry("fxml-table-tableinfo", 210000),
            Map.entry("tabs-function-functiondesigntab", 19000),
            Map.entry("tabs-home-hometab", 3000),
            Map.entry("tabs-tab", 3600),
            Map.entry("tabs-table-tabledesigntab", 2000),
            Map.entry("tabs-view-viewdesigntab", 700));

    private static final Map<String, Integer> MYSQL_TAB_BUDGET = Map.ofEntries(
            Map.entry("fxml-connect-shelladdsqliteconnect/基础", 8500),
            Map.entry("fxml-connect-shelladdsqliteconnect/扩展", 13000),
            Map.entry("fxml-connect-shellupdatesqliteconnect/基础", 8500),
            Map.entry("fxml-connect-shellupdatesqliteconnect/扩展", 13000),
            Map.entry("fxml-table-tableinfo/基础", 3000),
            Map.entry("tabs-function-functiondesigntab/定义", 18000),
            Map.entry("tabs-table-tabledesigntab/字段", 900),
            Map.entry("tabs-table-tabledesigntab/索引", 400),
            Map.entry("tabs-table-tabledesigntab/外键", 4500),
            Map.entry("tabs-table-tabledesigntab/选项", 10000));

    private static final Map<String, Integer> DAMENG_TAB_BUDGET = Map.ofEntries(
            Map.entry("fxml-connect-shelladdsqliteconnect/基础", 8700),
            Map.entry("fxml-connect-shelladdsqliteconnect/扩展", 13000),
            Map.entry("fxml-connect-shellupdatesqliteconnect/基础", 8700),
            Map.entry("fxml-connect-shellupdatesqliteconnect/扩展", 13000),
            Map.entry("fxml-table-tableinfo/基础", 210000),
            Map.entry("tabs-function-functiondesigntab/定义", 18000),
            Map.entry("tabs-table-tabledesigntab/字段", 900),
            Map.entry("tabs-table-tabledesigntab/索引", 3200),
            Map.entry("tabs-table-tabledesigntab/外键", 4500),
            Map.entry("tabs-table-tabledesigntab/选项", 4000));

    @BeforeClass
    public static void startJavaFx() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue("JavaFX toolkit startup timed out", startup.await(20, TimeUnit.SECONDS));
        Platform.runLater(() -> Application.setUserAgentStylesheet(ThemeManager.currentUserAgentStylesheet()));
    }

    @Test
    public void generateFullUiParityReport() throws Exception {
        List<PagePair> pairs = this.pagePairs();
        assertTrue("No SQLite page pairs found", !pairs.isEmpty());
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<List<PageResult>> resultsRef = new AtomicReference<>();
        CountDownLatch rendered = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                resultsRef.set(this.renderAll(pairs));
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                rendered.countDown();
            }
        });
        assertTrue("Full UI parity rendering timed out", rendered.await(120, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Full UI parity rendering failed", error.get());
        }
        List<PageResult> results = resultsRef.get();
        assertNotNull(results);
        this.writeReport(results);
        for (PageResult result : results) {
            assertTrue(result.name + " rendered an empty scene", result.sqlite().nonBlank());
            assertTrue(result.name + " SQLite/MySQL diff exceeded budget: "
                            + result.sqliteMysql().differentPixels(),
                    result.sqliteMysql().differentPixels()
                            <= MYSQL_PAGE_BUDGET.getOrDefault(result.name(), 0));
            assertTrue(result.name + " SQLite/Dameng diff exceeded budget: "
                            + result.sqliteDameng().differentPixels(),
                    result.sqliteDameng().differentPixels()
                            <= DAMENG_PAGE_BUDGET.getOrDefault(result.name(), 0));
        }
    }

    @Test
    public void generateTabContentParityReport() throws Exception {
        List<PagePair> pairs = this.pagePairs();
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<List<TabResult>> resultsRef = new AtomicReference<>();
        CountDownLatch rendered = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                resultsRef.set(this.renderAllTabs(pairs));
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                rendered.countDown();
            }
        });
        assertTrue("Tab content parity rendering timed out", rendered.await(120, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Tab content parity rendering failed", error.get());
        }
        List<TabResult> results = resultsRef.get();
        assertNotNull(results);
        this.writeTabReport(results);
        for (TabResult result : results) {
            assertTrue(result.page() + "/" + result.tab() + " rendered an empty SQLite tab",
                    result.sqlite().nonBlank());
            String key = result.page() + "/" + result.tab();
            if (result.sqliteMysql() == null) {
                assertTrue(key + " has an unexpected content size mismatch", false);
            } else {
                assertTrue(key + " SQLite/MySQL tab diff exceeded budget: "
                                + result.sqliteMysql().differentPixels(),
                        result.sqliteMysql().differentPixels()
                                <= MYSQL_TAB_BUDGET.getOrDefault(key, 0));
            }
            if (result.sqliteDameng() == null) {
                assertTrue(key + " has an unexpected content size mismatch", false);
            } else {
                assertTrue(key + " SQLite/Dameng tab diff exceeded budget: "
                                + result.sqliteDameng().differentPixels(),
                        result.sqliteDameng().differentPixels()
                                <= DAMENG_TAB_BUDGET.getOrDefault(key, 0));
            }
        }
    }

    private List<PageResult> renderAll(List<PagePair> pairs) throws IOException {
        Files.createDirectories(REPORT_ROOT);
        List<PageResult> results = new ArrayList<>();
        for (PagePair pair : pairs) {
            LoadedPage sqlite = this.load(pair.sqlite());
            LoadedPage mysql = this.load(pair.mysql());
            LoadedPage dameng = this.load(pair.dameng());
            int width = Math.max(FALLBACK_WIDTH,
                    Math.max(sqlite.width(), Math.max(mysql.width(), dameng.width())));
            int height = Math.max(FALLBACK_HEIGHT,
                    Math.max(sqlite.height(), Math.max(mysql.height(), dameng.height())));
            Snapshot sqliteSnapshot = this.snapshot(sqlite.root(), width, height);
            Snapshot mysqlSnapshot = this.snapshot(mysql.root(), width, height);
            Snapshot damengSnapshot = this.snapshot(dameng.root(), width, height);
            Diff sqliteMysql = this.compare(sqliteSnapshot, mysqlSnapshot);
            Diff sqliteDameng = this.compare(sqliteSnapshot, damengSnapshot);
            String key = pair.key();
            this.writeImage(key + "-mysql.png", mysqlSnapshot.image());
            this.writeImage(key + "-dameng.png", damengSnapshot.image());
            this.writeImage(key + "-sqlite.png", sqliteSnapshot.image());
            if (sqliteMysql.differentPixels() > 0) {
                this.writeImage(key + "-sqlite-vs-mysql-diff.png", sqliteMysql.diffImage());
            }
            if (sqliteDameng.differentPixels() > 0) {
                this.writeImage(key + "-sqlite-vs-dameng-diff.png", sqliteDameng.diffImage());
            }
            results.add(new PageResult(pair.name(), sqliteSnapshot, mysqlSnapshot, damengSnapshot,
                    sqliteMysql, sqliteDameng));
        }
        return results;
    }

    private List<TabResult> renderAllTabs(List<PagePair> pairs) throws IOException {
        Path tabRoot = REPORT_ROOT.resolve("tabs");
        Files.createDirectories(tabRoot);
        List<TabResult> results = new ArrayList<>();
        for (PagePair pair : pairs) {
            LoadedPage sqlite = this.load(pair.sqlite());
            LoadedPage mysql = this.load(pair.mysql());
            LoadedPage dameng = this.load(pair.dameng());
            TabPane sqlitePane = this.findTabPane(sqlite.root());
            TabPane mysqlPane = this.findTabPane(mysql.root());
            TabPane damengPane = this.findTabPane(dameng.root());
            if (sqlitePane == null || mysqlPane == null || damengPane == null) {
                continue;
            }
            int width = Math.max(FALLBACK_WIDTH,
                    Math.max(sqlite.width(), Math.max(mysql.width(), dameng.width())));
            int height = Math.max(FALLBACK_HEIGHT,
                    Math.max(sqlite.height(), Math.max(mysql.height(), dameng.height())));
            Scene sqliteScene = this.createScene(sqlite.root(), width, height);
            Scene mysqlScene = this.createScene(mysql.root(), width, height);
            Scene damengScene = this.createScene(dameng.root(), width, height);
            for (Tab sqliteTab : sqlitePane.getTabs()) {
                Tab mysqlTab = this.findTab(mysqlPane, sqliteTab);
                Tab damengTab = this.findTab(damengPane, sqliteTab);
                if (mysqlTab == null || damengTab == null || sqliteTab.getContent() == null
                        || mysqlTab.getContent() == null || damengTab.getContent() == null) {
                    continue;
                }
                this.select(sqlitePane, sqliteTab, sqliteScene, sqlite.root());
                this.select(mysqlPane, mysqlTab, mysqlScene, mysql.root());
                this.select(damengPane, damengTab, damengScene, dameng.root());
                Snapshot sqliteSnapshot = this.snapshotNode(sqliteTab.getContent());
                Snapshot mysqlSnapshot = this.snapshotNode(mysqlTab.getContent());
                Snapshot damengSnapshot = this.snapshotNode(damengTab.getContent());
                if (sqliteSnapshot.width() != mysqlSnapshot.width()
                        || sqliteSnapshot.height() != mysqlSnapshot.height()
                        || sqliteSnapshot.width() != damengSnapshot.width()
                        || sqliteSnapshot.height() != damengSnapshot.height()) {
                    results.add(new TabResult(pair.name(), this.tabName(sqliteTab), sqliteSnapshot,
                            mysqlSnapshot, damengSnapshot, null, null));
                    continue;
                }
                Diff sqliteMysql = this.compare(sqliteSnapshot, mysqlSnapshot);
                Diff sqliteDameng = this.compare(sqliteSnapshot, damengSnapshot);
                String key = pair.key() + "-" + this.safeName(this.tabName(sqliteTab));
                this.writeImage(tabRoot, key + "-sqlite.png", sqliteSnapshot.image());
                this.writeImage(tabRoot, key + "-mysql.png", mysqlSnapshot.image());
                this.writeImage(tabRoot, key + "-dameng.png", damengSnapshot.image());
                if (sqliteMysql.differentPixels() > 0) {
                    this.writeImage(tabRoot, key + "-sqlite-vs-mysql-diff.png", sqliteMysql.diffImage());
                }
                if (sqliteDameng.differentPixels() > 0) {
                    this.writeImage(tabRoot, key + "-sqlite-vs-dameng-diff.png", sqliteDameng.diffImage());
                }
                results.add(new TabResult(pair.name(), this.tabName(sqliteTab), sqliteSnapshot,
                        mysqlSnapshot, damengSnapshot, sqliteMysql, sqliteDameng));
            }
        }
        return results;
    }

    private Scene createScene(Parent root, int width, int height) {
        Scene scene = new Scene(root, width, height);
        root.applyCss();
        root.layout();
        return scene;
    }

    private void select(TabPane pane, Tab tab, Scene scene, Parent root) {
        pane.getSelectionModel().select(tab);
        root.applyCss();
        root.layout();
        if (tab.getContent() instanceof Parent content) {
            content.applyCss();
            content.layout();
        }
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }

    private TabPane findTabPane(Parent root) {
        List<TabPane> panes = new ArrayList<>();
        this.collectTabPanes(root, panes);
        return panes.stream().max(Comparator.comparingInt(pane -> pane.getTabs().size())).orElse(null);
    }

    private void collectTabPanes(Node node, List<TabPane> panes) {
        if (node instanceof TabPane pane) {
            panes.add(pane);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                this.collectTabPanes(child, panes);
            }
        }
    }

    private Tab findTab(TabPane pane, Tab source) {
        for (Tab tab : pane.getTabs()) {
            if (source.getId() != null && source.getId().equals(tab.getId())) {
                return tab;
            }
        }
        for (Tab tab : pane.getTabs()) {
            if (source.getText() != null && source.getText().equals(tab.getText())) {
                return tab;
            }
        }
        return null;
    }

    private Snapshot snapshotNode(Node node) {
        if (node instanceof Parent parent) {
            parent.applyCss();
            parent.layout();
        }
        WritableImage image = node.snapshot(null, null);
        int[] pixels = this.pixels(image);
        int first = pixels.length == 0 ? 0 : pixels[0];
        boolean nonBlank = pixels.length > 0;
        for (int pixel : pixels) {
            if (pixel != first) {
                nonBlank = true;
                break;
            }
        }
        return new Snapshot(image, pixels, nonBlank);
    }

    private String tabName(Tab tab) {
        return tab.getText() == null ? String.valueOf(tab.getId()) : tab.getText();
    }

    private String safeName(String value) {
        return value.replaceAll("[^\\p{L}\\p{N}]+", "_");
    }

    private LoadedPage load(String resource) throws IOException {
        FXMLLoader loader = new FXMLLoader(ShellSqliteFullUiParityTest.class.getResource(resource));
        loader.setResources(I18nResourceBundle.INSTANCE);
        Parent root = loader.load();
        double width = root.prefWidth(-1);
        double height = root.prefHeight(-1);
        return new LoadedPage(root, Double.isFinite(width) ? (int) Math.ceil(width) : 0,
                Double.isFinite(height) ? (int) Math.ceil(height) : 0);
    }

    private Snapshot snapshot(Parent root, int width, int height) {
        Scene scene = new Scene(root, width, height);
        root.applyCss();
        root.layout();
        WritableImage image = scene.snapshot(null);
        int[] pixels = this.pixels(image);
        int first = pixels.length == 0 ? 0 : pixels[0];
        boolean nonBlank = pixels.length > 0;
        for (int pixel : pixels) {
            if (pixel != first) {
                nonBlank = true;
                break;
            }
        }
        return new Snapshot(image, pixels, nonBlank);
    }

    private Diff compare(Snapshot left, Snapshot right) {
        assertEquals("snapshot width", left.width(), right.width());
        assertEquals("snapshot height", left.height(), right.height());
        int[] leftPixels = left.pixels();
        int[] rightPixels = right.pixels();
        int differentPixels = 0;
        int maxChannelDelta = 0;
        int minX = left.width();
        int minY = left.height();
        int maxX = -1;
        int maxY = -1;
        int[] diffPixels = new int[leftPixels.length];
        System.arraycopy(leftPixels, 0, diffPixels, 0, leftPixels.length);
        for (int y = 0; y < left.height(); y++) {
            for (int x = 0; x < left.width(); x++) {
                int index = y * left.width() + x;
                if (leftPixels[index] != rightPixels[index]) {
                    differentPixels++;
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                    maxChannelDelta = Math.max(maxChannelDelta,
                            this.channelDelta(leftPixels[index], rightPixels[index]));
                    diffPixels[index] = 0xFFFF00FF;
                }
            }
        }
        WritableImage diffImage = new WritableImage(left.width(), left.height());
        diffImage.getPixelWriter().setPixels(0, 0, left.width(), left.height(),
                PixelFormat.getIntArgbInstance(), diffPixels, 0, left.width());
        return new Diff(differentPixels, maxChannelDelta, minX, minY, maxX, maxY, diffImage);
    }

    private int channelDelta(int left, int right) {
        int delta = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            delta = Math.max(delta, Math.abs(((left >> shift) & 0xFF) - ((right >> shift) & 0xFF)));
        }
        return delta;
    }

    private int[] pixels(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height,
                PixelFormat.getIntArgbInstance(), pixels, 0, width);
        return pixels;
    }

    private void writeImage(String name, WritableImage image) throws IOException {
        this.writeImage(REPORT_ROOT, name, image);
    }

    private void writeImage(Path directory, String name, WritableImage image) throws IOException {
        Files.createDirectories(directory);
        BufferedImage buffered = new BufferedImage((int) image.getWidth(), (int) image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        int[] pixels = this.pixels(image);
        buffered.setRGB(0, 0, (int) image.getWidth(), (int) image.getHeight(), pixels, 0,
                (int) image.getWidth());
        ImageIO.write(buffered, "png", directory.resolve(name).toFile());
    }

    private void writeReport(List<PageResult> results) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# SQLite 全页面 UI 像素差异诊断\n\n");
        report.append("- 生成时间: ").append(LocalDateTime.now()).append("\n");
        report.append("- 页面数: ").append(results.size()).append("\n");
        report.append("- 比较方式: 每套实现加载原始 FXML，使用同一主题、同一 Scene 尺寸渲染后逐像素比较\n\n");
        report.append("| 页面 | SQLite vs MySQL | SQLite vs 达梦 | 截图 |\n");
        report.append("| --- | ---: | ---: | --- |\n");
        for (PageResult result : results) {
            report.append("| ").append(result.name()).append(" | ")
                    .append(result.sqliteMysql().differentPixels()).append(" / ")
                    .append(result.sqlite().width() * result.sqlite().height()).append(" | ")
                    .append(result.sqliteDameng().differentPixels()).append(" / ")
                    .append(result.sqlite().width() * result.sqlite().height()).append(" | ")
                    .append(this.markdownLink(result.name())).append(" |\n");
        }
        Files.writeString(REPORT_ROOT.resolve("report.md"), report.toString(), StandardCharsets.UTF_8);
    }

    private void writeTabReport(List<TabResult> results) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# SQLite 页签内容区 UI 像素差异诊断\n\n");
        report.append("- 生成时间: ").append(LocalDateTime.now()).append("\n");
        report.append("- 可比较页签数: ").append(results.size()).append("\n");
        report.append("- 比较方式: 选择同名/同 ID 页签后，仅截取页签内容区并逐像素比较\n\n");
        report.append("| 页面 | 页签 | SQLite vs MySQL | SQLite vs 达梦 | 尺寸 |\n");
        report.append("| --- | --- | ---: | ---: | --- |\n");
        for (TabResult result : results) {
            String mysqlDiff = result.sqliteMysql() == null ? "SIZE MISMATCH"
                    : result.sqliteMysql().differentPixels() + " / "
                    + result.sqlite().width() * result.sqlite().height();
            String damengDiff = result.sqliteDameng() == null ? "SIZE MISMATCH"
                    : result.sqliteDameng().differentPixels() + " / "
                    + result.sqlite().width() * result.sqlite().height();
            report.append("| ").append(result.page()).append(" | ").append(result.tab()).append(" | ")
                    .append(mysqlDiff).append(" | ").append(damengDiff).append(" | ")
                    .append(result.sqlite().width()).append(" x ").append(result.sqlite().height())
                    .append(" |\n");
        }
        Files.writeString(REPORT_ROOT.resolve("tab-report.md"), report.toString(), StandardCharsets.UTF_8);
    }

    private String markdownLink(String name) {
        return "[" + name + "](" + name + "-sqlite.png)";
    }

    private List<PagePair> pagePairs() throws IOException {
        Map<String, PagePair> pairs = new LinkedHashMap<>();
        try (Stream<Path> paths = Files.walk(RESOURCE_ROOT)) {
            List<Path> sqliteFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".fxml"))
                    .filter(path -> path.toString().contains("/sqlite/"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
            for (Path sqlite : sqliteFiles) {
                String sqliteResource = "/" + RESOURCE_ROOT.relativize(sqlite).toString().replace('\\', '/');
                String mysqlResource = this.otherDialect(sqliteResource, "mysql");
                String damengResource = this.otherDialect(sqliteResource, "dameng");
                if (!this.exists(mysqlResource) || !this.exists(damengResource)) {
                    continue;
                }
                String key = this.key(sqliteResource);
                pairs.put(key, new PagePair(key, sqliteResource, mysqlResource, damengResource));
            }
        }
        return new ArrayList<>(pairs.values());
    }

    private String otherDialect(String resource, String dialect) {
        String value = resource.replace("/sqlite/", "/" + dialect + "/");
        value = value.replace("Sqlite", dialect.equals("mysql") ? "Mysql" : "Dameng");
        return value;
    }

    private boolean exists(String resource) {
        return this.getClass().getResource(resource) != null;
    }

    private String key(String resource) {
        String value = resource.substring(1).replace('/', '-').replace(".fxml", "");
        return value.replace("sqlite-", "").replace("shellSqlite", "").toLowerCase();
    }

    private record LoadedPage(Parent root, int width, int height) {
    }

    private record Snapshot(WritableImage image, int[] pixels, boolean nonBlank) {
        int width() {
            return (int) this.image.getWidth();
        }

        int height() {
            return (int) this.image.getHeight();
        }
    }

    private record Diff(int differentPixels, int maxChannelDelta, int minX, int minY, int maxX, int maxY,
                        WritableImage diffImage) {
    }

    private record PagePair(String key, String sqlite, String mysql, String dameng) {
        String name() {
            return this.key;
        }
    }

    private record PageResult(String name, Snapshot sqlite, Snapshot mysql, Snapshot dameng,
                              Diff sqliteMysql, Diff sqliteDameng) {
    }

    private record TabResult(String page, String tab, Snapshot sqlite, Snapshot mysql, Snapshot dameng,
                             Diff sqliteMysql, Diff sqliteDameng) {
    }
}
