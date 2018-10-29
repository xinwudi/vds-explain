package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueLong;
import org.h2.value.ValueNull;

import java.sql.Timestamp;

/**
 * Created by shihailong on 2017/12/8.
 */
public class UnixTimestamp {
    public static final String NAME = "UNIX_TIMESTAMP";

    public void checkParameterCount(int len) {

    }

    public Value getValueWithArgs(Session session, Expression[] args) {
        if (args.length == 0) {
            return ValueLong.get(System.currentTimeMillis() / 1000L);
        }
        if (args.length == 1) {
            Value value = args[0].getValue(session);
            if (value == null || value.equals(ValueNull.INSTANCE)) {
                return ValueNull.INSTANCE;
            }
            Timestamp timestamp = value.getTimestamp();
            return ValueLong.get(timestamp.getTime());
        }
        throw DbException.throwInternalError("only unix_timestamp() or unix_timestamp(date)");
    }
}
