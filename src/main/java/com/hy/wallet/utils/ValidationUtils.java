package com.hy.wallet.utils;

import java.util.regex.Pattern;

/**
 * 地址格式校验工具类
 * 基于简单正则对各链地址进行格式校验（不涉具体校验码），用于基本自检。
 */
public class ValidationUtils {
    private static final Pattern BTC_P2PKH = Pattern.compile("^[13][a-km-zA-HJ-NP-Z1-9]{25,34}$");
    private static final Pattern BTC_BECH32 = Pattern.compile("^(bc1)[0-9a-z]{25,62}$");
    private static final Pattern ETH = Pattern.compile("^(0x)[0-9a-fA-F]{40}$");
    private static final Pattern TRON = Pattern.compile("^T[1-9A-HJ-NP-Za-km-z]{33}$");
    private static final Pattern SOL = Pattern.compile("^[1-9A-HJ-NP-Za-km-z]{32,44}$");

    /** BTC P2PKH 简单校验 */
    public static boolean isValidBtcP2pkh(String addr) { return BTC_P2PKH.matcher(addr).matches(); }
    /** BTC Bech32 简单校验 */
    public static boolean isValidBtcBech32(String addr) { return BTC_BECH32.matcher(addr).matches(); }
    /** ETH 地址校验 */
    public static boolean isValidEth(String addr) { return ETH.matcher(addr).matches(); }
    /** TRON 地址校验 */
    public static boolean isValidTron(String addr) { return TRON.matcher(addr).matches(); }
    /** Solana 地址校验 */
    public static boolean isValidSol(String addr) { return SOL.matcher(addr).matches(); }
}

