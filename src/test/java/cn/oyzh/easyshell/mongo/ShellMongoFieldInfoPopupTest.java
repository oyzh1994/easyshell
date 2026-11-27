package cn.oyzh.easyshell.mongo;

import cn.oyzh.easyshell.fx.mongo.ShellMongoRecordColumn;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.popups.mongo.ShellMongoFieldInfoPopupController;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.mongo.ShellMongoNodeUtil;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupAttribute;
import cn.oyzh.fx.plus.window.PopupManager;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * MongoDB 字段信息弹窗测试。
 *
 * @since 2026-11-27
 */
public class ShellMongoFieldInfoPopupTest {

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
    public void testPopupResource() {
        PopupAttribute attribute = ShellMongoFieldInfoPopupController.class.getAnnotation(PopupAttribute.class);
        assertNotNull(attribute);
        assertEquals("/popups/mongo/shellMongoFieldInfoPopup.fxml", attribute.value());
        assertNotNull("字段信息弹窗的 fxml 不存在", this.getClass().getResource(attribute.value()));
        assertEquals(1, attribute.cssUrls().length);
        assertNotNull("字段信息弹窗的样式表不存在", this.getClass().getResource(attribute.cssUrls()[0]));
    }

    @Test
    public void testPopupFillsColumnInfo() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                MongoColumn column = new MongoColumn("order_no");
                column.setType("string");
                column.setDbName("demo");
                column.setCollectionName("orders");

                PopupAdapter popup = PopupManager.parsePopup(ShellMongoFieldInfoPopupController.class);
                assertNotNull(popup);
                assertTrue(popup.content() instanceof FXVBox);
                assertTrue(popup.getController() instanceof ShellMongoFieldInfoPopupController);

                ShellMongoFieldInfoPopupController controller = (ShellMongoFieldInfoPopupController) popup.getController();
                controller.fillColumn(column);
                assertEquals("order_no", this.text(controller, "name"));
                assertEquals("STRING", this.text(controller, "type"));
                assertEquals("demo", this.text(controller, "database"));
                assertEquals("orders", this.text(controller, "collection"));
                // 普通字段没有标签，标签行应当隐藏
                FXHBox tagsBox = (FXHBox) this.node(controller, "tagsBox");
                assertFalse(tagsBox.isVisible());
                double baseHeight = ((FXVBox) popup.content()).getPrefHeight();

                // 主键字段会显示标签行
                MongoColumn id = new MongoColumn("_id");
                id.setType("objectid");
                id.setDbName("demo");
                id.setCollectionName("orders");
                controller.fillColumn(id);
                assertTrue(tagsBox.isVisible());
                FXHBox tagsContainer = (FXHBox) this.node(controller, "tagsContainer");
                assertEquals(1, tagsContainer.getChildren().size());
                assertEquals(I18nHelper.primaryKey(), ((Label) tagsContainer.getChildren().get(0)).getText());
                // 标签行让弹窗高出 30
                assertEquals(baseHeight + 30, ((FXVBox) popup.content()).getPrefHeight(), 0.5);
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("Mongo 字段信息弹窗测试超时", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Mongo 字段信息弹窗测试失败", error.get());
        }
    }

    @Test
    public void testGenerateTags() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // 主键字段
                MongoColumn id = new MongoColumn("_id");
                id.setType("objectid");
                List<FXLabel> tags = ShellMongoNodeUtil.generateTags(id);
                assertEquals(1, tags.size());
                assertEquals(I18nHelper.primaryKey(), tags.get(0).getText());

                // 嵌套字段
                MongoColumn nested = new MongoColumn("addr.city");
                nested.setType("string");
                tags = ShellMongoNodeUtil.generateTags(nested);
                assertEquals(1, tags.size());
                assertEquals(ShellI18nHelper.mongoNestedField(), tags.get(0).getText());

                // 普通字段
                MongoColumn normal = new MongoColumn("order_no");
                normal.setType("string");
                assertTrue(ShellMongoNodeUtil.generateTags(normal).isEmpty());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("Mongo 字段标签测试超时", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Mongo 字段标签测试失败", error.get());
        }
    }

    @Test
    public void testRecordColumnHasColumnInfoMenu() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                MongoColumn column = new MongoColumn("name");
                column.setType("string");
                ShellMongoRecordColumn recordColumn = new ShellMongoRecordColumn(column);
                // 字段信息、复制字段名、分隔符、显示字段类型、显示注释
                assertEquals(5, recordColumn.getMenuItems().size());
                assertEquals(I18nHelper.columnInfo(), recordColumn.getMenuItems().get(0).getText());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("Mongo 字段信息菜单测试超时", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Mongo 字段信息菜单测试失败", error.get());
        }
    }

    /**
     * 读取控制器里的节点
     */
    private Object node(ShellMongoFieldInfoPopupController controller, String name) throws Exception {
        Field field = ShellMongoFieldInfoPopupController.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(controller);
    }

    /**
     * 读取控制器里的输入框文本
     */
    private String text(ShellMongoFieldInfoPopupController controller, String name) throws Exception {
        Field field = ShellMongoFieldInfoPopupController.class.getDeclaredField(name);
        field.setAccessible(true);
        TextField textField = (TextField) field.get(controller);
        assertNotNull(name + " 未注入", textField);
        return textField.getText();
    }
}
