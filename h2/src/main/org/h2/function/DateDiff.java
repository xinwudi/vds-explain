package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueLong;
import org.h2.value.ValueNull;

import java.util.Date;

public class DateDiff {
    public static final String NAME = "DATEDIFF";


    public Value getValueWithArgs(Session session, Expression[] args) {
        Value value1 = ValueNull.INSTANCE;
        if(Functions.isNullValueAll(args,session))
            return value1;
        value1 = args[0].getValue(session);
        Value value2 = args[1].getValue(session);
        try {
            Date a= value1.getDate();
            Date b= value2.getDate();
            return ValueLong.get(sub(a,b));
        }catch (Exception e){
            return ValueNull.INSTANCE;
        }
    }
    private long sub(Date date1, Date date2) {
        return (date1.getTime() - date2.getTime()) / (24 * 60 * 60 * 1000);
    }
    /**
     * 下列方法可以忽略‘天’后的时间，这种实现与mysql相似。例如：
     * SELECT DATEDIFF('2017-08-08 03','2016-08-17 05');
     */
//    private static Date toDate(String dateString) {
//        try {
//            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA);
//            return DATE_FORMAT.parse(dateString);
//        } catch (Exception e) {
//            return null;
//        }
//    }

    public void checkParameterCount(int len) {
        if (len > 2)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
