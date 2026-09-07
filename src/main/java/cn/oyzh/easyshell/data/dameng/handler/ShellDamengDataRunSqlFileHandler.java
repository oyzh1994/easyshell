package cn.oyzh.easyshell.data.dameng.handler;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.fx.db.data.handler.DBDataRunFileHandler;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author oyzh
 * @since 2024/09/10
 */
public class ShellDamengDataRunSqlFileHandler extends DBDataRunFileHandler<String> {

    private final ShellDamengClient dbClient;

    public ShellDamengDataRunSqlFileHandler(ShellDamengClient dbClient, String dbName) {
        super(dbName);
        this.dbClient = dbClient;
    }

    @Override
    public void runFile() throws Exception {
        this.message("Run Sql File Starting");
        // 文件读取
        try (BufferedReader reader = FileUtil.getReader(this.file, StandardCharsets.UTF_8)) {
            // 暂存数据拼接对象
            StringBuilder builder = new StringBuilder();
            // 多行注释标志位
            AtomicBoolean commentFlag = new AtomicBoolean(false);
            // 创建表、视图标志位
            AtomicBoolean createFlag1 = new AtomicBoolean(false);
            // 创建触发器、函数、过程、事件标志位
            AtomicBoolean createFlag2 = new AtomicBoolean(false);
            // 自增标志位
            AtomicBoolean identityflag = new AtomicBoolean(false);
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
                    // 业务处理
                    if (!createFlag1.get() && !createFlag2.get()) {
                        // 自增开关处理
                        if (StringUtil.startWithAnyIgnoreCase(line, "SET IDENTITY_INSERT ")) {
                            if (StringUtil.endWithIgnoreCase(line, "ON;")) {
                                this.doBatchInsert();
                                this.getInsertList().add(line);
                                identityflag.set(true);
                            } else {
                                this.getInsertList().add(line);
                                this.doBatchInsert(this.getInsertList(), false);
                                this.getInsertList().clear();
                                identityflag.set(false);
                            }
                        }
                        // 新增记录用批量处理
                        if (StringUtil.startWithAnyIgnoreCase(line, "INSERT INTO ")) {
                            if (identityflag.get()) {
                                this.getInsertList().add(line);
                            } else {
                                this.addInsert(line);
                            }
                        }
                        // 删除表、函数、过程、触发器、设置变量等
                        if (StringUtil.startWithAnyIgnoreCase(line, "SET ", "DROP ")) {
                            this.dbClient.executeSqlSimple(this.dbName, line);
                            this.processedIncr();
                            continue;
                        }
                        // 注释
                        if (StringUtil.startWithAnyIgnoreCase(line, "COMMENT ON ")) {
                            this.dbClient.executeSqlSimple(this.dbName, line);
                            this.processedIncr();
                            continue;
                        }
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
                            if (builder.toString().endsWith("\n;;\n")) {
                                builder.delete(builder.length() - 4, builder.length());
                            }
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
                    StringUtil.clear(builder);
                    this.getInsertList().clear();
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

    @Override
    public void doBatchInsert(List<String> list, boolean parallel) throws Exception {
        try {
            int result = this.dbClient.insertBatch(this.dbName, list, parallel);
            this.processedIncr(result);
        } catch (Exception ex) {
            this.processedDecr(list.size());
            throw ex;
        }
    }
}

