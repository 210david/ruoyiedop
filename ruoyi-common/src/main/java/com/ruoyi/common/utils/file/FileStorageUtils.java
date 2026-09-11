package com.ruoyi.common.utils.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.config.RuoYiConfig;

/**
 * 文件存储安全工具类
 * <p>
 * 上传流程：原始文件 → GZIP压缩 → AES加密 → 写入磁盘（.enc）
 * 读取流程：.enc文件 → AES解密 → GZIP解压 → 输出原始内容
 * <p>
 * 磁盘上的文件为加密压缩格式，直接打开是乱码，无法被第三方工具识别。
 * 加密密钥配置在 application.yml 的 ruoyi.fileEncryptionKey 中。
 *
 * @author ruoyi
 */
public class FileStorageUtils
{
    private static final Logger log = LoggerFactory.getLogger(FileStorageUtils.class);

    /** 加密算法 */
    private static final String ALGORITHM = "AES";

    /** 加密文件后缀 */
    private static final String ENCRYPTED_SUFFIX = ".enc";

    /** 文件头魔数（用于标识本工具加密的文件） */
    private static final byte[] MAGIC_HEADER = "EDOPENC".getBytes(StandardCharsets.UTF_8);

    /** 版本号（用于未来格式升级兼容） */
    private static final byte VERSION = 1;

    /** 默认密钥（当配置未指定时使用，建议生产环境必须配置） */
    private static final String DEFAULT_KEY = "EDOP2026SecureKey";

    /**
     * 获取AES加密密钥
     * 从 RuoYiConfig 中读取 fileEncryptionKey 配置，未配置则使用默认密钥
     */
    private static SecretKeySpec getSecretKey()
    {
        String key = RuoYiConfig.getFileEncryptionKey();
        if (key == null || key.length() < 16)
        {
            key = DEFAULT_KEY;
        }
        // AES-128 需要16字节密钥，取前16字节
        byte[] keyBytes = new byte[16];
        byte[] srcBytes = key.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(srcBytes, 0, keyBytes, 0, Math.min(srcBytes.length, 16));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * 加密并压缩文件数据（上传时使用）
     * 格式：[魔数7B][版本1B][AES加密(GZIP压缩(原始数据))]
     *
     * @param data 原始文件字节数据
     * @return 加密压缩后的字节数据
     */
    public static byte[] encryptAndCompress(byte[] data) throws IOException
    {
        try
        {
            // 1. GZIP 压缩
            byte[] compressed = gzipCompress(data);

            // 2. AES 加密
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encrypted = cipher.doFinal(compressed);

            // 3. 组装输出：魔数 + 版本 + 加密数据
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(MAGIC_HEADER);
            out.write(VERSION);
            out.write(encrypted);
            return out.toByteArray();
        }
        catch (Exception e)
        {
            log.error("文件加密压缩失败", e);
            throw new IOException("文件加密失败", e);
        }
    }

    /**
     * 解密并解压文件数据（读取时使用）
     * 格式：[魔数7B][版本1B][AES加密(GZIP压缩(原始数据))]
     *
     * @param inputData 加密压缩的文件数据
     * @return 解密解压后的原始文件数据
     */
    public static byte[] decryptAndDecompress(byte[] inputData) throws IOException
    {
        try
        {
            // 0. 校验最小长度
            if (inputData == null || inputData.length < MAGIC_HEADER.length + 1)
            {
                throw new IOException("文件数据格式无效");
            }

            // 1. 校验魔数
            for (int i = 0; i < MAGIC_HEADER.length; i++)
            {
                if (inputData[i] != MAGIC_HEADER[i])
                {
                    throw new IOException("文件格式不匹配（非本系统加密文件）");
                }
            }

            // 2. 读取版本
            int version = inputData[MAGIC_HEADER.length] & 0xFF;
            if (version != VERSION)
            {
                throw new IOException("不支持的文件版本: " + version);
            }

            // 3. 提取加密数据
            int offset = MAGIC_HEADER.length + 1;
            byte[] encrypted = new byte[inputData.length - offset];
            System.arraycopy(inputData, offset, encrypted, 0, encrypted.length);

            // 4. AES 解密
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] compressed = cipher.doFinal(encrypted);

            // 5. GZIP 解压
            return gzipDecompress(compressed);
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("文件解密解压失败", e);
            throw new IOException("文件解密失败", e);
        }
    }

    /**
     * 将输入流加密压缩后写入输出流（上传时使用）
     *
     * @param input 原始文件输入流
     * @param output 写入磁盘的输出流
     */
    public static void encryptStream(InputStream input, OutputStream output) throws IOException
    {
        byte[] data = input.readAllBytes();
        byte[] encrypted = encryptAndCompress(data);
        output.write(encrypted);
        output.flush();
    }

    /**
     * 从输入流读取加密数据，解密解压后写入输出流（读取时使用）
     *
     * @param input 磁盘文件输入流
     * @param output 浏览器输出流
     */
    public static void decryptStream(InputStream input, OutputStream output) throws IOException
    {
        byte[] inputData = input.readAllBytes();
        byte[] decrypted = decryptAndDecompress(inputData);
        output.write(decrypted);
        output.flush();
    }

    /**
     * 判断文件是否为本系统加密的文件
     * 通过检查文件头魔数判断
     *
     * @param data 文件前几字节数据
     * @return 是否为加密文件
     */
    public static boolean isEncryptedFile(byte[] data)
    {
        if (data == null || data.length < MAGIC_HEADER.length)
        {
            return false;
        }
        for (int i = 0; i < MAGIC_HEADER.length; i++)
        {
            if (data[i] != MAGIC_HEADER[i])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * GZIP 压缩
     */
    private static byte[] gzipCompress(byte[] data) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out))
        {
            gzip.write(data);
        }
        return out.toByteArray();
    }

    /**
     * GZIP 解压
     */
    private static byte[] gzipDecompress(byte[] compressed) throws IOException
    {
        try (ByteArrayInputStream in = new ByteArrayInputStream(compressed);
             GZIPInputStream gzip = new GZIPInputStream(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = gzip.read(buffer)) > 0)
            {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        }
    }
}
