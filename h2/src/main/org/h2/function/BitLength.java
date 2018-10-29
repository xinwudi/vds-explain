package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueLong;
import org.h2.value.ValueNull;

public class BitLength {

    public static final String NAME = "BIT_LENGTH";

    public Value getValueWithArgs(Session session, Expression[] args) {
        try {
            Value value1= ValueNull.INSTANCE;
            if(Functions.isNullValue(args[0],session))
                return value1;
            value1 = args[0].getValue(session);
            String a= value1.getObject().toString();
            return ValueLong.get(getLength(a)*8);
//                return ValueLong.get(a.getBytes("utf-8").length);
        }catch (Exception e){
            return ValueNull.INSTANCE;
        }
    }

    protected static long getLength(String s) {
        int length = 0;
        for(long i = 0; i < s.length(); i++)
        {
            long ascii = Character.codePointAt(s,(int)i);
            //utf8汉语占3个字节，vds的到的value为utf8
            length=ascii>=0&&ascii<=255?length+1:length+3;
        }
        return length;
    }


    public void checkParameterCount(int len) {
        if (len > 1)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
