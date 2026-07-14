package com.ruoyi.framework.manager.factory;

import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.LogUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.UserAgentUtils;
import com.ruoyi.common.utils.ip.AddressUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.common.alert.AlarmInfo;
import com.ruoyi.system.domain.SysAlarmLog;
import com.ruoyi.system.domain.SysLogininfor;
import com.ruoyi.system.domain.SysOperLog;
import com.ruoyi.system.service.ISysAlarmLogService;
import com.ruoyi.system.service.ISysLogininforService;
import com.ruoyi.system.service.ISysOperLogService;

/**
 * 异步工厂（产生任务用）
 * 
 * @author ruoyi
 */
public class AsyncFactory
{
    private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");

    /**
     * 记录登录信息
     * 
     * @param username 用户名
     * @param status 状态
     * @param message 消息
     * @param args 列表
     * @return 任务task
     */
    public static TimerTask recordLogininfor(final String username, final String status, final String message,
            final Object... args)
    {
        final String userAgent = ServletUtils.getRequest().getHeader("User-Agent");
        final String ip = IpUtils.getIpAddr();
        return new TimerTask()
        {
            @Override
            public void run()
            {
                String address = AddressUtils.getRealAddressByIP(ip);
                StringBuilder s = new StringBuilder();
                s.append(LogUtils.getBlock(ip));
                s.append(address);
                s.append(LogUtils.getBlock(username));
                s.append(LogUtils.getBlock(status));
                s.append(LogUtils.getBlock(message));
                // 打印信息到日志
                sys_user_logger.info(s.toString(), args);
                // 获取客户端操作系统
                String os = UserAgentUtils.getOperatingSystem(userAgent);
                // 获取客户端浏览器
                String browser = UserAgentUtils.getBrowser(userAgent);
                // 获取登录设备类型
                String loginDevice = getDeviceType(userAgent);
                // 封装对象
                SysLogininfor logininfor = new SysLogininfor();
                logininfor.setUserName(username);
                logininfor.setIpaddr(ip);
                logininfor.setLoginLocation(address);
                logininfor.setBrowser(browser);
                logininfor.setOs(os);
                logininfor.setLoginDevice(loginDevice);
                logininfor.setMsg(message);
                // 日志状态
                if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER))
                {
                    logininfor.setStatus(Constants.SUCCESS);
                }
                else if (Constants.LOGIN_FAIL.equals(status))
                {
                    logininfor.setStatus(Constants.FAIL);
                }
                // 插入数据
                SpringUtils.getBean(ISysLogininforService.class).insertLogininfor(logininfor);
            }
        };
    }

    /**
     * 根据User-Agent判断登录设备类型
     * 
     * @param userAgent 用户代理字符串
     * @return 设备类型：PC/手机/平板
     */
    private static String getDeviceType(String userAgent)
    {
        if (StringUtils.isEmpty(userAgent))
        {
            return "未知";
        }
        String ua = userAgent.toLowerCase();
        // 判断是否为平板
        if (ua.contains("ipad") || ua.contains("tablet") || (ua.contains("android") && !ua.contains("mobile")))
        {
            return "平板";
        }
        // 判断是否为手机
        if (ua.contains("mobile") || ua.contains("iphone") || ua.contains("android") || ua.contains("phone")
                || ua.contains("ipod") || ua.contains("symbian") || ua.contains("windows phone"))
        {
            return "手机";
        }
        // 默认为PC
        return "PC";
    }

    /**
     * 操作日志记录
     * 
     * @param operLog 操作日志信息
     * @return 任务task
     */
    public static TimerTask recordOper(final SysOperLog operLog)
    {
        return new TimerTask()
        {
            @Override
            public void run()
            {
                // 远程查询操作地点
                operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
                SpringUtils.getBean(ISysOperLogService.class).insertOperlog(operLog);
            }
        };
    }

    /**
     * 告警日志记录
     * 
     * @param alarm 告警信息
     * @return 任务task
     */
    public static TimerTask recordAlarm(final AlarmInfo alarm)
    {
        return new TimerTask()
        {
            @Override
            public void run()
            {
                SysAlarmLog alarmLog = new SysAlarmLog();
                alarmLog.setAlarmType(alarm.getType());
                alarmLog.setAlarmLevel(alarm.getLevel());
                alarmLog.setAlarmTitle(alarm.getTitle());
                alarmLog.setAlarmDetail(alarm.getContent());
                alarmLog.setStatus("0");
                SpringUtils.getBean(ISysAlarmLogService.class).insertAlarmLog(alarmLog);
            }
        };
    }
}
