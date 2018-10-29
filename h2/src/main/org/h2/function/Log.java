package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueDouble;
import org.h2.value.ValueNull;

public class Log {
    public static final String NAME = "LOG";

    public Value getValueWithArgs(Session session, Expression[] args) {
        try {
            return handle(session,args,false);
        }catch (Exception e){
            return ValueNull.INSTANCE;
        }
    }

    static Value handle(Session session, Expression[] args, boolean n){
        Value v1 = ValueNull.INSTANCE;
        if(Functions.isNullValueAll(args,session))
            return v1;
        v1=args[0].getValue(session);
        Double d1=v1.getDouble();
        boolean sign=d1>0&&d1!=1;
        if (args.length>1&&sign){
            Value v2=args[0].getValue(session);
            sign=v2.getDouble()>0;
            Double b=Math.log(v2.getDouble())/Math.log(d1);
            return sign? ValueDouble.get(b): ValueNull.INSTANCE;
        }
        if (n) return sign? ValueDouble.get(Math.log10(d1)): ValueNull.INSTANCE;
        return sign? ValueDouble.get(Math.log(d1)): ValueNull.INSTANCE;
    }


    public void checkParameterCount(int len) {
        if (len > 2)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
