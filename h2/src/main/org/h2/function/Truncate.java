package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueDecimal;
import org.h2.value.ValueInt;
import org.h2.value.ValueNull;

import java.math.BigDecimal;

public class Truncate {
    public static final String NAME = "TRUNCATE";

    public Value getValueWithArgs(Session session, Expression[] args) {
        try {
            if(Functions.isNullValueAll(args,session))
                return ValueNull.INSTANCE;
            double x=args[0].getValue(session).getDouble();//null or value
            int y=args[1].getValue(session).getInt();
            String s=String.valueOf(x);
            int n=s.indexOf(".");//numberformat和String("%.6f",s)都会四舍五入
            if (y==0)
                return ValueInt.get(Integer.parseInt(s.substring(0, n)));
            if (y<0)
                return ValueInt.get(0);
            return ValueDecimal.get(new BigDecimal(s.substring(0,Math.min(n+y+1,s.length()))));
        }catch (Exception e){
            return ValueNull.INSTANCE;
        }
    }

    public void checkParameterCount(int len) {
        if (len != 2)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
