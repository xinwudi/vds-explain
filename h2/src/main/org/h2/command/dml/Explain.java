/*
 * Copyright 2004-2018 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.command.dml;

import java.sql.SQLException;
import java.sql.Wrapper;
import java.util.*;
import java.util.Map.Entry;
import java.util.Set;

import org.h2.command.Command;
import org.h2.command.CommandInterface;
import org.h2.command.Prepared;
import org.h2.engine.Database;
import org.h2.engine.DbObject;
import org.h2.engine.Session;
import org.h2.expression.*;
import org.h2.mvstore.db.MVTableEngine;
import org.h2.mvstore.db.MVTableEngine.Store;
import org.h2.result.LocalResult;
import org.h2.result.ResultInterface;
import org.h2.store.PageStore;
import org.h2.table.Column;
import org.h2.table.Table;
import org.h2.table.TableFilter;
import org.h2.value.Value;
import org.h2.value.ValueArray;
import org.h2.value.ValueInt;
import org.h2.value.ValueString;

import javax.swing.text.html.HTMLDocument;

/**
 * This class represents the statement
 * EXPLAIN
 */
public class Explain extends Prepared {

    private Prepared command;
    private LocalResult result;
    private boolean executeCommand;
    private Database database;

    public Explain(Session session) {
        super(session);
    }

    public void setCommand(Prepared command) {
        this.command = command;
    }

    public Prepared getCommand() {
        return command;
    }

    @Override
    public void prepare() {
        command.prepare();
    }

    public void setExecuteCommand(boolean executeCommand) {
        this.executeCommand = executeCommand;
    }

    @Override
    public ResultInterface queryMeta() {
        return query(-1);
    }

    @Override
    public void checkParameters() {
        // Check params only in case of EXPLAIN ANALYZE
        if (executeCommand) {
            super.checkParameters();
        }
    }

    @Override
    public ResultInterface query(int maxrows) {
        if(!executeCommand) {
            return getLogicExplain(maxrows);
        }else{
            return getPhysicalExplain(maxrows);
        }
    }

    private ResultInterface getPhysicalExplain(int maxrows) {
            PageStore store = null;
            Store mvStore = null;
            database = session.getDatabase();
            if (database.isPersistent()) {
                store = database.getPageStore();
                if (store != null) {
                    store.statisticsStart();
                }
                mvStore = database.getMvStore();
                if (mvStore != null) {
                    mvStore.statisticsStart();
                }
            }
            if (command.isQuery()) {
                command.query(maxrows);
            } else {
                command.update();
            }
        String plan = command.getPlanSQL();
            Map<String, Integer> statistics = null;
            if (store != null) {
                statistics = store.statisticsEnd();
            } else if (mvStore != null) {
                statistics = mvStore.statisticsEnd();
            }
            if (statistics != null) {
                int total = 0;
                for (Entry<String, Integer> e : statistics.entrySet()) {
                    total += e.getValue();
                }
                if (total > 0) {
                    statistics = new TreeMap<>(statistics);
                    StringBuilder buff = new StringBuilder();
                    if (statistics.size() > 1) {
                        buff.append("total: ").append(total).append('\n');
                    }
                    for (Entry<String, Integer> e : statistics.entrySet()) {
                        int value = e.getValue();
                        int percent = (int) (100L * value / total);
                        buff.append(e.getKey()).append(": ").append(value);
                        if (statistics.size() > 1) {
                            buff.append(" (").append(percent).append("%)");
                        }
                        buff.append('\n');
                    }
                    plan += "\n/*\n" + buff.toString() + "*/";
                }
            }
            Expression[] expressions = getAnalyzeExpressions();
            result = new LocalResult(session, expressions, expressions.length);
            if (maxrows >= 0) {
                    Value[] row = new Value[]{
                            ValueInt.get(0),
                            ValueString.get(""),
                            ValueString.get(""),
                            ValueString.get("NULL")
                    };
                this.add(row);
            }
                result.done();
                return result;
    }
    //Explain逻辑执行计划
    private ResultInterface getLogicExplain(int maxrows) {
        //用来存visitor
        List<TableFilter> tableFiltersList = new ArrayList<>();
        ExpressionVisitor visitor = ExpressionVisitor.getTablefilterVisitor(tableFiltersList);
        try {
            if (command instanceof Select) {
                ((Select)command).isEverything(visitor);
            }else if(command instanceof Wrapper && ((Wrapper) command).isWrapperFor(Select.class)){
                ((Wrapper) command).unwrap(Select.class).isEverything(visitor);
            }else if(command instanceof Wrapper && ((Wrapper) command).isWrapperFor(SelectUnion.class)){
                ((Wrapper) command).unwrap(SelectUnion.class).isEverything(visitor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Explain中getLogicExplain方法异常。");
        }
        database = session.getDatabase();
        Expression[] expressions = getExpressions();
        result = new LocalResult(session, expressions, expressions.length);
        if (maxrows >= 0) {
            judgeSelectType(visitor);
            List listTablefilter = new ArrayList();
            List listType = new ArrayList();
            UnionId(visitor);
            for (int i=0;i<tableFiltersList.size();i++) {
                judgeType(visitor, listTablefilter, listType, i);
                Value[] row = new Value[]{
                        ValueInt.get(visitor.getIds().get(i)),
                        ValueString.get(visitor.getSelect_types().size() == 0 ? "null" : visitor.getSelect_types().get(i)),
                        ValueString.get(visitor.getTablefilterya().get(i)),
                        ValueString.get(visitor.getTypes().get(i)),
                        ValueString.get("NULL"),
                        ValueString.get(tableFiltersList.get(i).getIndex()==null ? "null" :(tableFiltersList.get(i).getIndex().getName()==null ? "null" : tableFiltersList.get(i).getIndex().getName())),
                        ValueString.get(tableFiltersList.get(i).getIndex()==null ? "null" :(tableFiltersList.get(i).getIndex().getName()==null ? "null" : tableFiltersList.get(i).getIndex().getName())),
                        ValueInt.get(tableFiltersList.get(i).getIndex()==null ? 0 :(tableFiltersList.get(i).getIndex().getName()==null ? 0 : tableFiltersList.get(i).getIndex().getName().length())),
                        ValueString.get(visitor.getRefs().get(i)),
                        ValueString.get(visitor.getRowCountApproximationList().get(i).toString()),
                        ValueString.get(visitor.getTotalSelectvits().get(i).toString()),
                        ValueString.get(visitor.getExtraList().get(i))
                };
                this.add(row);
            }
            if (this.toString().toLowerCase().contains("union")){
                Value[] row = new Value[]{
                ValueString.get("null"), ValueString.get("UNION RESULT"), ValueString.get(visitor.getUnionTable()), ValueString.get("null"), ValueString.get("null"), ValueString.get("null"),
                ValueString.get("null"), ValueString.get("null"), ValueString.get("null"), ValueString.get("null"), ValueString.get("null"), ValueString.get("Using temporary"),
                };
                this.add(row);

            }
            listType.clear();
            listTablefilter.clear();

        }
        result.done();
        return result;
    }

    private void judgeSelectType(ExpressionVisitor visitor) {
        Set s = new HashSet(visitor.getIds());
        if (s.size()==1){
            List listselect_type=new ArrayList();
            for (int i=0;i<visitor.getIds().size();i++){
                listselect_type.add("SIMPLE");
            }
            visitor.setSelect_types(listselect_type);
        }else {
            List listselect_type=new ArrayList();
            for (int i=0;i<visitor.getIds().size();i++){
                if (visitor.getIds().get(i)==1){
                    listselect_type.add("PRIMARY");
                    visitor.setSelect_types(listselect_type);
                }else if (visitor.getUnionId()!=null && visitor.getIds().get(i)==visitor.getUnionId().get(visitor.getUnionId().size()-1)){
                    listselect_type.add("UNION");
                }else{
                    listselect_type.add("SUBQUERY");
                }
            }
            visitor.setSelect_types(listselect_type);
        }
    }

    private void UnionId(ExpressionVisitor visitor) {
        StringBuilder sb = new StringBuilder();
        if (visitor.getUnionId()!=null){
            for (int i =0;i<visitor.getUnionId().size();i++){
                if (i==0){
                    sb.append("<union"+visitor.getUnionId().get(0));
                }else if (i==visitor.getUnionId().size()-1) {
                    sb.append("," + visitor.getUnionId().get(i) + ">");
                }else {
                    if (i==visitor.getUnionId().size()-2){
                        sb.append(","+visitor.getUnionId().get(i));
                    }else{
                        sb.append(","+visitor.getUnionId().get(i)+",");
                    }
                }
            }
        }
        visitor.setUnionTable(sb.toString());
    }

    private void judgeType(ExpressionVisitor visitor, List listTablefilter, List listType, int i) {
        if (visitor.getTablefilters().get(i).toString().contains("PUBLIC")){
            listTablefilter.add(visitor.getTablefilters().get(i).toString().substring(visitor.getTablefilters().get(i).toString().indexOf(".")).replace(".",""));
            visitor.setTablefilterya(listTablefilter);
        }else {
            listTablefilter.add("<derived>");
            visitor.setTablefilterya(listTablefilter);
        }
        if (visitor.getTablefilterya().get(i).contains("<derived>")){
            listType.add("System");
            visitor.setTypes(listType);
        } else if (visitor.getRefs().get(i)=="null"){
            listType.add("range");
            visitor.setTypes(listType);
        } else if (visitor.getTablefilters().get(i).getIndex() != null && visitor.getTablefilters().get(i).getIndex().getName() != null) {
            listType.add("index");
            visitor.setTypes(listType);
        } else{
                listType.add("ALL");
                visitor.setTypes(listType);
            }
    }

    private Expression[] getAnalyzeExpressions(){
        ExpressionColumn expr = new ExpressionColumn(database, new Column("id", Value.STRING));
        ExpressionColumn expr1 = new ExpressionColumn(database, new Column("select_type", Value.STRING));
        ExpressionColumn expr2 = new ExpressionColumn(database, new Column("table",Value.STRING));
        ExpressionColumn expr3 = new ExpressionColumn(database, new Column("type", Value.STRING));
        return new Expression[]{ expr,expr1,expr2,expr3};
    }

    private Expression[] getExpressions() {
        Column column = new Column("id", Value.STRING);
        ExpressionColumn expr = new ExpressionColumn(database, column);
        ExpressionColumn expr1 = new ExpressionColumn(database, new Column("select_type", Value.STRING));
        ExpressionColumn expr2 = new ExpressionColumn(database, new Column("table",Value.STRING));
        ExpressionColumn expr3 = new ExpressionColumn(database, new Column("type", Value.STRING));
        ExpressionColumn expr10 = new ExpressionColumn(database, new Column("partitions", Value.STRING));
        ExpressionColumn expr4 = new ExpressionColumn(database, new Column("possible_keys", Value.STRING));
        ExpressionColumn expr5 = new ExpressionColumn(database, new Column("key", Value.STRING));
        ExpressionColumn expr6 = new ExpressionColumn(database, new Column("key_len", Value.STRING));
        ExpressionColumn expr7 = new ExpressionColumn(database, new Column("ref", Value.STRING));
        ExpressionColumn expr8 = new ExpressionColumn(database, new Column("rows", Value.STRING));
        ExpressionColumn expr11 = new ExpressionColumn(database, new Column("filtered", Value.STRING));
        ExpressionColumn expr9 = new ExpressionColumn(database, new Column("Extra", Value.STRING));
        return new Expression[]{ expr,expr1,expr2,expr3,expr10,expr4,expr5,expr6,expr7,expr8,expr11,expr9};
    }

    private void add(Value[] row) {

        result.addRow(row);
    }

    @Override
    public boolean isQuery() {
        return true;
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return command.isReadOnly();
    }

    @Override
    public int getType() {
        return executeCommand ? CommandInterface.EXPLAIN_ANALYZE : CommandInterface.EXPLAIN;
    }
}
