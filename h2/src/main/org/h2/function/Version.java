package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.value.Value;
import org.h2.value.ValueString;

/**
 * Created by shihailong on 2017/10/11.
 */
public class Version {
    public static final String NAME = "VERSION";

    public Value getValueWithArgs(Session session, Expression[] args) {
        return ValueString.get("VDS 3");
    }
}
