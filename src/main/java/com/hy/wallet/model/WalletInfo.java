package com.hy.wallet.model;

import java.util.List;

/**
 * 钱包信息模型类
 * 封装单个钱包的助记词、各链地址以及对应私钥/密钥格式。
 */
public class WalletInfo {
    // 助记词（12个英文单词）
    private List<String> mnemonic;

    // BTC - Legacy (P2PKH)
    private String btcLegacyAddress;
    private String btcLegacyWif;

    // BTC - Native SegWit (Bech32 / P2WPKH)
    private String btcSegwitAddress;
    private String btcSegwitWif;

    // ETH
    private String ethAddress;
    private String ethPrivateHex;

    // Solana
    private String solAddress;
    private String solPrivate;

    // Tron
    private String tronAddress;
    private String tronPrivateHex;

    /**
     * 获取助记词
     * @return 12个助记词单词列表
     */
    public List<String> getMnemonic() {
        return mnemonic;
    }

    /**
     * 设置助记词
     * @param mnemonic 助记词列表
     */
    public void setMnemonic(List<String> mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getBtcLegacyAddress() {
        return btcLegacyAddress;
    }

    public void setBtcLegacyAddress(String btcLegacyAddress) {
        this.btcLegacyAddress = btcLegacyAddress;
    }

    public String getBtcLegacyWif() {
        return btcLegacyWif;
    }

    public void setBtcLegacyWif(String btcLegacyWif) {
        this.btcLegacyWif = btcLegacyWif;
    }

    public String getBtcSegwitAddress() {
        return btcSegwitAddress;
    }

    public void setBtcSegwitAddress(String btcSegwitAddress) {
        this.btcSegwitAddress = btcSegwitAddress;
    }

    public String getBtcSegwitWif() {
        return btcSegwitWif;
    }

    public void setBtcSegwitWif(String btcSegwitWif) {
        this.btcSegwitWif = btcSegwitWif;
    }

    public String getEthAddress() {
        return ethAddress;
    }

    public void setEthAddress(String ethAddress) {
        this.ethAddress = ethAddress;
    }

    public String getEthPrivateHex() {
        return ethPrivateHex;
    }

    public void setEthPrivateHex(String ethPrivateHex) {
        this.ethPrivateHex = ethPrivateHex;
    }

    public String getSolAddress() {
        return solAddress;
    }

    public void setSolAddress(String solAddress) {
        this.solAddress = solAddress;
    }

    public String getSolPrivate() {
        return solPrivate;
    }

    public void setSolPrivate(String solPrivate) {
        this.solPrivate = solPrivate;
    }

    public String getTronAddress() {
        return tronAddress;
    }

    public void setTronAddress(String tronAddress) {
        this.tronAddress = tronAddress;
    }

    public String getTronPrivateHex() {
        return tronPrivateHex;
    }

    public void setTronPrivateHex(String tronPrivateHex) {
        this.tronPrivateHex = tronPrivateHex;
    }
}

