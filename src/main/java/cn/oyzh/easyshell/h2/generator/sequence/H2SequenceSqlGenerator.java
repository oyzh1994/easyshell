package cn.oyzh.easyshell.h2.generator.sequence;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.sequence.H2Sequence;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * H2 序列 SQL 生成器。
 *
 * @since 2026-11-28
 */
public final class H2SequenceSqlGenerator {

    private H2SequenceSqlGenerator() {
    }

    public static String create(H2Sequence sequence) {
        H2GeneratorUtil.requireName(sequence == null ? null : sequence.getName());
        validate(sequence, null);
        StringBuilder sql = new StringBuilder("CREATE SEQUENCE ")
                .append(H2GeneratorUtil.qualified(sequence.getSchema(), sequence.getName()));
        appendOptions(sql, sequence, true, true, false);
        return sql.toString();
    }

    public static List<String> alter(String originalName, H2Sequence original,
                                     H2Sequence sequence) {
        H2GeneratorUtil.requireName(sequence == null ? null : sequence.getName());
        validate(sequence, original);
        List<String> scripts = new ArrayList<>();
        StringBuilder sql = new StringBuilder("ALTER SEQUENCE ")
                .append(H2GeneratorUtil.qualified(sequence.getSchema(), originalName));
        int optionStart = sql.length();
        String currentValue = original == null ? null
                : StringUtil.blankToDefault(original.getCurrentValue(), original.getStartValue());
        boolean restart = original == null
                || !StringUtil.equals(currentValue, sequence.getStartValue());
        boolean updateStart = original != null
                && StringUtil.isNotBlank(sequence.getStartValue())
                && !inRange(original.getStartValue(),
                sequence.getMinimumValue(), sequence.getMaximumValue());
        appendOptions(sql, sequence, false, updateStart, restart);
        if (sql.length() == optionStart) {
            return scripts;
        }
        scripts.add(sql.toString());
        return scripts;
    }

    private static void appendOptions(StringBuilder sql, H2Sequence sequence,
                                      boolean create, boolean updateStart, boolean restart) {
        if (updateStart && StringUtil.isNotBlank(sequence.getStartValue())) {
            sql.append(" START WITH ").append(sequence.getStartValue().trim());
        }
        if (restart && StringUtil.isNotBlank(sequence.getStartValue())) {
            sql.append(" RESTART WITH ").append(sequence.getStartValue().trim());
        }
        if (StringUtil.isNotBlank(sequence.getIncrement())) {
            sql.append(" INCREMENT BY ").append(sequence.getIncrement().trim());
        }
        if (StringUtil.isNotBlank(sequence.getMinimumValue())) {
            sql.append(" MINVALUE ").append(sequence.getMinimumValue().trim());
        }
        if (StringUtil.isNotBlank(sequence.getMaximumValue())) {
            sql.append(" MAXVALUE ").append(sequence.getMaximumValue().trim());
        }
        if (StringUtil.isNotBlank(sequence.getCacheSize())) {
            sql.append(" CACHE ").append(sequence.getCacheSize().trim());
        }
        if (sequence.isCycle()) {
            sql.append(" CYCLE");
        } else if (!create) {
            sql.append(" NO CYCLE");
        }
    }

    /**
     * 校验 H2 序列属性。
     * <p>
     * H2 同时校验 BASE_VALUE 和 START_VALUE，因此修改范围时必须保证当前值仍在范围内。
     *
     * @param sequence 目标序列
     * @param original 原序列
     */
    public static void validate(H2Sequence sequence, H2Sequence original) {
        if (sequence == null) {
            throw new IllegalArgumentException("Sequence is required");
        }
        BigInteger minimum = number(sequence.getMinimumValue(), "minimum value");
        BigInteger maximum = number(sequence.getMaximumValue(), "maximum value");
        BigInteger increment = number(sequence.getIncrement(), "increment");
        BigInteger cache = number(sequence.getCacheSize(), "cache size");
        BigInteger target = number(sequence.getStartValue(), "start value");
        if (minimum != null && maximum != null && minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException(
                    "Sequence minimum value cannot be greater than maximum value");
        }
        if (increment != null && increment.signum() == 0) {
            throw new IllegalArgumentException("Sequence increment cannot be zero");
        }
        if (cache != null && cache.signum() <= 0) {
            throw new IllegalArgumentException("Sequence cache size must be greater than zero");
        }
        if (target == null && original == null) {
            target = BigInteger.ONE;
        }
        if (target != null) {
            requireInRange(target, minimum, maximum, "start value");
        }
        if (original != null) {
            String currentText = StringUtil.blankToDefault(
                    original.getCurrentValue(), original.getStartValue());
            BigInteger current = number(currentText, "current value");
            boolean restarted = sequence.getStartValue() != null
                    && !StringUtil.equals(currentText, sequence.getStartValue());
            requireInRange(restarted ? target : current,
                    minimum, maximum, "current value");
        }
    }

    private static boolean inRange(String value, String minimumText, String maximumText) {
        BigInteger number = number(value, "value");
        if (number == null) {
            return true;
        }
        BigInteger minimum = number(minimumText, "minimum value");
        BigInteger maximum = number(maximumText, "maximum value");
        return (minimum == null || number.compareTo(minimum) >= 0)
                && (maximum == null || number.compareTo(maximum) <= 0);
    }

    private static void requireInRange(BigInteger value, BigInteger minimum,
                                       BigInteger maximum, String name) {
        if (value != null
                && ((minimum != null && value.compareTo(minimum) < 0)
                || (maximum != null && value.compareTo(maximum) > 0))) {
            throw new IllegalArgumentException(
                    "Sequence " + name + " must be between minimum and maximum values");
        }
    }

    private static BigInteger number(String value, String name) {
        if (StringUtil.isBlank(value)) {
            return null;
        }
        try {
            return new BigInteger(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid sequence " + name + ": " + value, ex);
        }
    }

    public static String drop(String schema, String name) {
        H2GeneratorUtil.requireName(name);
        return "DROP SEQUENCE IF EXISTS " + H2GeneratorUtil.qualified(schema, name);
    }
}
