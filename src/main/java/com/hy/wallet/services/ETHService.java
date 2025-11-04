package com.hy.wallet.services;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import com.hy.wallet.utils.EthChecksum;

import java.math.BigInteger;

/**
 * ETH服务类
 * 使用BIP44路径 m/44'/60'/0'/0/0 派生secp256k1私钥，并计算标准以太坊地址。
 */
public class ETHService {

    /**
     * ETH地址与私钥封装
     * 
     * @param address    地址（0x前缀）
     * @param privateHex 私钥（16进制，0x前缀）
     */
    public record EthPair(String address, String privateHex) {
    }

    /**
     * 生成ETH地址与私钥
     * 
     * @param seedBytes BIP32种子
     * @return 地址与私钥
     */
    public static EthPair generateEth(byte[] seedBytes) {
        // 使用bitcoinj进行BIP32派生，获取最终私钥
        DeterministicKey root = HDKeyDerivation.createMasterPrivateKey(seedBytes);
        DeterministicKey purpose44 = HDKeyDerivation.deriveChildKey(root, new ChildNumber(44, true));
        DeterministicKey coinType60 = HDKeyDerivation.deriveChildKey(purpose44, new ChildNumber(60, true));
        DeterministicKey account0 = HDKeyDerivation.deriveChildKey(coinType60, new ChildNumber(0, true));
        DeterministicKey change0 = HDKeyDerivation.deriveChildKey(account0, new ChildNumber(0, false));
        DeterministicKey index0 = HDKeyDerivation.deriveChildKey(change0, new ChildNumber(0, false));

        BigInteger priv = new BigInteger(1, index0.getPrivKeyBytes());
        ECKeyPair ecKeyPair = ECKeyPair.create(priv);
        // 以web3j获得地址（小写无校验），再转换为EIP-55校验和地址
        String lower = Keys.getAddress(ecKeyPair.getPublicKey());
        String address = EthChecksum.toChecksumAddress(lower);
        // 私钥统一输出为0x前缀的64位小写hex（左侧补零）
        String privateHexNoPrefix = leftPad64(priv.toString(16));
        String privateHex = "0x" + privateHexNoPrefix;
        return new EthPair(address, privateHex);
    }

    /**
     * 左侧补零至64位hex字符串
     */
    private static String leftPad64(String hex) {
        String lower = hex.toLowerCase();
        return "0".repeat(Math.max(0, 64 - lower.length())) + lower;
    }
}
