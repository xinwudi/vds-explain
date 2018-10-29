package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.util.StringUtils;
import org.h2.value.Value;
import org.h2.value.ValueLong;
import org.h2.value.ValueNull;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;

/**
 * java Calendar类的实现与mysql有不同，
 * 目前发现的有月份不一致（Calendar月计数从零开始），周不一致（例如2018.1.20，mysql返回2，此处返回3）
 */
public class Extract {
    public static final String NAME = "EXTRACT";


    public Value getValueWithArgs(Session session, Expression[] args) {
        Value v1 = ValueNull.INSTANCE;
        if(Functions.isNullValueAll(args,session))
            return v1;
        v1=args[0].getValue(session);
        Value v2=args[1].getValue(session);
        String part= StringUtils.toUpperEnglish(v1.getString());
        Calendar c=Calendar.getInstance(Locale.US);
        try {
            Timestamp timestamp=v2.getTimestamp();
            c.setTime(timestamp);//v2.getDate()方法精确度不足
            return result(c,v2,part);
        }catch (Exception e){
            return ValueLong.get(0);
        }
    }
    private String microSecond(Calendar c,Value value){
        int n=c.get(Calendar.MILLISECOND);
        if (n==0){
            return "000000";
        }
        String s=value.getObject().toString();
        String num=s.substring(s.lastIndexOf(".")+1);
        StringBuilder sb;
        if(num.length()<6){
            sb=new StringBuilder();
            sb.append(num);
            for (int i = 0; i <6-num.length() ; i++) {
                sb.append("0");
            }
            return sb.toString();
        }else if (num.length()>6){
            return num.substring(0,6);
        }
        return num;
    }
    private Value result(Calendar c, Value v2, String part){
        int n=0;
        String s=null;
        switch (part){
            case "MICROSECOND":
                s=microSecond(c,v2);
                break;
            case "MINUTE":
                n=c.get(Calendar.MINUTE);
                break;
            case "SECOND":
                n=c.get(Calendar.SECOND);
                break;
            case "HOUR":
                n=c.get(Calendar.HOUR_OF_DAY);
                break;
            case "DAY":
                n=c.get(Calendar.DAY_OF_MONTH);
                break;
            case "WEEK"://2017
                throw DbException.throwInternalError("WEEK函数未实现");
//                n=c.get(Calendar.WEEK_OF_YEAR)-1;
            case "MONTH":
                n=c.get(Calendar.MONTH)+1;
                break;
            case "QUARTER":
                int a=c.get(Calendar.MONTH);
                n=a%3==0?a/3:a/3+1;
                break;
            case "YEAR":
                n=c.get(Calendar.YEAR);
                break;
            case "SECOND_MICROSECOND":
                s=c.get(Calendar.SECOND)+microSecond(c,v2);
                break;
            case "MINUTE_MICROSECOND":
                s=c.get(Calendar.MINUTE)*100
                        +c.get(Calendar.SECOND)
                        +microSecond(c,v2);
                break;
            case "MINUTE_SECOND":
                n=c.get(Calendar.MINUTE)*100
                        +c.get(Calendar.SECOND);
                break;
            case "HOUR_MICROSECOND":
                s=c.get(Calendar.HOUR_OF_DAY)*10000
                        +c.get(Calendar.MINUTE)*100
                        +c.get(Calendar.SECOND)
                        +microSecond(c,v2);
                break;
            case "HOUR_SECOND":
                n=c.get(Calendar.HOUR_OF_DAY)*10000
                        +c.get(Calendar.MINUTE)*100
                        +c.get(Calendar.SECOND);
                break;
            case "HOUR_MINUTE":
                n=c.get(Calendar.HOUR_OF_DAY)*100
                        +c.get(Calendar.MINUTE);
                break;
            case "DAY_MICROSECOND":
                s=c.get(Calendar.DAY_OF_MONTH)*1000000
                        +c.get(Calendar.HOUR_OF_DAY)*10000
                        +c.get(Calendar.MINUTE)*100
                        +c.get(Calendar.SECOND)
                        +microSecond(c,v2);
                break;
            case "DAY_SECOND":
                n=c.get(Calendar.DAY_OF_MONTH)*1000000
                        +c.get(Calendar.HOUR_OF_DAY)*10000
                        +c.get(Calendar.MINUTE)*100
                        +c.get(Calendar.SECOND);
                break;
            case "DAY_MINUTE":
                n=c.get(Calendar.DAY_OF_MONTH)*10000
                        +c.get(Calendar.HOUR_OF_DAY)*100
                        +c.get(Calendar.MINUTE);
                break;
            case "DAY_HOUR":
                n=c.get(Calendar.DAY_OF_MONTH)*100
                        +c.get(Calendar.HOUR_OF_DAY);
                break;
            case "YEAR_MONTH":
                n=c.get(Calendar.YEAR)*100
                        +c.get(Calendar.MONTH)+1;
                break;
            default:
                break;
        }
        if (n==0&&!StringUtils.isNullOrEmpty(s)){
            return ValueLong.get(Long.parseLong(s));
        }
        return ValueLong.get(n);
    }

    public void checkParameterCount(int len) {
    }
}
