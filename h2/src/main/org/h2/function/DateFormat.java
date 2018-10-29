package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueNull;
import org.h2.value.ValueString;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 语法
 * DATE_FORMAT(date,format)
 * date 参数是合法的日期。format 规定日期/wa。
 * 可以使用的格式有：
 * 格式	描述
 * %a	缩写星期名
 * %b	缩写月名
 * %c	月，数值
 * %D	带有英文前缀的月中的天
 * %d	月的天，数值(00-31)
 * %e	月的天，数值(0-31)
 * %f	微秒
 * %H	小时 (00-23)
 * %h	小时 (01-12)
 * %I	小时 (01-12)
 * %i	分钟，数值(00-59)
 * %j	年的天 (001-366)
 * %k	小时 (0-23)
 * %l	小时 (1-12)
 * %M	月名
 * %m	月，数值(00-12)
 * %p	AM 或 PM
 * %r	时间，12-小时（hh:mm:ss AM 或 PM）
 * %S	秒(00-59)
 * %s	秒(00-59)
 * %T	时间, 24-小时 (hh:mm:ss)
 * %U	周 (00-53) 星期日是一周的第一天
 * %u	周 (00-53) 星期一是一周的第一天
 * %V	周 (01-53) 星期日是一周的第一天，与 %X 使用
 * %v	周 (01-53) 星期一是一周的第一天，与 %x 使用
 * %W	星期名
 * %w	周的天 （0=星期日, 6=星期六）
 * %X	年，其中的星期日是周的第一天，4 位，与 %V 使用
 * %x	年，其中的星期一是周的第一天，4 位，与 %v 使用
 * %Y	年，4 位
 * %y	年，2 位
 * Created by shihailong on 2017/7/18.
 */
public class DateFormat {
    public static final String NAME = "DATE_FORMAT";
    private static final Locale LOCALE = Locale.ENGLISH;



    public Value getValueWithArgs(Session session, Expression[] args) {
        Value dateValue= ValueNull.INSTANCE;
        if(Functions.isNullValueAll(args,session))
            return dateValue;
        dateValue=args[0].getValue(session);
        final Timestamp timestamp = dateValue.getTimestamp();
        Value formatValue = args[1].getValue(session);
        final String format = formatValue.getString();
        return ValueString.get(format(timestamp, format));
    }

    private String format(Timestamp timestamp, String pattern) {
        StringBuilder format = new StringBuilder(pattern.length());
        char c;
        boolean inPattern = false;
        Calendar calendar = Calendar.getInstance(LOCALE);
        calendar.setTime(timestamp);
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < pattern.length(); ++i) {
            c = pattern.charAt(i);
            if (c == '%') {
                inPattern = true;
                continue;
            }
            if (!inPattern) {
                format.append(c);
                continue;
            }
            inPattern = false;
            Object value;
            switch (c) {
                case 'a':
                    value = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, LOCALE);
                    break;
                case 'b':
                    value = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, LOCALE);
                    break;
                case 'c':
                    value = calendar.get(Calendar.MONTH) + 1;
                    break;
                case 'D':
                    value = String.format("%dth", calendar.get(Calendar.DAY_OF_MONTH));
                    break;
                case 'd':
                    value = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
                    break;
                case 'e':
                    value = calendar.get(Calendar.DAY_OF_MONTH);
                    break;
                case 'f':
                    value = String.format("%06d", timestamp.getNanos() / 1000);
                    break;
                case 'H':
                    value = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
                    break;
                case 'h':
                case 'I':
                    value = String.format("%02d", calendar.get(Calendar.HOUR));
                    break;
                case 'i':
                    value = String.format("%02d", calendar.get(Calendar.MINUTE));
                    break;
                case 'j':
                    value = String.format("%03d", calendar.get(Calendar.DAY_OF_YEAR));
                    break;
                case 'k':
                    value = calendar.get(Calendar.HOUR_OF_DAY);
                    break;
                case 'l':
                    value = calendar.get(Calendar.HOUR);
                    break;
                case 'M':
                    value = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, LOCALE);
                    break;
                case 'm':
                    value = String.format("%02d", calendar.get(Calendar.MONTH)+1);
                    break;
                case 'p':
                    value = calendar.getDisplayName(Calendar.AM_PM, Calendar.LONG, LOCALE);
                    break;
                case 'r':
                    value = new SimpleDateFormat("hh:mm:ss a", LOCALE).format(timestamp);
                    break;
                case 'S':
                case 's':
                    value = String.format("%02d",calendar.get(Calendar.SECOND));
                    break;
                case 'T':
                    value = new SimpleDateFormat("HH:mm:ss", LOCALE).format(timestamp);
                    break;
                case 'W':
                    value = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, LOCALE);
                    break;
                case 'w':
                    value = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                    break;
                case 'Y':
                    value = calendar.get(Calendar.YEAR);
                    break;
                case 'y':
                    value = String.format("%02d", calendar.get(Calendar.YEAR) % 100);
                    break;
                case 'U':
                case 'u':
                case 'V':
                case 'v':
                case 'X':
                case 'x':
                    throw DbException.throwInternalError("date_format暂不支持UuVvXx格式化");
                default:
                    throw DbException.throwInternalError();
            }
            values.add(value);
            format.append('%').append(String.class.isInstance(value) ? 's' : 'd');
        }
        return String.format(format.toString(), values.toArray(new Object[values.size()]));
    }

    public void checkParameterCount(int len) {
        if (len > 2)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
