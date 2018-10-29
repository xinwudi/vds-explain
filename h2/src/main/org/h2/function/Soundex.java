package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.util.StringUtils;
import org.h2.value.Value;
import org.h2.value.ValueNull;
import org.h2.value.ValueString;

import java.util.regex.Pattern;


public class Soundex {
    public static final String NAME = "SOUNDEX";


    public Value getValueWithArgs(Session session, Expression[] args) {
        Value v = ValueNull.INSTANCE;
        if(Functions.isNullValue(args[0],session))
            return v;
        v=args[0].getValue(session);
        try {
            String s= StringUtils.toUpperEnglish(v.getString());
            Pattern pattern = Pattern.compile("[0-9!@#$%^&*():<>?`~_+-=;',./|{}"+'"'+"]");
            s=pattern.matcher(s).replaceAll("").trim();
            char[] chars=s.toCharArray();
            return ValueString.get(soundex(chars));
        }catch (Exception e){
            return ValueNull.INSTANCE;
        }
    }
    //结果返回除0外的所有结果数字，最少为三位数字，不足以0补（与mysql结果保持相同）。
    private String soundex(char[] chars){
        int[] n=new int[]{0,1,2,3,0,1,2,0,0,2,2,4,5,5,0,1,2,6,2,3,0,1,0,2,0,2};
        StringBuilder sb=new StringBuilder();
        int sign=0;//相邻去重
        for (char aChar : chars) {
            String chari = String.valueOf(aChar);
            int charInt = (int) aChar;
            if (charInt >= 91 && charInt <= 93) continue;//过滤'[]\'三个字符（正则表达式如何过滤这三个字符？？？）
            if (sb.length() < 1) {
                sb.append(chari);//结果的首个字符；
                continue;
            }
            int j = charInt - (int) 'A';
            if (j >= 0 && j <= 26 && j != sign) {
                sign = j;//相邻去重
                int rs = n[j];
                if (rs != 0) sb.append(rs);
            }
        }
        while (sb.length()>0&&sb.length()<4) sb.append(0);
        return sb.toString();
    }

    public void checkParameterCount(int len) {
        if (len > 1)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
