# HY Web3 钱包生成工具

一个基于 Java 的多链（BTC、ETH、TRON、Solana）钱包生成与校验工具，支持随机生成、批量生成、以及通过 12 个英文助记词（BIP39）恢复生成，并输出严格的验证报告（包含以太坊 EIP-55 校验、地址与私钥格式检查等）。

---

## 1. 工具介绍

- 名称：HY Web3 钱包生成工具（hy-wallet-generate）
- 用途：
  - 快速生成多链钱包地址与私钥
  - 通过 12 个英文助记词恢复派生同样的钱包信息
  - 对生成结果进行格式与规范的严格校验并输出验证报告
- 主要功能：
  - 生成 1 个钱包
  - 批量生成多个钱包
  - 通过助记词（12 词）生成钱包
  - 验证报告：包括 EIP-55 检查、地址格式合规（BTC P2PKH/Bech32、ETH、TRON、SOL）、私钥长度与字符集规范、基础一致性自检
- 技术栈与核心依赖：
  - JDK 21
  - Maven（maven-shade-plugin 打包可执行 Fat JAR）
  - bitcoinj（BIP39/32/44/84，BTC 地址与 WIF）
  - web3j（ETH 地址与签名算法）
  - BouncyCastle（加密算法与 Keccak）
  - SLF4J（日志接口，当前版本固定为 1.7.36）
- 版本信息与维护状态：
  - 当前版本：v1.0.0（active）
  - 维护状态：活跃维护，后续将根据需求新增特性（如参数模式、CSV/JSON 导出、更多链支持等）

---

## 2. 使用方式

### 2.1 安装与配置

1. 安装 JDK 21（确保 `JAVA_HOME` 已正确设置，`java -version` 输出为 21 或以上）。
2. 安装 Maven 3.8+（推荐 3.9+）。
3. 克隆或下载本项目到本地目录（例如 `d:\java-works\hy-wallet-generate`）。
4. 在项目根目录执行打包命令：
   ```bash
   mvn -q -DskipTests package
   ```
   打包成功后，会在 `target/` 目录生成 `hy-wallet-generate.jar`。

### 2.2 运行命令与参数说明（交互式）

- 基本运行：
  ```bash
  java -jar target/hy-wallet-generate.jar
  ```
  控制台菜单：
  - 1. 生成1个钱包
  - 2. 批量生成钱包（需输入生成数量）
  - 3. 通过助记词生成钱包（输入12个英文单词）
  - 请输入选项(1/2/3):

- 选项说明：
  - 1：随机生成 1 个钱包，输出各链地址与私钥，同时附带验证报告。
  - 2：输入生成数量（正整数），批量生成并依次输出每个钱包的信息与验证报告。
  - 3：输入 12 个英文助记词（单词之间用单个空格分隔，大小写不敏感，统一按小写校验），校验通过后生成同样的钱包信息与验证报告。

- Windows 下将“选项”和“助记词”一并通过管道传入可能受 Shell 行为影响，建议交互式逐步输入。如果确需一次性输入，可使用 PowerShell Here-String：
  ```powershell
  @"
  3
  abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about
  "@ | java -jar target\hy-wallet-generate.jar
  ```

### 2.3 典型使用场景的示例代码（程序化调用）

> 以下示例演示如何在自定义 Java 程序中调用核心生成器。请确保以 JDK 21 编译，且已将本项目源码或产物纳入依赖。

```java
// 示例：程序化调用 WalletGenerator 生成一个钱包
// 说明：该代码片段仅为演示用途，需在同一代码库或正确引入依赖的情况下编译运行。

import com.hy.wallet.core.WalletGenerator;
import com.hy.wallet.model.WalletInfo;

public class ExampleGenerateOne {
    /**
     * 示例主方法：生成一个钱包并打印基本信息
     */
    public static void main(String[] args) {
        WalletGenerator generator = new WalletGenerator();
        WalletInfo wallet = generator.generateOne();
        // 打印部分信息（完整输出请参考 CLI 工具）
        System.out.println("Mnemonic: " + String.join(" ", wallet.getMnemonic()));
        System.out.println("ETH Address: " + wallet.getEthAddress());
        System.out.println("ETH Private: " + wallet.getEthPrivateHex());
    }
}
```

```java
// 示例：使用 12 个助记词生成钱包
// 助记词必须通过 BIP39 校验，WalletGenerator 会基于该助记词派生多链地址与私钥。

import com.hy.wallet.core.WalletGenerator;
import com.hy.wallet.model.WalletInfo;
import java.util.Arrays;

public class ExampleFromMnemonic {
    /**
     * 示例主方法：基于助记词生成钱包
     */
    public static void main(String[] args) {
        WalletGenerator generator = new WalletGenerator();
        // 统一使用小写助记词，确保为 12 词，单空格分隔
        var mnemonic = Arrays.asList(
            "abandon", "abandon", "abandon", "abandon", "abandon", "abandon",
            "abandon", "abandon", "abandon", "abandon", "abandon", "about"
        );
        WalletInfo wallet = generator.generateFromMnemonic(mnemonic);
        System.out.println("BTC Legacy: " + wallet.getBtcLegacyAddress());
        System.out.println("BTC WIF   : " + wallet.getBtcLegacyWif());
    }
}
```

### 2.4 构建与部署

- 本项目使用 `maven-shade-plugin` 打包为可执行的 Fat JAR（包含依赖），可在安装了 **Java 21** 的操作系统上直接运行（Windows/Linux/macOS）。
- 部署方式：分发 `target/hy-wallet-generate.jar` 文件并在目标环境执行：
  ```bash
  java -jar hy-wallet-generate.jar
  ```
- 日志：使用 SLF4J 1.7.36，默认输出到控制台。如果遇到 `StaticLoggerBinder` 相关警告，请确保 `slf4j-api` 版本与绑定器兼容（本项目已固定为 1.7.36）。

---

## 3. 注意事项

### 3.1 系统环境要求与兼容性

- JDK：必须为 Java 21（推荐），低版本 JDK 可能导致编译或运行异常。
- 操作系统：Windows/Linux/macOS（已在 Windows 环境验证）。
- 网络：生成与校验为本地计算操作，无需联网。

### 3.2 安全注意事项

- 私钥与助记词为极高敏感信息，请在离线、可信环境中运行，并妥善保管输出内容。
- 请勿将助记词或私钥复制到不可信的软件或网络服务中。
- 建议结合企业合规要求使用加密文件输出（后续版本将提供加密导出与参数模式）。
- 本工具不会将生成结果持久化到磁盘，除非在扩展功能中显式开启。

### 3.3 已知问题与限制

- 验证报告主要进行格式与规范层面的校验，不与链上状态交互（例如余额、UTXO、Nonce 不在校验范围）。
- Solana 私钥输出格式依赖当前实现的编码方式（项目内一致），若需特定格式（如 64 字节十六进制或 base58），可在后续版本中按需调整与扩展。
- Windows 下通过管道一次性输入“选项 + 助记词”可能受 Shell 行为差异影响，建议交互式输入或使用 PowerShell Here-String。
- 若出现 SLF4J `StaticLoggerBinder` 警告，请确认依赖版本或使用本项目的固定版本配置；若自行改动依赖版本，需要确保绑定器与 API 对齐。

### 3.4 故障排除与常见问题（FAQ）

- 问：运行提示“无效的选项，程序结束。”
  - 答：该提示通常因为未按菜单输入或未提供后续输入。请按菜单输入 1/2/3，并按提示继续输入参数或助记词。

- 问：助记词校验失败（长度/词表/校验和错误）如何处理？
  - 答：
    - 长度错误：确保正好为 12 个英文单词。
    - 词表错误：确保所有单词均在 BIP39 英文标准词表中。
    - 校验和错误：请重新确认助记词的正确性（大小写不敏感，但工具统一以小写校验）。

- 问：如何批量生成并保存到文件？
  - 答：当前版本为交互式输出，后续将提供命令行参数与 CSV/JSON 导出功能。可暂时将控制台输出重定向到文件保存：
    ```bash
    java -jar target/hy-wallet-generate.jar > wallets.txt
    ```

- 问：出现 `SecurityException: Invalid signature file digest` 问题怎么办？
  - 答：该问题一般源于可执行 JAR 中的签名文件与依赖混合导致。项目已在打包阶段通过 `maven-shade-plugin` 排除签名文件；如自行变更打包配置，请确保仍然排除 `META-INF/*.SF`、`META-INF/*.DSA`、`META-INF/*.RSA`。

- 问：出现 SLF4J 相关警告或日志不输出？
  - 答：请确保 `slf4j-api` 与绑定器版本一致，建议使用本项目固定的 `1.7.36`。重新执行：
    ```bash
    mvn -q -DskipTests package
    ```

---

## 4. 项目结构（简要）

> 目录结构仅展示关键位置。

```
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/hy/wallet/
                ├── Main.java                  // 控制台交互入口，包含菜单与输出
                ├── core/WalletGenerator.java // 核心生成器：随机、批量、助记词生成
                ├── model/WalletInfo.java     // 钱包信息模型
                ├── services/                 // 各链服务（BTC/ETH/TRON/SOL）
                ├── utils/                    // 校验与加密相关工具（例如 EIP-55 校验）
                └── validation/Validator.java // 严格校验并输出验证报告
```

---

## 5. 路线图（Roadmap）

- 命令行参数模式（如 `--mnemonic "..."`、`--count N`）
- CSV/JSON 导出（含验证报告）
- 更严格的地址/私钥校验（深度反向推导、更多链支持）
- 单元测试与持续集成

---

## 6. 许可证

- License：待定（TBD）。如需开源发布，请补充相应许可证（如 MIT、Apache-2.0 等）。

---

## 7. 反馈与支持

- 问题反馈：可在项目 Issue 中提交详细复现步骤与系统环境信息。
- 功能建议：欢迎提出新链支持、导出格式与自动化审计需求。
