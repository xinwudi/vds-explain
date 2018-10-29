package org.h2.expression;

import org.h2.engine.Session;
import org.h2.table.ColumnResolver;
import org.h2.table.TableFilter;
import org.h2.value.Value;
import org.h2.value.ValueInterval;

public class IntervalExpression extends Expression {
    private Expression expression;
    private String unit;

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public Value getValue(Session session) {
        Value v = expression.getValue(session);
        return ValueInterval.get(v, unit);
    }

    @Override
    public int getType() {
        return ValueInterval.TYPE;
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        expression.mapColumns(resolver, level);
    }

    @Override
    public Expression optimize(Session session) {
        this.expression = expression.optimize(session);
        /**
         * 此时应该确定type
         */
        return this;
    }

    @Override
    public void setEvaluatable(TableFilter tableFilter, boolean value) {
        expression.setEvaluatable(tableFilter, value);
    }

    @Override
    public int getScale() {
        return expression.getScale();
    }

    @Override
    public long getPrecision() {
        return expression.getPrecision();
    }

    @Override
    public int getDisplaySize() {
        return expression.getDisplaySize();
    }

    @Override
    public String getSQL() {
        return "INTERVAL " + expression.getSQL() + " " + unit;
    }

    @Override
    public void updateAggregate(Session session) {
        expression.updateAggregate(session);
    }

    @Override
    public boolean isEverything(ExpressionVisitor visitor) {
        return expression.isEverything(visitor);
    }

    @Override
    public int getCost() {
        return expression.getCost();
    }

    @Override
    public String toString() {
        return getSQL();
    }
}