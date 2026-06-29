package cn.oyzh.easyshell.data.mongo.handler;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author oyzh
 * @since 2024/09/10
 */
public class ShellMongoDataRunFileHandler extends DBDataRunFileHandler {

    public ShellMongoDataRunFileHandler(ShellMongoClient dbClient, String dbName) {
        super(dbClient, dbName);
    }

    @Override
    public void runFile() throws Exception {
        this.message("Run Script File Starting");
        // 文件读取
        try (BufferedReader reader = FileUtil.getReader(this.sqlFile, StandardCharsets.UTF_8)) {
            // 暂存数据拼接对象
            StringBuilder builder = new StringBuilder();
            // 多行注释标志位
            AtomicBoolean commentFlag = new AtomicBoolean(false);
            // 创建表、视图标志位
            AtomicBoolean createFlag = new AtomicBoolean(false);
            // 执行
            while (reader.ready()) {
                try {
                    // 检查中断
                    this.checkInterrupt();
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    // 单行注释
                    if (line.stripLeading().startsWith("//")) {
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
                    // 命令开始
                    if (line.stripLeading().startsWith("db.")) {
                        createFlag.set(true);
                    }
                    // 命令结束
                    if (createFlag.get() && line.stripTrailing().endsWith(";")) {
                        createFlag.set(false);
                        builder.append(line).append("\n");
                        this.engine.eval(builder.toString());
                        builder.delete(0, builder.length());
                        this.processedIncr();
                        continue;
                    }
                    // 命令中间过程
                    if (createFlag.get()) {
                        builder.append(line).append("\n");
                    }
                } catch (Exception ex) {
                    this.exception(ex);
                    this.processedDecr();
                    builder.delete(0, builder.length());
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
            this.message("Run Script File Finished");
        }
    }

}

