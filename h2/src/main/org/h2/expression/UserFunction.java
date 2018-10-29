package org.h2.expression;

import org.h2.engine.Database;
import org.h2.message.DbException;
import org.h2.util.StatementBuilder;
import org.h2.util.StringUtils;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * Created by shihailong on 2017/7/20.
 */
public abstract class UserFunction extends Function {
    static final Map<String, Constructor<? extends Function>> USER_FUNCTIONS = new HashMap<>();
    static final Set<String> DISABLE_FUNCTIONS = new HashSet<>();

    static {
        final InputStream inputStream = UserFunction.class.getClassLoader().getResourceAsStream("function.disable");
        try {
            if (inputStream != null) {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                String line = bufferedReader.readLine();
                while (line != null) {
                    int ci = line.indexOf('#');
                    if (ci >= 0) line = line.substring(0, ci);
                    line = line.trim();
                    if (!line.isEmpty()) {
                        for (String name : StringUtils.split(line, ',')) {
                            DISABLE_FUNCTIONS.add(name.trim().toUpperCase());
                        }
                    }
                    line = bufferedReader.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            final String PREFIX = "META-INF/services/";
            String fullName = PREFIX + Function.class.getName();
            final Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(fullName);
            while (urlEnumeration.hasMoreElements()) {
                final URL url = urlEnumeration.nextElement();
                try (final InputStream inputStream = url.openStream();
                     final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                ) {
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        int ci = line.indexOf('#');
                        if (ci >= 0) line = line.substring(0, ci);
                        line = line.trim();
                        if (!line.isEmpty()) {
                            final Class<?> clazz = Class.forName(line);
                            if (Function.class.isAssignableFrom(clazz)) {
                                //noinspection unchecked
                                add((Class<? extends Function>) clazz);
                            }
                        }
                        line = bufferedReader.readLine();
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw DbException.convert(e);
        }
    }

    private static void add(Class<? extends Function> clazz) {
        try {
            final Field name = clazz.getDeclaredField("NAME");
            name.setAccessible(true);
            add((String) name.get(clazz), clazz);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw DbException.convert(e);
        }
    }

    private static void add(String name, Class<? extends Function> clazz) {
        try {
            UserFunction.add(name, clazz.getConstructor(Database.class));
        } catch (NoSuchMethodException e) {
            throw DbException.convert(e);
        }
    }

    public static void add(String name, Constructor<? extends Function> constructor) {
        USER_FUNCTIONS.put(name, constructor);
    }

    protected UserFunction(Database database, String name, int type, int dataType, int parameterCount, boolean deterministic) {
        super(database, define(name, type, dataType, parameterCount, deterministic));
    }

//    protected UserFunction(Database database, String name, int type, int dataType, int parameterCount) {
//        this(database, name, type, dataType, parameterCount, true);
//    }

//    protected UserFunction(Database database, String name, int type, int dataType) {
//        this(database, name, type, dataType, VAR_ARGS);
//    }

    private static FunctionInfo define(String name, int type, int dataType, int parameterCount, boolean deterministic) {
        FunctionInfo functionInfo = new FunctionInfo();
        functionInfo.name = name;
        functionInfo.returnDataType = dataType;
        functionInfo.parameterCount = parameterCount;
        functionInfo.type = type;
        functionInfo.nullIfParameterIsNull = true;
        functionInfo.deterministic = deterministic;
        return functionInfo;
    }

}
