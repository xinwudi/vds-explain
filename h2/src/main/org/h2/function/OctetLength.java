package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueLong;
import org.h2.value.ValueNull;

public class OctetLength {
    public static final String NAME = "OCTET_LENGTH";

    public Value getValueWithArgs(Session session, Expression[] args) {
        try {
            Value v1 = ValueNull.INSTANCE;
            if(Functions.isNullValue(args[0],session))
                return v1;
            v1=args[0].getValue(session);
            String s=v1.getObject().toString();
            return ValueLong.get(BitLength.getLength(s));
        }catch (Exception e){
            return ValueNull.INSTANCE;
        }
    }

    public void checkParameterCount(int len) {
        if (len > 1)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
