package com.ruoyi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 * 
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "ruoyi")
public class RuoYiConfig
{
    /** 项目名称 */
    private String name;

    /** 版本 */
    private String version;

    /** 版权年份 */
    private String copyrightYear;

    /** 上传路径 */
    private static String profile;

    /** 获取地址开关 */
    private static boolean addressEnabled;

    /** 验证码类型 */
    private static String captchaType;

    /** 文件加密密钥（用于上传文件加密存储，建议生产环境修改） */
    private static String fileEncryptionKey;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getCopyrightYear()
    {
        return copyrightYear;
    }

    public void setCopyrightYear(String copyrightYear)
    {
        this.copyrightYear = copyrightYear;
    }

    public static String getProfile()
    {
        return profile;
    }

    public void setProfile(String profile)
    {
        RuoYiConfig.profile = profile;
    }

    public static boolean isAddressEnabled()
    {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled)
    {
        RuoYiConfig.addressEnabled = addressEnabled;
    }

    public static String getCaptchaType() {
        return captchaType;
    }

    public void setCaptchaType(String captchaType) {
        RuoYiConfig.captchaType = captchaType;
    }

    public static String getFileEncryptionKey() {
        return fileEncryptionKey;
    }

    public void setFileEncryptionKey(String fileEncryptionKey) {
        RuoYiConfig.fileEncryptionKey = fileEncryptionKey;
    }

    /**
     * 获取导入上传路径
     */
    public static String getImportPath()
    {
        return getProfile() + "/import";
    }

    /**
     * 获取头像上传路径
     */
    public static String getAvatarPath()
    {
        return getProfile() + "/avatar";
    }

    /**
     * 获取下载路径
     */
    public static String getDownloadPath()
    {
        return getProfile() + "/download/";
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath()
    {
        return getProfile() + "/upload";
    }

    /**
     * 运行期动态更新上传根目录（管理端配置修改后调用）
     * 注意：仅更新内存值，持久化由调用方负责；路径合法性由调用方校验
     */
    public static void updateProfile(String newProfile)
    {
        RuoYiConfig.profile = newProfile;
    }

    /**
     * 启动自检：确保上传根目录存在且可写
     * 不存在则自动创建；不可写则抛异常，避免程序"静默"写到错误位置
     *
     * @throws IllegalStateException 目录无法创建或不可写时抛出
     */
    public static void validateAndPrepareProfile()
    {
        if (profile == null || profile.trim().isEmpty())
        {
            throw new IllegalStateException("[EDOP启动自检] 上传根目录(ruoyi.profile)未配置，请检查 application.yml 或环境变量 EDOP_UPLOAD_PATH");
        }
        java.io.File dir = new java.io.File(profile);
        if (!dir.exists())
        {
            boolean created = dir.mkdirs();
            if (!created)
            {
                throw new IllegalStateException("[EDOP启动自检] 上传根目录不存在且无法创建: " + profile
                        + "，请检查磁盘/权限，或修改 ruoyi.profile / 设置环境变量 EDOP_UPLOAD_PATH");
            }
        }
        else if (!dir.isDirectory())
        {
            throw new IllegalStateException("[EDOP启动自检] 上传根目录路径被占用（不是目录）: " + profile);
        }
        if (!dir.canWrite())
        {
            throw new IllegalStateException("[EDOP启动自检] 上传根目录不可写: " + profile
                    + "，请检查目录权限（运行服务的账号需具备读写权限）");
        }
    }
}
