package cn.oyzh.easyshell.ui.llm;

import cn.oyzh.easyshell.controller.llm.ShellLLMAddMcpServerController;
import cn.oyzh.easyshell.controller.llm.ChatController;
import cn.oyzh.easyshell.controller.llm.ShellLLMMcpServerListController;
import cn.oyzh.easyshell.controller.llm.ShellLLMMcpImportController;
import cn.oyzh.easyshell.controller.llm.ShellLLMUpdateMcpServerController;
import cn.oyzh.fx.gui.svg.glyph.JSONSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.llm.TestSVGGlyph;
import cn.oyzh.fx.plus.ext.FXMLResult;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LLMMcpFxmlTest {

    @BeforeClass
    public static void startJavaFx() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue(startup.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void mcpPagesLoadWithExpectedControllers() throws Throwable {
        assertFxml("/fxml/llm/chat.fxml", ChatController.class);
        assertFxml("/fxml/llm/shellLLMMcpServerList.fxml", ShellLLMMcpServerListController.class);
        assertFxml("/fxml/llm/shellLLMMcpImport.fxml", ShellLLMMcpImportController.class);
        assertFxml("/fxml/llm/shellLLMAddMcpServer.fxml", ShellLLMAddMcpServerController.class);
        assertFxml("/fxml/llm/shellLLMUpdateMcpServer.fxml", ShellLLMUpdateMcpServerController.class);
    }

    @Test
    public void mcpListUsesRequestedTestAndJsonGlyphs() throws Throwable {
        Throwable[] error = new Throwable[1];
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLResult result = FXMLLoaderExt.loadFromUrl("/fxml/llm/shellLLMMcpServerList.fxml");
                Parent root = (Parent) result.getNode();
                new Scene(root, 980, 560);
                root.applyCss();
                root.layout();
                assertTrue(root.lookup("#testGlyph") instanceof TestSVGGlyph);
                assertTrue(root.lookup("#jsonGlyph") instanceof JSONSVGGlyph);
            } catch (Throwable ex) {
                error[0] = ex;
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(20, TimeUnit.SECONDS));
        if (error[0] != null) {
            throw new AssertionError("MCP icon UI verification failed", error[0]);
        }
    }

    private static void assertFxml(String resource, Class<?> controllerType) throws Throwable {
        Throwable[] error = new Throwable[1];
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLResult result = FXMLLoaderExt.loadFromUrl(resource);
                assertNotNull(result.getNode());
                assertTrue(controllerType.isInstance(result.getController()));
            } catch (Throwable ex) {
                error[0] = ex;
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(20, TimeUnit.SECONDS));
        if (error[0] != null) {
            throw new AssertionError("FXML load failed: " + resource, error[0]);
        }
    }
}
