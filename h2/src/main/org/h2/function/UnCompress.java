package org.h2.function;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueNull;
import org.h2.value.ValueString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class UnCompress {
    public static final String NAME = "UNCOMPRESS";

    public Value getValueWithArgs(Session session, Expression[] args) {
        if(Functions.isNullValue(args[0],session))
            return ValueNull.INSTANCE;
        final Value value1 = args[0].getValue(session);
        String a= value1.getString();
        try {
            return ValueString.get (
                    new String(inflater(base64Decode(a)),"utf-8"));//先解码再解压
        } catch (Exception e) {
            throw DbException.convert(e);
        }

    }
    /**
     * 解压缩.
     */
    private byte[] inflater(final byte[] inputByte) throws IOException {
        int compressedDataLength;
        Inflater compresser = new Inflater(false);
        compresser.setInput(inputByte, 0, inputByte.length);
        ByteArrayOutputStream o = new ByteArrayOutputStream(inputByte.length);
        byte[] result = new byte[1024];
        try {
            while (!compresser.finished()) {
                compressedDataLength = compresser.inflate(result);
                if (compressedDataLength == 0) {
                    break;
                }
                o.write(result, 0, compressedDataLength);
            }
        } catch (DataFormatException ex) {
            throw DbException.convert(ex);
        } finally {
            o.close();
        }
        compresser.end();
        return o.toByteArray();
    }
    /**
     * BASE64解码
     */
    static byte[] base64Decode(String inputByte) throws IOException {
        return Base64.decode(inputByte);
    }

    public void checkParameterCount(int len) {
    }
}
