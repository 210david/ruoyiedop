package com.ruoyi.dms.gateway.manager;

import com.ruoyi.dms.gateway.adapter.DataCollectionAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 适配器管理器
 * 统一管理所有数据采集适配器的生命周期
 */
@Slf4j
@Component
public class AdapterManager {

    @Autowired
    private List<DataCollectionAdapter> adapters;

    private final Map<String, DataCollectionAdapter> adapterMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 注册所有适配器
        for (DataCollectionAdapter adapter : adapters) {
            adapterMap.put(adapter.getAdapterType(), adapter);
            log.info("注册数据采集适配器: type={}, protocol={}",
                    adapter.getAdapterType(), adapter.getProtocolType());
        }
    }

    @PreDestroy
    public void destroy() {
        // 停止所有适配器
        stopAllAdapters();
    }

    /**
     * 启动指定适配器
     */
    public boolean startAdapter(String adapterType) {
        DataCollectionAdapter adapter = adapterMap.get(adapterType);
        if (adapter == null) {
            log.warn("适配器不存在: {}", adapterType);
            return false;
        }

        if (adapter.isRunning()) {
            log.info("适配器已在运行: {}", adapterType);
            return true;
        }

        adapter.start();
        log.info("适配器启动成功: {}", adapterType);
        return true;
    }

    /**
     * 停止指定适配器
     */
    public boolean stopAdapter(String adapterType) {
        DataCollectionAdapter adapter = adapterMap.get(adapterType);
        if (adapter == null) {
            log.warn("适配器不存在: {}", adapterType);
            return false;
        }

        if (!adapter.isRunning()) {
            log.info("适配器已停止: {}", adapterType);
            return true;
        }

        adapter.stop();
        log.info("适配器停止成功: {}", adapterType);
        return true;
    }

    /**
     * 启动所有适配器
     */
    public void startAllAdapters() {
        for (DataCollectionAdapter adapter : adapterMap.values()) {
            try {
                adapter.start();
            } catch (Exception e) {
                log.error("启动适配器失败: type={}, error={}",
                        adapter.getAdapterType(), e.getMessage());
            }
        }
    }

    /**
     * 停止所有适配器
     */
    public void stopAllAdapters() {
        for (DataCollectionAdapter adapter : adapterMap.values()) {
            try {
                adapter.stop();
            } catch (Exception e) {
                log.error("停止适配器失败: type={}, error={}",
                        adapter.getAdapterType(), e.getMessage());
            }
        }
    }

    /**
     * 获取适配器状态
     */
    public Map<String, Boolean> getAdapterStatus() {
        Map<String, Boolean> status = new HashMap<>();
        for (Map.Entry<String, DataCollectionAdapter> entry : adapterMap.entrySet()) {
            status.put(entry.getKey(), entry.getValue().isRunning());
        }
        return status;
    }

    /**
     * 获取适配器
     */
    public DataCollectionAdapter getAdapter(String adapterType) {
        return adapterMap.get(adapterType);
    }

    /**
     * 获取所有适配器
     */
    public Map<String, DataCollectionAdapter> getAllAdapters() {
        return new HashMap<>(adapterMap);
    }
}
