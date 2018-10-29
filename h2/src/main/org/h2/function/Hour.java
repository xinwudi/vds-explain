package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueLong;
import org.h2.value.ValueNull;

import java.util.Calendar;

public class Hour {
    public static final String NAME = "HOUR";


    public Value getValueWithArgs(Session session, Expression[] args) {
        Value v = ValueNull.INSTANCE;
        if(Functions.isNullValue(args[0],session))
            return v;
        v=args[0].getValue(session);
        Calendar c=Calendar.getInstance();
        try {
            c.setTime(v.getTime());
            return ValueLong.get(c.get(Calendar.HOUR_OF_DAY));
        }catch (Exception e){
            return ValueLong.get(0);
        }
    }

    public void checkParameterCount(int len) {
        if (len > 1)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
