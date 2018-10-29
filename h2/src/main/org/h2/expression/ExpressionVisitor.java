/*
 * Copyright 2004-2018 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.expression;

import org.h2.engine.DbObject;
import org.h2.table.*;

import java.util.*;

/**
 * The visitor pattern is used to iterate through all expressions of a query
 * to optimize a statement.
 */
public class ExpressionVisitor {

    /**
     * Is the value independent on unset parameters or on columns of a higher
     * level query, or sequence values (that means can it be evaluated right
     * now)?
     */
    public static final int INDEPENDENT = 0;

    /**
     * The visitor singleton for the type INDEPENDENT.
     */
    public static final ExpressionVisitor INDEPENDENT_VISITOR =
            new ExpressionVisitor(INDEPENDENT);

    /**
     * Are all aggregates MIN(column), MAX(column), or COUNT(*) for the given
     * table (getTable)?
     */
    public static final int OPTIMIZABLE_MIN_MAX_COUNT_ALL = 1;

    /**
     * Does the expression return the same results for the same parameters?
     */
    public static final int DETERMINISTIC = 2;

    /**
     * The visitor singleton for the type DETERMINISTIC.
     */
    public static final ExpressionVisitor DETERMINISTIC_VISITOR =
            new ExpressionVisitor(DETERMINISTIC);

    /**
     * Can the expression be evaluated, that means are all columns set to
     * 'evaluatable'?
     */
    public static final int EVALUATABLE = 3;

    /**
     * The visitor singleton for the type EVALUATABLE.
     */
    public static final ExpressionVisitor EVALUATABLE_VISITOR =
            new ExpressionVisitor(EVALUATABLE);

    /**
     * Request to set the latest modification id (addDataModificationId).
     */
    public static final int SET_MAX_DATA_MODIFICATION_ID = 4;

    /**
     * Does the expression have no side effects (change the data)?
     */
    public static final int READONLY = 5;

    /**
     * The visitor singleton for the type EVALUATABLE.
     */
    public static final ExpressionVisitor READONLY_VISITOR =
            new ExpressionVisitor(READONLY);

    /**
     * Does an expression have no relation to the given table filter
     * (getResolver)?
     */
    public static final int NOT_FROM_RESOLVER = 6;

    /**
     * Request to get the set of dependencies (addDependency).
     */
    public static final int GET_DEPENDENCIES = 7;
    public static final int GET_TABLEFILTERS = 10;

    /**
     * Can the expression be added to a condition of an outer query. Example:
     * ROWNUM() can't be added as a condition to the inner query of select id
     * from (select t.*, rownum as r from test t) where r between 2 and 3; Also
     * a sequence expression must not be used.
     */
    public static final int QUERY_COMPARABLE = 8;
    /**
     * Get all referenced columns.
     */
    public static final int GET_COLUMNS = 9;

    /**
     * The visitor singleton for the type QUERY_COMPARABLE.
     */
    public static final ExpressionVisitor QUERY_COMPARABLE_VISITOR =
            new ExpressionVisitor(QUERY_COMPARABLE);
    private final int type;
    private final int queryLevel;
    private final HashSet<DbObject> dependencies;
    //改动
    private int id;
    private List<Integer> ids;
    private final List<TableFilter> tableFilters;
    private final HashSet<Column> columns;
    private final Table table;
    private final long[] maxDataModificationId;
    private final ColumnResolver resolver;
    private List<String> tablefilterya;
    public boolean isUnion = false;
    private List<Integer> unionId;
    private List<String> ExtraList;
    private List<String> refs;
    private String ref;
    private int totalSelectvit;
    private String unionTable;

    public String getUnionTable() {
        return unionTable;
    }

    public void setUnionTable(String unionTable) {
        this.unionTable = unionTable;
    }

    public int getTotalSelectvit() {
        return totalSelectvit;
    }

    public void setTotalSelectvit(int totalSelectvit) {
        this.totalSelectvit = totalSelectvit;
    }

    public void addTotalSelectvits(){
        totalSelectvits.add(getTotalSelectvit());
    }

    public List<Integer> getTotalSelectvits() {
        return totalSelectvits;
    }

    public void setTotalSelectvits(List<Integer> totalSelectvits) {
        this.totalSelectvits = totalSelectvits;
    }

    private List<Integer> totalSelectvits;
//    private String indexs;
//      当索引为多个时，
//    public void addIndex(Table table){
//        StringBuilder sb = new StringBuilder();
//        if (table.getIndexes()!=null && table.getIndexes().size() > 1){
//            for (int i = 1; i < table.getIndexes().size(); i++){
//                if (table.getIndexes().size() >= 2){
//                    if (table.getIndexes().size()-1==i){
//                        sb.append(table.getIndexes().get(i).getName());
//                    }else{
//                        sb.append(table.getIndexes().get(i).getName()+",");
//                    }
//                }else{
//                    sb.append(table.getIndexes().get(0).getName());
//                }
//            }
//            indexs = sb.toString();
//        }else {
//            sb.append("NULL");
//            indexs = sb.toString();
//        }
//    }
//    public String getIndex(){
//        return indexs;
//    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public List<String> getRefs() {
        return refs;
    }
    public void setRefs(List<String> refs) {
        this.refs = refs;
    }
    public void addRefs(){
        refs.add(ref);
    }

    public List<String> getExtraList() {
        return ExtraList;
    }

    public void setExtraList(List<String> extraList) {
        ExtraList = extraList;
    }

    public void addExtraList(){
        if(ExtraList==null){
            ExtraList=new ArrayList<>();
        }
        ExtraList.add(Extra);
    }

    private String Extra;

    public String getExtra() {
        return Extra;
    }

    public void setExtra(String extra) {
        Extra = extra;
    }

    public List<String> getTablefilterya() {
        return tablefilterya;
    }

    public void setTablefilterya(List<String> tablefilterya) {
        this.tablefilterya = tablefilterya;
    }

    //select type
    private List<String> select_types;
    public List<String> getSelect_types() {
        return select_types;
    }
    public void setSelect_types(List<String> select_types) {
        this.select_types = select_types;
    }
    private List<String> types;

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<Integer> getUnionId() {
        return unionId;
    }

    public void addUnionId(int unionId) {
        if (this.unionId == null) {
            this.unionId = new ArrayList<>();
        }
        this.unionId.add(unionId);
    }
    private List rowCountApproximationList;
    private Long rowCountApproximation;

    public Long getRowCountApproximation() {
        return rowCountApproximation;
    }

    public void setRowCountApproximation(Long rowCountApproximation) {
        this.rowCountApproximation = rowCountApproximation;
    }

    public List getRowCountApproximationList() {
        return rowCountApproximationList;
    }
    public void addRowCountApproximationList(){
        if (rowCountApproximationList==null){
            rowCountApproximationList = new ArrayList();
        }
        rowCountApproximationList.add(getRowCountApproximation());
    }
    public void setRowCountApproximationList(List rowCountApproximationList) {
        this.rowCountApproximationList = rowCountApproximationList;
    }

    private ExpressionVisitor(int type,
                              int queryLevel,
                              HashSet<DbObject> dependencies,
                              List<TableFilter> tableFilters,
                              HashSet<Column> columns, Table table, ColumnResolver resolver,
                              long[] maxDataModificationId) {
        this.type = type;
        this.queryLevel = queryLevel;
        this.dependencies = dependencies;
        this.tableFilters = tableFilters;
        this.columns = columns;
        this.table = table;
        this.resolver = resolver;
        this.maxDataModificationId = maxDataModificationId;
        this.id = 1;
        this.ids = new ArrayList<>();
        this.select_types = new ArrayList<>();
        this.tablefilterya = new ArrayList<>();
        this.ExtraList = new ArrayList<>();
        this.refs = new ArrayList<>();
        this.totalSelectvits = new ArrayList<>();
    }


    private ExpressionVisitor(int type) {
        this.type = type;
        this.queryLevel = 0;
        this.dependencies = null;
        this.tableFilters = null;
        this.columns = null;
        this.table = null;
        this.resolver = null;
        this.maxDataModificationId = null;
        this.id = 1;
        this.ids = null;
        this.select_types = null;
        this.tablefilterya = null;
        this.ExtraList = null;
        this.refs = null;
        this.totalSelectvits = null;
    }

    /**
     * Create a new visitor object to collect dependencies.
     *
     * @param //dependencies the dependencies set
     * @return the new visitor
     */
    public static ExpressionVisitor getDependenciesVisitor(
            HashSet<DbObject> dependencies) {
        return new ExpressionVisitor(GET_DEPENDENCIES, 0, dependencies,null, null,
                null, null, null);
    }
    //改动
    public static ExpressionVisitor getTablefilterVisitor(
            List<TableFilter> tableFilters) {
        return new ExpressionVisitor(GET_TABLEFILTERS, 0, null,tableFilters, null,
                null, null, null);
    }
    /**
     * Create a new visitor to check if all aggregates are for the given table.
     *
     * @param table the table
     * @return the new visitor
     */
    public static ExpressionVisitor getOptimizableVisitor(Table table) {
        return new ExpressionVisitor(OPTIMIZABLE_MIN_MAX_COUNT_ALL, 0, null,null,
                null, table, null, null);
    }

    /**
     * Create a new visitor to check if no expression depends on the given
     * resolver.
     *
     * @param resolver the resolver
     * @return the new visitor
     */
    public static ExpressionVisitor getNotFromResolverVisitor(ColumnResolver resolver) {
        return new ExpressionVisitor(NOT_FROM_RESOLVER, 0, null, null, null,null,
                resolver, null);
    }

    /**
     * Create a new visitor to get all referenced columns.
     *
     * @param columns the columns map
     * @return the new visitor
     */
    public static ExpressionVisitor getColumnsVisitor(HashSet<Column> columns) {
        return new ExpressionVisitor(GET_COLUMNS, 0, null,null, columns, null, null, null);
    }

    public static ExpressionVisitor getMaxModificationIdVisitor() {
        return new ExpressionVisitor(SET_MAX_DATA_MODIFICATION_ID, 0,null, null,
                null, null, null, new long[1]);
    }

    /**
     * Add a new dependency to the set of dependencies.
     * This is used for GET_DEPENDENCIES visitors.
     *
     * @param obj the additional dependency.
     */
    public void addDependency(DbObject obj) {
        dependencies.add(obj);
    }
    //改动
    public void addTablefilter(TableFilter obj) {
        tableFilters.add(obj);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void addIds() {
        ids.add(id);
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    /**
     * Add a new column to the set of columns.
     * This is used for GET_COLUMNS visitors.
     *
     * @param column the additional column.
     */
    void addColumn(Column column) {
        columns.add(column);
    }

    /**
     * Get the dependency set.
     * This is used for GET_DEPENDENCIES visitors.
     *
     * @return the set
     */
    public HashSet<DbObject> getDependencies() {
        return dependencies;
    }
    //改动
    public List<TableFilter> getTablefilters() {
        return tableFilters;
    }

    /**
     * Increment or decrement the query level.
     *
     * @param offset 1 to increment, -1 to decrement
     * @return a clone of this expression visitor, with the changed query level
     */
    //改动一点
    public ExpressionVisitor incrementQueryLevel(int offset) {
        ExpressionVisitor visitor = new ExpressionVisitor(type, queryLevel + offset, dependencies, tableFilters,
                columns, table, resolver, maxDataModificationId);
        visitor.setId(id);
        visitor.setIds(ids);
        visitor.setRowCountApproximationList(rowCountApproximationList);
        visitor.setExtraList(ExtraList);
        visitor.setRefs(refs);
        visitor.setTotalSelectvits(totalSelectvits);
        return visitor;
    }

    /**
     * Get the column resolver.
     * This is used for NOT_FROM_RESOLVER visitors.
     *
     * @return the column resolver
     */
    public ColumnResolver getResolver() {
        return resolver;
    }

    /**
     * Update the field maxDataModificationId if this value is higher
     * than the current value.
     * This is used for SET_MAX_DATA_MODIFICATION_ID visitors.
     *
     * @param value the data modification id
     */
    public void addDataModificationId(long value) {
        long m = maxDataModificationId[0];
        if (value > m) {
            maxDataModificationId[0] = value;
        }
    }

    /**
     * Get the last data modification.
     * This is used for SET_MAX_DATA_MODIFICATION_ID visitors.
     *
     * @return the maximum modification id
     */
    public long getMaxDataModificationId() {
        return maxDataModificationId[0];
    }

    int getQueryLevel() {
        return queryLevel;
    }

    /**
     * Get the table.
     * This is used for OPTIMIZABLE_MIN_MAX_COUNT_ALL visitors.
     *
     * @return the table
     */
    public Table getTable() {
        return table;
    }

    /**
     * Get the visitor type.
     *
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * Get the set of columns of all tables.
     *
     * @param filters the filters
     * @return the set of columns
     */
    public static HashSet<Column> allColumnsForTableFilters(TableFilter[] filters) {
        HashSet<Column> allColumnsSet = new HashSet<>();
        for (TableFilter filter : filters) {
            if (filter.getSelect() != null) {
                filter.getSelect().isEverything(ExpressionVisitor.getColumnsVisitor(allColumnsSet));
            }
        }
        return allColumnsSet;
    }

}
