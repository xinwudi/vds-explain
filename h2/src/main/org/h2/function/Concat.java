package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.value.Value;
import org.h2.value.ValueNull;
import org.h2.value.ValueString;

public class Concat {
    public static final String NAME = "CONCAT";

    public Value getValueWithArgs(Session session, Expression[] args) {
        String result="";
        for (Expression arg : args) {
            Value v = arg.getValue(session);
            if (v==null||v == ValueNull.INSTANCE) {
                return ValueNull.INSTANCE;
            }
            String tmp = v.getString();
            result = result.concat(tmp);
        }
        return ValueString.get(result);
    }

    public void checkParameterCount(int len) {
    }
}
