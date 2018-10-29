package org.h2.function;

import org.h2.engine.Database;
import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.util.StringUtils;
import org.h2.value.Value;
import org.h2.value.ValueNull;
import org.h2.value.ValueString;

public class Concat_ws {
    public static final String NAME = "CONCAT_WS";

    public Value getValueWithArgs(Session session, Expression[] args, Database database) {
        Value result = ValueNull.INSTANCE;
        if(Functions.isNullValue(args[0],session))
            return result;
        String separator =args[0].getValue(session).getString();
        for (int i = 1; i < args.length; i++) {
            Value v =args[i].getValue(session);
            if (v == ValueNull.INSTANCE) {
                continue;
            }
            if (result == ValueNull.INSTANCE) {
                result = v;
            } else {
                String tmp = v.getString();
                if (!StringUtils.isNullOrEmpty(separator)
                        && !StringUtils.isNullOrEmpty(tmp)) {
                    tmp = separator.concat(tmp);
                }
                result = ValueString.get(result.getString().concat(tmp),
                        database.getMode().treatEmptyStringsAsNull);
            }
        }
        if (result == ValueNull.INSTANCE) {
            result = ValueString.get("", database.getMode().treatEmptyStringsAsNull);
        }
        return result;
    }

    public void checkParameterCount(int len) {
    }
}
