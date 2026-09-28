package com.ruoyi.web.controller.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.LimitType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileStorageUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import com.ruoyi.framework.config.ServerConfig;

/**
 * 通用请求处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/common")
public class CommonController
{
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private ServerConfig serverConfig;

    private static final String FILE_DELIMITER = ",";

    /**
     * 通用下载请求
     * 
     * @param fileName 文件名称
     * @param delete 是否删除
     */
    @GetMapping("/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request)
    {
        try
        {
            if (!FileUtils.checkAllowDownload(fileName))
            {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = RuoYiConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete)
            {
                FileUtils.deleteFile(filePath);
            }
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求（单个）
     */
    @RateLimiter(time = 60, count = 100, limitType = LimitType.IP)
    @PostMapping("/upload")
    public AjaxResult uploadFile(MultipartFile file) throws Exception
    {
        try
        {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", fileName);
            ajax.put("newFileName", FileUtils.getName(fileName));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 通用上传请求（多个）
     */
    @PostMapping("/uploads")
    public AjaxResult uploadFiles(List<MultipartFile> files) throws Exception
    {
        try
        {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            List<String> urls = new ArrayList<String>();
            List<String> fileNames = new ArrayList<String>();
            List<String> newFileNames = new ArrayList<String>();
            List<String> originalFilenames = new ArrayList<String>();
            for (MultipartFile file : files)
            {
                // 上传并返回新文件名称
                String fileName = FileUploadUtils.upload(filePath, file);
                String url = serverConfig.getUrl() + fileName;
                urls.add(url);
                fileNames.add(fileName);
                newFileNames.add(FileUtils.getName(fileName));
                originalFilenames.add(file.getOriginalFilename());
            }
            AjaxResult ajax = AjaxResult.success();
            ajax.put("urls", StringUtils.join(urls, FILE_DELIMITER));
            ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMITER));
            ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMITER));
            ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMITER));
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     * 文件已加密存储，下载时需解密解压
     */
    @GetMapping("/download/resource")
    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
        FileInputStream fis = null;
        try
        {
            if (!FileUtils.checkAllowDownload(resource))
            {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            // 本地资源路径
            String localPath = RuoYiConfig.getProfile();
            // 数据库资源地址
            String downloadPath = localPath + FileUtils.stripPrefix(resource);
            // 下载名称
            String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            // 解密解压后输出
            fis = new FileInputStream(downloadPath);
            OutputStream os = response.getOutputStream();
            FileStorageUtils.decryptStream(fis, os);
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
        finally
        {
            IOUtils.closeQuietly(fis);
        }
    }

    /**
     * 安全的文件访问接口（需登录认证）
     * 替代原先 /profile/** 静态资源直接映射，所有上传文件的访问必须经过此接口
     * 支持 /profile/upload/2024/01/01/xxx.pdf 格式的资源路径
     * 图片/PDF 返回 inline（可内联预览），其他文件返回 attachment（强制下载）
     *
     * @param resource 资源路径，如 /profile/upload/2024/01/01/xxx.pdf
     */
    @GetMapping("/file/access")
    public void fileAccess(String resource, HttpServletResponse response)
    {
        FileInputStream fis = null;
        try
        {
            // 1. 安全校验：禁止目录穿越
            if (StringUtils.isEmpty(resource) || resource.contains(".."))
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                log.warn("非法文件访问请求: {}", resource);
                return;
            }

            // 2. 安全校验：必须以 /profile/ 开头
            String normalized = resource.startsWith("/") ? resource : "/" + resource;
            if (!normalized.startsWith("/profile/"))
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                log.warn("非法文件访问路径(非/profile/): {}", resource);
                return;
            }

            // 3. 安全校验：提取文件扩展名并校验白名单
            String extension = StringUtils.substringAfterLast(resource, ".").toLowerCase();
            if (StringUtils.isEmpty(extension) || !isAllowedAccessExtension(extension))
            {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                log.warn("非法文件访问(禁止的扩展名): {}", resource);
                return;
            }

            // 4. 安全校验：构建本地文件路径，确保不会越出上传根目录
            String localPath = RuoYiConfig.getProfile();
            String relativePath = StringUtils.substringAfter(normalized, "/profile");
            Path basePath = Paths.get(localPath).toAbsolutePath().normalize();
            Path targetPath = basePath.resolve(relativePath.substring(1)).normalize();

            if (!targetPath.startsWith(basePath))
            {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                log.warn("非法文件访问(越出根目录): {}", resource);
                return;
            }

            File file = targetPath.toFile();
            if (!file.exists() || !file.isFile())
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                log.warn("访问的文件不存在: {}", resource);
                return;
            }

            // 5. 根据文件类型设置 Content-Type
            String contentType = getAccessContentType(extension);
            // 图片和PDF可以内联预览，其他类型强制下载
            boolean inline = isInlinePreviewExtension(extension);
            if (inline)
            {
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "inline");
            }
            else
            {
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                String downloadName = file.getName();
                FileUtils.setAttachmentResponseHeader(response, downloadName);
            }
            // 注意：加密文件解压后的实际大小与磁盘文件大小不同，不设置 Content-Length

            // 6. 读取加密文件并解密解压后输出
            fis = new FileInputStream(file);
            OutputStream os = response.getOutputStream();
            FileStorageUtils.decryptStream(fis, os);
        }
        catch (Exception e)
        {
            log.error("安全文件访问失败: {}", resource, e);
        }
        finally
        {
            IOUtils.closeQuietly(fis);
        }
    }

    /**
     * Office 文档通用在线预览（服务端 LibreOffice 转 PDF，与 AI 知识问答数据来源预览同一能力）
     *
     * 规范（unified-development-standards.md「Office 文档预览规范」）：
     * - doc/docx/xls/xlsx/ppt/pptx/rtf 等禁止仅依赖前端 docx-preview 渲染，必须优先服务端转 PDF
     * - 转换结果缓存 uploadPath/convert/{md5(resource)}.pdf，源文件 mtime 变化自动失效重转
     * - 失败/不支持时返回 500 + JSON 错误体，前端按规范回退前端渲染或提示下载
     *
     * @param resource 资源路径，如 /profile/upload/2026/09/21/xxx.docx
     */
    @GetMapping("/file/preview")
    public void filePreview(String resource, HttpServletResponse response)
    {
        try
        {
            // 安全校验：与 /common/file/access 同口径（禁目录穿越、限 /profile/、扩展名白名单）
            if (StringUtils.isEmpty(resource) || resource.contains(".."))
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String normalized = resource.startsWith("/") ? resource : "/" + resource;
            if (!normalized.startsWith("/profile/"))
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String extension = StringUtils.substringAfterLast(normalized, ".").toLowerCase();
            if (StringUtils.isEmpty(extension) || !isAllowedAccessExtension(extension))
            {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            if (!com.ruoyi.ai.core.OfficeConvertUtils.isConvertible(extension))
            {
                writeJsonError(response, "该格式不支持服务端转换预览");
                return;
            }

            // 定位本地文件
            String relativePath = StringUtils.substringAfter(normalized, "/profile");
            Path basePath = Paths.get(RuoYiConfig.getProfile()).toAbsolutePath().normalize();
            Path targetPath = basePath.resolve(relativePath.substring(1)).normalize();
            if (!targetPath.startsWith(basePath))
            {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            File srcFile = targetPath.toFile();
            if (!srcFile.exists() || !srcFile.isFile())
            {
                writeJsonError(response, "原文件已丢失，请下载后查看");
                return;
            }

            // 解密解压到临时文件后转换（磁盘文件可能为加密存储）
            File decFile = File.createTempFile("edop-preview-src-", "." + extension);
            try (FileInputStream fis = new FileInputStream(srcFile))
            {
                FileStorageUtils.decryptStream(fis, java.nio.file.Files.newOutputStream(decFile.toPath()));
            }
            String cacheKey = org.springframework.util.DigestUtils.md5DigestAsHex(normalized.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            File pdf = com.ruoyi.ai.core.OfficeConvertUtils.getOrCreatePdf(cacheKey, decFile);
            decFile.delete();
            if (pdf == null || !pdf.exists())
            {
                writeJsonError(response, "文档转换预览失败，请下载原文件查看");
                return;
            }

            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            response.setHeader("Content-Disposition", "inline");
            response.setContentLengthLong(pdf.length());
            FileInputStream pis = new FileInputStream(pdf);
            OutputStream os = response.getOutputStream();
            byte[] buf = new byte[8192];
            int len;
            while ((len = pis.read(buf)) > 0)
            {
                os.write(buf, 0, len);
            }
            pis.close();
        }
        catch (Exception e)
        {
            log.error("Office 文档预览失败: {}", resource, e);
        }
    }

    /** 预览失败时输出 JSON 错误体（前端据此回退渲染方式） */
    private void writeJsonError(HttpServletResponse response, String msg)
    {
        try
        {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"msg\":\"" + msg + "\",\"code\":500}");
        }
        catch (Exception ignored)
        {
        }
    }

    /**
     * 判断文件扩展名是否允许通过 /common/file/access 访问
     * 只允许图片、文档、视频、压缩包等安全类型
     */
    private boolean isAllowedAccessExtension(String extension)
    {
        // 禁止访问的扩展名（可执行/脚本文件）
        if (MimeTypeUtils.isForbiddenExtension(extension))
        {
            return false;
        }
        // 允许访问的扩展名白名单
        String[] allowed = MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION;
        for (String ext : allowed)
        {
            if (ext.equalsIgnoreCase(extension))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断文件是否可以内联预览（图片和PDF）
     */
    private boolean isInlinePreviewExtension(String extension)
    {
        return MimeTypeUtils.isImageExtension(extension) || "pdf".equalsIgnoreCase(extension);
    }

    /**
     * 根据扩展名获取访问的 Content-Type
     */
    private String getAccessContentType(String extension)
    {
        switch (extension.toLowerCase())
        {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "pdf":
                return "application/pdf";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            default:
                return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}
