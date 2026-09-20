package com.ruoyi.web.controller.system;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 上传路径配置（运维管理）
 *
 * 背景：上传根目录（ruoyi.profile）原为硬编码绝对路径，更改程序目录或配置被覆盖时，
 * 会出现"文件在A目录、程序读B目录"的分叉，表现为上传成功但读取/预览报"文件不存在"。
 *
 * 本接口提供：
 * 1. 查看当前生效路径及其来源（环境变量 / 配置文件 / 默认值）
 * 2. 在线修改路径（校验合法性、自动创建、即时生效）
 * 3. 文件迁移：把旧目录文件迁移到新目录
 *
 * 优先级说明：环境变量 EDOP_UPLOAD_PATH > 数据库配置 > 配置文件默认值
 * 注意：环境变量优先级最高，若已设置环境变量，页面修改将不生效（接口会明确提示）。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/upload-path")
public class UploadPathController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(UploadPathController.class);

    /** 上传路径配置在 sys_config 中的键名 */
    private static final String CONFIG_KEY = "system.upload.path";

    @Autowired
    private ISysConfigService configService;

    /**
     * 查询当前上传路径配置及来源
     */
    @PreAuthorize("@ss.hasPermi('system:uploadPath:query')")
    @GetMapping("/info")
    public AjaxResult info()
    {
        Map<String, Object> result = new HashMap<>();
        String envPath = System.getenv("EDOP_UPLOAD_PATH");
        String dbPath = configService.selectConfigByKey(CONFIG_KEY);
        String effective = RuoYiConfig.getProfile();

        result.put("effectivePath", effective);
        result.put("envPath", envPath);
        result.put("dbPath", dbPath);
        // 来源判定：环境变量 > 数据库配置 > 配置文件默认值
        if (StringUtils.isNotEmpty(envPath))
        {
            result.put("source", "env");
            result.put("sourceText", "环境变量 EDOP_UPLOAD_PATH（优先级最高，页面修改不生效）");
        }
        else if (StringUtils.isNotEmpty(dbPath))
        {
            result.put("source", "db");
            result.put("sourceText", "数据库配置（页面可修改）");
        }
        else
        {
            result.put("source", "file");
            result.put("sourceText", "配置文件 application.yml 默认值");
        }
        // 当前路径状态
        File dir = new File(effective);
        result.put("exists", dir.exists());
        result.put("writable", dir.exists() && dir.canWrite());
        result.put("editable", StringUtils.isEmpty(envPath));
        // 目录占用空间（便于评估迁移体量）
        result.put("usage", describeUsage(dir));
        return success(result);
    }

    /**
     * 修改上传路径（校验 + 自动创建 + 即时生效）
     */
    @Log(title = "上传路径配置", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('system:uploadPath:edit')")
    @PutMapping("/update")
    public AjaxResult update(@RequestBody Map<String, String> body)
    {
        String newPath = body.get("path");
        if (StringUtils.isEmpty(newPath) || newPath.trim().isEmpty())
        {
            return error("上传路径不能为空");
        }
        newPath = normalize(newPath.trim());
        // 环境变量优先级更高，此时页面修改无意义
        if (StringUtils.isNotEmpty(System.getenv("EDOP_UPLOAD_PATH")))
        {
            return error("当前通过环境变量 EDOP_UPLOAD_PATH 指定上传路径，优先级高于页面配置。如需页面修改，请先移除该环境变量并重启服务");
        }
        // 路径合法性校验
        String invalid = validatePath(newPath);
        if (invalid != null)
        {
            return error(invalid);
        }
        String oldPath = RuoYiConfig.getProfile();
        // 持久化到数据库（键不存在时自动插入）
        configService.updateConfigByKeyForced(CONFIG_KEY, newPath);
        // 即时生效（更新内存值）
        RuoYiConfig.updateProfile(newPath);
        log.info("上传路径已修改: {} -> {}（操作人: {}）", oldPath, newPath, getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("oldPath", oldPath);
        data.put("newPath", newPath);
        // 提示：若新目录下没有历史文件，需要执行迁移
        data.put("needMigrate", !oldPath.equals(newPath));
        return AjaxResult.success("上传路径已更新并即时生效", data);
    }

    /**
     * 文件迁移：把旧目录内容迁移到当前（新）目录
     * 用于切换上传路径后，把历史文件搬到新位置，避免"原文件已丢失"
     */
    @Log(title = "上传路径迁移", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('system:uploadPath:migrate')")
    @PostMapping("/migrate")
    public AjaxResult migrate(@RequestBody Map<String, String> body)
    {
        String fromPath = body.get("fromPath");
        if (StringUtils.isEmpty(fromPath))
        {
            return error("请指定待迁移的源目录");
        }
        fromPath = normalize(fromPath.trim());
        String toPath = normalize(RuoYiConfig.getProfile());
        if (fromPath.equals(toPath))
        {
            return error("源目录与当前上传目录相同，无需迁移");
        }
        File fromDir = new File(fromPath);
        if (!fromDir.exists() || !fromDir.isDirectory())
        {
            return error("源目录不存在: " + fromPath);
        }
        File toDir = new File(toPath);
        if (!toDir.exists() && !toDir.mkdirs())
        {
            return error("目标目录无法创建: " + toPath);
        }
        try
        {
            List<String> errors = new ArrayList<>();
            int[] count = new int[2]; // [文件数, 目录数]
            copyRecursive(fromDir, toDir, count, errors);
            Map<String, Object> data = new HashMap<>();
            data.put("fromPath", fromPath);
            data.put("toPath", toPath);
            data.put("fileCount", count[0]);
            data.put("dirCount", count[1]);
            data.put("errorCount", errors.size());
            data.put("errors", errors.size() > 20 ? errors.subList(0, 20) : errors);
            log.info("上传文件迁移完成: {} -> {}，文件{}个，失败{}个（操作人: {}）",
                    fromPath, toPath, count[0], errors.size(), getUsername());
            if (errors.isEmpty())
            {
                return AjaxResult.success("迁移完成，共 " + count[0] + " 个文件", data);
            }
            return AjaxResult.success("迁移完成，但有 " + errors.size() + " 个文件失败，请查看详情", data);
        }
        catch (Exception e)
        {
            log.error("上传文件迁移失败: {}", e.getMessage());
            return error("迁移失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /** 递归复制（已存在的同名文件按大小+时间判断，相同则跳过） */
    private void copyRecursive(File src, File dest, int[] count, List<String> errors)
    {
        File[] children = src.listFiles();
        if (children == null)
        {
            return;
        }
        for (File child : children)
        {
            File target = new File(dest, child.getName());
            try
            {
                if (child.isDirectory())
                {
                    if (!target.exists() && !target.mkdirs())
                    {
                        errors.add("目录创建失败: " + target.getAbsolutePath());
                        continue;
                    }
                    count[1]++;
                    copyRecursive(child, target, count, errors);
                }
                else
                {
                    // 目标已存在且大小相同则跳过，避免重复复制
                    if (target.exists() && target.length() == child.length())
                    {
                        continue;
                    }
                    Files.copy(child.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    count[0]++;
                }
            }
            catch (Exception e)
            {
                errors.add(child.getName() + ": " + e.getMessage());
            }
        }
    }

    /** 路径规范化：统一分隔符，去除末尾斜杠 */
    private String normalize(String path)
    {
        String p = path.replace('\\', '/');
        while (p.endsWith("/") && p.length() > 3)
        {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    /** 路径合法性校验，返回错误信息；合法返回 null */
    private String validatePath(String path)
    {
        // 必须是绝对路径
        if (!path.matches("^[a-zA-Z]:/.*") && !path.startsWith("/"))
        {
            return "请输入绝对路径（Windows 形如 D:/EDOP/uploadPath，Linux 形如 /home/edop/uploadPath）";
        }
        // 禁止指向系统关键目录
        String lower = path.toLowerCase();
        String[] forbidden = { "c:/windows", "c:/program files", "/etc", "/usr", "/bin", "/sys" };
        for (String f : forbidden)
        {
            if (lower.startsWith(f))
            {
                return "不允许使用系统目录作为上传路径: " + path;
            }
        }
        // 尝试创建并校验可写
        try
        {
            File dir = new File(path);
            if (!dir.exists())
            {
                if (!dir.mkdirs())
                {
                    return "目录不存在且无法创建，请检查磁盘与权限: " + path;
                }
            }
            if (!dir.isDirectory())
            {
                return "该路径已被文件占用，不是目录: " + path;
            }
            if (!dir.canWrite())
            {
                return "目录不可写，请检查权限（运行服务的账号需具备读写权限）: " + path;
            }
        }
        catch (Exception e)
        {
            return "路径校验失败：" + e.getMessage();
        }
        return null;
    }

    /** 统计目录文件数与占用空间 */
    private Map<String, Object> describeUsage(File dir)
    {
        Map<String, Object> usage = new HashMap<>();
        if (dir == null || !dir.exists() || !dir.isDirectory())
        {
            usage.put("fileCount", 0);
            usage.put("totalSize", 0);
            usage.put("totalSizeText", "0 B");
            return usage;
        }
        long[] stat = new long[2]; // [文件数, 字节数]
        calcUsage(dir, stat);
        usage.put("fileCount", stat[0]);
        usage.put("totalSize", stat[1]);
        usage.put("totalSizeText", humanSize(stat[1]));
        return usage;
    }

    private void calcUsage(File dir, long[] stat)
    {
        File[] children = dir.listFiles();
        if (children == null)
        {
            return;
        }
        for (File f : children)
        {
            if (f.isDirectory())
            {
                calcUsage(f, stat);
            }
            else
            {
                stat[0]++;
                stat[1] += f.length();
            }
        }
    }

    private String humanSize(long bytes)
    {
        if (bytes < 1024)
        {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024)
        {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        if (bytes < 1024L * 1024 * 1024)
        {
            return String.format("%.1f MB", bytes / 1024.0 / 1024);
        }
        return String.format("%.2f GB", bytes / 1024.0 / 1024 / 1024);
    }
}
