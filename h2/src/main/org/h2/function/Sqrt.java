package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueDouble;
import org.h2.value.ValueNull;


public class Sqrt {
    public static final String NAME = "SQRT";

    public Value getValueWithArgs(Session session, Expression[] args) {
        try {
            Value v = ValueNull.INSTANCE;
            if(Functions.isNullValue(args[0],session))
                return v;
            v=args[0].getValue(session);
            double db=v.getDouble();
            if (v.getDouble()<0) db=0;
            return ValueDouble.get(Math.sqrt(db));
        }catch (Exception e){
            return ValueDouble.get(0);
        }
    }

    public int getScale() {
        return ValueDouble.PRECISION;
    }


    public void checkParameterCount(int len) {
        if (len > 1)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
