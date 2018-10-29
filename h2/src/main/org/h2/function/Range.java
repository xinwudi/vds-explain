package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.CompareMode;
import org.h2.value.Value;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Range {
    public static final String NAME = "RANGE";


    public Value getValueWithArgs(Session session, Expression[] args) {
        return new ValueRange(args[0].getValue(session),
                args[1].getValue(session),
                args[2].getBooleanValue(session),
                args[3].getBooleanValue(session));
    }

    public static class ValueRange extends Value {
        private Value left;
        private Value right;
        private boolean leftClosed;
        private boolean rightClosed;

        private ValueRange(Value left, Value right, boolean leftClosed, boolean rightClosed) {
            this.left = left;
            this.right = right;
            this.leftClosed = leftClosed;
            this.rightClosed = rightClosed;
        }

        @Override
        public String getSQL() {
            return "rang(" + left.getSQL() + "," + right.getSQL() + ","
                    + (leftClosed ? 1 : 0) + "," + (rightClosed ? 1 : 0);
        }

        @Override
        public int getType() {
            return left.getType();
        }

        @Override
        public int compareSecure(Value v, CompareMode mode) {
            int l = v.compareTo(left, mode);
            int r = v.compareTo(right, mode);

            if(l == -1 || (l == 0 && !leftClosed)){
                return 1;
            }
            if(r == 1 || (r == 0 && !rightClosed)){
                return -1;
            }
            return 0;
        }

        @Override
        public int hashCode() {
            return left.hashCode() * 31 + right.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof ValueRange) {
                return left.equals(((ValueRange) other).left) &&
                        right.equals(((ValueRange) other).right) &&
                        leftClosed == ((ValueRange) other).leftClosed &&
                        rightClosed == ((ValueRange) other).rightClosed;
            }
            return false;
        }

        @Override
        public long getPrecision() {
            return left.getPrecision();
        }

        @Override
        public int getDisplaySize() {
            return left.getDisplaySize();
        }

        @Override
        public String getString() {
            throw DbException.throwInternalError();
        }

        @Override
        public Object getObject() {
            throw DbException.throwInternalError();
        }

        @Override
        public void set(PreparedStatement prep, int parameterIndex) throws SQLException {
            throw DbException.throwInternalError();
        }
    }
}
