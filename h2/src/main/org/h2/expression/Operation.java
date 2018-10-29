/*
 * Copyright 2004-2018 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.expression;

import org.h2.engine.Mode;
import org.h2.engine.Session;
import org.h2.message.DbException;
import org.h2.table.ColumnResolver;
import org.h2.table.TableFilter;
import org.h2.util.MathUtils;
import org.h2.value.*;

import static org.h2.expression.Operation.OpType.CONCAT;
import static org.h2.expression.Operation.OpType.PLUS;

/**
 * A mathematical expression, or string concatenation.
 */
public class Operation extends Expression {

    public enum OpType {
        /**
         * This operation represents a string concatenation as in
         * 'Hello' || 'World'.
         */
        CONCAT,

        /**
         * This operation represents an addition as in 1 + 2.
         */
        PLUS,

        /**
         * This operation represents a subtraction as in 2 - 1.
         */
        MINUS,

        /**
         * This operation represents a multiplication as in 2 * 3.
         */
        MULTIPLY,

        /**
         * This operation represents a division as in 4 * 2.
         */
        DIVIDE,

        /**
         * This operation represents a negation as in - ID.
         */
        NEGATE,

        /**
         * This operation represents a modulus as in 5 % 2.
         */
        MODULUS
    }

    private OpType opType;
    private Expression left, right;
    private int dataType;
    private boolean convertRight = true;

    public Operation(OpType opType, Expression left, Expression right) {
        this.opType = opType;
        this.left = left;
        this.right = right;
    }

    @Override
    public String getSQL() {
        String sql;
        if (opType == OpType.NEGATE) {
            // don't remove the space, otherwise it might end up some thing like
            // --1 which is a line remark
            sql = "- " + left.getSQL();
        } else {
            // don't remove the space, otherwise it might end up some thing like
            // --1 which is a line remark
            sql = left.getSQL() + " " + getOperationToken() + " " + right.getSQL();
        }
        return "(" + sql + ")";
    }

    private String getOperationToken() {
        switch (opType) {
        case NEGATE:
            return "-";
        case CONCAT:
            return "||";
        case PLUS:
            return "+";
        case MINUS:
            return "-";
        case MULTIPLY:
            return "*";
        case DIVIDE:
            return "/";
        case MODULUS:
            return "%";
        default:
            throw DbException.throwInternalError("opType=" + opType);
        }
    }

    @Override
    public Value getValue(Session session) {
        Value l = left.getValue(session).convertTo(dataType);
        Value r;
        if (right == null) {
            r = null;
        } else {
            r = right.getValue(session);
            if (convertRight) {
                r = r.convertTo(dataType);
            }
        }
        switch (opType) {
        case NEGATE:
            return l == ValueNull.INSTANCE ? l : l.negate();
        case CONCAT: {
            Mode mode = session.getDatabase().getMode();
            if (l == ValueNull.INSTANCE) {
                if (mode.nullConcatIsNull) {
                    return ValueNull.INSTANCE;
                }
                return r;
            } else if (r == ValueNull.INSTANCE) {
                if (mode.nullConcatIsNull) {
                    return ValueNull.INSTANCE;
                }
                return l;
            }
            String s1 = l.getString(), s2 = r.getString();
            StringBuilder buff = new StringBuilder(s1.length() + s2.length());
            buff.append(s1).append(s2);
            return ValueString.get(buff.toString());
        }
        case PLUS:
            if (l == ValueNull.INSTANCE || r == ValueNull.INSTANCE) {
                return ValueNull.INSTANCE;
            }
            return l.add(r);
        case MINUS:
            if (l == ValueNull.INSTANCE || r == ValueNull.INSTANCE) {
                return ValueNull.INSTANCE;
            }
            return l.subtract(r);
        case MULTIPLY:
            if (l == ValueNull.INSTANCE || r == ValueNull.INSTANCE) {
                return ValueNull.INSTANCE;
            }
            return l.multiply(r);
        case DIVIDE:
            if (l == ValueNull.INSTANCE || r == ValueNull.INSTANCE) {
                return ValueNull.INSTANCE;
            }
            return l.divide(r);
        case MODULUS:
            if (l == ValueNull.INSTANCE || r == ValueNull.INSTANCE) {
                return ValueNull.INSTANCE;
            }
            return l.modulus(r);
        default:
            throw DbException.throwInternalError("type=" + opType);
        }
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        left.mapColumns(resolver, level);
        if (right != null) {
            right.mapColumns(resolver, level);
        }
    }

    @Override
    public Expression optimize(Session session) {
        left = left.optimize(session);
        switch (opType) {
            case NEGATE:
                dataType = left.getType();
                if (dataType == Value.UNKNOWN) {
                    dataType = Value.DECIMAL;
                }
                break;
            case CONCAT:
                right = right.optimize(session);
                dataType = Value.STRING;
                if (left.isConstant() && right.isConstant()) {
                    return ValueExpression.get(getValue(session));
                }
                break;
            case PLUS:
            case MINUS:
            case MULTIPLY:
            case DIVIDE:
            case MODULUS:
                right = right.optimize(session);
                int l = left.getType();
                int r = right.getType();
                if ((l == Value.NULL && r == Value.NULL) ||
                        (l == Value.UNKNOWN && r == Value.UNKNOWN)) {
                    // (? + ?) - use decimal by default (the most safe data type) or
                    // string when text concatenation with + is enabled
                    if (opType == PLUS && session.getDatabase().
                            getMode().allowPlusForStringConcat) {
                        dataType = Value.STRING;
                        opType = CONCAT;
                    } else {
                        dataType = Value.DECIMAL;
                    }
                } else if (l == Value.DATE || l == Value.TIMESTAMP ||
                        l == Value.TIME || r == Value.DATE ||
                        r == Value.TIMESTAMP || r == Value.TIME) {
                    /**
                     * 针对mysql 修改, 日期类型的加减运算不转换为函数
                     */
                    if (l != Value.getHigherOrder(l, r)) {
                        swap();
                    }
                    convertRight = false;
                    switch (r) {
                        case ValueInterval.TYPE:
                            dataType = left.getType();
                            break;
                        case Value.INT:
                        case Value.LONG:
                        case Value.SHORT:
                            convertRight = true;
                            dataType = Value.LONG;
                            break;
                        case Value.DECIMAL:
                        case Value.DOUBLE:
                        case Value.FLOAT:
                            convertRight = true;
                            dataType = Value.DOUBLE;
                            break;
                        default:
                            dataType = left.getType();
                            break;
                    }
                    break;
                } else {
                    dataType = Value.getHigherOrder(l, r);
                    if (DataType.isStringType(dataType) &&
                            session.getDatabase().getMode().allowPlusForStringConcat) {
                        opType = CONCAT;
                    }
                }
                break;
            default:
                throw DbException.throwInternalError("type=" + opType);
        }
        if (left.isConstant() && (right == null || right.isConstant())) {
            return ValueExpression.get(getValue(session));
        }
        return this;
    }

    private void swap() {
        Expression temp = left;
        left = right;
        right = temp;
    }

    @Override
    public void setEvaluatable(TableFilter tableFilter, boolean b) {
        left.setEvaluatable(tableFilter, b);
        if (right != null) {
            right.setEvaluatable(tableFilter, b);
        }
    }

    @Override
    public int getType() {
        return dataType;
    }

    @Override
    public long getPrecision() {
        if (right != null) {
            switch (opType) {
            case CONCAT:
                return left.getPrecision() + right.getPrecision();
            default:
                return Math.max(left.getPrecision(), right.getPrecision());
            }
        }
        return left.getPrecision();
    }

    @Override
    public int getDisplaySize() {
        if (right != null) {
            switch (opType) {
            case CONCAT:
                return MathUtils.convertLongToInt((long) left.getDisplaySize() +
                        (long) right.getDisplaySize());
            default:
                return Math.max(left.getDisplaySize(), right.getDisplaySize());
            }
        }
        return left.getDisplaySize();
    }

    @Override
    public int getScale() {
        if (right != null) {
            return Math.max(left.getScale(), right.getScale());
        }
        return left.getScale();
    }

    @Override
    public void updateAggregate(Session session) {
        left.updateAggregate(session);
        if (right != null) {
            right.updateAggregate(session);
        }
    }

    @Override
    public boolean isEverything(ExpressionVisitor visitor) {
        return left.isEverything(visitor) &&
                (right == null || right.isEverything(visitor));
    }

    @Override
    public int getCost() {
        return left.getCost() + 1 + (right == null ? 0 : right.getCost());
    }

}
