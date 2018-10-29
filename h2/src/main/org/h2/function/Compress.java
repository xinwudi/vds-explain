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
import java.util.zip.Deflater;

public class Compress {

    public static final String NAME = "COMPRESS";
    private static final String charSet="UTF8";

    public Value getValueWithArgs(Session session, Expression[] args) {
        Value value1= ValueNull.INSTANCE;
        if (Functions.isNullValue(args[0],session))
            return value1;
        value1 = args[0].getValue(session);
        try {
            byte[] a= value1.getString().getBytes(charSet);
            return ValueString.get(base64Encode(deflater(a)));//先压缩再编码
        } catch (Exception e) {
            throw DbException.convert(e);
        }
    }

    /**
     * 压缩.
     */
    private byte[] deflater(final byte[] inputByte) throws IOException {
        int compressedDataLength;
        Deflater compresser = new Deflater();
        compresser.setInput(inputByte);
        compresser.finish();
        ByteArrayOutputStream o = new ByteArrayOutputStream(inputByte.length);
        byte[] result = new byte[1024];
        try {
            while (!compresser.finished()) {
                compressedDataLength = compresser.deflate(result);
                o.write(result, 0, compressedDataLength);
            }
        } finally {
            o.close();
        }
        compresser.end();
        return o.toByteArray();
    }

    /**
     * BASE64编码
     */
    static String base64Encode(byte[] inputByte) throws IOException {
        return Base64.encode(inputByte);
    }

    public void checkParameterCount(int len) {
        if (len > 1)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
}
