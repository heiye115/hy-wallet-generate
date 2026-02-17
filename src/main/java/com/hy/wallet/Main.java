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
        Scanner scanner = new Scanner(System.in);
        WalletGenerator generator = new WalletGenerator();

        while (true) {
            try {
                System.out.println("================= HY Web3 钱包生成工具 =================");
                System.out.println("请选择功能：");
                System.out.println("1. 生成1个钱包");
                System.out.println("2. 批量生成钱包（需输入生成数量）");
                System.out.println("3. 通过助记词生成钱包（输入12个英文单词）");
                System.out.println("4. 派生指定索引的钱包（输入助记词和索引号）");
                System.out.println("5. 退出");
                System.out.print("请输入选项(1/2/3/4/5): ");

                String option = scanner.nextLine().trim();
                switch (option) {
                    case "1" -> {
                        WalletInfo wallet = generator.generateOne();
                        printWallet(wallet, 1, true);
                    }
                    case "2" -> {
                        System.out.print("请输入生成数量(正整数): ");
                        String countStr = scanner.nextLine().trim();
                        try {
                            int count = Integer.parseInt(countStr);
                            if (count <= 0) {
                                System.err.println("数量必须为正整数！");
                                break;
                            }
                            List<WalletInfo> wallets = generator.generateBatch(count);
                            for (int i = 0; i < wallets.size(); i++) {
                                printWallet(wallets.get(i), i + 1, false);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("输入无效，请输入正整数！");
                        }
                    }
                    case "3" -> {
                        System.out.println("请输入12个英文助记词（单词之间使用单个空格分隔）:");
                        String line = scanner.nextLine().trim();
                        List<String> mnemonic = parseAndValidateMnemonic(line);
                        if (mnemonic == null) break;
                        WalletInfo wallet = generator.generateFromMnemonic(mnemonic);
                        printWallet(wallet, 1, true);
                    }
                    case "4" -> {
                        System.out.println("请输入12个英文助记词（单词之间使用单个空格分隔）:");
                        String line = scanner.nextLine().trim();
                        List<String> mnemonic = parseAndValidateMnemonic(line);
                        if (mnemonic == null) break;

                        System.out.print("请输入地址索引号(非负整数): ");
                        String indexStr = scanner.nextLine().trim();
                        try {
                            int index = Integer.parseInt(indexStr);
                            if (index < 0) {
                                System.err.println("索引号必须为非负整数！");
                                break;
                            }
                            WalletInfo wallet = generator.generateFromMnemonic(mnemonic, index);
                            printWallet(wallet, 1, true);
                        } catch (NumberFormatException e) {
                            System.err.println("索引号格式错误！");
                        }
                    }
                    case "5", "退出" -> {
                        System.out.println("程序已退出。");
                        return;
                    }
                    default -> System.err.println("输入无效，请重新选择");
                }
                System.out.println(); // 换行，方便阅读
            } catch (Exception e) {
                log.error("程序运行出现异常", e);
                System.err.println("发生错误: " + e.getMessage());
            }
        }
    }

    /**
     * 解析并校验助记词
     * @param line 输入行
     * @return 助记词列表，校验失败返回null
     */
    private static List<String> parseAndValidateMnemonic(String line) {
        // 检查是否为单个空格分隔的12个词
        if (!line.matches("(?i)^[a-z]+( [a-z]+){11}$")) {
            System.err.println("输入格式错误：必须为12个英文单词，且使用单个空格分隔。/ Format error: Must be 12 English words separated by single spaces.");
            return null;
        }
        String[] parts = line.split(" ");
        List<String> mnemonic = new java.util.ArrayList<>();
        for (String p : parts)
            mnemonic.add(p.toLowerCase());
        // 使用bitcoinj进行BIP39严格校验（词表与校验和）
        try {
            org.bitcoinj.crypto.MnemonicCode.INSTANCE.check(mnemonic);
            return mnemonic;
        } catch (org.bitcoinj.crypto.MnemonicException.MnemonicLengthException e) {
            System.err.println("助记词长度错误：" + e.getMessage());
        } catch (org.bitcoinj.crypto.MnemonicException.MnemonicWordException e) {
            System.err.println("存在非BIP39标准英文单词：" + e.getMessage());
        } catch (org.bitcoinj.crypto.MnemonicException.MnemonicChecksumException e) {
            System.err.println("助记词校验失败（checksum错误）：" + e.getMessage());
        } catch (Exception e) {
            System.err.println("助记词校验异常：" + e.getMessage());
        }
        return null;
    }

    /**
     * 按指定输出模板打印单个钱包信息。
     *
     * @param wallet           钱包信息对象
     * @param index            序号，从1开始
     * @param enableValidation 是否显示验证报告
     */
    private static void printWallet(WalletInfo wallet, int index, boolean enableValidation) {
        String border = "=".repeat(80);
        String subBorder = "-".repeat(80);
        
        System.out.println(border);
        System.out.printf(" 🌟 钱包序号: %d%n", index);
        System.out.println(subBorder);
        
        // 助记词部分
        System.out.println(" [助记词 / Mnemonic]");
        System.out.println(" " + String.join(" ", wallet.getMnemonic()));
        System.out.println(subBorder);

        // 地址与私钥部分
        String format = " %-20s | %s%n";
        System.out.println(" [链 / Chain]          | [地址 / Address] & [私钥 / Private Key]");
        System.out.println(subBorder);
        
        printRow("BTC (Legacy)", wallet.getBtcLegacyAddress(), wallet.getBtcLegacyWif());
        printRow("BTC (SegWit)", wallet.getBtcSegwitAddress(), wallet.getBtcSegwitWif());
        printRow("ETH (EVM)", wallet.getEthAddress(), wallet.getEthPrivateHex());
        printRow("SOL (Solana)", wallet.getSolAddress(), wallet.getSolPrivate());
        printRow("TRON (TRC20)", wallet.getTronAddress(), wallet.getTronPrivateHex());
        
        System.out.println(border);

        // 追加严格验证报告
        if (enableValidation) {
            try {
                String report = com.hy.wallet.validation.Validator.validateWallet(wallet);
                System.out.println("\n [验证报告]");
                System.out.println(subBorder);
                System.out.println(report);
                System.out.println(border);
            } catch (Exception e) {
                System.out.println("[验证] 发生错误: " + e.getMessage());
            }
        }
    }

    private static void printRow(String chain, String address, String privateKey) {
        System.out.printf(" %-20s | Addr: %s%n", chain, address);
        System.out.printf(" %-20s | Priv: %s%n", "", privateKey);
        System.out.println("-".repeat(80)); // 每行之间的分隔符
    }
}
