package org.h2.function;


import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueDouble;
import org.h2.value.ValueNull;

public class Atan {

public static final String NAME = "ATAN";


    public Value getValueWithArgs(Session session, Expression[] args) {
        if (Functions.isNullValueAll(args,session))
            return ValueNull.INSTANCE;
        Value value1= args[0].getValue(session);
        double a= Double.parseDouble(value1.getObject().toString());
        if (args.length>1){
            Value value2= args[1].getValue(session);
            double b= Double.parseDouble(value2.getObject().toString());
            return ValueDouble.get(Math.atan2(a,b));
        }
        return ValueDouble.get(Math.atan(a));
    }


    public void checkParameterCount(int len) {
       if (len > 2)
           throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
