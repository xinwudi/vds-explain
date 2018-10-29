package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueNull;
import org.h2.value.ValueString;

public class Trim {
    public static final String NAME = "TRIM";


    public Value getValueWithArgs(Session session, Expression[] args) {
        try {
            if(Functions.isNullValueAll(args,session))
                return ValueNull.INSTANCE;
            String st=args[0].getValue(session).getString();
            if(args.length>1){
                String sr=args[1].getValue(session).getString();
                return ValueString.get(ltimStr(sr,rtimStr(sr,st)));
            }
            return ValueString.get(st.trim());
        }catch (Exception e){
            return ValueNull.INSTANCE;
        }
    }
    private static String rtimStr(String sr,String st){
        int n=st.lastIndexOf(sr);
        if (n+sr.length()==st.length()){
            return rtimStr(sr,st.substring(0,n));
        }
        return st;
    }
    private static String ltimStr(String sr,String st){
        int n=st.indexOf(sr);
        if (n==0){
            return ltimStr(sr,st.substring(sr.length()));
        }
        return st;
    }

    public static class Rtrim {
        public static final String NAME = "RTRIM";


        public Value getValueWithArgs(Session session, Expression[] args) {
            try {
                if(Functions.isNullValueAll(args,session))
                    return ValueNull.INSTANCE;
                String st=args[0].getValue(session).getString();
                if(args.length>1){
                    String sr=args[1].getValue(session).getString();
                    return ValueString.get(rtimStr(sr,st));
                }
                return ValueString.get(rtimStr(" ",st));
            }catch (Exception e){
                return ValueNull.INSTANCE;
            }
        }

        public void checkParameterCount(int len) {
            if (len > 2)
                throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
        }
    }
    public static class Ltrim {
        public static final String NAME = "LTRIM";


        public Value getValueWithArgs(Session session, Expression[] args) {
            try {
                if(Functions.isNullValueAll(args,session))
                    return ValueNull.INSTANCE;
                String st=args[0].getValue(session).getString();
                if(args.length>1){
                    String sr=args[1].getValue(session).getString();
                    return ValueString.get(ltimStr(sr,st));
                }
                return ValueString.get(ltimStr(" ",st));
            }catch (Exception e){
                return ValueNull.INSTANCE;
            }
        }

        public void checkParameterCount(int len) {
            if (len > 2)
                throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
        }
    }
}
