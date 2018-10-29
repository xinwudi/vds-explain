package org.h2.value;

import org.h2.util.MathUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;

/**
 * 增加year类型
 * Created by shihailong on 2017/7/19.
 */
public class ValueYear extends Value {
    private ValueShort value;
    public static final int TYPE = Value.TYPE_COUNT + 1;



    @Override
    public String getSQL() {
        return value.getSQL();
    }

    @Override
    public int getType() {
        return TYPE;
    }

    @Override
    public long getPrecision() {
        return 4;
    }

    @Override
    public int getDisplaySize() {
        return 4;
    }

    @Override
    public String getString() {
        return value.getString();
    }

    @Override
    public Object getObject() {
        return null;
    }

    @Override
    public void set(PreparedStatement prep, int parameterIndex) throws SQLException {
        value.set(prep, parameterIndex);
    }

    @Override
    protected int compareSecure(Value v, CompareMode mode) {
        ValueYear year = (ValueYear) v;
        return Integer.compare(value.getInt(), year.value.getInt());
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ValueYear && value.equals(((ValueYear) other).value);
    }

    public static Value get(Value value) {
        ValueYear v = new ValueYear();
        if (value.getType() == Value.TIMESTAMP || value.getType() == Value.DATE) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(value.getTimestamp());
            v.value = ValueShort.get((short) calendar.get(Calendar.YEAR));
            return v;
        }
        if(DataType.isStringType(value.getType()) && value.getString().isEmpty()){
            v.value = ValueShort.get((short) 0);
            return v;
        }
        v.value = (ValueShort) value.convertTo(Value.SHORT);
        return v;
    }
}
