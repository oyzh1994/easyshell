package cn.oyzh.easyshell.internal;

import cn.oyzh.common.dto.Project;
import cn.oyzh.common.log.JulUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMThinkingLevel;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.easyshell.local.ShellLocalCommandExecutor;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.store.ShellFileCollectStore;
import cn.oyzh.easyshell.store.ShellGroupStore;
import cn.oyzh.easyshell.store.ShellJumpConfigStore;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.store.ShellProxyConfigStore;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.store.ShellSSLConfigStore;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.ShellSnippetStore;
import cn.oyzh.easyshell.store.ShellTerminalHistoryStore;
import cn.oyzh.easyshell.store.ShellTunnelingConfigStore;
import cn.oyzh.easyshell.store.ShellX11ConfigStore;
import cn.oyzh.easyshell.store.llm.LLMProviderStore;
import cn.oyzh.easyshell.store.llm.LLMSessionStore;
import cn.oyzh.easyshell.store.zk.ShellZKAuthStore;
import cn.oyzh.easyshell.store.zk.ShellZKSASLConfigStore;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.easyshell.tabs.ShellTabPane;
import cn.oyzh.easyshell.x11.ShellX11Manager;
import cn.oyzh.fx.plus.font.FontConfig;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.theme.ThemeUtil;
import cn.oyzh.fx.plus.theme.Themes;
import cn.oyzh.fx.plus.util.FXUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import javafx.scene.control.Tab;
import javafx.scene.text.Font;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShellLLMTools implements LLMTools {

    private ShellTabPane tabPane;

    private final ShellLocalCommandExecutor localCommandExecutor;

    public ShellLLMTools() {
        this.localCommandExecutor = new ShellLocalCommandExecutor();
    }

    public ShellLLMTools(ShellTabPane tabPane) {
        this();
        this.tabPane = tabPane;
    }

    public void setTabPane(ShellTabPane tabPane) {
        this.tabPane = tabPane;
    }

    @Tool(name = "getProjectInfo", value = "Get EasyShell project metadata")
    public String getProjectInfo() {
        try {
            Project project = Project.load();
            if (project == null) {
                return "Project metadata is unavailable.";
            }
            StringBuilder result = new StringBuilder();
            this.append(result, "name", project.getName());
            this.append(result, "type", project.getType());
            this.append(result, "version", project.getVersion());
            this.append(result, "updateDate", project.getUpdateDate());
            this.append(result, "copyright", project.getCopyright());
            this.append(result, "implementationTitle", this.value(
                    ShellLLMTools.class.getPackage().getImplementationTitle()));
            this.append(result, "implementationVersion", this.value(
                    ShellLLMTools.class.getPackage().getImplementationVersion()));
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get project info", exception);
        }
    }

    @Tool(name = "getProjectVersion", value = "Get the EasyShell application version")
    public String getProjectVersion() {
        try {
            Project project = Project.load();
            return "projectVersion: " + this.value(project == null ? null : project.getVersion());
        } catch (Exception exception) {
            return this.failure("Get project version", exception);
        }
    }

    @Tool(name = "getJdkVersion", value = "Get the Java runtime and JDK version used by EasyShell")
    public String getJdkVersion() {
        try {
            Runtime.Version runtimeVersion = Runtime.version();
            StringBuilder result = new StringBuilder();
            this.append(result, "javaVersion", runtimeVersion.toString());
            this.append(result, "javaFeature", runtimeVersion.feature());
            this.append(result, "javaRuntimeVersion", this.property("java.runtime.version"));
            this.append(result, "javaVmName", this.property("java.vm.name"));
            this.append(result, "javaVmVersion", this.property("java.vm.version"));
            this.append(result, "javaVendor", this.property("java.vendor"));
            this.append(result, "javaHome", this.property("java.home"));
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get JDK version", exception);
        }
    }

    @Tool(name = "getRuntimeInfo", value = "Get process, operating system, memory, locale, and timezone information")
    public String getRuntimeInfo() {
        try {
            Runtime runtime = Runtime.getRuntime();
            MemoryUsage heapUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
            long uptimeSeconds = TimeUnit.MILLISECONDS.toSeconds(
                    ManagementFactory.getRuntimeMXBean().getUptime());
            StringBuilder result = new StringBuilder();
            this.append(result, "pid", ProcessHandle.current().pid());
            this.append(result, "uptimeSeconds", uptimeSeconds);
            this.append(result, "osName", this.property("os.name"));
            this.append(result, "osVersion", this.property("os.version"));
            this.append(result, "osArch", this.property("os.arch"));
            this.append(result, "availableProcessors", runtime.availableProcessors());
            this.append(result, "heapUsed", this.formatBytes(heapUsage.getUsed()));
            this.append(result, "heapCommitted", this.formatBytes(heapUsage.getCommitted()));
            this.append(result, "heapMax", this.formatBytes(heapUsage.getMax()));
            this.append(result, "nonHeapUsed", this.formatBytes(nonHeapUsage.getUsed()));
            this.append(result, "threadCount", ManagementFactory.getThreadMXBean().getThreadCount());
            this.append(result, "locale", Locale.getDefault().toString());
            this.append(result, "timezone", TimeZone.getDefault().getID());
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get runtime info", exception);
        }
    }

    @Tool(name = "getProjectPaths", value = "Get EasyShell data, cache, key cache, node cache, and log paths")
    public String getProjectPaths() {
        try {
            Path databasePath = Path.of(ShellConst.getStorePath(), "db");
            StringBuilder result = new StringBuilder();
            this.append(result, "storeDirectory", ShellConst.getStorePath());
            this.append(result, "cacheDirectory", ShellConst.getCachePath());
            this.append(result, "keyCacheDirectory", ShellConst.getKeyCachePath());
            this.append(result, "nodeCacheDirectory", ShellConst.getNodeCachePath());
            this.append(result, "logDirectory", this.value(JulUtil.getLogsDir()));
            this.append(result, "database", this.describePath(databasePath));
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get project paths", exception);
        }
    }

    @Tool(name = "getStoredDataSummary", value = "Get counts of locally stored EasyShell data without exposing record contents")
    public String getStoredDataSummary() {
        StringBuilder result = new StringBuilder();
        this.appendCount(result, "keys", () -> ShellKeyStore.INSTANCE.selectList().size());
        this.appendCount(result, "groups", () -> ShellGroupStore.INSTANCE.selectList().size());
        this.appendCount(result, "queries", () -> ShellQueryStore.INSTANCE.selectList().size());
        this.appendCount(result, "snippets", () -> ShellSnippetStore.INSTANCE.selectList().size());
        this.appendCount(result, "llmSessions", () -> LLMSessionStore.INSTANCE.selectList().size());
        this.appendCount(result, "connections", () -> ShellConnectStore.INSTANCE.selectList().size());
        this.appendCount(result, "llmProviders", () -> LLMProviderStore.INSTANCE.selectList().size());
        this.appendCount(result, "sslConfigs", () -> ShellSSLConfigStore.INSTANCE.selectList().size());
        this.appendCount(result, "x11Configs", () -> ShellX11ConfigStore.INSTANCE.selectList().size());
        this.appendCount(result, "zkAuthConfigs", () -> ShellZKAuthStore.INSTANCE.selectList().size());
        this.appendCount(result, "jumpConfigs", () -> ShellJumpConfigStore.INSTANCE.selectList().size());
        this.appendCount(result, "proxyConfigs", () -> ShellProxyConfigStore.INSTANCE.selectList().size());
        this.appendCount(result, "fileFavorites", () -> ShellFileCollectStore.INSTANCE.selectList().size());
        this.appendCount(result, "zkSaslConfigs", () -> ShellZKSASLConfigStore.INSTANCE.selectList().size());
        this.appendCount(result, "terminalHistory", () -> ShellTerminalHistoryStore.INSTANCE.selectList().size());
        this.appendCount(result, "tunnelingConfigs", () -> ShellTunnelingConfigStore.INSTANCE.selectList().size());
        return result.toString();
    }

    @Tool(name = "getApplicationCapabilities", value = "List EasyShell connection protocols and supported feature areas")
    public String getApplicationCapabilities() {
        return LLMUtil.readLlmText("internal_capabilities.txt");
    }

    @Tool(name = "listSupportedProtocols", value = "List all supported connection protocols and their feature categories")
    public String listSupportedProtocols() {
        return LLMUtil.readLlmText("internal_protocols.txt");
    }

    @Tool(name = "listSavedConnections", value = "List saved EasyShell connections without passwords, keys, tokens, or certificates")
    public String listSavedConnections(
            @P(name = "filter", value = "Optional case-insensitive text filter for name, type, host, or remark") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            List<ShellConnect> connections = new ArrayList<>(ShellConnectStore.INSTANCE.selectList());
            connections.sort(Comparator.comparing(ShellConnect::getName, String.CASE_INSENSITIVE_ORDER));
            Map<String, String> groupNames = this.groupNames();
            List<ShellConnect> matched = connections.stream()
                    .filter(connection -> this.matchesConnection(connection, filter))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                ShellConnect connection = matched.get(index);
                result.append("id: ").append(this.value(connection.getId()))
                        .append(" | name: ").append(this.value(connection.getName()))
                        .append(" | type: ").append(this.value(connection.getType()))
                        .append(" | host: ").append(this.value(connection.hostIp()))
                        .append(" | port: ").append(connection.hostPort())
                        .append(" | group: ").append(this.value(groupNames.get(connection.getGroupId())))
                        .append(" | charset: ").append(this.value(connection.getCharset()))
                        .append(" | authConfigured: ").append(this.hasAuthentication(connection))
                        .append(" | remark: ").append(this.value(connection.getRemark()))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List saved connections", exception);
        }
    }

    @Tool(name = "listOpenConnections", value = "List connection tabs currently open in EasyShell with live connection state")
    public String listOpenConnections(
            @P(name = "filter", value = "Optional case-insensitive text filter for tab title, name, type, or host") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            List<OpenConnectionSnapshot> connections = this.openConnections();
            List<OpenConnectionSnapshot> matched = connections.stream()
                    .filter(connection -> this.matchesOpenConnection(connection, filter))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                OpenConnectionSnapshot connection = matched.get(index);
                result.append("tabId: ").append(this.value(connection.tabId()))
                        .append(" | selected: ").append(connection.selected())
                        .append(" | tabTitle: ").append(this.value(connection.title()))
                        .append(" | connectionId: ").append(this.value(connection.connect().getId()))
                        .append(" | name: ").append(this.value(connection.connect().getName()))
                        .append(" | type: ").append(this.value(connection.connect().getType()))
                        .append(" | host: ").append(this.value(connection.connect().hostIp()))
                        .append(" | port: ").append(connection.connect().hostPort())
                        .append(" | state: ").append(this.value(connection.state()))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List open connections", exception);
        }
    }

    @Tool(name = "listGroups", value = "List saved connection groups and their connection counts")
    public String listGroups(
            @P(name = "filter", value = "Optional case-insensitive group name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 500 and capped at 1000") int maxResults) {
        try {
            List<ShellGroup> groups = new ArrayList<>(ShellGroupStore.INSTANCE.selectList());
            groups.sort(Comparator.comparing(ShellGroup::getName, String.CASE_INSENSITIVE_ORDER));
            Map<String, Integer> connectionCounts = new LinkedHashMap<>();
            for (ShellConnect connection : ShellConnectStore.INSTANCE.selectList()) {
                connectionCounts.merge(connection.getGroupId(), 1, Integer::sum);
            }
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<ShellGroup> matched = groups.stream()
                    .filter(group -> query.isBlank() || this.value(group.getName()).toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 500, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                ShellGroup group = matched.get(index);
                result.append("id: ").append(this.value(group.getGid()))
                        .append(" | parentId: ").append(this.value(group.getPid()))
                        .append(" | name: ").append(this.value(group.getName()))
                        .append(" | expanded: ").append(group.isExpand())
                        .append(" | connections: ").append(connectionCounts.getOrDefault(group.getGid(), 0))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List groups", exception);
        }
    }

    @Tool(name = "getDatabaseSupportInfo", value = "Get supported database and NoSQL types plus bundled client driver versions")
    public String getDatabaseSupportInfo() {
        StringBuilder result = new StringBuilder();
        result.append("localStore: SQLite\n");
        result.append("mysql: type=SQL | liveVersionAvailable=true | driverVersion=")
                .append(this.packageVersion("com.mysql.cj.conf.PropertyDefinitions")).append('\n');
        result.append("mariadb: type=SQL | liveVersionAvailable=true | driverVersion=")
                .append(this.packageVersion("org.mariadb.jdbc.Driver")).append('\n');
        result.append("dameng: type=SQL | liveVersionAvailable=true | driverVersion=")
                .append(this.packageVersion("dm.jdbc.driver.DmDriver")).append('\n');
        result.append("mongodb: type=NoSQL | liveVersionAvailable=true | driverVersion=")
                .append(this.packageVersion("com.mongodb.client.MongoClient")).append('\n');
        result.append("redis: type=NoSQL | liveVersionAvailable=true | driverVersion=")
                .append(this.packageVersion("redis.clients.jedis.JedisPooled")).append('\n');
        result.append("zookeeper: type=Coordination | liveVersionAvailable=false | driverVersion=")
                .append(this.packageVersion("org.apache.zookeeper.ZooKeeper")).append('\n');
        return result.toString();
    }

    @Tool(name = "getOpenDatabaseVersions", value = "Get database or NoSQL server versions for currently open connected tabs")
    public String getOpenDatabaseVersions(
            @P(name = "filter", value = "Optional case-insensitive connection name or type filter") String filter) {
        try {
            List<OpenConnectionSnapshot> connections = this.openConnections().stream()
                    .filter(connection -> this.isDatabaseClient(connection.client()))
                    .filter(connection -> this.matchesOpenConnection(connection, filter))
                    .toList();
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(connections.size()).append('\n');
            for (OpenConnectionSnapshot connection : connections) {
                result.append("name: ").append(this.value(connection.connect().getName()))
                        .append(" | type: ").append(this.value(connection.connect().getType()))
                        .append(" | state: ").append(this.value(connection.state()))
                        .append(" | serverVersion: ").append(this.value(this.databaseVersion(connection.client())))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get open database versions", exception);
        }
    }

    @Tool(name = "getApplicationSettingsSummary", value = "Get non-sensitive EasyShell application settings")
    public String getApplicationSettingsSummary() {
        try {
            ShellSetting setting = ShellSettingStore.SETTING;
            if (setting == null) {
                return "Application settings are unavailable.";
            }
            StringBuilder result = new StringBuilder();
            this.append(result, "theme", this.value(setting.getTheme()));
            this.append(result, "fontFamily", this.value(setting.getFontFamily()));
            this.append(result, "fontSize", setting.getFontSize());
            this.append(result, "fontWeight", setting.getFontWeight());
            this.append(result, "editorFontFamily", this.value(setting.getEditorFontFamily()));
            this.append(result, "editorFontSize", this.value(setting.getEditorFontSize()));
            this.append(result, "editorFontWeight", setting.getEditorFontWeight());
            this.append(result, "terminalFontFamily", this.value(setting.getTerminalFontFamily()));
            this.append(result, "terminalFontSize", this.value(setting.getTerminalFontSize()));
            this.append(result, "terminalFontWeight", setting.getTerminalFontWeight());
            this.append(result, "llmThinkingLevel", this.enumName(
                    LLMThinkingLevel.values(), setting.getLlmThinkingLevel()));
            this.append(result, "llmStreaming", setting.isLlmStreaming());
            this.append(result, "llmMaxMessages", setting.getLlmMaxMessages());
            this.append(result, "llmExecutionMode", this.enumName(
                    LLMExecutionMode.values(), setting.getLlmExecutionMode()));
            this.append(result, "terminalType", this.value(setting.getTermType()));
            this.append(result, "terminalMaxLineCount", this.value(
                    setting.getTermMaxLineCount() == null ? null : String.valueOf(setting.getTermMaxLineCount())));
            this.append(result, "terminalRefreshRate", this.value(
                    setting.getTermRefreshRate() == null ? null : String.valueOf(setting.getTermRefreshRate())));
            this.append(result, "terminalCopyOnSelected", setting.isTermCopyOnSelected());
            this.append(result, "terminalUseAntialiasing", setting.isTermUseAntialiasing());
            this.append(result, "terminalParseHyperlink", setting.isTermParseHyperlink());
            this.append(result, "redisKeyLoadLimit", setting.getKeyLoadLimit());
            this.append(result, "redisRowPageLimit", setting.getRowPageLimit());
            this.append(result, "recordPageLimit", setting.getRecordPageLimit());
            this.append(result, "mongoRecordPageLimit", setting.getMongoRecordPageLimit());
            this.append(result, "shortcutKeyEnabled", setting.isEnableShortcutKey());
            this.append(result, "graphicMode", setting.getGraphicMode());
            this.append(result, "x11Port", setting.getX11Port());
            this.append(result, "x11Glx", setting.isX11Glx());
            this.append(result, "x11Local", setting.isX11Local());
            this.append(result, "x11FocusOnClick", setting.isX11FocusOnClick());
            this.append(result, "x11SingleWindow", setting.isX11SingleWindow());
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get application settings", exception);
        }
    }

    @Tool(name = "listThemes", value = "List available application themes")
    public String listThemes(
            @P(name = "filter", value = "Optional case-insensitive theme name or description filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<ThemeStyle> themes = new ArrayList<>(Themes.allThemes());
            themes.sort(Comparator.comparing(
                    theme -> theme.getDesc(Locale.getDefault()), String.CASE_INSENSITIVE_ORDER));
            List<ThemeStyle> matched = themes.stream()
                    .filter(theme -> query.isBlank()
                            || this.value(theme.getName()).toLowerCase(Locale.ROOT).contains(query)
                            || this.value(theme.getDesc(Locale.getDefault())).toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                result.append(this.themeSummary(matched.get(index))).append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List themes", exception);
        }
    }

    @Tool(name = "getCurrentTheme", value = "Get the currently applied application theme and colors")
    public String getCurrentTheme() {
        try {
            return this.themeSummary(ThemeManager.currentTheme());
        } catch (Exception exception) {
            return this.failure("Get current theme", exception);
        }
    }

    @Tool(name = "setTheme", value = "Change and persist the application theme")
    public String setTheme(
            @P(name = "themeName", value = "Exact theme name returned by listThemes") String themeName) {
        try {
            if (themeName == null || themeName.isBlank()) {
                return "Theme name is required.";
            }
            ThemeStyle theme = Themes.getTheme(themeName.trim());
            if (theme == null) {
                return "Theme not found: " + themeName;
            }
            this.applyTheme(theme);
            return "Applied theme:\n" + this.themeSummary(theme);
        } catch (Exception exception) {
            return this.failure("Set theme", exception);
        }
    }

    @Tool(name = "toggleTheme", value = "Switch between the current theme and its light or dark counterpart")
    public String toggleTheme() {
        try {
            ThemeStyle current = ThemeManager.currentTheme();
            ThemeStyle target = ThemeUtil.getInverseTheme(current);
            if (target == null) {
                return "No inverse theme is available.";
            }
            this.applyTheme(target);
            return "Applied theme:\n" + this.themeSummary(target);
        } catch (Exception exception) {
            return this.failure("Toggle theme", exception);
        }
    }

    @Tool(name = "listFonts", value = "List system fonts available to JavaFX")
    public String listFonts(
            @P(name = "filter", value = "Optional case-insensitive font family filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<String> fonts = Font.getFamilies().stream()
                    .filter(font -> query.isBlank() || font.toLowerCase(Locale.ROOT).contains(query))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(fonts.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(fonts.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                result.append(fonts.get(index)).append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List fonts", exception);
        }
    }

    @Tool(name = "getCurrentFont", value = "Get current application, editor, and terminal font settings")
    public String getCurrentFont() {
        try {
            ShellSetting setting = ShellSettingStore.SETTING;
            Font current = FontManager.currentFont();
            StringBuilder result = new StringBuilder();
            this.append(result, "appliedName", current.getName());
            this.append(result, "appliedFamily", current.getFamily());
            this.append(result, "appliedSize", current.getSize());
            if (setting != null) {
                this.appendFontConfig(result, "application", setting.fontConfig());
                this.appendFontConfig(result, "editor", setting.editorFontConfig());
                this.appendFontConfig(result, "terminal", setting.terminalFontConfig());
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get current font", exception);
        }
    }

    @Tool(name = "setApplicationFont", value = "Change and persist the main application font")
    public String setApplicationFont(
            @P(name = "fontFamily", value = "Exact font family returned by listFonts") String fontFamily,
            @P(name = "fontSize", value = "Font size from 8 to 72") int fontSize,
            @P(name = "fontWeight", value = "Font weight from 100 to 900 in steps of 100") int fontWeight) {
        try {
            if (fontFamily == null || fontFamily.isBlank()) {
                return "Font family is required.";
            }
            String matchedFamily = Font.getFamilies().stream()
                    .filter(family -> family.equalsIgnoreCase(fontFamily.trim()))
                    .findFirst()
                    .orElse(null);
            if (matchedFamily == null) {
                return "Font family not found: " + fontFamily;
            }
            if (fontSize < 8 || fontSize > 72) {
                return "Font size must be between 8 and 72.";
            }
            if (fontWeight < 100 || fontWeight > 900 || fontWeight % 100 != 0) {
                return "Font weight must be 100, 200, 300, 400, 500, 600, 700, 800, or 900.";
            }
            ShellSetting setting = ShellSettingStore.SETTING;
            setting.setFontFamily(matchedFamily);
            setting.setFontSize((byte) fontSize);
            setting.setFontWeight((short) fontWeight);
            ShellSettingStore.INSTANCE.replace(setting);
            FXUtil.runWait(() -> FontManager.apply(setting.fontConfig()));
            return "Applied application font:\n" + this.fontConfigSummary("application", setting.fontConfig());
        } catch (Exception exception) {
            return this.failure("Set application font", exception);
        }
    }

    @Tool(name = "getDependencyVersions", value = "Get JavaFX, LangChain4j, database driver, and other bundled dependency versions")
    public String getDependencyVersions() {
        Map<String, String> versions = new LinkedHashMap<>();
        versions.put("javaFx", this.packageVersion("javafx.application.Application"));
        versions.put("langChain4j", this.packageVersion("dev.langchain4j.service.AiServices"));
        versions.put("mysqlDriver", this.packageVersion("com.mysql.cj.conf.PropertyDefinitions"));
        versions.put("mariaDbDriver", this.packageVersion("org.mariadb.jdbc.Driver"));
        versions.put("damengDriver", this.packageVersion("dm.jdbc.driver.DmDriver"));
        versions.put("mongoDriver", this.packageVersion("com.mongodb.client.MongoClient"));
        versions.put("jedis", this.packageVersion("redis.clients.jedis.JedisPooled"));
        versions.put("zookeeper", this.packageVersion("org.apache.zookeeper.ZooKeeper"));
        versions.put("sqlite", this.packageVersion("org.sqlite.JDBC"));
        versions.put("h2", this.packageVersion("org.h2.Driver"));
        StringBuilder result = new StringBuilder();
        versions.forEach((name, version) -> this.append(result, name, version));
        return result.toString();
    }

    @Tool(name = "executeLocalCommandDirect",
            value = "Directly execute a command on the local machine running EasyShell")
    public String executeLocalCommand(
            @P(name = "command", value = "The command to execute") String command) {
        return this.executeLocalCommand(command, null, ShellLocalCommandExecutor.DEFAULT_TIMEOUT_SECONDS);
    }

    @Tool(name = "executeLocalCommandInDirectoryDirect",
            value = "Directly execute a command in a specific local working directory")
    public String executeLocalCommandInDirectory(
            @P(name = "workingDirectory", value = "Absolute or relative local working directory") String workingDirectory,
            @P(name = "command", value = "The command to execute") String command,
            @P(name = "timeoutSeconds", value = "Timeout in seconds, maximum 600") long timeoutSeconds) {
        return this.executeLocalCommand(command, workingDirectory, timeoutSeconds);
    }

    private String executeLocalCommand(String command, String workingDirectory, long timeoutSeconds) {
        try {
            return this.localCommandExecutor.execute(command, workingDirectory, timeoutSeconds).format();
        } catch (Exception exception) {
            return this.failure("Execute local command", exception);
        }
    }

    @Tool(name = "getXServerStatus", value = "Get built-in X server status and non-sensitive X11 settings")
    public String getXServerStatus() {
        try {
            int statusCode = ShellX11Manager.xServerStatus();
            String status = switch (statusCode) {
                case 0 -> "NOT_STARTED";
                case 1 -> "RUNNING";
                case 2 -> "STOPPED";
                default -> "UNKNOWN";
            };
            ShellSetting setting = ShellSettingStore.SETTING;
            StringBuilder result = new StringBuilder();
            this.append(result, "statusCode", statusCode);
            this.append(result, "status", status);
            this.append(result, "clients", ShellX11Manager.xServerClients());
            this.append(result, "port", setting == null ? null : setting.getX11Port());
            this.append(result, "glx", setting == null ? null : setting.isX11Glx());
            this.append(result, "local", setting == null ? null : setting.isX11Local());
            this.append(result, "focusOnClick", setting == null ? null : setting.isX11FocusOnClick());
            this.append(result, "singleWindow", setting == null ? null : setting.isX11SingleWindow());
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get X server status", exception);
        }
    }

    @Tool(name = "startXServer", value = "Start EasyShell's built-in X server")
    public String startXServer() {
        try {
            ShellX11Manager.startXServer_built_in();
            return "X server start completed.";
        } catch (Exception exception) {
            return this.failure("Start X server", exception);
        }
    }

    @Tool(name = "stopXServer", value = "Stop EasyShell's built-in X server")
    public String stopXServer() {
        try {
            ShellX11Manager.stopXServer_built_in();
            return "X server stopped.";
        } catch (Exception exception) {
            return this.failure("Stop X server", exception);
        }
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        return this.toolSystemMessage() + "\n\n"
                + LLMUtil.languageSystemMessage() + "\n\n"
                + LLMUtil.executionSystemMessage(executionMode);
    }

    @Override
    public void close() {
        this.localCommandExecutor.close();
    }

    public String toolSystemMessage() {
        String base = LLMUtil.readLlmText("internal_tool.txt");
        Project project = Project.load();
        String environment = LLMUtil.readLlmText("internal_environment.txt")
                .replace("{projectName}", this.value(project == null ? null : project.getName()))
                .replace("{projectVersion}", this.value(project == null ? null : project.getVersion()))
                .replace("{projectType}", this.value(project == null ? null : project.getType()))
                .replace("{jdkVersion}", this.value(Runtime.version().toString()))
                .replace("{os}", this.value(System.getProperty("os.name")))
                .replace("{storePath}", this.value(ShellConst.getStorePath()));
        return base + "\n\n" + environment;
    }

    private void append(StringBuilder result, String name, Object value) {
        result.append(name).append(": ").append(this.value(value)).append('\n');
    }

    private void appendCount(StringBuilder result, String name, CountSource source) {
        try {
            this.append(result, name, source.get());
        } catch (Exception exception) {
            this.append(result, name, "unavailable (" + this.error(exception) + ")");
        }
    }

    private String property(String name) {
        return this.value(System.getProperty(name));
    }

    private String describePath(Path path) {
        try {
            boolean exists = Files.exists(path);
            boolean directory = Files.isDirectory(path);
            boolean regularFile = Files.isRegularFile(path);
            String size = regularFile ? this.formatBytes(Files.size(path)) : "-";
            return path + " | exists=" + exists + " | directory=" + directory
                    + " | regularFile=" + regularFile + " | size=" + size;
        } catch (Exception exception) {
            return path + " | error=" + this.error(exception);
        }
    }

    private String enumName(Enum<?>[] values, int ordinal) {
        if (ordinal < 0 || ordinal >= values.length) {
            return "UNKNOWN";
        }
        return values[ordinal].name();
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double value = bytes;
        String[] units = {"KiB", "MiB", "GiB", "TiB"};
        int unitIndex = -1;
        while (value >= 1024 && unitIndex < units.length - 1) {
            value /= 1024;
            unitIndex++;
        }
        return String.format(Locale.ROOT, "%.2f %s", value, units[unitIndex]);
    }

    private String failure(String action, Exception exception) {
        return action + " failed: " + this.error(exception);
    }

    private String error(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }

    private String value(Object value) {
        if (value == null) {
            return "-";
        }
        String text = String.valueOf(value);
        return text.isBlank() ? "-" : text;
    }

    private Map<String, String> groupNames() {
        Map<String, String> result = new LinkedHashMap<>();
        for (ShellGroup group : ShellGroupStore.INSTANCE.selectList()) {
            result.put(group.getGid(), group.getName());
        }
        return result;
    }

    private boolean matchesConnection(ShellConnect connection, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        String query = filter.trim().toLowerCase(Locale.ROOT);
        return this.value(connection.getName()).toLowerCase(Locale.ROOT).contains(query)
                || this.value(connection.getType()).toLowerCase(Locale.ROOT).contains(query)
                || this.value(connection.getHost()).toLowerCase(Locale.ROOT).contains(query)
                || this.value(connection.getRemark()).toLowerCase(Locale.ROOT).contains(query);
    }

    private boolean matchesOpenConnection(OpenConnectionSnapshot connection, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        String query = filter.trim().toLowerCase(Locale.ROOT);
        return this.value(connection.title()).toLowerCase(Locale.ROOT).contains(query)
                || this.value(connection.connect().getName()).toLowerCase(Locale.ROOT).contains(query)
                || this.value(connection.connect().getType()).toLowerCase(Locale.ROOT).contains(query)
                || this.value(connection.connect().getHost()).toLowerCase(Locale.ROOT).contains(query);
    }

    private boolean hasAuthentication(ShellConnect connection) {
        return !this.value(connection.getPassword()).equals("-")
                || !this.value(connection.getKeyId()).equals("-")
                || !this.value(connection.getCertificate()).equals("-");
    }

    private List<OpenConnectionSnapshot> openConnections() {
        List<OpenConnectionSnapshot> result = new ArrayList<>();
        if (this.tabPane == null) {
            return result;
        }
        FXUtil.runWait(() -> {
            Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();
            for (Tab tab : new ArrayList<>(this.tabPane.getTabs())) {
                if (!(tab instanceof ShellConnectTab connectTab)) {
                    continue;
                }
                ShellBaseClient client = connectTab.client();
                ShellConnect connect = connectTab.shellConnect();
                if (client == null || connect == null) {
                    continue;
                }
                result.add(new OpenConnectionSnapshot(
                        tab.getId(),
                        tab.getText(),
                        tab == selectedTab,
                        connect,
                        this.value(client.getState()),
                        client));
            }
        });
        return result;
    }

    private boolean isDatabaseClient(ShellBaseClient client) {
        return client instanceof ShellMysqlClient
                || client instanceof ShellMariadbClient
                || client instanceof ShellDamengClient
                || client instanceof ShellMongoClient
                || client instanceof ShellRedisClient;
    }

    private String databaseVersion(ShellBaseClient client) {
        try {
            if (client instanceof ShellMysqlClient mysqlClient) {
                return mysqlClient.selectVersion();
            }
            if (client instanceof ShellMariadbClient mariadbClient) {
                return mariadbClient.selectVersion();
            }
            if (client instanceof ShellDamengClient damengClient) {
                return damengClient.selectVersion();
            }
            if (client instanceof ShellMongoClient mongoClient) {
                return mongoClient.selectVersion();
            }
            if (client instanceof ShellRedisClient redisClient) {
                String info = redisClient.info("server");
                if (info != null) {
                    for (String line : info.split("\\R")) {
                        if (line.startsWith("redis_version:")) {
                            return line.substring(line.indexOf(':') + 1).trim();
                        }
                    }
                }
            }
        } catch (Exception exception) {
            return "unavailable (" + this.error(exception) + ")";
        }
        return "-";
    }

    private int normalizeLimit(int requested, int defaultLimit, int maxLimit) {
        if (requested <= 0) {
            return defaultLimit;
        }
        return Math.min(requested, maxLimit);
    }

    private String packageVersion(String className) {
        try {
            Class<?> type = Class.forName(className, false, ShellLLMTools.class.getClassLoader());
            Package javaPackage = type.getPackage();
            if (javaPackage == null) {
                return "-";
            }
            String implementationVersion = javaPackage.getImplementationVersion();
            if (implementationVersion != null && !implementationVersion.isBlank()) {
                return implementationVersion;
            }
            URL location = type.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return "-";
            }
            String fileName = Path.of(location.toURI()).getFileName().toString();
            Matcher matcher = Pattern.compile("-(\\d+(?:\\.\\d+)+(?:[-+][A-Za-z0-9.]+)?)\\.jar$").matcher(fileName);
            return matcher.find() ? matcher.group(1) : "-";
        } catch (Exception exception) {
            return "-";
        }
    }

    private String themeSummary(ThemeStyle theme) {
        if (theme == null) {
            return "-";
        }
        return "name: " + this.value(theme.getName())
                + " | description: " + this.value(theme.getDesc(Locale.getDefault()))
                + " | dark: " + theme.isDarkMode()
                + " | background: " + this.value(theme.getBackgroundColorHex())
                + " | foreground: " + this.value(theme.getForegroundColorHex())
                + " | accent: " + this.value(theme.getAccentColorHex());
    }

    private void applyTheme(ThemeStyle theme) {
        ShellSetting setting = ShellSettingStore.SETTING;
        setting.setTheme(theme.getName());
        setting.setBgColor(theme.getBackgroundColorHex());
        setting.setFgColor(theme.getForegroundColorHex());
        setting.setAccentColor(theme.getAccentColorHex());
        ShellSettingStore.INSTANCE.replace(setting);
        FXUtil.runWait(() -> ThemeManager.apply(theme));
    }

    private void appendFontConfig(StringBuilder result, String name, FontConfig config) {
        result.append(this.fontConfigSummary(name, config)).append('\n');
    }

    private String fontConfigSummary(String name, FontConfig config) {
        return name + " | family: " + this.value(config == null ? null : config.getFamily())
                + " | size: " + this.value(config == null ? null : config.getSize())
                + " | weight: " + this.value(config == null ? null : config.getWeight());
    }

    private record OpenConnectionSnapshot(
            String tabId,
            String title,
            boolean selected,
            ShellConnect connect,
            String state,
            ShellBaseClient client) {
    }

    @FunctionalInterface
    private interface CountSource {
        int get() throws Exception;
    }
}
