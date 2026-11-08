package cn.oyzh.easyshell.http;

import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.fx.http.ShellHttpKeyValueTableView;
import cn.oyzh.easyshell.tabs.http.request.ShellHttpRequestTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ShellHttpRequestSyncTest {

    @Test
    public void paramsChangeUpdatesUrlAndDisableRemovesQuery() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue(startup.await(10, TimeUnit.SECONDS));
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(ShellHttpRequestSyncTest.class.getResource(
                        "/tabs/http/request/shellHttpRequestTab.fxml"));
                loader.setResources(cn.oyzh.i18n.I18nResourceBundle.INSTANCE);
                Parent root = loader.load();
                ShellHttpRequestTabController controller = loader.getController();
                Field requestField = ShellHttpRequestTabController.class.getDeclaredField("request");
                requestField.setAccessible(true);
                requestField.set(controller, new ShellHttpRequest());
                ClearableTextField url = field(controller, "requestUrl");
                ShellHttpKeyValueTableView params = field(controller, "paramsTable");
                url.setText("http://example.com/api");
                Method bind = ShellHttpRequestTabController.class.getDeclaredMethod("bindParamsChange");
                bind.setAccessible(true);
                bind.invoke(controller);
                ShellHttpKeyValue row = new ShellHttpKeyValue("a", "1");
                params.getItems().add(row);
                assertEquals("http://example.com/api?a=1", url.getText());
                row.setEnabled(false);
                assertEquals("http://example.com/api", url.getText());
                row.setEnabled(true);
                assertEquals("http://example.com/api?a=1", url.getText());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    private static <T> T field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(object);
    }
}
