package org.h2.value;

import org.h2.message.DbException;
import org.h2.util.New;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;

/**
 * 仅用于date and time function 传递参数
 * <p/>
 * date/ time / timestamp +- ValueInterval 需要拦截Operation, 进行检查
 * Created by shihailong on 2017/7/21.
 */
public class ValueInterval extends Value {
    /**
     * type order 应该高于int
     */
    public static final int TYPE = Value.TYPE_COUNT + 2;

    private static final HashMap<String, Integer> DATE_PART = New.hashMap();

    static {
        // DATE_PART
        DATE_PART.put("SQL_TSI_YEAR", Calendar.YEAR);
        DATE_PART.put("YEAR", Calendar.YEAR);
        DATE_PART.put("YYYY", Calendar.YEAR);
        DATE_PART.put("YY", Calendar.YEAR);
        DATE_PART.put("SQL_TSI_MONTH", Calendar.MONTH);
        DATE_PART.put("MONTH", Calendar.MONTH);
        DATE_PART.put("MM", Calendar.MONTH);
        DATE_PART.put("M", Calendar.MONTH);
        DATE_PART.put("SQL_TSI_WEEK", Calendar.WEEK_OF_YEAR);
        DATE_PART.put("WW", Calendar.WEEK_OF_YEAR);
        DATE_PART.put("WK", Calendar.WEEK_OF_YEAR);
        DATE_PART.put("WEEK", Calendar.WEEK_OF_YEAR);
        DATE_PART.put("DAY", Calendar.DAY_OF_MONTH);
        DATE_PART.put("DD", Calendar.DAY_OF_MONTH);
        DATE_PART.put("D", Calendar.DAY_OF_MONTH);
        DATE_PART.put("SQL_TSI_DAY", Calendar.DAY_OF_MONTH);
        DATE_PART.put("DAYOFYEAR", Calendar.DAY_OF_YEAR);
        DATE_PART.put("DAY_OF_YEAR", Calendar.DAY_OF_YEAR);
        DATE_PART.put("DY", Calendar.DAY_OF_YEAR);
        DATE_PART.put("DOY", Calendar.DAY_OF_YEAR);
        DATE_PART.put("SQL_TSI_HOUR", Calendar.HOUR_OF_DAY);
        DATE_PART.put("HOUR", Calendar.HOUR_OF_DAY);
        DATE_PART.put("HH", Calendar.HOUR_OF_DAY);
        DATE_PART.put("SQL_TSI_MINUTE", Calendar.MINUTE);
        DATE_PART.put("MINUTE", Calendar.MINUTE);
        DATE_PART.put("MI", Calendar.MINUTE);
        DATE_PART.put("N", Calendar.MINUTE);
        DATE_PART.put("SQL_TSI_SECOND", Calendar.SECOND);
        DATE_PART.put("SECOND", Calendar.SECOND);
        DATE_PART.put("SS", Calendar.SECOND);
        DATE_PART.put("S", Calendar.SECOND);
        DATE_PART.put("MILLISECOND", Calendar.MILLISECOND);
        DATE_PART.put("MS", Calendar.MILLISECOND);
    }

    private ValueInterval(Value value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    private Value value;

    public String getUnit() {
        return unit;
    }

    private String unit;

    @Override
    public String getSQL() {
        return "INTERVAL " + value.getSQL() + " " + unit;
    }

    @Override
    public int getType() {
        return TYPE;
    }

    @Override
    public long getPrecision() {
        return value.getPrecision();
    }

    @Override
    public int getDisplaySize() {
        return value.getDisplaySize() + "INTERVAL  ".length() + unit.length();
    }

    @Override
    public String getString() {
        return value.getString();
    }

    @Override
    public Object getObject() {
        return value.getObject();
    }

    @Override
    public void set(PreparedStatement prep, int parameterIndex) throws SQLException {
        value.set(prep, parameterIndex);
    }

    @Override
    protected int compareSecure(Value v, CompareMode mode) {
        if (v instanceof ValueInterval) {
            return value.compareTo(((ValueInterval) v).value, mode);
        }
        return value.compareTo(v, mode);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ValueInterval && value.equals(((ValueInterval) other).value);
    }

    @Override
    public Value convertTo(int targetType) {
        return value.convertTo(targetType);
    }

    public static Value get(Value v, String unit) {
        return new ValueInterval(v, unit);
    }

    public static Value subtract(ValueTimestamp valueTimestamp, ValueInterval interval) {
        final Timestamp timestamp = valueTimestamp.getTimestamp();
        return subtract(timestamp, interval);
    }

    public static Value subtract(Timestamp timestamp, ValueInterval interval) {
        final Integer field = DATE_PART.get(interval.unit);
        if (field == null) {
            throw DbException.throwInternalError("无效的unit " + interval.unit);
        }
        if (field == Calendar.MILLISECOND) {
            Timestamp ts = new Timestamp(timestamp.getTime());
            ts.setNanos(ts.getNanos() - (interval.value.getInt() % 1000000));
            return ValueTimestamp.get(ts);
        }
        Calendar calendar = Calendar.getInstance();
        int nanos = timestamp.getNanos() % 1000000;
        calendar.setTime(timestamp);
        //noinspection MagicConstant
        calendar.add(field, 0 - interval.value.getInt());
        long t = calendar.getTime().getTime();
        Timestamp ts = new Timestamp(t);
        ts.setNanos(ts.getNanos() + nanos);
        return ValueTimestamp.get(ts);
    }

    public static Value add(ValueTimestamp valueTimestamp, ValueInterval interval) {
        final Timestamp timestamp = valueTimestamp.getTimestamp();

        return add(timestamp, interval);
    }

    public static Value add(Timestamp timestamp, ValueInterval interval) {
        final Integer field = DATE_PART.get(interval.unit);
        if (field == null) {
            throw DbException.throwInternalError("无效的unit " + interval.unit);
        }
        if (field == Calendar.MILLISECOND) {
            Timestamp ts = new Timestamp(timestamp.getTime());
            ts.setNanos(ts.getNanos() + (interval.value.getInt() % 1000000));
            return ValueTimestamp.get(ts);
        }
        Calendar calendar = Calendar.getInstance();
        int nanos = timestamp.getNanos() % 1000000;
        calendar.setTime(timestamp);
        //noinspection MagicConstant
        calendar.add(field, interval.value.getInt());
        long t = calendar.getTime().getTime();
        Timestamp ts = new Timestamp(t);
        ts.setNanos(ts.getNanos() + nanos);
        return ValueTimestamp.get(ts);
    }
}
