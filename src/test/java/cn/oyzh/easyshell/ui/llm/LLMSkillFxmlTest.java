package cn.oyzh.easyshell.ui.llm;

import cn.oyzh.easyshell.controller.llm.ShellChatController;
import cn.oyzh.easyshell.controller.llm.ShellLLMAddSkillController;
import cn.oyzh.easyshell.controller.llm.ShellLLMSkillListController;
import cn.oyzh.easyshell.controller.llm.ShellLLMUpdateSkillController;
import cn.oyzh.fx.gui.svg.glyph.llm.SkillSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.RefreshSVGGlyph;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.ext.FXMLResult;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LLMSkillFxmlTest {

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
    public void skillPagesLoadWithExpectedControllers() throws Throwable {
        assertFxml("/fxml/llm/chat.fxml", ShellChatController.class, SkillSVGGlyph.class);
        assertFxml("/fxml/llm/shellLLMSkillList.fxml", ShellLLMSkillListController.class,
                RefreshSVGGlyph.class);
        assertFxml("/fxml/llm/shellLLMAddSkill.fxml", ShellLLMAddSkillController.class, null);
        assertFxml("/fxml/llm/shellLLMUpdateSkill.fxml", ShellLLMUpdateSkillController.class, null);
    }

    @Test
    public void addAndUpdatePagesDoNotExposeEnabledSwitch() throws Throwable {
        assertNoNode("/fxml/llm/shellLLMAddSkill.fxml", FXCheckBox.class);
        assertNoNode("/fxml/llm/shellLLMUpdateSkill.fxml", FXCheckBox.class);
    }

    private static void assertFxml(String resource,
                                   Class<?> controllerType,
                                   Class<? extends Node> requiredNode) throws Throwable {
        Throwable[] error = new Throwable[1];
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLResult result = FXMLLoaderExt.loadFromUrl(resource);
                assertNotNull(result.getNode());
                assertTrue(controllerType.isInstance(result.getController()));
                if (requiredNode != null) {
                    assertNotNull(findNode((Parent) result.getNode(), requiredNode));
                }
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

    private static void assertNoNode(String resource, Class<? extends Node> type) throws Throwable {
        Throwable[] error = new Throwable[1];
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLResult result = FXMLLoaderExt.loadFromUrl(resource);
                assertNull(findNode((Parent) result.getNode(), type));
            } catch (Throwable ex) {
                error[0] = ex;
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(20, TimeUnit.SECONDS));
        if (error[0] != null) {
            throw new AssertionError("FXML verification failed: " + resource, error[0]);
        }
    }

    private static <T extends Node> T findNode(Parent parent, Class<T> type) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (type.isInstance(child)) {
                return type.cast(child);
            }
            if (child instanceof Parent childParent) {
                T result = findNode(childParent, type);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
