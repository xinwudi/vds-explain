package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueNull;
import org.h2.value.ValueString;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

import static org.h2.function.Compress.base64Encode;


public class EnCode {
    public static final String NAME = "ENCODE";

    public Value getValueWithArgs(Session session, Expression[] args) {
        Value v1 = ValueNull.INSTANCE;
        if(Functions.isNullValueAll(args,session))
            return v1;
        v1=args[0].getValue(session);
        Value v2 = ValueNull.INSTANCE;
        if(Functions.isNullValue(args[1],session))
            return v2;
        v2=args[1].getValue(session);
        Value value;
        try {
            String s=base64Encode(aesEncryptToBytes(v1.getString(), v2.getString()));
            value= ValueString.get(s);
        } catch (Exception e) {
            throw DbException.convert(e);
        }
        return value;
    }
    /**
     *AES加密
     */
    private byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom random=SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(encryptKey.getBytes("utf-8"));
        kgen.init(128, random);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));

        return cipher.doFinal(content.getBytes("utf-8"));
    }

    public void checkParameterCount(int len) {
    }
}
