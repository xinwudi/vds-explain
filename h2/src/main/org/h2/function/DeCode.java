package org.h2.function;

import org.h2.engine.Database;
import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.expression.UserFunction;
import org.h2.message.DbException;
import org.h2.util.StringUtils;
import org.h2.value.Value;
import org.h2.value.ValueNull;
import org.h2.value.ValueString;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

import static org.h2.function.UnCompress.base64Decode;

/**
 * 为了兼容mysql和oracle的decode函数. 以参数的数量来进行区分.
 */
public class DeCode extends UserFunction {
    public static final String NAME = "DECODE";
    private Database database;

    public DeCode(Database database) {
        super(database, NAME, Functions.TYPE_DECODE, Value.NULL, -1, true);
        this.database = database;
    }

    public Value getValueWithArgs(Session session, Expression[] args) {
        if (args.length == 2) {
            return getMysqlDecodeValue(session, args);
        }
        return getOraDecodeValue(session, args);
    }

    private Value getMysqlDecodeValue(Session session, Expression[] args) {
        Value v1 = ValueNull.INSTANCE;
        if(Functions.isNullValueAll(args,session))
            return v1;
        v1=args[0].getValue(session);
        Value v2=args[1].getValue(session);
        Value value;
        try {
            String s = StringUtils.isNullOrEmpty(v1.getString())
                    ? "" : aesDecryptByBytes(base64Decode(v1.getString()), v2.getString());
            value = ValueString.get(s);
        } catch (Exception e) {
            throw DbException.convert(e);
        }
        return value;
    }

    private Value getOraDecodeValue(Session session, Expression[] args) {
        Value[] values = new Value[args.length];
        Value v0 = getNullOrValue(session, args, values, 0);
        int index = -1;
        for (int i = 1, len = args.length - 1; i < len; i += 2) {
            if (database.areEqual(v0,
                    getNullOrValue(session, args, values, i))) {
                index = i + 1;
                break;
            }
        }
        if (index < 0 && args.length % 2 == 0) {
            index = args.length - 1;
        }
        Value v = index < 0 ? ValueNull.INSTANCE :
                getNullOrValue(session, args, values, index);
        return v == null ? ValueNull.INSTANCE : (Value.NULL == super.getType() ? v : v.convertTo(super.getType()));
    }

    /**
     * AES解密
     */
    private String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(decryptKey.getBytes("utf-8"));
        kgen.init(128, random);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
        byte[] decryptBytes = cipher.doFinal(encryptBytes);

        return new String(decryptBytes, "utf-8");
    }

    public void checkParameterCount(int len) {
    }
}
