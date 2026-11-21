package cn.oyzh.easyshell.ui.llm;

import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.ext.FXMLResult;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import javafx.application.Platform;
import javafx.scene.Parent;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LLMManagementSearchFxmlTest {

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
    public void allManagementListsExposeClearableSearchFields() throws Exception {
        for (String resource : List.of(
                "/fxml/llm/shellLLMSessionList.fxml",
                "/fxml/llm/shellLLMMcpServerList.fxml",
                "/fxml/llm/shellLLMSkillList.fxml",
                "/fxml/llm/shellLLMProviderList.fxml",
                "/fxml/llm/shellLLMPromptList.fxml")) {
            assertSearchField(resource);
        }
    }

    private static void assertSearchField(String resource) throws Exception {
        Throwable[] error = new Throwable[1];
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLResult result = FXMLLoaderExt.loadFromUrl(resource);
                Parent root = (Parent) result.getNode();
                assertNotNull(resource, root.lookup("#searchField"));
                assertTrue(root.lookup("#searchField") instanceof ClearableTextField);
            } catch (Throwable ex) {
                error[0] = ex;
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(20, TimeUnit.SECONDS));
        if (error[0] != null) {
            throw new AssertionError("Search field verification failed: " + resource, error[0]);
        }
    }
}
