package cn.oyzh.easyshell.mysql.data;

import cn.oyzh.common.file.SkipAbleFileReader;
import cn.oyzh.easyshell.mysql.data.MysqlDataImportConfig;
import cn.oyzh.easyshell.mysql.data.MysqlTypeFileReader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-03
 */
public class MysqlCsvTypeFileReader extends MysqlTypeFileReader {

    /**
     * 字段列表
     */
    private List<String> columns;

    /**
     * 导入配置
     */
    private MysqlDataImportConfig config;

    /**
     * 文件读取器
     */
    private SkipAbleFileReader reader;

    public MysqlCsvTypeFileReader( File file, MysqlDataImportConfig config) throws IOException {
        this.config = config;
        this.reader = new SkipAbleFileReader(file, Charset.forName(config.getCharset()));
        this.init();
    }

    @Override
    protected void init() throws IOException {
        this.reader.jumpLine(this.config.getColumnIndex());
        this.columns = new ArrayList<>();
        String line = this.reader.readLine();
        this.columns.addAll(this.parseLine(line, this.config.txtIdentifierChar(), ','));
        this.reader.jumpLine(this.config.getDataStartIndex());
    }

    @Override
    public Map<String, Object> readObject() throws IOException {
        String line = this.reader.readLine();
        if (line != null) {
            List<String> arr = this.parseLine(line, this.config.txtIdentifierChar(), ',');
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < arr.size(); i++) {
                map.put(this.columns.get(i), arr.get(i));
            }
            return map;
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
        this.reader = null;
        this.config = null;
        this.columns = null;
    }
}
