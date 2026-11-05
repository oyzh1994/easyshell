package cn.oyzh.easyshell.internal;

import cn.oyzh.common.dto.Project;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.local.ShellLocalLLMTools;
import dev.langchain4j.agent.tool.Tool;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShellLLMToolsTest {

    @Test
    public void returnsProjectAndJdkInformation() {
        ShellLLMTools tools = new ShellLLMTools();
        Project project = Project.load();

        String projectInfo = tools.getProjectInfo();
        String projectVersion = tools.getProjectVersion();
        String jdkVersion = tools.getJdkVersion();
        String projectPaths = tools.getProjectPaths();

        assertTrue(projectInfo.contains(project.getName()));
        assertTrue(projectInfo.contains(project.getVersion()));
        assertTrue(projectVersion.contains(project.getVersion()));
        assertTrue(jdkVersion.contains(Runtime.version().toString()));
        assertTrue(projectPaths.contains(ShellConst.getStorePath()));
        assertTrue(projectPaths.contains("llmToolAuditFile"));
    }

    @Test
    public void returnsCapabilitiesAndSystemMessage() {
        ShellLLMTools tools = new ShellLLMTools();

        String capabilities = tools.getApplicationCapabilities();
        String systemMessage = tools.systemMessage(LLMExecutionMode.APPROVE);

        assertTrue(capabilities.contains("terminalProtocols"));
        assertTrue(capabilities.contains("llmFeatures"));
        assertTrue(systemMessage.contains("EasyShell"));
        assertTrue(systemMessage.contains("startXServer"));
        assertTrue(systemMessage.contains("executeLocalCommandDirect"));
    }

    @Test
    public void parsesSafeTypeOptionsWithoutPersistingConnection() throws Exception {
        ShellLLMTools tools = new ShellLLMTools();
        Method applyTypeOptions = ShellLLMTools.class.getDeclaredMethod(
                "applyTypeOptions", ShellConnect.class, String.class, String.class, String.class);
        applyTypeOptions.setAccessible(true);

        ShellConnect connection = new ShellConnect();
        applyTypeOptions.invoke(tools, connection, "zookeeper", "",
                "{\"sessionTimeoutMinutes\":45,\"zkCompatibility\":1,\"saslAuth\":true,"
                        + "\"backspaceType\":2,\"altSendsEscape\":true}");

        assertEquals(Integer.valueOf(45), connection.getSessionTimeOut());
        assertEquals(Integer.valueOf(1), connection.getCompatibility());
        assertEquals(Integer.valueOf(2), connection.getBackspaceType());
        assertTrue(connection.isSASLAuth());
        assertTrue(connection.isAltSendsEscape());
    }

    @Test
    public void executesLocalCommandsDirectly() {
        try (ShellLLMTools tools = new ShellLLMTools()) {
            String result = tools.executeLocalCommand("echo hello");

            assertTrue(result.contains("exitCode: 0"));
            assertTrue(result.contains("hello"));
        }
    }

    @Test
    public void returnsStoredDataAndSettingsSummaries() {
        ShellLLMTools tools = new ShellLLMTools();

        String dataSummary = tools.getStoredDataSummary();
        String settingsSummary = tools.getApplicationSettingsSummary();

        assertTrue(dataSummary.contains("connections:"));
        assertTrue(dataSummary.contains("llmSessions:"));
        assertTrue(settingsSummary.contains("llmThinkingLevel:"));
        assertTrue(settingsSummary.contains("x11Port:"));
    }

    @Test
    public void returnsProtocolConnectionThemeFontAndDatabaseInformation() {
        ShellLLMTools tools = new ShellLLMTools();

        String protocols = tools.listSupportedProtocols();
        String connections = tools.listSavedConnections(null, 10);
        String openConnections = tools.listOpenConnections(null, 10);
        String groups = tools.listGroups(null, 10);
        String snippets = tools.listSnippets(null, 10);
        String keys = tools.listSavedKeys(null, 10);
        String queries = tools.listQueries(null, 10);
        String fileFavorites = tools.listFileFavorites(null, 10);
        String redisFavorites = tools.listRedisFavorites(null, 10);
        String zkFavorites = tools.listZkFavorites(null, 10);
        String llmProviders = tools.listLlmProviders(null, 10);
        String llmSessions = tools.listLlmSessions(null, 10);
        String themes = tools.listThemes(null, 10);
        String fonts = tools.listFonts(null, 10);
        String database = tools.getDatabaseSupportInfo();
        String dependencies = tools.getDependencyVersions();

        assertTrue(protocols.contains("MySQL"));
        assertTrue(protocols.contains("S3/MinIO"));
        assertTrue(connections.contains("count:"));
        assertTrue(openConnections.contains("count: 0"));
        assertTrue(groups.contains("count:"));
        assertTrue(snippets.contains("count:"));
        assertTrue(keys.contains("count:"));
        assertTrue(queries.contains("count:"));
        assertTrue(fileFavorites.contains("count:"));
        assertTrue(redisFavorites.contains("count:"));
        assertTrue(zkFavorites.contains("count:"));
        assertTrue(llmProviders.contains("count:"));
        assertTrue(llmSessions.contains("count:"));
        assertTrue(themes.contains("count:"));
        assertTrue(fonts.contains("count:"));
        assertTrue(database.contains("mysql:"));
        assertTrue(dependencies.contains("javaFx:"));
        assertFalse(dependencies.contains("javaFx: -"));
        assertFalse(dependencies.contains("langChain4j: -"));
    }

    @Test
    public void toolNamesAreUnique() {
        Set<String> names = new HashSet<>();
        int toolCount = 0;
        for (Method method : ShellLLMTools.class.getMethods()) {
            Tool tool = method.getAnnotation(Tool.class);
            if (tool != null) {
                toolCount++;
                assertTrue(names.add(tool.name()));
            }
        }
        assertEquals(toolCount, names.size());
        assertTrue(names.contains("getProjectVersion"));
        assertTrue(names.contains("getJdkVersion"));
        assertTrue(names.contains("getStoredDataSummary"));
        assertTrue(names.contains("getSnippet"));
        assertTrue(names.contains("getQuery"));
        assertTrue(names.contains("listFileFavorites"));
        assertTrue(names.contains("listRedisFavorites"));
        assertTrue(names.contains("listZkFavorites"));
        assertTrue(names.contains("listLlmProviders"));
        assertTrue(names.contains("listLlmSessions"));
        assertTrue(names.contains("executeLocalCommandDirect"));
        assertTrue(names.contains("executeLocalCommandInDirectoryDirect"));
        assertFalse(names.contains("deleteConnection"));
        assertFalse(names.contains("deleteGroup"));
        assertFalse(names.contains("deleteSnippet"));
        assertFalse(names.contains("deleteQuery"));
        assertFalse(names.contains("updateConnectionCredentials"));
    }

    @Test
    public void internalAndSessionLocalToolNamesAreUnique() {
        Set<String> names = new HashSet<>();
        for (Method method : ShellLLMTools.class.getMethods()) {
            Tool tool = method.getAnnotation(Tool.class);
            if (tool != null) {
                assertTrue(names.add(tool.name()));
            }
        }
        for (Method method : ShellLocalLLMTools.class.getMethods()) {
            Tool tool = method.getAnnotation(Tool.class);
            if (tool != null) {
                assertTrue(names.add(tool.name()));
            }
        }
    }
}
