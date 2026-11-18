package cn.oyzh.easyshell.util.db;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 序列值工具类
 *
 * @author oyzh
 * @since 2026-11-18
 */
public final class ShellSequenceUtil {

    private ShellSequenceUtil() {
    }

    /**
     * 获取任意精度整数序列值
     *
     * @param resultSet 结果集
     * @param column    字段名称
     * @return 序列值
     * @throws SQLException 查询异常
     */
    public static BigInteger getBigInteger(ResultSet resultSet, String column) throws SQLException {
        BigDecimal value = resultSet.getBigDecimal(column);
        return value == null ? null : value.toBigInteger();
    }
}
