package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueString;

public class Current_user {
    public static final String NAME = "CURRENT_USER";


    public Value getValueWithArgs(Session session, Expression[] args) {
        return ValueString.get(session.getUser().getName()+"@"+session.getUser().getId());
    }


    public void checkParameterCount(int len) {
        if (len > 0)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
