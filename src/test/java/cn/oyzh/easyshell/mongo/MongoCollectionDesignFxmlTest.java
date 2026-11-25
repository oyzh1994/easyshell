package cn.oyzh.easyshell.mongo;

import cn.oyzh.easyshell.tabs.mongo.collection.ShellMongoCollectionDesignTabController;
import cn.oyzh.easyshell.tabs.mongo.view.ShellMongoViewDesignTabController;
import cn.oyzh.easyshell.tabs.mongo.view.ShellMongoViewRecordTabController;
import cn.oyzh.easyshell.controller.mongo.collection.ShellMongoCollectionInfoController;
import cn.oyzh.easyshell.controller.mongo.function.ShellMongoFunctionInfoController;
import cn.oyzh.easyshell.controller.mongo.view.ShellMongoViewInfoController;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * MongoDB 集合设计页 FXML 加载测试。
 */
public class MongoCollectionDesignFxmlTest {

    @Test
    public void collectionDesignFxmlLoads() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue(startup.await(10, TimeUnit.SECONDS));

        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch loaded = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/tabs/mongo/collection/shellMongoCollectionDesignTab.fxml"));
                loader.setResources(I18nResourceBundle.INSTANCE);
                Parent root = loader.load();
                assertNotNull(root);
                assertTrue(loader.getController() instanceof ShellMongoCollectionDesignTabController);
                for (String fieldName : List.of(
                        "tabPane", "capped", "sizeInBytes", "maxDocuments",
                        "validationLevel", "validationAction", "validator", "indexTable", "preview")) {
                    Field field = loader.getController().getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    assertNotNull(fieldName, field.get(loader.getController()));
                }
                Field tabPaneField = loader.getController().getClass().getDeclaredField("tabPane");
                tabPaneField.setAccessible(true);
                FXTabPane tabPane = (FXTabPane) tabPaneField.get(loader.getController());
                assertEquals("indexTab", tabPane.getTabs().get(0).getId());
                assertEquals("validatorTab", tabPane.getTabs().get(1).getId());
                assertEquals("optionsTab", tabPane.getTabs().get(2).getId());
                assertEquals("previewTab", tabPane.getTabs().get(3).getId());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                loaded.countDown();
            }
        });
        assertTrue(loaded.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @Test
    public void viewDesignFxmlLoads() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue(startup.await(10, TimeUnit.SECONDS));

        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch loaded = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/tabs/mongo/view/shellMongoViewDesignTab.fxml"));
                loader.setResources(I18nResourceBundle.INSTANCE);
                Parent root = loader.load();
                assertNotNull(root);
                assertTrue(loader.getController() instanceof ShellMongoViewDesignTabController);
                for (String fieldName : List.of("tabPane", "viewOn", "pipeline", "preview")) {
                    Field field = loader.getController().getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    assertNotNull(fieldName, field.get(loader.getController()));
                }
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                loaded.countDown();
            }
        });
        assertTrue(loaded.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @Test
    public void viewRecordFxmlLoads() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue(startup.await(10, TimeUnit.SECONDS));

        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch loaded = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/tabs/mongo/view/shellMongoViewRecordTab.fxml"));
                loader.setResources(I18nResourceBundle.INSTANCE);
                Parent root = loader.load();
                assertNotNull(root);
                assertTrue(loader.getController() instanceof ShellMongoViewRecordTabController);
                for (String fieldName : List.of("root", "filter", "pageBox", "recordTable")) {
                    Field field = loader.getController().getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    assertNotNull(fieldName, field.get(loader.getController()));
                }
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                loaded.countDown();
            }
        });
        assertTrue(loaded.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @Test
    public void mongoInfoFxmlLoads() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue(startup.await(10, TimeUnit.SECONDS));

        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch loaded = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                for (String resource : List.of(
                        "/fxml/mongo/collection/shellMongoCollectionInfo.fxml",
                        "/fxml/mongo/function/shellMongoFunctionInfo.fxml",
                        "/fxml/mongo/view/shellMongoViewInfo.fxml")) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
                    loader.setResources(I18nResourceBundle.INSTANCE);
                    Parent root = loader.load();
                    assertNotNull(resource, root);
                    Object controller = loader.getController();
                    assertNotNull(resource, controller);
                    if (resource.contains("/collection/")) {
                        assertTrue(controller instanceof ShellMongoCollectionInfoController);
                    } else if (resource.contains("/function/")) {
                        assertTrue(controller instanceof ShellMongoFunctionInfoController);
                    } else {
                        assertTrue(controller instanceof ShellMongoViewInfoController);
                    }
                }
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                loaded.countDown();
            }
        });
        assertTrue(loaded.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }
}
