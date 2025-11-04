package com.hy.wallet.services;

import com.hy.wallet.utils.Slip10Ed25519;
import org.bitcoinj.core.Base58;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

import java.util.Arrays;

/**
 * Solana服务类
 * 使用SLIP-0010 Ed25519硬化派生规则，路径 m/44'/501'/0'/0' 生成Keypair。
 * 地址即为Ed25519公钥的Base58表示；私钥以Base58格式输出（64字节密钥，含公钥部分）。
 */
public class SolanaService {

    /**
     * Solana地址与私钥封装
     * 
     * @param address        Base58地址（公钥）
     * @param privateEncoded Base58编码的64字节私钥（secret key）
     */
    public record SolPair(String address, String privateEncoded) {
    }

    /**
     * 生成Solana地址与私钥
     * 
     * @param seedBytes BIP32种子
     * @return 地址与私钥
     */
    public static SolPair generateSol(byte[] seedBytes) {
        // 路径：m/44'/501'/0'/0'
        Slip10Ed25519.Node master = Slip10Ed25519.master(seedBytes);
        Slip10Ed25519.Node m44 = Slip10Ed25519.deriveHardened(master, 44);
        Slip10Ed25519.Node c501 = Slip10Ed25519.deriveHardened(m44, 501);
        Slip10Ed25519.Node acc0 = Slip10Ed25519.deriveHardened(c501, 0);
        Slip10Ed25519.Node change0 = Slip10Ed25519.deriveHardened(acc0, 0);

        byte[] privKey32 = change0.getKey();
        Ed25519PrivateKeyParameters priv = new Ed25519PrivateKeyParameters(privKey32, 0);
        Ed25519PublicKeyParameters pub = priv.generatePublicKey();

        byte[] pubBytes = pub.getEncoded();
        String address = Base58.encode(pubBytes);

        // Solana常见的私钥导出为64字节（32私钥+32公钥），这里按此方式输出Base58，兼容常见工具
        byte[] secret64 = new byte[64];
        System.arraycopy(privKey32, 0, secret64, 0, 32);
        System.arraycopy(pubBytes, 0, secret64, 32, 32);

        String privateBase58 = Base58.encode(secret64);
        return new SolPair(address, privateBase58);
    }
}
