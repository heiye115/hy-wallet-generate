package com.hy.wallet.services;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
// 使用ECKey自带方法输出WIF，无需依赖DumpedPrivateKey类

/**
 * BTC服务类
 * 负责使用BIP32/BIP44派生路径生成：
 * - Legacy (P2PKH) 地址：m/44'/0'/0'/0/0
 * - Native SegWit (P2WPKH/Bech32) 地址：m/84'/0'/0'/0/0
 */
public class BTCService {

    /**
     * 封装BTC地址与WIF私钥
     * @param address 地址
     * @param wif WIF格式私钥
     */
    public record BtcPair(String address, String wif) {}

    private static final NetworkParameters MAIN = MainNetParams.get();

    /**
     * 生成Legacy (P2PKH) 地址与私钥（WIF）
     * @param seedBytes BIP32种子字节
     * @return 地址与WIF私钥
     */
    public static BtcPair generateLegacy(byte[] seedBytes) {
        // m/44'/0'/0'/0/0
        DeterministicKey root = HDKeyDerivation.createMasterPrivateKey(seedBytes);
        DeterministicKey purpose44 = HDKeyDerivation.deriveChildKey(root, new ChildNumber(44, true));
        DeterministicKey coinType0 = HDKeyDerivation.deriveChildKey(purpose44, new ChildNumber(0, true));
        DeterministicKey account0 = HDKeyDerivation.deriveChildKey(coinType0, new ChildNumber(0, true));
        DeterministicKey change0 = HDKeyDerivation.deriveChildKey(account0, new ChildNumber(0, false));
        DeterministicKey index0 = HDKeyDerivation.deriveChildKey(change0, new ChildNumber(0, false));

        ECKey ecKey = ECKey.fromPrivate(index0.getPrivKeyBytes());
        String address = LegacyAddress.fromKey(MAIN, ecKey).toString();
        String wif = ecKey.getPrivateKeyAsWiF(MAIN);
        return new BtcPair(address, wif);
    }

    /**
     * 生成Native SegWit (Bech32/P2WPKH) 地址与私钥（WIF）
     * @param seedBytes BIP32种子字节
     * @return 地址与WIF私钥
     */
    public static BtcPair generateSegwit(byte[] seedBytes) {
        // m/84'/0'/0'/0/0
        DeterministicKey root = HDKeyDerivation.createMasterPrivateKey(seedBytes);
        DeterministicKey purpose84 = HDKeyDerivation.deriveChildKey(root, new ChildNumber(84, true));
        DeterministicKey coinType0 = HDKeyDerivation.deriveChildKey(purpose84, new ChildNumber(0, true));
        DeterministicKey account0 = HDKeyDerivation.deriveChildKey(coinType0, new ChildNumber(0, true));
        DeterministicKey change0 = HDKeyDerivation.deriveChildKey(account0, new ChildNumber(0, false));
        DeterministicKey index0 = HDKeyDerivation.deriveChildKey(change0, new ChildNumber(0, false));

        ECKey ecKey = ECKey.fromPrivate(index0.getPrivKeyBytes());
        String address = SegwitAddress.fromKey(MAIN, ecKey).toString();
        String wif = ecKey.getPrivateKeyAsWiF(MAIN);
        return new BtcPair(address, wif);
    }
}
