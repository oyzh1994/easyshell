package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.easyshell.query.dameng.ShellDamengQueryPromptItem;
import cn.oyzh.easyshell.query.dameng.ShellDamengQueryPromptListView;
import cn.oyzh.easyshell.query.dameng.ShellDamengQueryPromptPopup;
import cn.oyzh.easyshell.query.mysql.ShellMysqlQueryPromptItem;
import cn.oyzh.easyshell.query.mysql.ShellMysqlQueryPromptListView;
import cn.oyzh.easyshell.query.mysql.ShellMysqlQueryPromptPopup;
import cn.oyzh.fx.db.query.DBQueryPromptItem;
import cn.oyzh.fx.db.query.ui.DBQueryPromptListView;
import cn.oyzh.fx.gui.svg.glyph.KeywordsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ColumnSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.DatabaseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * SQLite 查询提示弹窗 UI 与 MySQL、达梦的像素级一致性测试。
 *
 * @since 2026-11-04
 */
public class ShellSqliteQueryPromptUiParityTest {

    private static final int SCENE_WIDTH = 400;

    private static final int SCENE_HEIGHT = 300;

    private static final Color EXT_TEXT_COLOR = Color.valueOf("#D3D3D3");

    @BeforeClass
    public static void startJavaFx() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue("JavaFX toolkit startup timed out", startup.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void testPopupWiringMatchesMysqlAndDameng() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch rendered = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ShellMysqlQueryPromptPopup mysql = new ShellMysqlQueryPromptPopup();
                assertTrue(mysql.listView() instanceof ShellMysqlQueryPromptListView);
                this.assertPopupListSize(mysql.listView());

                ShellDamengQueryPromptPopup dameng = new ShellDamengQueryPromptPopup();
                assertTrue(dameng.listView() instanceof ShellDamengQueryPromptListView);
                this.assertPopupListSize(dameng.listView());

                ShellSqliteQueryPromptPopup sqlite = new ShellSqliteQueryPromptPopup();
                assertTrue(sqlite.listView() instanceof ShellSqliteQueryPromptListView);
                this.assertPopupListSize(sqlite.listView());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                rendered.countDown();
            }
        });
        assertTrue("Prompt popup wiring timed out", rendered.await(30, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Prompt popup wiring is not aligned", error.get());
        }
    }

    @Test
    public void testPromptListRowsMatchMysqlAndDameng() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch rendered = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ShellMysqlQueryPromptListView mysql = new ShellMysqlQueryPromptListView();
                mysql.init(this.mysqlItems());
                this.assertRows(mysql);

                ShellDamengQueryPromptListView dameng = new ShellDamengQueryPromptListView();
                dameng.init(this.damengItems());
                this.assertRows(dameng);

                ShellSqliteQueryPromptListView sqlite = new ShellSqliteQueryPromptListView();
                sqlite.init(this.sqliteItems());
                this.assertRows(sqlite);
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                rendered.countDown();
            }
        });
        assertTrue("Prompt list rendering timed out", rendered.await(30, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Prompt list structure is not aligned", error.get());
        }
    }

    @Test
    public void testSqlitePromptListMatchesMysqlAndDamengPixelForPixel() throws Exception {
        AtomicReference<RenderResult> resultRef = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch rendered = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Application.setUserAgentStylesheet(ThemeManager.currentUserAgentStylesheet());

                ShellMysqlQueryPromptListView mysql = new ShellMysqlQueryPromptListView();
                mysql.init(this.mysqlItems());
                WritableImage mysqlImage = this.render(mysql);

                ShellDamengQueryPromptListView dameng = new ShellDamengQueryPromptListView();
                dameng.init(this.damengItems());
                WritableImage damengImage = this.render(dameng);

                ShellSqliteQueryPromptListView sqlite = new ShellSqliteQueryPromptListView();
                sqlite.init(this.sqliteItems());
                WritableImage sqliteImage = this.render(sqlite);

                resultRef.set(new RenderResult(mysqlImage, damengImage, sqliteImage));
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                rendered.countDown();
            }
        });
        assertTrue("Pixel rendering timed out", rendered.await(30, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Prompt list pixel rendering failed", error.get());
        }

        RenderResult result = resultRef.get();
        assertNotNull(result);
        assertTrue("SQLite prompt screenshot is blank", this.distinctColors(result.sqlite()) > 20);
        assertTrue("MySQL prompt screenshot is blank", this.distinctColors(result.mysql()) > 20);
        assertTrue("Dameng prompt screenshot is blank", this.distinctColors(result.dameng()) > 20);
        Diff mysqlDameng = this.compare("MySQL vs Dameng", result.mysql(), result.dameng());
        Diff sqliteMysql = this.compare("SQLite vs MySQL", result.sqlite(), result.mysql());
        Diff sqliteDameng = this.compare("SQLite vs Dameng", result.sqlite(), result.dameng());

        Path reportDir = this.writeReport(result, mysqlDameng, sqliteMysql, sqliteDameng);
        String reportHint = "UI report: " + reportDir.toAbsolutePath();

        assertEquals(reportHint + "\n" + mysqlDameng, 0, mysqlDameng.differentPixels());
        assertEquals(reportHint + "\n" + sqliteMysql, 0, sqliteMysql.differentPixels());
        assertEquals(reportHint + "\n" + sqliteDameng, 0, sqliteDameng.differentPixels());
    }

    private void assertRows(DBQueryPromptListView<?> list) {
        this.assertPopupListSize(list);
        List<?> rows = new ArrayList<>(list.getItems());
        assertEquals(7, rows.size());
        this.assertRow(rows.get(0), DatabaseSVGGlyph.class, "main", null, false);
        this.assertRow(rows.get(1), KeywordsSVGGlyph.class, "SELECT", null, false);
        this.assertRow(rows.get(2), TableSVGGlyph.class, "prompt_item", "main", true);
        this.assertRow(rows.get(3), ColumnSVGGlyph.class, "name", "\"main\".\"prompt_item\"", true);
        this.assertRow(rows.get(4), ViewSVGGlyph.class, "prompt_view", "main", false);
        this.assertRow(rows.get(5), FunctionSVGGlyph.class, "length", null, true);
        this.assertRow(rows.get(6), ProcedureSVGGlyph.class, "refresh_cache", null, true);
    }

    private void assertRow(Object value, Class<?> glyphType, String content, String extContent, boolean fixedWidth) {
        FXHBox row = (FXHBox) value;
        int expectedChildren = extContent == null ? 1 : 2;
        assertEquals(expectedChildren, row.getChildren().size());

        SVGLabel prompt = (SVGLabel) row.getChildren().get(0);
        assertTrue("Expected " + glyphType.getSimpleName() + " but was "
                        + prompt.getGraphic().getClass().getSimpleName(),
                glyphType.isInstance(prompt.getGraphic()));
        assertEquals(content, prompt.getText());
        assertEquals(content, prompt.getTipText());
        if (fixedWidth) {
            assertEquals(240.0, prompt.getPrefWidth(), 0.001);
        } else {
            assertTrue(prompt.getPrefWidth() != 240.0);
        }

        if (extContent != null) {
            FXLabel ext = (FXLabel) row.getChildren().get(1);
            assertEquals(extContent, ext.getText());
            assertEquals(content, ext.getTipText());
            assertEquals(EXT_TEXT_COLOR, ext.getTextFill());
        }
    }

    private void assertPopupListSize(DBQueryPromptListView<?> list) {
        assertEquals(360.0, list.getPrefWidth(), 0.001);
        assertEquals(240.0, list.getPrefHeight(), 0.001);
    }

    private WritableImage render(DBQueryPromptListView<?> list) {
        list.changeTheme(ThemeManager.currentTheme());
        StackPane root = new StackPane(list);
        root.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT);
        root.setStyle("-fx-background-color: #202225;");
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        root.applyCss();
        root.layout();
        return scene.snapshot(null);
    }

    private Diff compare(String name, WritableImage left, WritableImage right) {
        int width = (int) left.getWidth();
        int height = (int) left.getHeight();
        assertEquals(name + " width", width, (int) right.getWidth());
        assertEquals(name + " height", height, (int) right.getHeight());
        int[] leftPixels = this.pixels(left, width, height);
        int[] rightPixels = this.pixels(right, width, height);
        int differentPixels = 0;
        int maxChannelDelta = 0;
        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (leftPixels[index] != rightPixels[index]) {
                    differentPixels++;
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                    maxChannelDelta = Math.max(maxChannelDelta,
                            this.channelDelta(leftPixels[index], rightPixels[index]));
                }
            }
        }
        return new Diff(name, width, height, differentPixels, maxChannelDelta,
                minX, minY, maxX, maxY);
    }

    private int channelDelta(int left, int right) {
        int delta = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            delta = Math.max(delta, Math.abs(((left >> shift) & 0xFF) - ((right >> shift) & 0xFF)));
        }
        return delta;
    }

    private int[] pixels(WritableImage image, int width, int height) {
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height,
                PixelFormat.getIntArgbInstance(), pixels, 0, width);
        return pixels;
    }

    private int distinctColors(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[] pixels = this.pixels(image, width, height);
        java.util.Set<Integer> colors = new java.util.HashSet<>();
        for (int pixel : pixels) {
            colors.add(pixel);
            if (colors.size() > 20) {
                return colors.size();
            }
        }
        return colors.size();
    }

    private Path writeReport(RenderResult result, Diff... diffs) throws IOException {
        Path dir = Path.of(System.getProperty("easyshell.ui.report.dir",
                "target/ui-test-report/sqlite-query-prompt"));
        Files.createDirectories(dir);
        this.writeImage(result.mysql(), new File(dir.toFile(), "mysql.png"));
        this.writeImage(result.dameng(), new File(dir.toFile(), "dameng.png"));
        this.writeImage(result.sqlite(), new File(dir.toFile(), "sqlite.png"));
        this.writeDiff(result.mysql(), result.sqlite(), new File(dir.toFile(), "sqlite-vs-mysql-diff.png"));
        this.writeDiff(result.dameng(), result.sqlite(), new File(dir.toFile(), "sqlite-vs-dameng-diff.png"));

        StringBuilder report = new StringBuilder();
        report.append("# SQLite 查询提示弹窗 UI 像素测试报告\n\n");
        report.append("- 生成时间: ").append(LocalDateTime.now()).append("\n");
        report.append("- Java: ").append(System.getProperty("java.runtime.version")).append("\n");
        report.append("- JavaFX: ").append(System.getProperty("javafx.version", "unknown")).append("\n");
        report.append("- 渲染尺寸: ").append(SCENE_WIDTH).append(" x ").append(SCENE_HEIGHT).append("\n");
        report.append("- 提示项样式: database/keyword/table/column/view/function/procedure 共 7 类\n");
        report.append("- SQLite 查询索引仅生成 database/keyword/table/view，不生成字段提示\n\n");
        report.append("| 对比 | 差异像素 | 总像素 | 最大通道差 | 差异边界 | 结果 |\n");
        report.append("| --- | ---: | ---: | ---: | --- | --- |\n");
        for (Diff diff : diffs) {
            report.append("| ").append(diff.name()).append(" | ")
                    .append(diff.differentPixels()).append(" | ")
                    .append(diff.width() * diff.height()).append(" | ")
                    .append(diff.maxChannelDelta()).append(" | ")
                    .append(diff.bounds()).append(" | ")
                    .append(diff.differentPixels() == 0 ? "PASS" : "FAIL").append(" |\n");
        }
        report.append("\n图片: mysql.png, dameng.png, sqlite.png, sqlite-vs-mysql-diff.png, sqlite-vs-dameng-diff.png\n");
        Files.writeString(dir.resolve("report.md"), report.toString(), StandardCharsets.UTF_8);
        return dir;
    }

    private void writeImage(WritableImage image, File file) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        buffered.setRGB(0, 0, width, height, this.pixels(image, width, height), 0, width);
        ImageIO.write(buffered, "png", file);
    }

    private void writeDiff(WritableImage left, WritableImage right, File file) throws IOException {
        int width = (int) left.getWidth();
        int height = (int) left.getHeight();
        int[] leftPixels = this.pixels(left, width, height);
        int[] rightPixels = this.pixels(right, width, height);
        BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (leftPixels[index] == rightPixels[index]) {
                    int rgb = leftPixels[index] & 0x00FFFFFF;
                    int gray = (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / 3;
                    buffered.setRGB(x, y, 0xFF000000 | (gray / 4 << 16) | (gray / 4 << 8) | (gray / 4));
                } else {
                    buffered.setRGB(x, y, 0xFFFF0033);
                }
            }
        }
        ImageIO.write(buffered, "png", file);
    }

    private List<ShellMysqlQueryPromptItem> mysqlItems() {
        List<ShellMysqlQueryPromptItem> items = new ArrayList<>();
        for (Row row : this.rows()) {
            ShellMysqlQueryPromptItem item = new ShellMysqlQueryPromptItem();
            this.apply(item, row);
            items.add(item);
        }
        return items;
    }

    private List<ShellDamengQueryPromptItem> damengItems() {
        List<ShellDamengQueryPromptItem> items = new ArrayList<>();
        for (Row row : this.rows()) {
            ShellDamengQueryPromptItem item = new ShellDamengQueryPromptItem();
            this.apply(item, row);
            items.add(item);
        }
        return items;
    }

    private List<ShellSqliteQueryPromptItem> sqliteItems() {
        List<ShellSqliteQueryPromptItem> items = new ArrayList<>();
        for (Row row : this.rows()) {
            ShellSqliteQueryPromptItem item = new ShellSqliteQueryPromptItem();
            this.apply(item, row);
            items.add(item);
        }
        return items;
    }

    private void apply(DBQueryPromptItem item, Row row) {
        item.setType(row.type());
        item.setContent(row.content());
        item.setExtContent(row.extContent());
        item.setCorrelation(1);
    }

    private List<Row> rows() {
        return List.of(
                new Row((byte) 1, "main", null),
                new Row((byte) 4, "SELECT", null),
                new Row((byte) 2, "prompt_item", "main"),
                new Row((byte) 3, "name", "\"main\".\"prompt_item\""),
                new Row((byte) 5, "prompt_view", "main"),
                new Row((byte) 6, "length", "main"),
                new Row((byte) 7, "refresh_cache", "main")
        );
    }

    private record Row(byte type, String content, String extContent) {
    }

    private record RenderResult(WritableImage mysql, WritableImage dameng, WritableImage sqlite) {
    }

    private record Diff(String name, int width, int height, int differentPixels,
                        int maxChannelDelta, int minX, int minY, int maxX, int maxY) {

        private String bounds() {
            if (this.differentPixels == 0) {
                return "none";
            }
            return "[" + this.minX + "," + this.minY + "] - [" + this.maxX + "," + this.maxY + "]";
        }

        @Override
        public String toString() {
            return this.name + ": differentPixels=" + this.differentPixels
                    + ", totalPixels=" + (this.width * this.height)
                    + ", maxChannelDelta=" + this.maxChannelDelta
                    + ", bounds=" + this.bounds();
        }
    }
}
