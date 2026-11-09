package cn.oyzh.easyshell.http;

import cn.oyzh.easyshell.controller.connect.http.ShellAddHttpConnectController;
import cn.oyzh.easyshell.controller.connect.http.ShellUpdateHttpConnectController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.http.ShellHttpCollection;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.tabs.http.ShellHttpTabController;
import cn.oyzh.easyshell.trees.http.ShellHttpTreeView;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * HTTP 协议接入与资源完整性测试。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpArchitectureTest {

    @Test
    public void clientAndConnectTypeAreWired() {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.HTTP);
        connect.setName("HTTP");
        assertTrue(connect.isHttpType());
        assertTrue(ShellClientUtil.newClient(connect) instanceof ShellHttpClient);
    }

    @Test
    public void controllerResourcesAreDeclaredAndParseable() throws Exception {
        StageAttribute add = ShellAddHttpConnectController.class.getAnnotation(StageAttribute.class);
        StageAttribute update = ShellUpdateHttpConnectController.class.getAnnotation(StageAttribute.class);
        assertNotNull(add);
        assertNotNull(update);
        assertEquals("/fxml/connect/http/shellAddHttpConnect.fxml", add.value());
        assertEquals("/fxml/connect/http/shellUpdateHttpConnect.fxml", update.value());

        for (String resource : List.of(add.value(), update.value(), "/tabs/http/shellHttpTab.fxml",
                "/tabs/http/home/shellHttpHomeTab.fxml",
                "/tabs/http/request/shellHttpRequestTab.fxml",
                "/tabs/http/environment/shellHttpEnvironmentTab.fxml",
                "/tabs/http/settings/shellHttpSettingsTab.fxml")) {
            URL url = ShellHttpArchitectureTest.class.getResource(resource);
            assertNotNull(resource, url);
            try (InputStream input = url.openStream()) {
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
            }
        }
    }

    @Test
    public void tabFxmlLoadsAndBuildsThreeTopLevelNodes() throws Exception {
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
                FXMLLoader loader = new FXMLLoader(
                        ShellHttpArchitectureTest.class.getResource("/tabs/http/shellHttpTab.fxml"));
                loader.setResources(cn.oyzh.i18n.I18nResourceBundle.INSTANCE);
                Parent root = loader.load();
                assertNotNull(root);
                assertTrue(loader.getController() instanceof ShellHttpTabController);
                ShellHttpTreeView treeView = new ShellHttpTreeView();
                assertNotNull(treeView.getItemFilter());
                assertTrue(treeView.getSelectionModel().getSelectionMode() == javafx.scene.control.SelectionMode.SINGLE);
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
    public void tabPagesLoad() throws Exception {
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
                        "/tabs/http/home/shellHttpHomeTab.fxml",
                        "/tabs/http/request/shellHttpRequestTab.fxml",
                        "/tabs/http/environment/shellHttpEnvironmentTab.fxml",
                        "/tabs/http/settings/shellHttpSettingsTab.fxml")) {
                    FXMLLoader loader = new FXMLLoader(ShellHttpArchitectureTest.class.getResource(resource));
                    loader.setResources(cn.oyzh.i18n.I18nResourceBundle.INSTANCE);
                    Parent root = loader.load();
                    assertNotNull(resource, root);
                    assertNotNull(resource, loader.getController());
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
    public void treeLoadsCollectionsAndRequests() {
        ShellHttpCollection collection = new ShellHttpCollection();
        collection.setName("Example");
        ShellHttpRequest request = new ShellHttpRequest();
        request.setName("Hello");
        collection.getRequests().add(request);

        ShellHttpWorkspace workspace = new ShellHttpWorkspace();
        workspace.setIid("test");
        workspace.getCollections().add(collection);

        ShellHttpTreeView treeView = new ShellHttpTreeView();
        treeView.init(workspace, null, null);

        assertNotNull(treeView.collectionsItem());
        assertEquals(1, treeView.collectionsItem().unfilteredChildren().size());
        assertTrue(treeView.collectionsItem().unfilteredChildren().getFirst()
                instanceof cn.oyzh.easyshell.trees.http.collection.ShellHttpCollectionTreeItem);
        assertEquals(1, treeView.collectionsItem().unfilteredChildren().getFirst()
                .getChildren().size());
    }


    @Test
    public void httpI18nKeysExistInAllLocales() throws Exception {
        String[] locales = {"en", "zh_CN", "zh_TW", "de", "fr_FR", "it_IT", "ja_JP", "ko_KR"};
        java.util.Properties base = new java.util.Properties();
        try (InputStream input = ShellHttpArchitectureTest.class.getResourceAsStream("/i18n_en.properties")) {
            assertNotNull(input);
            base.load(input);
        }
        java.util.List<String> keys = base.stringPropertyNames().stream()
                .filter(key -> key.startsWith("shell.http."))
                .sorted()
                .toList();
        assertTrue(keys.size() > 50);
        for (String locale : locales) {
            java.util.Properties properties = new java.util.Properties();
            try (InputStream input = ShellHttpArchitectureTest.class.getResourceAsStream("/i18n_" + locale + ".properties")) {
                assertNotNull(locale, input);
                properties.load(input);
            }
            for (String key : keys) {
                assertTrue(locale + " missing " + key, properties.containsKey(key));
                assertTrue(locale + " blank " + key, !properties.getProperty(key).isBlank());
            }
        }
    }


    @Test
    public void defaultsToHttp11ForCompatibility() {
        cn.oyzh.easyshell.domain.http.ShellHttpSetting settings = new cn.oyzh.easyshell.domain.http.ShellHttpSetting();
        assertEquals(cn.oyzh.easyshell.domain.http.ShellHttpSetting.VERSION_HTTP_1_1, settings.getHttpVersion());
        settings.setHttpVersion(cn.oyzh.easyshell.domain.http.ShellHttpSetting.VERSION_HTTP_2);
        assertEquals(cn.oyzh.easyshell.domain.http.ShellHttpSetting.VERSION_HTTP_2,
                settings.copy().getHttpVersion());
    }


    @Test
    public void requestAndEnvironmentJsonColumnsRoundTrip() {
        ShellHttpRequest request = new ShellHttpRequest();
        ShellHttpKeyValue param = new ShellHttpKeyValue("q", "value");
        param.setDescription("query description");
        request.getParams().add(param);
        request.getHeaders().add(new ShellHttpKeyValue("X-Test", "1"));
        ShellHttpKeyValue formRow = new ShellHttpKeyValue("field", "2");
        formRow.setDescription("form description");
        formRow.setFile(true);
        request.getFormBody().add(formRow);
        request.syncJson();

        ShellHttpRequest loaded = new ShellHttpRequest();
        loaded.setParamsJson(request.getParamsJson());
        loaded.setHeadersJson(request.getHeadersJson());
        loaded.setFormBodyJson(request.getFormBodyJson());
        assertEquals(1, loaded.getParams().size());
        assertEquals("q", loaded.getParams().getFirst().getKey());
        assertEquals("query description", loaded.getParams().getFirst().getDescription());
        assertEquals(1, loaded.getHeaders().size());
        assertEquals(1, loaded.getFormBody().size());
        assertTrue(loaded.getFormBody().getFirst().isFile());
        assertEquals("form description", loaded.getFormBody().getFirst().getDescription());

        ShellHttpEnvironment environment = new ShellHttpEnvironment();
        environment.getValues().add(new ShellHttpKeyValue("baseUrl", "https://example.com"));
        environment.syncJson();
        ShellHttpEnvironment loadedEnvironment = new ShellHttpEnvironment();
        loadedEnvironment.setValuesJson(environment.getValuesJson());
        assertEquals("baseUrl", loadedEnvironment.getValues().getFirst().getKey());
    }

}
