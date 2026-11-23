package cn.oyzh.easyshell.ui.llm;

import cn.oyzh.easyshell.controller.llm.ShellLLMAddModelController;
import cn.oyzh.easyshell.controller.llm.ShellLLMModelListController;
import cn.oyzh.easyshell.controller.llm.ShellLLMUpdateModelController;
import cn.oyzh.easyshell.fx.llm.LLMCustomModelTableView;
import cn.oyzh.easyshell.fx.llm.LLMModelContextWindowComboBox;
import cn.oyzh.easyshell.llm.LLMModelContextWindowUtil;
import cn.oyzh.fx.plus.ext.FXMLResult;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import javafx.application.Platform;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * LLM 模型管理 FXML 装载测试。
 */
public class LLMModelFxmlTest {

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
    public void listAndFormLoad() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLResult listResult = FXMLLoaderExt.loadFromUrl("/fxml/llm/shellLLMModelList.fxml");
                assertTrue(listResult.getController() instanceof ShellLLMModelListController);
                Field tableField = ShellLLMModelListController.class.getDeclaredField("modelTableView");
                tableField.setAccessible(true);
                assertNotNull(tableField.get(listResult.getController()));
                assertTrue(tableField.get(listResult.getController()) instanceof LLMCustomModelTableView);

                FXMLResult addResult = FXMLLoaderExt.loadFromUrl("/fxml/llm/shellLLMAddModel.fxml");
                assertTrue(addResult.getController() instanceof ShellLLMAddModelController);
                assertContextWindow(ShellLLMAddModelController.class, addResult.getController());

                FXMLResult updateResult = FXMLLoaderExt.loadFromUrl("/fxml/llm/shellLLMUpdateModel.fxml");
                assertTrue(updateResult.getController() instanceof ShellLLMUpdateModelController);
                assertContextWindow(ShellLLMUpdateModelController.class, updateResult.getController());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("LLM model FXML load timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("LLM model FXML load failed", error.get());
        }
    }

    private static void assertContextWindow(Class<?> controllerType, Object controller) throws Exception {
        Field contextField = controllerType.getDeclaredField("contextWindow");
        contextField.setAccessible(true);
        LLMModelContextWindowComboBox contextWindow =
                (LLMModelContextWindowComboBox) contextField.get(controller);
        assertNotNull(contextWindow);
        assertFalse(contextWindow.getItems().isEmpty());
        assertEquals(LLMModelContextWindowUtil.DEFAULT_TOKENS,
                contextWindow.getSelectedTokens());
    }
}
