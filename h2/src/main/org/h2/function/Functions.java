package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.expression.Function;
import org.h2.util.StatementBuilder;
import org.h2.value.Value;
import org.h2.value.ValueNull;

/**
 * Created by shihailong on 2017/12/14.
 */
public class Functions {
    public static final int TYPE_TRIM = 78;
    public static final int TYPE_EXTRACT = 120;

    public static final int TYPE_DATE_FORMAT = 1024;
    public static final int TYPE_VERSION = 1025;
    public static final int TYPE_DATABASE = 1026;
    public static final int TYPE_CONNECTION_ID = 1027;
    public static final int TYPE_ATAN = 1028;
    public static final int TYPE_BIT_LENGTH = 1029;
    public static final int TYPE_COMPRESS = 1031;
    public static final int TYPE_UNCOMPRESS = 1032;
    public static final int TYPE_DATEDIFF = 1033;
    public static final int TYPE_CURTIME = 1034;
    public static final int TYPE_CURRENT_USER = 1036;
    public static final int TYPE_CONCAT = 1037;
    public static final int TYPE_CONCAT_WS = 1038;
    public static final int TYPE_ENCODE = 1041;
    public static final int TYPE_DECODE = 1042;
    public static final int TYPE_HOUR = 1044;
    public static final int TYPE_LOG = 1045;
    public static final int TYPE_LN = 1046;
    public static final int TYPE_LOG10 = 1049;
    public static final int TYPE_OCTET_LENGTH = 1047;
    public static final int TYPE_SECOND = 1048;
    public static final int TYPE_SOUNDEX = 1050;
    public static final int TYPE_SQRT = 1051;
    public static final int TYPE_TRUNCATE = 1054;
    public static final int TYPE_WEEK = 1055;
    public static final int TYPE_UNIX_TIMESTAMP = 1056;
    public static final int TYPE_SUB_DATE = 1057;
    public static final int TYPE_LTRIM = 1058;
    public static final int TYPE_RTRIM = 1059;
    public static final int TYPE_HISTORY = 1060;
    public static final int TYPE_RANGE = 1061;


    static boolean isNullValue(Expression expression, Session session) {
        if (expression == null) {
            return true;
        }
        Value value = expression.getValue(session);
        return value == null || value.equals(ValueNull.INSTANCE);
    }
    public static boolean isNullValueAll(Expression[] args, Session session) {
        for (Expression e : args) {
            if (isNullValue(e,session))
                return true;
        }
        return false;
    }

    public static String getSQL(org.h2.expression.Function function){
        if(function.getFunctionType() == Function.DATE_DIFF){
            StatementBuilder buff = new StatementBuilder(function.getName());
            buff.append('(');
            Expression[] args = function.getArgs();
            for (int i=0; i< args.length ; ++i) {
                buff.appendExceptFirst(", ");
                String sql = args[i].getSQL();
                if(i == 0 && sql.startsWith("'") && sql.endsWith("'")){
                    sql = sql.substring(1, sql.length()-1);
                }
                buff.append(sql);
            }
            return buff.append(')').toString();
        }
        return null;
    }
}
