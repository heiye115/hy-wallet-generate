package com.hy.wallet.utils;

import java.security.SecureRandom;

/**
 * 加密与安全相关通用工具类
 * 提供：
 * - 安全随机数实例
 */
public class CryptoUtils {

    /**
     * 获取全局安全随机数生成器（强随机）
     * 
     * @return SecureRandom实例
     */
    public static SecureRandom secureRandom() {
        // 默认JDK实现为强随机源，满足真随机需求
        return new SecureRandom();
    }
}
