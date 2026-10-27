package com.ruoyi.pms.service.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.pms.domain.dto.PmsEnterpriseInfoDTO;
import com.ruoyi.pms.service.IPmsEnterpriseLookupService;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 企业信息查询 Service实现
 *
 * 从sys_config读取API配置，调用第三方企业信息API查询企业信息。
 * 支持天眼查、企查查及自定义API。
 *
 * 配置项（sys_config表）：
 *   pms.enterprise.lookup.enabled   — 是否启用（true/false）
 *   pms.enterprise.lookup.provider  — API提供商（tianyancha/qichacha/custom）
 *   pms.enterprise.lookup.api.url   — API地址
 *   pms.enterprise.lookup.api.key   — API密钥
 *
 * @author ruoyi
 */
@Service
public class PmsEnterpriseLookupServiceImpl implements IPmsEnterpriseLookupService
{
    private static final Logger log = LoggerFactory.getLogger(PmsEnterpriseLookupServiceImpl.class);

    @Autowired
    private ISysConfigService configService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PmsEnterpriseInfoDTO lookup(String keyword)
    {
        if (StringUtils.isEmpty(keyword))
        {
            throw new ServiceException("请输入统一社会信用代码");
        }
        
        // 统一社会信用代码基本校验（18位）
        if (keyword.length() != 18)
        {
            throw new ServiceException("统一社会信用代码应为18位，请检查输入");
        }

        // 读取配置
        String enabled = configService.selectConfigByKey("pms.enterprise.lookup.enabled");
        if (!"true".equals(enabled))
        {
            throw new ServiceException("企业信息查询功能未启用，请在「企业查询配置」中开启");
        }

        String apiUrl = configService.selectConfigByKey("pms.enterprise.lookup.api.url");
        String apiKey = configService.selectConfigByKey("pms.enterprise.lookup.api.key");
        String apiSecret = configService.selectConfigByKey("pms.enterprise.lookup.api.secret");
        String provider = configService.selectConfigByKey("pms.enterprise.lookup.provider");

        if (StringUtils.isEmpty(apiUrl) || StringUtils.isEmpty(apiKey))
        {
            throw new ServiceException("API地址或密钥未配置，请在「企业查询配置」中设置");
        }

        return callApi(provider, apiUrl, apiKey, apiSecret, keyword);
    }

    /**
     * 调用第三方API查询企业信息
     */
    private PmsEnterpriseInfoDTO callApi(String provider, String apiUrl, String apiKey, String apiSecret, String keyword)
    {
        try
        {
            RestTemplate restTemplate = new RestTemplate();

            // 构建请求URL（不同API商参数名不同）
            String url;
            String response;
            
            if ("tianyancha".equals(provider))
            {
                // 天眼查API调用
                response = callTianyanchaApi(apiUrl, apiKey, keyword);
            }
            else if ("qichacha".equals(provider))
            {
                // 企查查需要特殊的鉴权方式
                response = callQichachaApi(apiUrl, apiKey, apiSecret, keyword);
            }
            else
            {
                // 自定义API，使用通用参数名
                url = apiUrl + "?keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8") + "&key=" + apiKey;
                log.info("自定义API请求URL: {}", url.replace(apiKey, "***"));
                response = restTemplate.getForObject(url, String.class);
            }

            log.info("API响应: {}", response);
            
            JsonNode root = objectMapper.readTree(response);

            // 检查API返回状态（适配企查查、天眼查等不同格式）
            
            // 天眼查错误处理：error_code 为 0 表示成功，其他表示失败
            if (root.has("error_code"))
            {
                int errorCode = root.get("error_code").asInt();
                if (errorCode != 0)
                {
                    String errorMsg = root.has("reason") ? root.get("reason").asText() : 
                                     root.has("error_msg") ? root.get("error_msg").asText() : "API返回错误";
                    log.error("天眼查返回错误: error_code={}, reason={}", errorCode, errorMsg);
                    throw new ServiceException("查询失败：" + errorMsg);
                }
                // error_code 为 0 但 result 为空，表示无结果
                if (!root.has("result") || root.get("result").isNull() || 
                    (root.get("result").isObject() && root.get("result").size() == 0))
                {
                    log.warn("天眼查查询无结果: {}", keyword);
                    PmsEnterpriseInfoDTO emptyDto = new PmsEnterpriseInfoDTO();
                    emptyDto.setDataSource(provider);
                    emptyDto.setEnterpriseName("【未找到】" + keyword);
                    emptyDto.setEnterpriseStatus("未查询到该统一社会信用代码对应的企业信息，请确认代码正确");
                    return emptyDto;
                }
            }
            
            if (root.has("error") && !root.get("error").isNull())
            {
                String errorMsg = root.get("error").has("message") ? root.get("error").get("message").asText() : "API返回错误";
                throw new ServiceException("查询失败：" + errorMsg);
            }
            
            // 企查查返回 Status 字段表示状态
            // 200: 查询成功，201: 查询无结果（也是有效请求）
            if (root.has("Status"))
            {
                String status = root.get("Status").asText();
                String message = root.has("Message") ? root.get("Message").asText() : "";
                log.info("企查查返回状态: Status={}, Message={}", status, message);
                
                if (!"200".equals(status) && !"201".equals(status))
                {
                    throw new ServiceException("查询失败：" + message);
                }
                // 201 表示查询无结果
                if ("201".equals(status))
                {
                    log.warn("企查查查询无结果: {}", keyword);
                    PmsEnterpriseInfoDTO emptyDto = new PmsEnterpriseInfoDTO();
                    emptyDto.setDataSource(provider);
                    // 给出明确提示
                    emptyDto.setEnterpriseName("【未找到】" + keyword);
                    emptyDto.setEnterpriseStatus("未查询到该统一社会信用代码对应的企业信息，请确认代码正确");
                    return emptyDto;
                }
            }

            // 通用JSON解析：适配天眼查/企查查等API返回格式
            JsonNode data = root.has("result") ? root.get("result")
                        : root.has("data") ? root.get("data")
                        : root.has("Result") ? root.get("Result")
                        : root;
            
            log.info("解析后的数据节点: {}", data);

            PmsEnterpriseInfoDTO dto = new PmsEnterpriseInfoDTO();
            // 适配多种API返回格式（小写/大写/驼峰）
            dto.setEnterpriseName(getText(data, "Name", "name", "enterpriseName", "companyName"));
            dto.setCreditCode(getText(data, "CreditCode", "creditCode", "regNo", "unifiedCode", "credit_no"));
            dto.setLegalPerson(getText(data, "OperName", "operName", "legalPersonName", "legalPerson", "frName", "oper_name"));
            dto.setRegisteredCapital(getDecimal(data, "RegistCapi", "registCapi", "regCapital", "registeredCapital"));
            dto.setEstablishedDate(getText(data, "StartDate", "startDate", "estiblishTime", "establishedDate", "start_date"));
            dto.setEnterpriseNature(getText(data, "EconKind", "econKind", "companyOrgType", "enterpriseType", "economyType"));
            dto.setAddress(getText(data, "Address", "address", "regLocation", "regAddress"));
            dto.setBusinessScope(getText(data, "Scope", "scope", "businessScope", "business_scope"));
            dto.setEnterpriseStatus(getText(data, "Status", "status", "regStatus", "enterpriseStatus", "open_status"));
            dto.setDataSource(provider != null ? provider : "第三方API");
            
            log.info("解析后的企业信息: name={}, creditCode={}, legalPerson={}", 
                dto.getEnterpriseName(), dto.getCreditCode(), dto.getLegalPerson());
            return dto;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("企业信息查询失败：" + e.getMessage());
        }
    }

    /**
     * 调用天眼查API
     * 天眼查鉴权方式：通过Authorization头传递token
     * 标准接口：/cloud-originapi/v2/baseinfo/normal
     */
    private String callTianyanchaApi(String apiUrl, String apiKey, String keyword)
    {
        try
        {
            RestTemplate restTemplate = new RestTemplate();
            
            // 构建请求URL - 天眼查使用keyword参数
            // 统一社会信用代码作为keyword查询
            String url = apiUrl + "?keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8");
            
            log.info("天眼查API请求URL: {}", url);
            
            // 设置请求头 - 天眼查使用Authorization头传递token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.set("Content-Type", "application/json;charset=UTF-8");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            return response.getBody();
        }
        catch (Exception e)
        {
            log.error("天眼查API调用失败", e);
            throw new ServiceException("天眼查API调用失败：" + e.getMessage());
        }
    }

    /**
     * 调用企查查API（带鉴权）
     * 企查查鉴权方式：Token = MD5(Key + Timespan + SecretKey)
     * 注意：SecretKey 是企查查提供的单独密钥，不是 MD5(Key)
     */
    private String callQichachaApi(String apiUrl, String apiKey, String apiSecret, String keyword)
    {
        try
        {
            RestTemplate restTemplate = new RestTemplate();
            
            // 生成时间戳（秒级）
            String timespan = String.valueOf(System.currentTimeMillis() / 1000);
            
            // 如果没有配置 SecretKey，则使用 MD5(Key) 作为兼容方案
            String secretKey = StringUtils.isNotEmpty(apiSecret) ? apiSecret : md5(apiKey);
            
            // 计算 Token: MD5(Key + Timespan + SecretKey)
            String token = md5(apiKey + timespan + secretKey);
            
            // 构建请求URL
            String url = apiUrl + "?key=" + apiKey + "&keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8");
            
            log.info("企查查API请求URL: {}", url.replace(apiKey, "***"));
            log.info("企查查API Timespan: {}", timespan);
            log.info("企查查API Token: {}", token);
            log.info("企查查API SecretKey 是否配置: {}", StringUtils.isNotEmpty(apiSecret) ? "是" : "否（使用MD5(Key)兼容）");
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Token", token);
            headers.set("Timespan", timespan);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            return response.getBody();
        }
        catch (Exception e)
        {
            log.error("企查查API调用失败", e);
            throw new ServiceException("企查查API调用失败：" + e.getMessage());
        }
    }
    
    /**
     * 当精确查询无结果时，使用搜索接口查找并返回第一个结果
     */
    private PmsEnterpriseInfoDTO searchAndGetFirstResult(String provider, String apiUrl, String apiKey, String apiSecret, String keyword)
    {
        try
        {
            // 将精确查询接口改为搜索接口
            // ECIV4/GetBasicDetailsByName -> ECIV4/Search 或 FuzzySearch/Search
            String searchUrl = apiUrl.replace("ECIV4/GetBasicDetailsByName", "FuzzySearch/Search");
            if (searchUrl.equals(apiUrl))
            {
                // 如果替换失败，手动构建搜索URL
                searchUrl = "https://api.qichacha.com/FuzzySearch/Search";
            }
            
            log.info("尝试使用企查查搜索接口: {}", searchUrl);
            
            RestTemplate restTemplate = new RestTemplate();
            String timespan = String.valueOf(System.currentTimeMillis() / 1000);
            String secretKey = StringUtils.isNotEmpty(apiSecret) ? apiSecret : md5(apiKey);
            String token = md5(apiKey + timespan + secretKey);
            
            // 搜索接口URL
            String url = searchUrl + "?key=" + apiKey + "&searchKey=" + java.net.URLEncoder.encode(keyword, "UTF-8") + "&pageSize=1";
            
            log.info("企查查搜索请求URL: {}", url.replace(apiKey, "***"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Token", token);
            headers.set("Timespan", timespan);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            String responseBody = response.getBody();
            log.info("企查查搜索响应: {}", responseBody);
            
            JsonNode root = objectMapper.readTree(responseBody);
            
            if (root.has("Status") && "200".equals(root.get("Status").asText()))
            {
                JsonNode result = root.get("Result");
                if (result != null && result.isArray() && result.size() > 0)
                {
                    JsonNode firstCompany = result.get(0);
                    log.info("找到企业: {}", firstCompany.has("Name") ? firstCompany.get("Name").asText() : "未知");
                    
                    // 使用找到的企业名称再次查询详细信息
                    String companyName = firstCompany.has("Name") ? firstCompany.get("Name").asText() : keyword;
                    return callApiWithName(provider, apiUrl, apiKey, apiSecret, companyName);
                }
            }
            
            log.warn("搜索也未找到结果");
            PmsEnterpriseInfoDTO emptyDto = new PmsEnterpriseInfoDTO();
            emptyDto.setDataSource(provider != null ? provider : "第三方API");
            return emptyDto;
        }
        catch (Exception e)
        {
            log.error("搜索接口调用失败", e);
            PmsEnterpriseInfoDTO emptyDto = new PmsEnterpriseInfoDTO();
            emptyDto.setDataSource(provider != null ? provider : "第三方API");
            return emptyDto;
        }
    }
    
    /**
     * 使用指定企业名称查询详细信息
     */
    private PmsEnterpriseInfoDTO callApiWithName(String provider, String apiUrl, String apiKey, String apiSecret, String companyName)
    {
        try
        {
            String response = callQichachaApi(apiUrl, apiKey, apiSecret, companyName);
            log.info("详细查询响应: {}", response);
            
            JsonNode root = objectMapper.readTree(response);
            
            if (root.has("Status") && "200".equals(root.get("Status").asText()))
            {
                JsonNode data = root.has("Result") ? root.get("Result") : root;
                
                PmsEnterpriseInfoDTO dto = new PmsEnterpriseInfoDTO();
                dto.setEnterpriseName(getText(data, "Name", "name", "enterpriseName", "companyName"));
                dto.setCreditCode(getText(data, "CreditCode", "creditCode", "regNo", "unifiedCode", "credit_no"));
                dto.setLegalPerson(getText(data, "OperName", "operName", "legalPersonName", "legalPerson", "frName", "oper_name"));
                dto.setRegisteredCapital(getDecimal(data, "RegistCapi", "registCapi", "regCapital", "registeredCapital"));
                dto.setEstablishedDate(getText(data, "StartDate", "startDate", "estiblishTime", "establishedDate", "start_date"));
                dto.setEnterpriseNature(getText(data, "EconKind", "econKind", "companyOrgType", "enterpriseType", "economyType"));
                dto.setAddress(getText(data, "Address", "address", "regLocation", "regAddress"));
                dto.setBusinessScope(getText(data, "Scope", "scope", "businessScope", "business_scope"));
                dto.setEnterpriseStatus(getText(data, "Status", "status", "regStatus", "enterpriseStatus", "open_status"));
                dto.setDataSource(provider != null ? provider : "第三方API");
                
                log.info("解析后的企业信息: name={}, creditCode={}, legalPerson={}", 
                    dto.getEnterpriseName(), dto.getCreditCode(), dto.getLegalPerson());
                return dto;
            }
            
            PmsEnterpriseInfoDTO emptyDto = new PmsEnterpriseInfoDTO();
            emptyDto.setDataSource(provider != null ? provider : "第三方API");
            return emptyDto;
        }
        catch (Exception e)
        {
            log.error("详细查询失败", e);
            PmsEnterpriseInfoDTO emptyDto = new PmsEnterpriseInfoDTO();
            emptyDto.setDataSource(provider != null ? provider : "第三方API");
            return emptyDto;
        }
    }

    /**
     * MD5加密（返回大写）
     * 企查查要求 MD5 结果为大写形式
     */
    private String md5(String input)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
            {
                sb.append(String.format("%02X", b));  // 大写 X
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    // ===== 工具方法 =====

    private String getText(JsonNode node, String... keys)
    {
        for (String key : keys)
        {
            if (node.has(key) && !node.get(key).isNull())
            {
                return node.get(key).asText();
            }
        }
        return null;
    }

    private BigDecimal getDecimal(JsonNode node, String... keys)
    {
        for (String key : keys)
        {
            if (node.has(key) && !node.get(key).isNull())
            {
                String val = node.get(key).asText().replaceAll("[^0-9.]", "");
                if (StringUtils.isNotEmpty(val))
                {
                    try { return new BigDecimal(val); } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }
}
