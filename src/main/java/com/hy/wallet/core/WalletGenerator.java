package com.hy.wallet.core;

import com.hy.wallet.model.WalletInfo;
import com.hy.wallet.services.BTCService;
import com.hy.wallet.services.ETHService;
import com.hy.wallet.services.SolanaService;
import com.hy.wallet.services.TronService;
import com.hy.wallet.utils.CryptoUtils;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.wallet.DeterministicSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * 钱包生成核心类
 * 负责：
 * 1. 使用SecureRandom生成128比特熵并生成12词BIP39助记词
 * 2. 从助记词派生BIP32种子
 * 3. 调用各链服务生成地址与私钥
 */
public class WalletGenerator {
    private static final Logger log = LoggerFactory.getLogger(WalletGenerator.class);

    /**
     * 生成单个钱包。
     * @return 封装好的钱包信息
     */
    public WalletInfo generateOne() {
        try {
            // 1) 生成128位熵（12词）
            byte[] entropy = new byte[16];
            SecureRandom sr = CryptoUtils.secureRandom();
            sr.nextBytes(entropy);

            // 2) BIP39助记词
            List<String> mnemonic = MnemonicCode.INSTANCE.toMnemonic(entropy);

            // 3) BIP32种子（空口令）
            DeterministicSeed seed = new DeterministicSeed(mnemonic, null, "", 0L);
            byte[] seedBytes = seed.getSeedBytes();

            // 4) 生成各链地址与私钥 (默认index=0)
            WalletInfo info = new WalletInfo();
            info.setMnemonic(mnemonic);

            // BTC：Legacy & Native SegWit
            BTCService.BtcPair legacy = BTCService.generateLegacy(seedBytes, 0);
            BTCService.BtcPair segwit = BTCService.generateSegwit(seedBytes, 0);
            info.setBtcLegacyAddress(legacy.address());
            info.setBtcLegacyWif(legacy.wif());
            info.setBtcSegwitAddress(segwit.address());
            info.setBtcSegwitWif(segwit.wif());

            // ETH
            ETHService.EthPair ethPair = ETHService.generateEth(seedBytes, 0);
            info.setEthAddress(ethPair.address());
            info.setEthPrivateHex(ethPair.privateHex());

            // SOL
            SolanaService.SolPair solPair = SolanaService.generateSol(seedBytes, 0);
            info.setSolAddress(solPair.address());
            info.setSolPrivate(solPair.privateEncoded());

            // TRON
            TronService.TronPair tronPair = TronService.generateTron(seedBytes, 0);
            info.setTronAddress(tronPair.address());
            info.setTronPrivateHex(tronPair.privateHex());

            return info;
        } catch (Exception e) {
            log.error("生成钱包失败", e);
            throw new RuntimeException("生成钱包失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量生成多个钱包。
     * @param count 生成数量
     * @return 钱包列表
     */
    public List<WalletInfo> generateBatch(int count) {
        List<WalletInfo> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(generateOne());
        }
        return list;
    }

    /**
     * 使用用户提供的12词助记词生成钱包（BIP39校验应在调用前完成）。
     * @param mnemonic 12个英文助记词（规范化为小写）
     * @return 钱包信息
     */
    public WalletInfo generateFromMnemonic(List<String> mnemonic) {
        return generateFromMnemonic(mnemonic, 0);
    }

    /**
     * 使用用户提供的12词助记词生成指定索引的钱包（BIP39校验应在调用前完成）。
     * @param mnemonic 12个英文助记词（规范化为小写）
     * @param index 地址索引
     * @return 钱包信息
     */
    public WalletInfo generateFromMnemonic(List<String> mnemonic, int index) {
        try {
            // BIP32种子（空口令）
            DeterministicSeed seed = new DeterministicSeed(mnemonic, null, "", 0L);
            byte[] seedBytes = seed.getSeedBytes();

            WalletInfo info = new WalletInfo();
            info.setMnemonic(mnemonic);

            // BTC：Legacy & Native SegWit
            BTCService.BtcPair legacy = BTCService.generateLegacy(seedBytes, index);
            BTCService.BtcPair segwit = BTCService.generateSegwit(seedBytes, index);
            info.setBtcLegacyAddress(legacy.address());
            info.setBtcLegacyWif(legacy.wif());
            info.setBtcSegwitAddress(segwit.address());
            info.setBtcSegwitWif(segwit.wif());

            // ETH
            ETHService.EthPair ethPair = ETHService.generateEth(seedBytes, index);
            info.setEthAddress(ethPair.address());
            info.setEthPrivateHex(ethPair.privateHex());

            // SOL
            SolanaService.SolPair solPair = SolanaService.generateSol(seedBytes, index);
            info.setSolAddress(solPair.address());
            info.setSolPrivate(solPair.privateEncoded());

            // TRON
            TronService.TronPair tronPair = TronService.generateTron(seedBytes, index);
            info.setTronAddress(tronPair.address());
            info.setTronPrivateHex(tronPair.privateHex());

            return info;
        } catch (Exception e) {
            log.error("基于助记词生成钱包失败", e);
            throw new RuntimeException("基于助记词生成钱包失败: " + e.getMessage(), e);
        }
    }
}
