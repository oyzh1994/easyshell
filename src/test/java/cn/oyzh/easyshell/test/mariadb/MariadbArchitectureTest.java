package cn.oyzh.easyshell.test.mariadb;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.mariadb.condition.MariadbConditionUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbColumnUtil;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.condition.DBConditionManager;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * MariaDB目录、类型分派与FXML资源完整性测试
 *
 * @since 2026-10-09
 */
public class MariadbArchitectureTest {

    private static final Pattern CLASS_NAME = Pattern.compile(
            "(?:import\\s+)?((?:cn\\.oyzh\\.easyshell\\.)[A-Za-z0-9_.]*mariadb(?:\\.[A-Za-z0-9_]+)*\\.[A-Z][A-Za-z0-9_]*)");

    @Test
    public void mariadbTypeDispatchesToOwnClient() throws Exception {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.MARIADB);
        assertTrue(connect.isMariadbType());
        ShellBaseClient client = ShellClientUtil.newClient(connect);
        assertTrue(client instanceof ShellMariadbClient);
        client.close();
    }

    @Test
    public void mariadbBusinessRegistriesUseOwnDialect() {
        MariadbConditionUtil.init();
        ShellMariadbColumnUtil.init();
        assertFalse(DBConditionManager.conditions(DBDialect.MARIADB).isEmpty());
        assertFalse(DBColumnFieldManager.fields(DBDialect.MARIADB).isEmpty());
    }

    @Test
    public void mariadbJavaAndResourcesUseOwnPackages() throws Exception {
        Path javaRoot = Paths.get("src/main/java/cn/oyzh/easyshell");
        Path resourceRoot = Paths.get("src/main/resources");
        List<Path> mariadbFiles = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(javaRoot)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().contains("/mariadb/"))
                    .forEach(mariadbFiles::add);
        }
        try (Stream<Path> stream = Files.walk(resourceRoot)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().contains("/mariadb/"))
                    .forEach(mariadbFiles::add);
        }
        assertFalse(mariadbFiles.isEmpty());
        for (Path file : mariadbFiles) {
            String content = stripComments(Files.readString(file));
            if (file.toString().endsWith(".java")) {
                assertFalse("MariaDB file uses MySQL dialect: " + file,
                        content.contains("DBDialect.MYSQL"));
            }
            assertFalse("MariaDB file references MySQL package: " + file,
                    content.contains("cn.oyzh.easyshell.") && content.matches("(?s).*cn\\.oyzh\\.easyshell\\..*\\.mysql\\..*"));
            assertFalse("MariaDB file references MySQL implementation: " + file,
                    content.matches("(?s).*(ShellMysql|Mysql[A-Z]).*"));
            Matcher matcher = CLASS_NAME.matcher(content);
            while (matcher.find()) {
                String className = matcher.group(1);
                if (className.endsWith(".*")) {
                    continue;
                }
                Class.forName(className, false, MariadbArchitectureTest.class.getClassLoader());
            }
        }
    }

    @Test
    public void mariadbControllerResourcesExist() throws IOException {
        Path javaRoot = Paths.get("src/main/java/cn/oyzh/easyshell");
        List<Path> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(javaRoot)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().contains("/mariadb/"))
                    .forEach(files::add);
        }
        for (Path file : files) {
            String content = Files.readString(file);
            for (String resource : extractResources(content)) {
                assertTrue("Missing MariaDB resource " + resource + " referenced by " + file,
                        Files.exists(resourceRootFor(resource)));
            }
        }
    }

    private List<String> extractResources(String content) {
        List<String> resources = new ArrayList<>();
        addMatches(resources, content, "FXConst.FXML_PATH\\s*\\+\\s*\"([^\"]+)\"", "fxml/");
        addMatches(resources, content, "FXConst.TAB_PATH\\s*\\+\\s*\"([^\"]+)\"", "tabs/");
        addMatches(resources, content, "FXConst.POPUP_PATH\\s*\\+\\s*\"([^\"]+)\"", "popups/");
        addMatches(resources, content, "return\\s+\"(/tabs/[^\"]+)\"", "");
        return resources;
    }

    private String stripComments(String content) {
        return content
                .replaceAll("(?s)/\\*.*?\\*/", "")
                .replaceAll("(?m)//.*$", "")
                .replaceAll("(?s)<!--.*?-->", "");
    }

    private void addMatches(List<String> target, String content, String regex, String prefix) {
        Matcher matcher = Pattern.compile(regex).matcher(content);
        while (matcher.find()) {
            String value = matcher.group(1);
            target.add(prefix + value.replaceFirst("^/", ""));
        }
    }

    private Path resourceRootFor(String resource) {
        return Paths.get("src/main/resources").resolve(resource);
    }
}
