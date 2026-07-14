package com.ruoyi.common.utils;

/**
 * 脱敏工具类
 *
 * @author ruoyi
 */
public class DesensitizedUtil
{
    /**
     * 密码的全部字符都用*代替，比如：******
     *
     * @param password 密码
     * @return 脱敏后的密码
     */
    public static String password(String password)
    {
        if (StringUtils.isBlank(password))
        {
            return StringUtils.EMPTY;
        }
        return StringUtils.repeat('*', password.length());
    }

    /**
     * 车牌中间用*代替，如果是错误的车牌，不处理
     *
     * @param carLicense 完整的车牌号
     * @return 脱敏后的车牌
     */
    public static String carLicense(String carLicense)
    {
        if (StringUtils.isBlank(carLicense))
        {
            return StringUtils.EMPTY;
        }
        // 普通车牌
        if (carLicense.length() == 7)
        {
            carLicense = StringUtils.hide(carLicense, 3, 6);
        }
        else if (carLicense.length() == 8)
        {
            // 新能源车牌
            carLicense = StringUtils.hide(carLicense, 3, 7);
        }
        return carLicense;
    }

    /**
     * 手机号脱敏，保留前3后4位，如 138****1234
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone)
    {
        if (StringUtils.isBlank(phone))
        {
            return StringUtils.EMPTY;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 身份证脱敏，保留前3后4位，如 320***********1234
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard)
    {
        if (StringUtils.isBlank(idCard))
        {
            return StringUtils.EMPTY;
        }
        return idCard.replaceAll("(\\d{3})\\d+(\\d{4})", "$1***********$2");
    }

    /**
     * 邮箱脱敏，仅显示第一个字母和@后面的域名，如 t***@example.com
     *
     * @param email 邮箱地址
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email)
    {
        if (StringUtils.isBlank(email))
        {
            return StringUtils.EMPTY;
        }
        return email.replaceAll("(^.)[^@]*(@.*$)", "$1***$2");
    }

    /**
     * 姓名脱敏，仅保留姓，如 张*
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String maskName(String name)
    {
        if (StringUtils.isBlank(name))
        {
            return StringUtils.EMPTY;
        }
        return name.replaceAll("(.).*", "$1*");
    }

    /**
     * 银行卡脱敏，保留前4后4位，如 6222***********1234
     *
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String maskBankCard(String bankCard)
    {
        if (StringUtils.isBlank(bankCard))
        {
            return StringUtils.EMPTY;
        }
        return bankCard.replaceAll("(\\d{4})\\d+(\\d{4})", "$1***********$2");
    }
}
