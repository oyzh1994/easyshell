package cn.oyzh.easyshell.test;

import cn.oyzh.common.date.LocalDateTimeUtil;
import cn.oyzh.common.util.StringUtil;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class DateTest {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final SimpleDateFormat FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static final SimpleDateFormat FORMAT_2 = new SimpleDateFormat("yyyy-MM-dd HH");

    public static final SimpleDateFormat FORMAT_T = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static final SimpleDateFormat FORMAT_T_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    public static final SimpleDateFormat FORMAT_T_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH");

    public static String format(Object value) {
        if (value == null) {
            return null;
        }
        long count = StringUtil.count(value.toString(), ':');
        SimpleDateFormat format;
        if (value.toString().contains("T")) {
            format = count == 3 ? FORMAT_T : count == 2 ? FORMAT_T_1 : FORMAT_T_2;
        } else {
            format = count == 3 ? FORMAT : count == 2 ? FORMAT_1 : FORMAT_2;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return LocalDateTimeUtil.format(localDateTime, format.toPattern());
        }
        if (value instanceof java.util.Date date) {
            return format.format(date);
        }
        return value.toString();
    }

    @Test
    public void test() throws ParseException {
        Date date = FORMAT_T_1.parse("2027-01-07T00:00");
        String s = format(date);
        System.out.println(s);
    }
}
