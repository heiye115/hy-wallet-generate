package com.hy.wallet.utils;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

import java.nio.ByteBuffer;

/**
 * SLIP-0010 Ed25519硬化派生实现工具类
 * 参考规范：https://github.com/satoshilabs/slips/blob/master/slip-0010.md
 * 仅支持硬化派生（Ed25519仅定义硬化派生），用于Solana等Ed25519链的BIP44路径。
 */
public class Slip10Ed25519 {

    /** 常量：HMAC-SHA512种子Key */
    private static final byte[] ED25519_SEED_KEY = "ed25519 seed".getBytes();

    /**
     * Node结构：包含派生私钥(左32字节)与链码(右32字节)
     */
    public static class Node {
        private final byte[] key; // 左32字节，作为私钥材料
        private final byte[] chainCode; // 右32字节

        public Node(byte[] key, byte[] chainCode) {
            this.key = key;
            this.chainCode = chainCode;
        }

        /** 获取私钥材料（32字节） */
        public byte[] getKey() {
            return key;
        }

        /** 获取链码（32字节） */
        public byte[] getChainCode() {
            return chainCode;
        }
    }

    /**
     * 生成主节点（Master node）。
     * 
     * @param seed BIP32种子字节
     * @return 主节点
     */
    public static Node master(byte[] seed) {
        byte[] out = hmacSha512(ED25519_SEED_KEY, seed);
        byte[] key = new byte[32];
        byte[] chainCode = new byte[32];
        System.arraycopy(out, 0, key, 0, 32);
        System.arraycopy(out, 32, chainCode, 0, 32);
        return new Node(key, chainCode);
    }

    /**
     * 硬化派生子节点：index'
     * 
     * @param parent 父节点
     * @param index  子索引（不含硬化位）
     * @return 子节点
     */
    public static Node deriveHardened(Node parent, int index) {
        // 输入数据：0x00 || key || index_with_hardened
        int hardenedIndex = index | 0x8000_0000; // 设置最高位
        ByteBuffer bb = ByteBuffer.allocate(1 + 32 + 4);
        bb.put((byte) 0x00);
        bb.put(parent.key);
        bb.putInt(hardenedIndex);

        byte[] out = hmacSha512(parent.chainCode, bb.array());
        byte[] key = new byte[32];
        byte[] chainCode = new byte[32];
        System.arraycopy(out, 0, key, 0, 32);
        System.arraycopy(out, 32, chainCode, 0, 32);
        return new Node(key, chainCode);
    }

    /**
     * HMAC-SHA512计算
     * 
     * @param key  Key字节
     * @param data 数据
     * @return 64字节输出
     */
    private static byte[] hmacSha512(byte[] key, byte[] data) {
        HMac hmac = new HMac(new SHA512Digest());
        hmac.init(new KeyParameter(key));
        hmac.update(data, 0, data.length);
        byte[] out = new byte[64];
        hmac.doFinal(out, 0);
        return out;
    }
}
