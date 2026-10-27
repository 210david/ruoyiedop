package com.ruoyi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@EnableScheduling
public class RuoYiApplication
{
    private static final Logger log = LoggerFactory.getLogger(RuoYiApplication.class);

    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        var context = SpringApplication.run(RuoYiApplication.class, args);

        // 上传根目录处理：
        // 1) 数据库配置优先于配置文件默认值（环境变量优先级最高，由 Spring 占位符处理）
        // 2) 自检目录存在性与可写性，避免配置错误时"静默"写到错误位置
        try
        {
            String envPath = System.getenv("EDOP_UPLOAD_PATH");
            String source;
            if (envPath != null && !envPath.trim().isEmpty())
            {
                source = "环境变量 EDOP_UPLOAD_PATH";
            }
            else
            {
                // 读取数据库中的上传路径配置（页面可改，即时生效）
                String dbPath = readUploadPathFromDb(context);
                if (dbPath != null && !dbPath.trim().isEmpty())
                {
                    com.ruoyi.common.config.RuoYiConfig.updateProfile(dbPath.trim());
                    source = "数据库配置 system.upload.path";
                }
                else
                {
                    source = "配置文件 application.yml";
                }
            }
            String profile = com.ruoyi.common.config.RuoYiConfig.getProfile();
            com.ruoyi.common.config.RuoYiConfig.validateAndPrepareProfile();
            log.info("上传根目录自检通过: {}（来源: {}）", profile, source);
        }
        catch (Exception e)
        {
            // 自检失败：打印明确错误并终止进程，避免带病运行导致文件写错位置
            log.error("上传根目录自检失败，程序退出: {}", e.getMessage());
            context.close();
            System.exit(1);
        }

        System.out.println("(♥◠‿◠)ﾉﾞ  若依启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }

    /**
     * 从数据库读取上传路径配置（失败时返回 null，回退到配置文件值）
     * 用反射调用避免启动类对 system 模块的强依赖
     */
    private static String readUploadPathFromDb(org.springframework.context.ApplicationContext context)
    {
        try
        {
            Object service = context.getBean("sysConfigServiceImpl");
            java.lang.reflect.Method m = service.getClass()
                    .getMethod("selectConfigByKey", String.class);
            Object val = m.invoke(service, "system.upload.path");
            return val == null ? null : val.toString();
        }
        catch (Exception e)
        {
            log.debug("读取数据库上传路径配置失败（将使用配置文件值）: {}", e.getMessage());
            return null;
        }
    }
}
