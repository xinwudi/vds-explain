package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.value.Value;
import org.h2.value.ValueInt;

/**
 * Created by shihailong on 2017/10/11.
 */
public class ConnectionId {
    public static final String NAME = "CONNECTION_ID";

    public Value getValueWithArgs(Session session, Expression[] args) {
        return ValueInt.get(0);
    }
}
