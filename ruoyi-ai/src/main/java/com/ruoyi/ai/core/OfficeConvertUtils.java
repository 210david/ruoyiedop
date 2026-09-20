package com.ruoyi.ai.core;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.config.RuoYiConfig;

/**
 * Office 文档转 PDF 工具（服务端转换，用于在线预览保真渲染）
 *
 * 背景：前端 docx-preview 对复杂表格/分栏文档渲染保真度差（列宽错乱、文字竖排），
 * 改用服务端 LibreOffice 无头模式转 PDF，由浏览器 PDF 阅读器渲染，所见即所得。
 *
 * 约定：
 * - 转换在服务端执行，客户端无需安装任何控件
 * - 结果缓存到 uploadPath/convert/{docId}.pdf，首次预览转换，之后直接命中缓存
 * - 源文件更新（mtime 变化）时缓存自动失效重转
 * - LibreOffice 不可用时返回 null，调用方回退前端渲染，不影响功能可用性
 *
 * 环境变量（可选）：
 * - SOFFICE_PATH：soffice 可执行文件完整路径，未配置时按常见安装路径探测
 *
 * @author ruoyi
 */
public class OfficeConvertUtils
{
    private static final Logger log = LoggerFactory.getLogger(OfficeConvertUtils.class);

    /** 可转换为 PDF 的源格式（与前端预览类型判断保持一致） */
    public static final List<String> CONVERTIBLE_TYPES = List.of(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "rtf", "odt", "ods", "odp");

    /** 单次转换超时（秒），超时视为失败并回退前端渲染 */
    private static final int CONVERT_TIMEOUT_SECONDS = 120;

    /** 缓存目录名（位于 uploadPath 下） */
    private static final String CACHE_DIR = "convert";

    /** 探测到的 soffice 路径缓存（避免每次转换都探测） */
    private static String resolvedSoffice = null;
    private static boolean sofficeResolved = false;

    /** 该格式是否支持服务端转 PDF */
    public static boolean isConvertible(String fileType)
    {
        return fileType != null && CONVERTIBLE_TYPES.contains(fileType.toLowerCase());
    }

    /**
     * 获取（必要时生成）文档的 PDF 预览缓存
     *
     * @param docId    文档ID（用于缓存命名）
     * @param srcFile  源文件
     * @return 缓存 PDF 文件；不支持转换/转换失败/LibreOffice 不可用时返回 null
     */
    public static File getOrCreatePdf(Long docId, File srcFile)
    {
        if (docId == null || srcFile == null || !srcFile.exists())
        {
            return null;
        }
        try
        {
            Path cacheDir = Paths.get(RuoYiConfig.getProfile(), CACHE_DIR);
            if (!Files.exists(cacheDir))
            {
                Files.createDirectories(cacheDir);
            }
            File cached = cacheDir.resolve(docId + ".pdf").toFile();
            // 缓存有效：已存在且比源文件新
            if (cached.exists() && cached.length() > 0 && cached.lastModified() >= srcFile.lastModified())
            {
                return cached;
            }
            return convert(docId, srcFile, cacheDir.toFile());
        }
        catch (Exception e)
        {
            log.warn("文档转PDF预览失败（将回退前端渲染）docId={}: {}", docId, e.getMessage());
            return null;
        }
    }

    /** 执行转换：soffice --headless --convert-to pdf --outdir <tmp> <src> */
    private static File convert(Long docId, File srcFile, File cacheDir) throws Exception
    {
        String soffice = resolveSoffice();
        if (soffice == null)
        {
            log.warn("未找到 LibreOffice(soffice)，Office 文档将使用前端渲染预览");
            return null;
        }
        // 中转目录：LibreOffice 以源文件名输出，转换后重命名为 docId.pdf，避免中文/特殊字符问题
        Path tmpDir = Files.createTempDirectory("edop-convert-");
        try
        {
            List<String> cmd = new ArrayList<>();
            cmd.add(soffice);
            cmd.add("--headless");
            cmd.add("--norestore");
            cmd.add("--nolockcheck");
            cmd.add("--convert-to");
            cmd.add("pdf");
            cmd.add("--outdir");
            cmd.add(tmpDir.toAbsolutePath().toString());
            cmd.add(srcFile.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            // 及时读取输出，避免管道缓冲区写满导致进程阻塞
            StringBuilder out = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (out.length() < 2000)
                    {
                        out.append(line).append(' ');
                    }
                }
            }
            boolean finished = process.waitFor(CONVERT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished)
            {
                process.destroyForcibly();
                log.warn("文档转PDF超时({}s) docId={}", CONVERT_TIMEOUT_SECONDS, docId);
                return null;
            }
            if (process.exitValue() != 0)
            {
                log.warn("文档转PDF失败 docId={} exit={} output={}", docId, process.exitValue(), out);
                return null;
            }
            // 查找输出 PDF（文件名与源文件同名，扩展名 pdf）
            File produced = null;
            File[] files = tmpDir.toFile().listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));
            if (files != null && files.length > 0)
            {
                produced = files[0];
            }
            if (produced == null || !produced.exists() || produced.length() == 0)
            {
                log.warn("文档转PDF无输出文件 docId={} output={}", docId, out);
                return null;
            }
            File target = new File(cacheDir, docId + ".pdf");
            Files.copy(produced.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            // 缓存时间对齐源文件，便于后续按 mtime 判断失效
            target.setLastModified(Math.max(System.currentTimeMillis(), srcFile.lastModified()));
            log.info("文档转PDF预览成功 docId={} size={}KB", docId, target.length() / 1024);
            return target;
        }
        finally
        {
            deleteQuietly(tmpDir.toFile());
        }
    }

    /** 解析 soffice 可执行文件路径：环境变量优先，其次常见安装路径 */
    private static synchronized String resolveSoffice()
    {
        if (sofficeResolved)
        {
            return resolvedSoffice;
        }
        sofficeResolved = true;
        List<String> candidates = new ArrayList<>();
        String env = System.getenv("SOFFICE_PATH");
        if (env != null && !env.trim().isEmpty())
        {
            candidates.add(env.trim());
        }
        if (isWindows())
        {
            candidates.add("C:/Program Files/LibreOffice/program/soffice.exe");
            candidates.add("C:/Program Files (x86)/LibreOffice/program/soffice.exe");
            candidates.add("D:/Program Files/LibreOffice/program/soffice.exe");
            candidates.add("E:/DevTools/LibreOffice/program/soffice.exe");
            candidates.add("D:/DevTools/LibreOffice/program/soffice.exe");
        }
        else
        {
            candidates.add("/usr/bin/soffice");
            candidates.add("/usr/local/bin/soffice");
            candidates.add("/opt/libreoffice/program/soffice");
        }
        for (String c : candidates)
        {
            File f = new File(c);
            if (f.exists() && f.isFile())
            {
                resolvedSoffice = f.getAbsolutePath();
                log.info("LibreOffice 转换程序: {}", resolvedSoffice);
                return resolvedSoffice;
            }
        }
        resolvedSoffice = null;
        return null;
    }

    private static boolean isWindows()
    {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static void deleteQuietly(File file)
    {
        if (file == null || !file.exists())
        {
            return;
        }
        File[] children = file.listFiles();
        if (children != null)
        {
            for (File child : children)
            {
                deleteQuietly(child);
            }
        }
        if (!file.delete())
        {
            log.debug("临时文件删除失败: {}", file.getAbsolutePath());
        }
    }
}
