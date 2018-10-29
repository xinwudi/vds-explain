package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.value.Value;
import org.h2.value.ValueLong;

import java.util.Calendar;
import java.util.Date;

public class Week {
    public static final String NAME = "WEEK";

    public Value getValueWithArgs(Session session, Expression[] args) {
        try {
            Date d=args[0].getValue(session).getDate();
            Calendar calendar=Calendar.getInstance();
            calendar.setTime(d);
            return ValueLong.get(calendar.get(Calendar.WEEK_OF_YEAR)-1);
        }catch (Exception e){
            return ValueLong.get(0);
        }
    }
}
