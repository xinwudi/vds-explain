package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.value.Value;
import org.h2.value.ValueString;

/**
 * Created by shihailong on 2017/8/31.
 */
public class Database {
    public static final String NAME = "DATABASE";


    public Value getValueWithArgs(Session session, Expression[] args) {
        return ValueString.get(session.getCurrentSchemaName().toLowerCase());
    }
}
