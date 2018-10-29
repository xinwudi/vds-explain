package org.h2.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.message.DbException;
import org.h2.value.Value;
import org.h2.value.ValueInt;
import org.h2.value.ValueNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Hash {

    public static final String NAME = "HASH";


    public Value getValueWithArgs(Session session, Expression[] args) {
        if (Functions.isNullValueAll(args,session))
            return ValueNull.INSTANCE;
        Value value1= args[0].getValue(session);
        int n=hash(value1.getObject().toString()).intValue();
        if(args.length>1){
           int NODE_NUM=args[1].getValue(session).getInt();
           n=jumpConsistentHash(hash(value1.getObject().toString()),NODE_NUM);
        }
        return ValueInt.get(Math.abs(n));
    }


    public void checkParameterCount(int len) {
        if (len > 3)
            throw DbException.throwInternalError(NAME+"函数参数的数量不匹配");
    }
//    private TreeMap<Long, Integer> nodes; // 虚拟节点
//    private int shards; // 真实机器节点
//    private final int NODE_NUM = 100; // 每个机器节点关联的虚拟节点个数
//
//
//    private void init() { // 初始化一致性hash环
//        nodes = new TreeMap<>();
//        for (int i = 0; i != shards; ++i) { // 每个真实机器节点都需要关联虚拟节点
//            final int shard = i;
//            for (int n = 0; n < NODE_NUM; n++)
//                // 一个真实机器节点关联NODE_NUM个虚拟节点
//                nodes.put(hash("SHARD-" + i + "-NODE-" + n), shard);
//        }
//    }
//
//    public int getShardInfo(String key) {
//        SortedMap<Long, Integer> tail = nodes.tailMap(hash(key)); // 沿环的顺时针找到一个虚拟节点
//        if (tail.size() == 0) {
//            return nodes.get(nodes.firstKey());
//        }
//        return tail.get(tail.firstKey()); // 返回该虚拟节点对应的真实机器节点的信息
//    }

    /**
     *  MurMurHash算法，是非加密HASH算法
     */
    private Long hash(String key) {

        ByteBuffer buf = ByteBuffer.wrap(key.getBytes());
        int seed = 0x1234ABCD;

        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        long m = 0xc6a4a7935bd1e995L;
        int r = 47;

        long h = seed ^ (buf.remaining() * m);

        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h ^= k;
            h *= m;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(
                    ByteOrder.LITTLE_ENDIAN);
            // for big-endian version, do this first:
            // finish.position(8-buf.remaining());
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        buf.order(byteOrder);
        return h;
    }

    /**
     *跳增一致性哈希
     */
    public static int jumpConsistentHash(final long key, final int NODE_NUM) {
        final long JUMP = 1L << 31;
        // If JDK >= 1.8, just use Long.parseUnsignedLong("2862933555777941757") instead.
        final long CONSTANT = Long.parseLong("286293355577794175", 10) * 10 + 7;
        if (NODE_NUM<0){
            throw DbException.throwInternalError("节点数量不能小于0！");
        }
        long k = key;
        long b = -1;
        long j = 0;

        while (j < NODE_NUM) {
            b = j;
            k = k * CONSTANT + 1L;

            j = (long) ((b + 1L) * (JUMP / toDouble((k >>> 33) + 1L)));
        }
        return (int) b;
    }
    private static double toDouble(final long n) {
        final long UNSIGNED_MASK = 0x7fffffffffffffffL;
        double d = n & UNSIGNED_MASK;
        if (n < 0) {
            d += 0x1.0p63;
        }
        return d;
    }
}
