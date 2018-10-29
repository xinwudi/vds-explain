package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueDate;
import org.h2.value.ValueTimestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Curtime {
    public static final String NAME="CURTIME";

    public Value getValueWithArgs(Session session, Expression[] args) {
        SimpleDateFormat DATE_FORMAT=new SimpleDateFormat("YYYY-MM-dd HH:MM:ss", Locale.CHINA);
        String s=DATE_FORMAT.format(new Date());
        try {
            final ValueTimestamp timestamp = ValueTimestamp.parse(s);
            return ValueDate.fromDateValue(timestamp.getDateValue());
        } catch (Exception e) {
            throw DbException.convert(e);
        }
    }

    public void checkParameterCount(int len) {
        if (len > 0)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
