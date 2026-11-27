package cn.oyzh.easyshell.ui.connect;

import cn.oyzh.easyshell.controller.connect.ShellAddConnectGuidController;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXRadioButton;
import cn.oyzh.fx.plus.controls.pane.FXFlowPane;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.ext.FXMLResult;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ShellAddConnectGuidFxmlTest {

    private static final String FXML = "/fxml/connect/shellAddConnectGuid.fxml";

    @BeforeClass
    public static void startJavaFx() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                startup.countDown();
            });
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue(startup.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void guideLoadsWithAllProtocolsAndCategories() throws Throwable {
        FXMLResult result = load();
        Parent root = (Parent) result.getNode();
        assertTrue(result.getController() instanceof ShellAddConnectGuidController);
        assertTrue(root instanceof FXVBox);

        Parent sectionBox = sectionBox(result);
        assertEquals(30, allCards(sectionBox).size());
        assertEquals(6, sectionBox.lookupAll(".connect-guide-section").size());
        assertEquals(7, root.lookupAll(".connect-guide-category").size());
        assertTrue(root.lookup(".connect-guide-search") instanceof FXTextField);
        assertFalse(sectionBox.lookupAll(".connect-guide-tiles").stream()
                .anyMatch(node -> !(node instanceof FXFlowPane)));

        // 全 FX 前缀组件：分类导航与协议卡片均为 FXRadioButton
        assertTrue(root.lookupAll(".connect-guide-category").stream()
                .allMatch(node -> node instanceof FXRadioButton));
        assertTrue(allCards(sectionBox).stream().allMatch(node -> node instanceof FXRadioButton));
    }

    @Test
    public void initializeSetsI18nTextAndDefaultSelection() throws Throwable {
        FXMLResult result = load();
        Parent root = (Parent) result.getNode();

        FXLabelText subtitle = labelText(root, ".connect-guide-subtitle");
        FXLabelText selection = labelText(root, ".connect-guide-selection");
        FXLabelText resultCount = labelText(root, ".connect-guide-result");
        Labeled empty = (Labeled) sectionBox(result).lookup(".connect-guide-empty");

        assertFalse(subtitle.text.isEmpty());
        assertEquals(ShellI18nHelper.connectGuidSubtitle(), subtitle.text);
        assertNotNull(empty);
        assertEquals(ShellI18nHelper.connectGuidEmpty(), empty.getText());
        assertTrue(resultCount.text.contains("30"));
        assertTrue(selection.text.contains("SSH"));

        // 默认选中 ssh
        FXRadioButton selected = (FXRadioButton) field(result.getController(), "firstCard");
        assertNotNull(selected);
        assertTrue(allCards(sectionBox(result)).stream()
                .anyMatch(node -> node instanceof FXRadioButton radio && radio.isSelected()
                        && "ssh".equals(radio.getUserData())));
    }

    @Test
    public void searchAndCategoryFilterProtocolCards() throws Throwable {
        FXMLResult result = load();
        Parent root = (Parent) result.getNode();
        Parent sectionBox = sectionBox(result);
        FXTextField search = (FXTextField) root.lookup(".connect-guide-search");

        runOnFx(() -> search.setText("redis"));
        assertEquals(1, visibleCards(sectionBox).size());
        assertEquals("redis", visibleCards(sectionBox).getFirst().getUserData());

        // 搜索按协议 id 子串匹配（当前 FXML 未声明中文别名，故用 id 子串验证）
        runOnFx(() -> search.setText("s3"));
        assertEquals(5, visibleCards(sectionBox).size());

        runOnFx(() -> search.setText("zzzz-not-exist"));
        assertEquals(0, visibleCards(sectionBox).size());
        assertTrue(sectionBox.lookup(".connect-guide-empty").isManaged());

        runOnFx(search::clear);
        assertEquals(30, visibleCards(sectionBox).size());
        assertFalse(sectionBox.lookup(".connect-guide-empty").isManaged());

        FXRadioButton database = category(root, "DATABASE");
        runOnFx(database::fire);
        assertEquals(8, visibleCards(sectionBox).size());
        assertTrue(visibleCards(sectionBox).stream()
                .allMatch(node -> "DATABASE".equals(node.getParent().getParent().getUserData())));

        runOnFx(category(root, "ALL")::fire);
        assertEquals(30, visibleCards(sectionBox).size());
    }

    @Test
    public void everyFxmlCardHasAFactoryAndCategory() throws Throwable {
        FXMLResult result = load();
        Parent root = (Parent) result.getNode();
        Parent sectionBox = sectionBox(result);

        // 卡片协议 id 契约：新增/删除协议入口时同步维护这里
        List<String> expectedIds = List.of("dameng", "ftp", "http", "local", "mariadb", "mongodb",
                        "mosh", "mysql", "oracle", "postgresql", "rdp", "redis", "rlogin", "rsh",
                        "s3", "s3_cos", "s3_minio", "s3_obs", "s3_oss", "serial", "sftp", "smb",
                        "sqlite", "sqlserver", "ssh", "telnet", "valkey", "vnc", "webdav", "zookeeper")
                .stream().sorted().toList();
        List<String> cardIds = allCards(sectionBox).stream()
                .map(node -> (String) node.getUserData()).sorted().toList();
        assertEquals(30, expectedIds.size());
        assertEquals(expectedIds, cardIds);

        List<String> sectionCategories = sectionBox.lookupAll(".connect-guide-section").stream()
                .map(node -> (String) node.getUserData()).sorted().toList();
        List<String> navCategories = root.lookupAll(".connect-guide-category").stream()
                .map(node -> (String) node.getUserData())
                .filter(id -> !"ALL".equals(id)).sorted().toList();
        assertEquals(navCategories, sectionCategories);
    }

    @Test
    public void searchFieldKeepsFixedHeightForEveryCategory() throws Throwable {
        FXMLResult result = load();
        Parent root = (Parent) result.getNode();
        Node search = root.lookup(".connect-guide-search");
        assertNotNull(search);

        AtomicReference<Scene> sceneRef = new AtomicReference<>();
        runOnFx(() -> sceneRef.set(new Scene(root, 960, 640)));

        // 「全部」会把 30 张卡片全部渲染，最容易把父 VBox 拖入收缩模式；
        // 逐个分类切换后，搜索框/工具栏/底部栏必须恒定保持 30px，滚动区高度不得随分类漂移。
        Double referenceScroll = null;
        for (String id : List.of("ALL", "TERMINAL", "FILE", "S3", "REMOTE", "DATABASE", "OTHER", "ALL")) {
            Heights h = measureHeight(sceneRef.get(), root, category(root, id), search);
            assertEquals("分类 " + id + "：搜索框应保持预设 30 高度", 30.0, h.search(), 0.5);
            assertEquals("分类 " + id + "：工具栏应保持 30 高度", 30.0, h.toolbar(), 0.5);
            assertEquals("分类 " + id + "：底部栏应保持 30 高度", 30.0, h.footer(), 0.5);
            assertTrue("分类 " + id + "：滚动区应有可用高度", h.scroll() > 0);
            if (referenceScroll == null) {
                referenceScroll = h.scroll();
            } else {
                assertEquals("分类 " + id + "：滚动区高度不应随分类变化",
                        referenceScroll, h.scroll(), 1.0);
            }
        }
    }

    private record Heights(double search, double toolbar, double scroll, double footer) {
    }

    private static Heights measureHeight(Scene scene, Parent root, FXRadioButton category, Node target) throws Throwable {
        AtomicReference<Heights> heights = new AtomicReference<>();
        runOnFx(() -> {
            category.fire();
            root.applyCss();
            root.layout();
            scene.snapshot(null);
            root.applyCss();
            root.layout();
            heights.set(new Heights(
                    ((javafx.scene.layout.Region) target).getHeight(),
                    ((javafx.scene.layout.Region) target.getParent()).getHeight(),
                    ((javafx.scene.layout.Region) root.lookup(".connect-guide-scroll")).getHeight(),
                    ((javafx.scene.layout.Region) root.lookup(".connect-guide-footer")).getHeight()));
        });
        return heights.get();
    }

    @Test
    public void hiddenSelectionIsClearedAndNextStepDisabled() throws Throwable {
        FXMLResult result = load();
        Parent root = (Parent) result.getNode();
        Parent sectionBox = sectionBox(result);
        FXTextField search = (FXTextField) root.lookup(".connect-guide-search");
        Object cardGroup = field(result.getController(), "cardGroup");

        // 选中项被搜索过滤掉后应取消选中，且「下一步」禁用
        runOnFx(() -> search.setText("mysql"));
        assertEquals(1, visibleCards(sectionBox).size());
        assertFalse(((javafx.scene.control.ToggleGroup) cardGroup).getSelectedToggle() != null
                && "ssh".equals(((javafx.scene.control.ToggleGroup) cardGroup)
                .getSelectedToggle().getUserData()));

        Labeled selection = (Labeled) root.lookup(".connect-guide-selection");
        assertTrue(selection.getText() == null || selection.getText().isEmpty());
        assertTrue(((javafx.scene.Node) field(result.getController(), "nextButton")).isDisable());
    }

    private static FXRadioButton category(Parent root, String id) {
        return (FXRadioButton) root.lookupAll(".connect-guide-category").stream()
                .filter(node -> id.equals(node.getUserData()))
                .findFirst().orElseThrow();
    }

    private static FXLabelText labelText(Parent root, String selector) {
        Node node = root.lookup(selector);
        assertNotNull(node);
        assertTrue(node instanceof Labeled);
        return new FXLabelText(((Labeled) node).getText());
    }

    private record FXLabelText(String text) {
    }

    private static Parent sectionBox(FXMLResult result) throws Exception {
        return (Parent) field(result.getController(), "sectionBox");
    }

    private static List<Node> allCards(Parent sectionBox) {
        return sectionBox.lookupAll(".connect-type-card").stream().toList();
    }

    private static List<Node> visibleCards(Parent sectionBox) {
        return allCards(sectionBox).stream().filter(Node::isManaged).toList();
    }

    private static Object field(Object target, String name) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static FXMLResult load() throws Throwable {
        AtomicReference<FXMLResult> result = new AtomicReference<>();
        runOnFx(() -> {
            FXMLResult loaded = FXMLLoaderExt.loadFromUrl(FXML);
            if (loaded != null && loaded.getNode() instanceof Parent parent) {
                parent.applyCss();
                parent.layout();
            }
            result.set(loaded);
        });
        assertNotNull(result.get());
        return result.get();
    }

    private static void runOnFx(Runnable action) throws Throwable {
        Throwable[] error = new Throwable[1];
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable ex) {
                error[0] = ex;
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(20, TimeUnit.SECONDS));
        if (error[0] != null) {
            throw error[0];
        }
    }
}
