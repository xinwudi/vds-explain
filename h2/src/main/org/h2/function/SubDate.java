package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueInterval;
import org.h2.value.ValueNull;

import java.sql.Timestamp;


/**
 * Created by shihailong on 2017/12/14.
 */
public class SubDate {

    public static final String NAME = "SUBDATE";


    public Value getValueWithArgs(Session session, Expression[] args) {
        if (Functions.isNullValueAll(args, session)) {
            return ValueNull.INSTANCE;
        }
        Timestamp timestamp = args[0].getValue(session).getTimestamp();
        Value sub = args[1].getValue(session);
        if (!(sub instanceof ValueInterval)) {
            sub = ValueInterval.get(sub, "DAY");
        }
        return ValueInterval.subtract(timestamp, (ValueInterval) sub);
    }

    public void checkParameterCount(int len) {
        if (len != 2)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
