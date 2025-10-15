package org.sunbird.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.graph.cache.util.RedisStoreUtil;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FrameworkCache {

    private static final int cacheTtl = Platform.config.hasPath("framework.cache.ttl") ? Platform.config.getInt("framework.cache.ttl") : 86400;
    protected static boolean cacheEnabled = Platform.config.hasPath("framework.cache.read") ? Platform.config.getBoolean("framework.cache.read") : false;
    private static final String CACHE_PREFIX = "fw_";
    protected static ObjectMapper mapper = new ObjectMapper();


    protected static String getFwCacheKey(String identifier, List<String> categoryNames) {
        Collections.sort(categoryNames);
        return CACHE_PREFIX + identifier.toLowerCase() + "_" + categoryNames.stream().map(cat -> cat.toLowerCase()).collect(Collectors.joining("_"));
    }

    public static Map<String, Object> get(String id, List<String> returnCategories) throws IOException {
        if (cacheEnabled) {
            String redisKey;
            if (CollectionUtils.isNotEmpty(returnCategories)) {
                Collections.sort(returnCategories);
                redisKey = getFwCacheKey(id, returnCategories);
                String cachedCategories = RedisStoreUtil.get(redisKey);
                try {
                    String keysListKey = CACHE_PREFIX+id + "_keys";
                    String existingKeysJson = RedisStoreUtil.get(keysListKey);
                    List<String> keysList;

                    if (StringUtils.isNotBlank(existingKeysJson)) {
                        keysList = mapper.readValue(existingKeysJson, new TypeReference<List<String>>() {});
                    } else {
                        keysList = new ArrayList<>();
                    }

                    if (!keysList.contains(redisKey)) {
                        keysList.add(redisKey);
                        RedisStoreUtil.save(keysListKey, mapper.writeValueAsString(keysList), cacheTtl * 2);
                        TelemetryManager.info("Added key " + redisKey + " to list " + keysListKey);
                    }
                } catch (Exception e) {
                    TelemetryManager.error("Error while maintaining _keys list for framework: " + id, e);
                }
                if (StringUtils.isNotBlank(cachedCategories)) {
                    return mapper.readValue(cachedCategories, new TypeReference<Map<String, Object>>() {});
                }
            } else {
                redisKey = id;
                String frameworkMetadata = RedisStoreUtil.get(redisKey);
                if (StringUtils.isNotBlank(frameworkMetadata)) {
                    return mapper.readValue(frameworkMetadata, new TypeReference<Map<String, Object>>() {});
                }
            }
        }
        return null;
    }


    public static void save(Map<String, Object> framework, List<String> categoryNames) throws JsonProcessingException {
        if(cacheEnabled && MapUtils.isNotEmpty(framework) && StringUtils.isNotBlank((String) framework.get("identifier")) && CollectionUtils.isNotEmpty(categoryNames)) {
            Collections.sort(categoryNames);
            String key = getFwCacheKey((String) framework.get("identifier"), categoryNames);
            RedisStoreUtil.save(key, mapper.writeValueAsString(framework), cacheTtl);
            List<String> keysList = new ArrayList<>();

            String existingKeysJson = RedisStoreUtil.get((String) framework.get("identifier") + "_keys");
            if (StringUtils.isNotBlank(existingKeysJson)) {
                try {
                    keysList = mapper.readValue(existingKeysJson, new TypeReference<List<String>>(){});
                } catch (IOException e) {
                    TelemetryManager.error("Error parsing existing framework keys: " + e.getMessage(), e);
                }
            }
            if (!keysList.contains(key)) {
                keysList.add(key);
            }
            RedisStoreUtil.save((String) framework.get("identifier")+"_keys", mapper.writeValueAsString(keysList), cacheTtl * 2);
        }
    }

    public static void delete(String id) {
        if(StringUtils.isNotBlank(id))
            RedisStoreUtil.deleteByPatternSafe(CACHE_PREFIX + id);
    }

}
