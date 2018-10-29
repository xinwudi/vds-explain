package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueInt;
import org.h2.value.ValueLong;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class Second {
    public static final String NAME = "SECOND";


    public Value getValueWithArgs(Session session, Expression[] args) {
        Value v = ValueInt.get(0);//参数为null时返回0
        if(Functions.isNullValue(args[0],session))
            return v;
        v=args[0].getValue(session);
        try {
            Time time=getTime(v);
            Calendar calendar=Calendar.getInstance();
            if (time==null)
                calendar.setTime(Timestamp.valueOf(v.getObject().toString()));
            else
                calendar.setTime(v.getTime());
            return ValueLong.get(calendar.get(Calendar.SECOND));
        }catch (Exception e){
            return ValueLong.get(0);
        }
    }
    private Time getTime(Value v){
        Time t;
        try {
           t=v.getTime();
        }catch (Exception e){
            t=null;
        }
            return t;
    }


    public void checkParameterCount(int len) {
        if (len > 1)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
