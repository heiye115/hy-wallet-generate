package com.hy.wallet;

import com.hy.wallet.core.WalletGenerator;
import com.hy.wallet.model.WalletInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;

/**
 * 主程序入口类
 * 负责提供控制台交互菜单，支持生成单个或批量钱包，并按指定模板输出。
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * 应用程序入口方法。
     * 运行方式：java -jar hy-wallet-generate.jar
     * 提供两种功能：
     * 1) 生成1个钱包
     * 2) 批量生成钱包（输入生成数量）
     *
     * @param args 启动参数（不使用）
     */
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            WalletGenerator generator = new WalletGenerator();

            System.out.println("================= HY Web3 钱包生成工具 =================");
            System.out.println("请选择功能：");
            System.out.println("1. 生成1个钱包");
            System.out.println("2. 批量生成钱包（需输入生成数量）");
            System.out.println("3. 通过助记词生成钱包（输入12个英文单词）");
            System.out.print("请输入选项(1/2/3): ");

            String option = scanner.nextLine().trim();
            switch (option) {
                case "1" -> {
                    WalletInfo wallet = generator.generateOne();
                    printWallet(wallet, 1, true);
                }
                case "2" -> {
                    System.out.print("请输入生成数量(正整数): ");
                    String countStr = scanner.nextLine().trim();
                    int count = Integer.parseInt(countStr);
                    if (count <= 0) {
                        System.err.println("数量必须为正整数！");
                        return;
                    }
                    List<WalletInfo> wallets = generator.generateBatch(count);
                    for (int i = 0; i < wallets.size(); i++) {
                        printWallet(wallets.get(i), i + 1, false);
                    }
                }
                case "3" -> {
                    System.out.println("请输入12个英文助记词（单词之间使用单个空格分隔）:");
                    String line = scanner.nextLine().trim();
                    // 检查是否为单个空格分隔的12个词
                    if (!line.matches("(?i)^[a-z]+( [a-z]+){11}$")) {
                        System.err.println("输入格式错误：必须为12个英文单词，且使用单个空格分隔。");
                        return;
                    }
                    String[] parts = line.split(" ");
                    List<String> mnemonic = new java.util.ArrayList<>();
                    for (String p : parts)
                        mnemonic.add(p.toLowerCase());
                    // 使用bitcoinj进行BIP39严格校验（词表与校验和）
                    try {
                        org.bitcoinj.crypto.MnemonicCode.INSTANCE.check(mnemonic);
                    } catch (org.bitcoinj.crypto.MnemonicException.MnemonicLengthException e) {
                        System.err.println("助记词长度错误：" + e.getMessage());
                        return;
                    } catch (org.bitcoinj.crypto.MnemonicException.MnemonicWordException e) {
                        System.err.println("存在非BIP39标准英文单词：" + e.getMessage());
                        return;
                    } catch (org.bitcoinj.crypto.MnemonicException.MnemonicChecksumException e) {
                        System.err.println("助记词校验失败（checksum错误）：" + e.getMessage());
                        return;
                    } catch (Exception e) {
                        System.err.println("助记词校验异常：" + e.getMessage());
                        return;
                    }
                    WalletInfo wallet = generator.generateFromMnemonic(mnemonic);
                    printWallet(wallet, 1, true);
                }
                default -> System.err.println("无效的选项，程序结束。");
            }
        } catch (Exception e) {
            log.error("程序运行出现异常", e);
            System.err.println("发生错误: " + e.getMessage());
        }
    }

    /**
     * 按指定输出模板打印单个钱包信息。
     *
     * @param wallet           钱包信息对象
     * @param index            序号，从1开始
     * @param enableValidation 是否显示验证报告
     */
    private static void printWallet(WalletInfo wallet, int index, boolean enableValidation) {
        System.out.println("==========钱包(" + index + ")==========");
        System.out.println("1.您的助记词(12位): " + String.join(" ", wallet.getMnemonic()));
        System.out.println("2.BTC(Legacy)地址: " + wallet.getBtcLegacyAddress() + "  私钥: " + wallet.getBtcLegacyWif());
        System.out.println(
                "3.BTC(Native SegWit)地址: " + wallet.getBtcSegwitAddress() + " 私钥: " + wallet.getBtcSegwitWif());
        System.out.println("4.ETH(EVM通用)地址: " + wallet.getEthAddress() + "  私钥: " + wallet.getEthPrivateHex());
        System.out.println("5.SOL地址: " + wallet.getSolAddress() + "  私钥: " + wallet.getSolPrivate());
        System.out.println("6.TRON地址: " + wallet.getTronAddress() + "  私钥: " + wallet.getTronPrivateHex());

        // 追加严格验证报告
        if (enableValidation) {
            try {
                String report = com.hy.wallet.validation.Validator.validateWallet(wallet);
                System.out.println(report);
            } catch (Exception e) {
                System.out.println("[验证] 发生错误: " + e.getMessage());
            }
        }
    }
}
