package cn.oyzh.easyshell.data.dameng.handler;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.column.DamengSelectColumnParam;
import cn.oyzh.easyshell.dameng.foreignKey.DamengForeignKey;
import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengSelectRecordParam;
import cn.oyzh.easyshell.dameng.table.DamengSelectTableParam;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.dameng.trigger.DamengTrigger;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.util.dameng.ShellDamengDataUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.handler.DBDataDumpHandler;
import cn.oyzh.fx.db.util.DBUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2024/09/10
 */
public class ShellDamengDataDumpHandler extends DBDataDumpHandler {

    /**
     * db客户端
     */
    protected ShellDamengClient dbClient;

    public ShellDamengDataDumpHandler(ShellDamengClient dbClient, String dbName) {
        super(dbName, DBDialect.DAMENG);
        this.dbClient = dbClient;
    }

    @Override
    public void doDump() throws Exception {
        if (this.fileWriter == null || this.dumpType == null || this.dataType == null) {
            throw new RuntimeException("parameter invalid!");
        }
        this.message("Dump Starting");
        this.writeHeader();
        try {
            if (this.dumpType == 1) {
                this.dumpTable();
                this.dumpView();
                this.dumpFunction();
                this.dumpProcedure();
                this.dumpTrigger();
            } else if (this.dumpType == 2) {
                DamengTable table = this.dbClient.selectTable(this.dbName, this.tableName);
                this.dumpTable(table);
                this.processedIncr();
            }
            this.writeTail();
            this.fileWriter.close();
        } catch (Exception ex) {
            this.exception(ex);
        }
        this.message("Dump Finished");
        this.message("Dump File To -> " + this.dumpFile.getPath());
    }

    @Override
    protected void writeHeader() throws IOException {
        String version = this.dbClient.selectVersion();
        String clientCharacter = this.dbClient.selectClientCharacter();
        String header = "/*\n";
        header += " EasyDB Data Transfer";
        header += "\n\n";
        header += " Source Server : " + this.dbClient.getShellConnect().getName();
        header += "\n";
        header += " Source Server Type : " + this.dbClient.dialect().name();
        header += "\n";
        header += " Source Server Version : " + version;
        header += "\n";
        header += " Source Host : " + this.dbClient.getShellConnect().getHost();
        header += "\n";
        header += " Source Schema : " + this.dbName;
        header += "\n\n";
        header += " Target Server Type : " + this.dbClient.dialect().name();
        header += "\n";
        header += " Target Server Version : " + version;
        header += "\n";
        header += " File Encoding : " + clientCharacter;
        header += "\n\n";
        header += " Date : " + DateHelper.formatDateTimeSimple();
        header += "\n";
        header += "*/";

        header += "\n\n";
        this.fileWriter.writeLines(List.of(header));
    }

    @Override
    protected void writeTail() throws IOException {

    }

    /**
     * 对表进行排序，被外键引用的父表排在前面，子表排在后面
     *
     * @param tables 表列表
     */
    private void sortTables(List<DamengTable> tables) throws InterruptedException {
        // 表名 -> 表对象映射
        Map<String, DamengTable> tableMap = tables.stream()
                .collect(Collectors.toMap(DamengTable::getName, t -> t, (a, b) -> a));
        Set<String> tableNames = tableMap.keySet();

        // 邻接表: 父表 -> 依赖它的子表集合
        Map<String, Set<String>> dependents = new HashMap<>();
        // 每个表的入度（被引用次数）
        Map<String, Integer> inDegree = new HashMap<>();
        for (DamengTable table : tables) {
            inDegree.putIfAbsent(table.getName(), 0);
            dependents.putIfAbsent(table.getName(), new LinkedHashSet<>());
        }

        // 查询外键，构建依赖图：子表 -> 父表（子表依赖父表）
        // 查询有外键的表名称
        List<String> fTables = this.dbClient.selectForeignKeyTables(tables.getFirst().getSchema());
        for (DamengTable table : tables) {
            if (!fTables.contains(table.getName())) {
                continue;
            }
            List<DamengForeignKey> foreignKeys = this.dbClient.selectForeignKeys(table.getSchema(), table.getName());
            for (DamengForeignKey foreignKey : foreignKeys) {
                this.checkInterrupt();
                String parentTable = foreignKey.getPrimaryKeyTable();
                // 仅处理属于当前表列表的引用
                if (parentTable != null && tableNames.contains(parentTable) && !parentTable.equals(table.getName())) {
                    dependents.computeIfAbsent(parentTable, k -> new LinkedHashSet<>()).add(table.getName());
                    inDegree.merge(table.getName(), 1, Integer::sum);
                }
                // 更新状态
                this.processed(0);
            }
        }

        // 拓扑排序（BFS / Kahn算法）
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<DamengTable> sorted = new ArrayList<>(tables.size());
        while (!queue.isEmpty()) {
            String tableName = queue.poll();
            sorted.add(tableMap.get(tableName));
            for (String child : dependents.getOrDefault(tableName, Collections.emptySet())) {
                int newDegree = inDegree.merge(child, -1, Integer::sum);
                if (newDegree == 0) {
                    queue.add(child);
                }
            }
        }

        // 存在循环依赖时，将剩余表追加到末尾
        if (sorted.size() < tables.size()) {
            Set<String> sortedNames = sorted.stream().map(DamengTable::getName).collect(Collectors.toSet());
            for (DamengTable table : tables) {
                if (!sortedNames.contains(table.getName())) {
                    sorted.add(table);
                }
            }
        }

        // 替换原列表内容
        tables.clear();
        tables.addAll(sorted);
    }

    protected void dumpTable() throws InterruptedException, IOException {
        DamengSelectTableParam selectTableParam = new DamengSelectTableParam();
        selectTableParam.setFull(true);
        selectTableParam.setSchema(this.dbName);
        this.message("select tables");
        List<DamengTable> tables = this.dbClient.selectTables(selectTableParam);
        if (CollectionUtil.isNotEmpty(tables)) {
            this.message("sort tables");
            // 按外键依赖排序，父表在前
            this.sortTables(tables);
            for (DamengTable table : tables) {
                this.checkInterrupt();
                this.dumpTable(table);
                this.processedIncr();
            }
        }
    }

    protected void dumpTable(DamengTable table) throws InterruptedException, IOException {
        String line0 = "";
        String line1 = "-- ----------------------------";
        String line2 = "-- Table structure for " + table.getName();
        String line3 = "-- ----------------------------";
        String dropTable = "DROP TABLE IF EXISTS " + DBUtil.wrap(table.getName(), DBDialect.DAMENG) + ";";
        String createDefinition = table.getCreateDefinition();
        if (!createDefinition.endsWith(";")) {
            createDefinition += ";";
        }
        //        createDefinition = createDefinition.replaceAll("CREATE\\s+TABLE\\s+\"[^\"]+\"\\.", "CREATE TABLE ");
        // TODO: 去除特定架构
        createDefinition = createDefinition.replaceAll(DBUtil.wrap(table.getSchema(), this.dialect) + ".", "");
        this.message("Dumping Table " + table.getName());
        this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTable, createDefinition));
        // 查询字段
        DamengColumns columns = new DamengColumns(this.dbClient.selectColumns(new DamengSelectColumnParam(this.dbName, table.getName())));
        // 设置字段注释
        for (DamengColumn column : columns) {
            String tableComment = """
                    COMMENT ON COLUMN "$1"."$2" IS $3;
                    """;
            tableComment = tableComment.replace("$1", column.getTableName());
            tableComment = tableComment.replace("$2", column.getName());
            if (StringUtil.isNotBlank(column.getComment())) {
                tableComment = tableComment.replace("$3", DBUtil.wrapData(column.getComment(), this.dialect).toString());
            } else {
                tableComment = tableComment.replace("$3", "''");
            }
            this.fileWriter.appendLine(tableComment);
        }
        // 设置表注释
        String tableComment = """
                COMMENT ON TABLE "$1" IS $2;
                """;
        tableComment = tableComment.replace("$1", table.getName());
        if (StringUtil.isNotBlank(table.getComment())) {
            tableComment = tableComment.replace("$2", DBUtil.wrapData(table.getComment(), this.dialect).toString());
        } else {
            tableComment = tableComment.replace("$2", "''");
        }
        this.fileWriter.appendLine(tableComment);
        if (this.isDumpRecord()) {
            this.message("Dumping Records of Table " + table.getName());
            this.dumpRecord(table, columns);
        }
    }

    protected void dumpRecord(DamengTable table, DamengColumns columns) throws InterruptedException, IOException {
        String createDefinition = table.getCreateDefinition();
        String tableName = table.getName();
        long start = 0;
        String line0 = "-- ----------------------------";
        String line1 = "-- Records of " + tableName;
        String line2 = "-- ----------------------------";
        this.fileWriter.appendLines(List.of(line0, line1, line2));
        boolean hasIdentity = columns.hasAutoIncrement() && !StringUtil.containsIgnoreCase(createDefinition, " AUTO_INCREMENT ");
        if (hasIdentity) {
            String line = "SET IDENTITY_INSERT " + DBUtil.wrap(tableName, DBDialect.DAMENG) + " ON;";
            this.fileWriter.appendLine(line);
        }
        while (true) {
            this.checkInterrupt();
            long start1 = System.currentTimeMillis();
            DamengSelectRecordParam param = new DamengSelectRecordParam();
            param.setStart(start);
            param.setReadonly(true);
            param.setColumns(columns);
            param.setSchema(this.dbName);
            param.setTableName(tableName);
            param.setLimit((long) this.queryLimit);
            List<DamengRecord> records = this.dbClient.selectRecords(param);
            if (CollectionUtil.isEmpty(records)) {
                break;
            }
            long end1 = System.currentTimeMillis();
            JulLog.info("查询耗时: {}ms", (end1 - start1));
            long start2 = System.currentTimeMillis();
            List<String> inserts = ShellDamengDataUtil.toInsertSql(columns, records, true);
            this.fileWriter.appendLines(inserts);
            long end2 = System.currentTimeMillis();
            JulLog.info("写入耗时: {}ms", (end2 - start2));
            start += this.queryLimit;
            this.processed(records.size());
        }
        if (hasIdentity) {
            String line = "SET IDENTITY_INSERT " + DBUtil.wrap(tableName, DBDialect.DAMENG) + " OFF;";
            this.fileWriter.appendLine(line);
        }
    }

    protected void dumpView() throws Exception {
        List<DamengView> views = this.dbClient.selectViews(this.dbName);
        if (CollectionUtil.isNotEmpty(views)) {
            for (DamengView view : views) {
                this.checkInterrupt();
                this.message("Dumping View " + view.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- View structure for " + view.getName();
                String line3 = "-- ----------------------------";
                String dropTable = "DROP VIEW IF EXISTS " + DBUtil.wrap(view.getName(), DBDialect.DAMENG) + ";";
                String createDefinition = view.getCreateDefinition();
                // TODO: 去除特定架构
                createDefinition = createDefinition.replaceAll(DBUtil.wrap(view.getSchema(), this.dialect) + ".", "");
                //                String createDefinition = this.dbClient.showCreateView(this.dbName, view.getName());
                if (!createDefinition.endsWith(";")) {
                    createDefinition += ";";
                }
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTable, createDefinition));
                this.processedIncr();
            }
        }
    }

    protected void dumpFunction() throws Exception {
        List<DamengFunction> functions = this.dbClient.selectFunctions(this.dbName);
        if (CollectionUtil.isNotEmpty(functions)) {
            for (DamengFunction function : functions) {
                this.checkInterrupt();
                this.message("Dumping Function " + function.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Function structure for " + function.getName();
                String line3 = "-- ----------------------------";
                String dropFunction = "DROP FUNCTION IF EXISTS " + DBUtil.wrap(function.getName(), DBDialect.DAMENG) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = function.getCreateDefinition();
                // TODO: 去除特定架构
                createDefinition = createDefinition.replaceAll(DBUtil.wrap(function.getSchema(), this.dialect) + ".", "");
                //                String createDefinition = this.dbClient.showCreateFunction(this.dbName, function.getName());
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropFunction, line4, createDefinition, line5, line6));
                this.processedIncr();
            }
        }
    }

    protected void dumpProcedure() throws Exception {
        List<DamengProcedure> procedures = this.dbClient.selectProcedures(this.dbName);
        if (CollectionUtil.isNotEmpty(procedures)) {
            for (DamengProcedure procedure : procedures) {
                this.checkInterrupt();
                this.message("Dumping Procedure " + procedure.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Procedure structure for " + procedure.getName();
                String line3 = "-- ----------------------------";
                String dropProcedure = "DROP PROCEDURE IF EXISTS " + DBUtil.wrap(procedure.getName(), DBDialect.DAMENG) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = procedure.getCreateDefinition();
                // TODO: 去除特定架构
                createDefinition = createDefinition.replaceAll(DBUtil.wrap(procedure.getSchema(), this.dialect) + ".", "");
                //                String createDefinition = this.dbClient.showCreateProcedure(this.dbName, procedure.getName());
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropProcedure, line4, createDefinition, line5, line6));
                this.processedIncr();
            }
        }
    }

    protected void dumpTrigger() throws Exception {
        List<DamengTrigger> triggers = this.dbClient.selectTriggers(this.dbName);
        if (CollectionUtil.isNotEmpty(triggers)) {
            for (DamengTrigger trigger : triggers) {
                this.message("Dumping Trigger " + trigger.getName());
                String line0 = "";
                String line1 = "-- ----------------------------";
                String line2 = "-- Trigger structure for " + trigger.getName();
                String line3 = "-- ----------------------------";
                String dropTrigger = "DROP TRIGGER IF EXISTS " + DBUtil.wrap(trigger.getName(), DBDialect.DAMENG) + ";";
                String line4 = "delimiter ;;";
                String line5 = ";;";
                String line6 = "delimiter ;";
                String createDefinition = trigger.getCreateDefinition();
                // TODO: 去除特定架构
                createDefinition = createDefinition.replaceAll(DBUtil.wrap(trigger.getSchema(), this.dialect) + ".", "");
                //                String createDefinition = this.dbClient.showCreateTrigger(this.dbName, trigger.getName());
                this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTrigger, line4, createDefinition, line5, line6));
                this.processedIncr();
            }
        }
    }

    // protected void dumpEvent() throws Exception {
    //     List<DamengEvent> events = this.dbClient.events(this.dbName);
    //     if (CollectionUtil.isNotEmpty(events)) {
    //         for (DamengEvent event : events) {
    //             this.message("Dumping Event " + event.getName());
    //             String line0 = "";
    //             String line1 = "-- ----------------------------";
    //             String line2 = "-- Event structure for " + event.getName();
    //             String line3 = "-- ----------------------------";
    //             String dropTrigger = "DROP EVENT IF EXISTS " + DBUtil.wrap(event.getName()) + ";";
    //             String line4 = "delimiter ;;";
    //             String line5 = ";;";
    //             String line6 = "delimiter ;";
    //             String createDefinition = event.getCreateDefinition();
    //             this.fileWriter.appendLines(List.of(line0, line1, line2, line3, dropTrigger, line4, createDefinition, line5, line6));
    //         }
    //         this.processed(events.size());
    //     }
    // }
}

