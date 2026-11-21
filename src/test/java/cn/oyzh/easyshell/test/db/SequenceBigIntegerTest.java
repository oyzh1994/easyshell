package cn.oyzh.easyshell.test.db;

import cn.oyzh.easyshell.dameng.generator.sequence.DamengSequenceAlertSqlGenerator;
import cn.oyzh.easyshell.dameng.generator.sequence.DamengSequenceCreateSqlGenerator;
import cn.oyzh.easyshell.dameng.sequence.DamengAlertSequenceParam;
import cn.oyzh.easyshell.dameng.sequence.DamengCreateSequenceParam;
import cn.oyzh.easyshell.dameng.sequence.DamengSequence;
import cn.oyzh.easyshell.oracle.generator.sequence.OracleSequenceAlertSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.sequence.OracleSequenceCreateSqlGenerator;
import cn.oyzh.easyshell.oracle.sequence.OracleAlertSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleCreateSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.easyshell.postgresql.generator.sequence.PostgresqlSequenceAlertSqlGenerator;
import cn.oyzh.easyshell.postgresql.generator.sequence.PostgresqlSequenceCreateSqlGenerator;
import cn.oyzh.easyshell.postgresql.sequence.PostgresqlAlertSequenceParam;
import cn.oyzh.easyshell.postgresql.sequence.PostgresqlCreateSequenceParam;
import cn.oyzh.easyshell.postgresql.sequence.PostgresqlSequence;
import cn.oyzh.easyshell.util.db.ShellSequenceUtil;
import org.junit.Test;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SequenceBigIntegerTest {

    private static final BigInteger MIN_VAL = new BigInteger("-99999999999999999999999999999");

    private static final BigInteger MAX_VAL = new BigInteger("9999999999999999999999999999");

    private static final BigInteger CURRENT_VAL = new BigInteger("9223372036854775808");

    private static final BigInteger CACHE_SIZE = new BigInteger("100000000000000000000");

    private static final BigInteger INCREMENT_BY = new BigInteger("9223372036854775808");

    @Test
    public void readsSequenceValuesBeyondLongRange() throws Exception {
        CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet();
        RowSetMetaDataImpl metaData = new RowSetMetaDataImpl();
        metaData.setColumnCount(1);
        metaData.setColumnName(1, "SEQUENCE_VALUE");
        metaData.setColumnLabel(1, "SEQUENCE_VALUE");
        metaData.setColumnType(1, Types.NUMERIC);
        metaData.setColumnTypeName(1, "NUMERIC");
        rowSet.setMetaData(metaData);
        rowSet.moveToInsertRow();
        rowSet.updateBigDecimal(1, new BigDecimal(MAX_VAL));
        rowSet.insertRow();
        rowSet.moveToCurrentRow();
        rowSet.beforeFirst();
        assertTrue(rowSet.next());

        assertEquals(MAX_VAL, ShellSequenceUtil.getBigInteger(rowSet, "SEQUENCE_VALUE"));
    }

    @Test
    public void oracleSequenceRetainsBigIntegerValues() {
        OracleSequence sequence = oracleSequence();

        assertEquals(MIN_VAL, sequence.getMinVal());
        assertEquals(MAX_VAL, sequence.getMaxVal());
        assertEquals(CURRENT_VAL, sequence.getCurrentVal());
        assertEquals(CACHE_SIZE, sequence.getCacheSize());
        assertEquals(INCREMENT_BY, sequence.getIncrementBy());
        assertSequenceSql(OracleSequenceCreateSqlGenerator.generateSqlSingle(createParam(sequence)),
                OracleSequenceAlertSqlGenerator.generateSqlSingle(alertParam(sequence)));
    }

    @Test
    public void oracleAlertSqlOmitsUnchangedMinValue() {
        OracleSequence original = oracleSequence();
        OracleSequence changed = oracleSequence();
        changed.setMaxVal(MAX_VAL.subtract(BigInteger.ONE));

        OracleAlertSequenceParam param = alertParam(changed);
        param.setOriginalSequence(original);
        String sql = OracleSequenceAlertSqlGenerator.generateSqlSingle(param);

        assertFalse(sql.contains(" MINVALUE "));
        assertTrue(sql.contains(" MAXVALUE " + changed.getMaxVal()));
        assertFalse(sql.endsWith(";"));
    }

    @Test
    public void oracleAlertChangesCurrentValueWithVersionFallback() {
        OracleSequence original = oracleSequence();
        original.setCurrentVal(BigInteger.TEN);
        OracleSequence changed = oracleSequence();
        changed.setCurrentVal(BigInteger.valueOf(1000));

        OracleAlertSequenceParam param = alertParam(changed);
        param.setOriginalSequence(original);

        List<String> modernSql = OracleSequenceAlertSqlGenerator.generateSql(param, true);
        assertEquals(1, modernSql.size());
        assertTrue(modernSql.getFirst().contains(" RESTART START WITH 1000"));

        List<String> legacySql = OracleSequenceAlertSqlGenerator.generateSql(param, false);
        assertEquals(1, legacySql.size());
        String fallback = legacySql.getFirst();
        assertFalse(fallback.contains(" RESTART "));
        assertTrue(fallback.contains("V_TARGET NUMBER := 1000"));
        assertTrue(fallback.contains("\"SYSTEM\".\"LARGE_SEQ\".NEXTVAL"));
        assertTrue(fallback.contains("INCREMENT BY ' || TO_CHAR(V_TARGET - V_CURRENT - V_INCREMENT)"));
    }

    @Test
    public void damengSequenceRetainsBigIntegerValues() {
        DamengSequence sequence = damengSequence();

        assertEquals(MIN_VAL, sequence.getMinVal());
        assertEquals(MAX_VAL, sequence.getMaxVal());
        assertEquals(CURRENT_VAL, sequence.getCurrentVal());
        assertEquals(CACHE_SIZE, sequence.getCacheSize());
        assertEquals(INCREMENT_BY, sequence.getIncrementBy());
        assertSequenceSql(DamengSequenceCreateSqlGenerator.generateSqlSingle(createParam(sequence)),
                DamengSequenceAlertSqlGenerator.generateSqlSingle(alertParam(sequence)));
    }

    @Test
    public void postgresqlSequenceRetainsBigIntegerValues() {
        PostgresqlSequence sequence = postgresqlSequence();

        assertEquals(MIN_VAL, sequence.getMinVal());
        assertEquals(MAX_VAL, sequence.getMaxVal());
        assertEquals(CURRENT_VAL, sequence.getCurrentVal());
        assertEquals(CACHE_SIZE, sequence.getCacheSize());
        assertEquals(INCREMENT_BY, sequence.getIncrementBy());
        assertSequenceSql(PostgresqlSequenceCreateSqlGenerator.generateSqlSingle(createParam(sequence)),
                PostgresqlSequenceAlertSqlGenerator.generateSqlSingle(alertParam(sequence)));
    }

    private static OracleSequence oracleSequence() {
        OracleSequence sequence = new OracleSequence("LARGE_SEQ");
        sequence.setSchema("SYSTEM");
        setValues(sequence::setMinVal, sequence::setMaxVal, sequence::setCurrentVal,
                sequence::setCacheSize, sequence::setIncrementBy);
        return sequence;
    }

    private static DamengSequence damengSequence() {
        DamengSequence sequence = new DamengSequence("LARGE_SEQ");
        sequence.setSchema("SYSDBA");
        setValues(sequence::setMinVal, sequence::setMaxVal, sequence::setCurrentVal,
                sequence::setCacheSize, sequence::setIncrementBy);
        return sequence;
    }

    private static PostgresqlSequence postgresqlSequence() {
        PostgresqlSequence sequence = new PostgresqlSequence("LARGE_SEQ");
        sequence.setSchema("public");
        setValues(sequence::setMinVal, sequence::setMaxVal, sequence::setCurrentVal,
                sequence::setCacheSize, sequence::setIncrementBy);
        return sequence;
    }

    private static void setValues(SequenceValueConsumer minVal, SequenceValueConsumer maxVal,
                                  SequenceValueConsumer currentVal, SequenceValueConsumer cacheSize,
                                  SequenceValueConsumer incrementBy) {
        minVal.accept(MIN_VAL);
        maxVal.accept(MAX_VAL);
        currentVal.accept(CURRENT_VAL);
        cacheSize.accept(CACHE_SIZE);
        incrementBy.accept(INCREMENT_BY);
    }

    private static void assertSequenceSql(String createSql, String alertSql) {
        for (String sql : new String[]{createSql, alertSql}) {
            assertFalse(sql.endsWith(";"));
            assertTrue(sql.contains(" INCREMENT BY " + INCREMENT_BY));
            assertTrue(sql.contains(" MAXVALUE " + MAX_VAL));
            assertTrue(sql.contains(" MINVALUE " + MIN_VAL));
            assertTrue(sql.contains(" CACHE " + CACHE_SIZE));
        }
    }

    private static OracleCreateSequenceParam createParam(OracleSequence sequence) {
        OracleCreateSequenceParam param = new OracleCreateSequenceParam();
        param.setSequence(sequence);
        return param;
    }

    private static OracleAlertSequenceParam alertParam(OracleSequence sequence) {
        OracleAlertSequenceParam param = new OracleAlertSequenceParam();
        param.setSequence(sequence);
        return param;
    }

    private static DamengCreateSequenceParam createParam(DamengSequence sequence) {
        DamengCreateSequenceParam param = new DamengCreateSequenceParam();
        param.setSequence(sequence);
        return param;
    }

    private static DamengAlertSequenceParam alertParam(DamengSequence sequence) {
        DamengAlertSequenceParam param = new DamengAlertSequenceParam();
        param.setSequence(sequence);
        return param;
    }

    private static PostgresqlCreateSequenceParam createParam(PostgresqlSequence sequence) {
        PostgresqlCreateSequenceParam param = new PostgresqlCreateSequenceParam();
        param.setSequence(sequence);
        return param;
    }

    private static PostgresqlAlertSequenceParam alertParam(PostgresqlSequence sequence) {
        PostgresqlAlertSequenceParam param = new PostgresqlAlertSequenceParam();
        param.setSequence(sequence);
        return param;
    }

    @FunctionalInterface
    private interface SequenceValueConsumer {
        void accept(BigInteger value);
    }
}
