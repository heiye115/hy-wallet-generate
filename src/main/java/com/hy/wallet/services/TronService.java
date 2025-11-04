package com.hy.wallet.services;

import org.bitcoinj.core.Base58;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bouncycastle.crypto.digests.KeccakDigest;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * TRON服务类
 * 使用BIP44路径 m/44'/195'/0'/0/0 派生secp256k1私钥，计算TRON地址（Base58Check）。
 * Tron地址生成流程：
 * 1) 公钥(未压缩，去除前缀0x04)做Keccak-256，取最后20字节
 * 2) 前缀0x41（主网）拼接 -> 得到21字节地址
 * 3) 对上述地址做double-SHA256取前4字节作为校验码
 * 4) 地址+校验码 做Base58编码
 */
public class TronService {

    /**
     * Tron地址与私钥封装
     * 
     * @param address    Base58地址
     * @param privateHex 私钥16进制（0x前缀）
     */
    public record TronPair(String address, String privateHex) {
    }

    /**
     * 生成TRON地址与私钥
     * 
     * @param seedBytes BIP32种子
     * @return 地址与私钥
     */
    public static TronPair generateTron(byte[] seedBytes) {
        // m/44'/195'/0'/0/0
        DeterministicKey root = HDKeyDerivation.createMasterPrivateKey(seedBytes);
        DeterministicKey purpose44 = HDKeyDerivation.deriveChildKey(root, new ChildNumber(44, true));
        DeterministicKey coinType195 = HDKeyDerivation.deriveChildKey(purpose44, new ChildNumber(195, true));
        DeterministicKey account0 = HDKeyDerivation.deriveChildKey(coinType195, new ChildNumber(0, true));
        DeterministicKey change0 = HDKeyDerivation.deriveChildKey(account0, new ChildNumber(0, false));
        DeterministicKey index0 = HDKeyDerivation.deriveChildKey(change0, new ChildNumber(0, false));

        BigInteger priv = new BigInteger(1, index0.getPrivKeyBytes());
        // 计算未压缩公钥
        byte[] uncompressedPubKey = index0.getPubKeyPoint().getEncoded(false); // 65字节，首字节0x04
        byte[] pubKeyNoPrefix = Arrays.copyOfRange(uncompressedPubKey, 1, uncompressedPubKey.length);

        // keccak-256
        byte[] keccak = keccak256(pubKeyNoPrefix);
        byte[] last20 = Arrays.copyOfRange(keccak, keccak.length - 20, keccak.length);

        // Tron前缀0x41
        byte[] tronAddr = new byte[last20.length + 1];
        tronAddr[0] = 0x41;
        System.arraycopy(last20, 0, tronAddr, 1, last20.length);

        // Base58Check（double sha256取校验后4字节）
        byte[] checksum = doubleSha256(tronAddr);
        byte[] addrWithChecksum = new byte[tronAddr.length + 4];
        System.arraycopy(tronAddr, 0, addrWithChecksum, 0, tronAddr.length);
        System.arraycopy(checksum, 0, addrWithChecksum, tronAddr.length, 4);
        String base58Addr = Base58.encode(addrWithChecksum);

        // 私钥统一输出为0x前缀的64位小写hex（左侧补零）
        String privateHex = "0x" + leftPad64(priv.toString(16));
        return new TronPair(base58Addr, privateHex);
    }

    /**
     * 计算Keccak-256
     * 
     * @param input 输入数据
     * @return 哈希输出
     */
    private static byte[] keccak256(byte[] input) {
        KeccakDigest keccak = new KeccakDigest(256);
        keccak.update(input, 0, input.length);
        byte[] out = new byte[32];
        keccak.doFinal(out, 0);
        return out;
    }

    /**
     * 对数据执行两次SHA-256，并返回前4字节校验码
     * 
     * @param input 输入数据
     * @return 校验码（前4字节）
     */
    private static byte[] doubleSha256(byte[] input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] first = md.digest(input);
            byte[] second = md.digest(first);
            return Arrays.copyOfRange(second, 0, 4);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256计算失败", e);
        }
    }

    /**
     * 左侧补零至64位hex字符串
     */
    private static String leftPad64(String hex) {
        String lower = hex.toLowerCase();
        return "0".repeat(Math.max(0, 64 - lower.length())) + lower;
    }
}
