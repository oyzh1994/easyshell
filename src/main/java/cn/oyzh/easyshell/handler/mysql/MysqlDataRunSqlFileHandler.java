package cn.oyzh.easyshell.handler.mysql;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.MysqlClient;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author oyzh
 * @since 2024/09/10
 */
public class MysqlDataRunSqlFileHandler extends DataRunSqlFileHandler {

    public MysqlDataRunSqlFileHandler(MysqlClient dbClient, String dbName) {
        super(dbClient, dbName);
    }

    @Override
    public void runSqlFile() throws Exception {
        this.message("Run Sql File Starting");
        // 文件读取
        try (BufferedReader reader = FileUtil.getReader(this.sqlFile, StandardCharsets.UTF_8)) {
            // 暂存数据拼接对象
            StringBuilder builder = new StringBuilder();
            // 多行注释标志位
            AtomicBoolean commentFlag = new AtomicBoolean(false);
            // 创建表、视图标志位
            AtomicBoolean createFlag1 = new AtomicBoolean(false);
            // 创建触发器、函数、过程、事件标志位
            AtomicBoolean createFlag2 = new AtomicBoolean(false);
            // 执行
            while (reader.ready()) {
                try {
                    // 检查中断
                    this.checkInterrupt();
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    // 单行注释1
                    if (line.stripLeading().startsWith("-- ")) {
                        continue;
                    }
                    // 单行注释2
                    if (line.stripLeading().startsWith("#")) {
                        continue;
                    }
                    // 多行注释开始
                    if (line.stripLeading().startsWith("/*")) {
                        commentFlag.set(true);
                    }
                    // 多行注释结束
                    if (line.stripTrailing().endsWith("*/")) {
                        commentFlag.set(false);
                        continue;
                    }
                    // 多行注释开始则跳过
                    if (commentFlag.get()) {
                        continue;
                    }
                    // 新增记录用批量处理
                    if (StringUtil.startWithAnyIgnoreCase(line, "INSERT INTO ")) {
                        this.addInsertSql(line);
                        continue;
                    }
                    // 删除表、函数、过程、触发器、设置变量等
                    if (StringUtil.startWithAnyIgnoreCase(line, "SET ", "DROP ")) {
                        this.dbClient.executeSqlSimple(this.dbName, line);
                        this.processedIncr();
                        continue;
                    }
                    // 创建表、视图结束
                    if (!createFlag2.get() && createFlag1.get() && line.stripTrailing().endsWith(";")) {
                        createFlag1.set(false);
                        builder.append(line).append("\n");
                        this.dbClient.executeSqlSimple(this.dbName, builder.toString());
                        builder.delete(0, builder.length());
                        this.processedIncr();
                        continue;
                    }
                    // 创建表、视图中间过程
                    if (createFlag1.get()) {
                        builder.append(line).append("\n");
                        continue;
                    }
                    // 创建表、视图开始
                    if (!createFlag2.get() && !createFlag1.get() && StringUtil.startWithIgnoreCase(line.stripLeading(), "CREATE ")) {
                        // 单行结束
                        if (line.stripTrailing().endsWith(";")) {
                            this.dbClient.executeSqlSimple(this.dbName, line);
                            this.processedIncr();
                        } else {// 多行
                            createFlag1.set(true);
                            builder.append(line).append("\n");
                        }
                        continue;
                    }
                    // 创建函数、触发器、过程、事件结束
                    if (createFlag2.get() && line.stripTrailing().startsWith("delimiter ;")) {
                        createFlag2.set(false);
                        if (!builder.isEmpty()) {
                            this.dbClient.executeSqlSimple(this.dbName, builder.toString());
                            builder.delete(0, builder.length());
                            this.processedIncr();
                        }
                        continue;
                    }
                    // 创建函数、触发器、过程、事件中间过程
                    if (createFlag2.get()) {
                        builder.append(line).append("\n");
                        continue;
                    }
                    // 创建函数、触发器、过程、事件开始
                    if (line.stripLeading().startsWith("delimiter ;")) {
                        createFlag2.set(true);
                    }
                } catch (Exception ex) {
                    this.exception(ex);
                    this.processedDecr();
                    if (!this.continueWithErrors) {
                        break;
                    }
                }
            }
            // 收尾批量插入
            this.doBatchInsert();
        } catch (Exception ex) {
            this.exception(ex);
        } finally {
            this.message("Run Sql File Finished");
        }
    }

}

