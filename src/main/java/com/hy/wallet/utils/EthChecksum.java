package com.hy.wallet.utils;

import org.bouncycastle.crypto.digests.KeccakDigest;

/**
 * 以太坊EIP-55校验和地址生成工具类
 * 参考规范：https://eips.ethereum.org/EIPS/eip-55
 * 输入小写地址（不含0x前缀），输出带混合大小写的校验和地址（含0x）。
 */
public class EthChecksum {

    /**
     * 根据EIP-55生成校验和地址。
     * @param lowerNoPrefix 小写地址（不含0x），长度40
     * @return 0x前缀的校验和地址
     */
    public static String toChecksumAddress(String lowerNoPrefix) {
        String lower = lowerNoPrefix.toLowerCase();
        byte[] hash = keccak256(lower.getBytes());
        StringBuilder sb = new StringBuilder("0x");
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            int hashNibble = (hash[i / 2] >> ((i % 2 == 0) ? 4 : 0)) & 0x0F; // 取对应半字节
            if (Character.isDigit(c)) {
                sb.append(c);
            } else {
                sb.append((hashNibble >= 8) ? Character.toUpperCase(c) : c);
            }
        }
        return sb.toString();
    }

    /**
     * 验证地址是否为有效EIP-55校验和地址
     * @param address 输入地址（必须以0x开头）
     * @return true=校验通过
     */
    public static boolean isValidChecksumAddress(String address) {
        if (address == null || !address.startsWith("0x") || address.length() != 42) return false;
        String hex = address.substring(2);
        if (!hex.matches("[0-9a-fA-F]{40}")) return false;
        String lower = hex.toLowerCase();
        String checksummed = toChecksumAddress(lower).substring(2);
        return address.substring(2).equals(checksummed);
    }

    /** Keccak-256 */
    private static byte[] keccak256(byte[] input) {
        KeccakDigest k = new KeccakDigest(256);
        k.update(input, 0, input.length);
        byte[] out = new byte[32];
        k.doFinal(out, 0);
        return out;
    }
}

