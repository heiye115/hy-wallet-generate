package com.hy.wallet.validation;

import com.hy.wallet.model.WalletInfo;
import com.hy.wallet.utils.EthChecksum;
import com.hy.wallet.utils.ValidationUtils;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Base58;
import org.bitcoinj.params.MainNetParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 钱包校验器
 * 执行各链地址与私钥的严格校验（包含以太坊EIP-55校验和），并生成详细验证报告。
 */
public class Validator {

    /**
     * 执行对单个钱包的全面校验，并返回报告文本。
     * @param wallet 钱包信息
     * @return 验证报告文本
     */
    public static String validateWallet(WalletInfo wallet) {
        List<String> report = new ArrayList<>();
        report.add("---- 验证报告 ----");

        // 1) 助记词数量与单词校验
        List<String> m = wallet.getMnemonic();
        boolean mnemonicOk = (m != null && m.size() == 12);
        report.add("[助记词] 数量=12 : " + (mnemonicOk ? "通过" : "不通过"));

        // 2) BTC Legacy 地址与WIF
        report.addAll(validateBtcLegacy(wallet.getBtcLegacyAddress(), wallet.getBtcLegacyWif()));

        // 3) BTC SegWit 地址与WIF
        report.addAll(validateBtcSegwit(wallet.getBtcSegwitAddress(), wallet.getBtcSegwitWif()));

        // 4) ETH 地址与私钥
        report.addAll(validateEth(wallet.getEthAddress(), wallet.getEthPrivateHex()));

        // 5) SOL 地址与私钥（Base58 64字节）
        report.addAll(validateSol(wallet.getSolAddress(), wallet.getSolPrivate()));

        // 6) TRON 地址与私钥
        report.addAll(validateTron(wallet.getTronAddress(), wallet.getTronPrivateHex()));

        return String.join(System.lineSeparator(), report);
    }

    /** BTC Legacy 校验 */
    private static List<String> validateBtcLegacy(String address, String wif) {
        List<String> lines = new ArrayList<>();
        lines.add("[BTC Legacy] 地址格式校验: " + (ValidationUtils.isValidBtcP2pkh(address) ? "通过" : "不通过"));
        // 解析地址（bitcoinj进行严格解析）
        boolean parseOk;
        try { Address.fromString(MainNetParams.get(), address); parseOk = true; } catch (Exception e) { parseOk = false; }
        lines.add("[BTC Legacy] 地址解析: " + (parseOk ? "通过" : "不通过"));
        // WIF校验
        lines.add("[BTC Legacy] 私钥WIF校验: " + (isValidWif(wif) ? "通过" : "不通过"));
        return lines;
    }

    /** BTC SegWit 校验 */
    private static List<String> validateBtcSegwit(String address, String wif) {
        List<String> lines = new ArrayList<>();
        lines.add("[BTC SegWit] 地址格式校验: " + (ValidationUtils.isValidBtcBech32(address) ? "通过" : "不通过"));
        boolean parseOk;
        try { Address.fromString(MainNetParams.get(), address); parseOk = true; } catch (Exception e) { parseOk = false; }
        lines.add("[BTC SegWit] 地址解析: " + (parseOk ? "通过" : "不通过"));
        lines.add("[BTC SegWit] 私钥WIF校验: " + (isValidWif(wif) ? "通过" : "不通过"));
        return lines;
    }

    /** ETH 校验（包含EIP-55）*/
    private static List<String> validateEth(String address, String privateHex) {
        List<String> lines = new ArrayList<>();
        // EIP-55校验
        boolean eip55 = EthChecksum.isValidChecksumAddress(address);
        lines.add("[ETH] EIP-55校验和地址: " + (eip55 ? "通过" : "不通过"));
        // 格式校验（0x + 40hex）
        lines.add("[ETH] 地址正则校验: " + (ValidationUtils.isValidEth(address) ? "通过" : "不通过"));
        // 私钥校验：0x + 64位小写hex
        boolean privFmt = isHex64LowerWith0x(privateHex);
        lines.add("[ETH] 私钥格式(0x+64位小写hex): " + (privFmt ? "通过" : "不通过"));
        return lines;
    }

    /** Solana 校验 */
    private static List<String> validateSol(String address, String privateBase58) {
        List<String> lines = new ArrayList<>();
        lines.add("[SOL] 地址正则校验(Base58): " + (ValidationUtils.isValidSol(address) ? "通过" : "不通过"));
        // 私钥Base58长度校验（解码后应为64字节）
        boolean privOk;
        try { byte[] dec = Base58.decode(privateBase58); privOk = (dec.length == 64); } catch (Exception e) { privOk = false; }
        lines.add("[SOL] 私钥Base58长度(64字节): " + (privOk ? "通过" : "不通过"));
        return lines;
    }

    /** TRON 校验 */
    private static List<String> validateTron(String address, String privateHex) {
        List<String> lines = new ArrayList<>();
        lines.add("[TRON] 地址正则校验(Base58): " + (ValidationUtils.isValidTron(address) ? "通过" : "不通过"));
        // 私钥校验：0x + 64位小写hex
        boolean privFmt = isHex64LowerWith0x(privateHex);
        lines.add("[TRON] 私钥格式(0x+64位小写hex): " + (privFmt ? "通过" : "不通过"));
        return lines;
    }

    /** 校验WIF格式（Base58Check验证 + 版本字节0x80） */
    private static boolean isValidWif(String wif) {
        try {
            byte[] all = Base58.decode(wif);
            if (all.length != 37 && all.length != 38) return false; // 1+32+4 或 1+32+1+4
            int payloadLen = all.length - 4;
            byte[] payload = Arrays.copyOfRange(all, 0, payloadLen);
            byte[] checksum = Arrays.copyOfRange(all, payloadLen, all.length);
            byte[] cs = doubleSha256(payload);
            for (int i = 0; i < 4; i++) if (cs[i] != checksum[i]) return false;
            // 版本字节应为0x80（主网）
            return (payload[0] & 0xFF) == 0x80;
        } catch (Exception e) {
            return false;
        }
    }

    /** 对数据执行两次SHA-256 */
    private static byte[] doubleSha256(byte[] input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] first = md.digest(input);
            return md.digest(first);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 私钥是否为0x前缀+64位小写hex */
    private static boolean isHex64LowerWith0x(String hex) {
        if (hex == null || !hex.startsWith("0x")) return false;
        String s = hex.substring(2);
        return s.matches("[0-9a-f]{64}");
    }
}

