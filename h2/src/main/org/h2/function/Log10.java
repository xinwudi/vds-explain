package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueNull;

public class Log10 {
    public static final String NAME = "LOG10";


    public Value getValueWithArgs(Session session, Expression[] args) {
        try {
            return Log.handle(session,args,true);
        }catch (Exception e){
            return ValueNull.INSTANCE;
        }
    }

    public void checkParameterCount(int len) {
        if (len > 1)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
