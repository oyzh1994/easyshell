package cn.oyzh.easyshell.trees;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class SequenceDesignRefreshTest {

    @Test
    public void sequenceDesignTabsCanReloadExistingSequences() throws Exception {
        List<String> dialects = List.of("dameng", "oracle", "postgresql", "sqlserver");
        for (String dialect : dialects) {
            String controllerName = switch (dialect) {
                case "dameng" -> "ShellDamengSequenceDesignTabController.java";
                case "oracle" -> "ShellOracleSequenceDesignTabController.java";
                case "postgresql" -> "ShellPostgresqlSequenceDesignTabController.java";
                case "sqlserver" -> "ShellSqlServerSequenceDesignTabController.java";
                default -> throw new IllegalStateException(dialect);
            };
            Path controller = Path.of("src/main/java/cn/oyzh/easyshell/tabs", dialect, "sequence", controllerName);
            String fxmlName = Character.toLowerCase(controllerName.charAt(0))
                    + controllerName.substring(1).replace("Controller.java", ".fxml");
            Path fxml = Path.of("src/main/resources/tabs", dialect, "sequence",
                    fxmlName);

            String controllerSource = Files.readString(controller);
            String fxmlSource = Files.readString(fxml);
            assertTrue(controller + " does not show refresh for existing sequences",
                    controllerSource.contains("NodeGroupUtil.display(this.getTab(), \"action3\")"));
            assertTrue(controller + " does not hide refresh for new sequences",
                    controllerSource.contains("NodeGroupUtil.disappear(this.getTab(), \"action3\")"));
            assertTrue(controller + " does not reload sequence data",
                    controllerSource.contains("this.init(this.sequence, this.dbItem);"));
            assertTrue(fxml + " has no refresh action",
                    fxmlSource.contains("onMousePrimaryClicked=\"#refresh\""));
        }
    }
}
