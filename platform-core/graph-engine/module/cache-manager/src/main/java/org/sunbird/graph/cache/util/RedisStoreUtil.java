package org.sunbird.graph.cache.util;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.cache.exception.GraphCacheErrorCodes;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.telemetry.logger.TelemetryManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import static org.sunbird.graph.cache.factory.JedisFactory.getRedisConncetion;
import static org.sunbird.graph.cache.factory.JedisFactory.returnConnection;

public class RedisStoreUtil {

	private static ObjectMapper mapper = new ObjectMapper();

	public static void saveNodeProperty(String graphId, String objectId, String nodeProperty, String propValue) {

		Jedis jedis = getRedisConncetion();
		try {
			String redisKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId, nodeProperty);
            TelemetryManager.info("RedisStoreUtil: Saving property '" + nodeProperty + "' for object " + objectId + " in graph " + graphId + " with key: " + redisKey);
			jedis.set(redisKey, propValue);
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}


	public static void save(String key, String value, int ttl) {

		Jedis jedis = getRedisConncetion();
		try {
			jedis.set(key, value);
			if(ttl > 0)
				jedis.expire(key, ttl);
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static String get(String key) {
		Jedis jedis = getRedisConncetion();
		try {
			return jedis.get(key);
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static String getNodeProperty(String graphId, String objectId, String nodeProperty) {

		Jedis jedis = getRedisConncetion();
		try {
			String redisKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId, nodeProperty);
			String value = jedis.get(redisKey);
			return value;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static void saveNodeProperties(String graphId, String objectId, Map<String, Object> metadata) {
		Jedis jedis = getRedisConncetion();
		try {
            TelemetryManager.info("RedisStoreUtil: Saving properties for node " + objectId + " in graph " + graphId + " with " + metadata.size() + " properties");
			for (Entry<String, Object> entry : metadata.entrySet()) {
				String propertyName = entry.getKey();
				String propertyValue = entry.getValue().toString();

				String redisKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId, propertyName);
				jedis.set(redisKey, propertyValue);
			}
            TelemetryManager.info("RedisStoreUtil: Successfully saved all properties for node " + objectId + " in graph " + graphId);
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static void deleteNodeProperties(String graphId, String objectId) {
		Jedis jedis = getRedisConncetion();
		try {

			String versionKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId,
					GraphDACParams.versionKey.name());
			String consumerId = CacheKeyGenerator.getNodePropertyKey(graphId, objectId,
					GraphDACParams.consumerId.name());
			jedis.del(versionKey, consumerId);

		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static void deleteAllNodeProperty(String graphId, String propertyName) {
		String delKeysPattern = CacheKeyGenerator.getAllNodePropertyKeysPattern(graphId, propertyName);
		deleteByPattern(delKeysPattern);

	}

	public static Double getNodePropertyIncVal(String graphId, String objectId, String nodeProperty) {

		Jedis jedis = getRedisConncetion();
		try {
			String redisKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId, nodeProperty);
			double inc = 1.0;
			double value = jedis.incrByFloat(redisKey, inc);
			return value;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	
	// TODO: always considering object as string. need to change this.
	public static void saveList(String key, List<Object> values) {
		Jedis jedis = getRedisConncetion();
		try {
			jedis.del(key);
			for (Object val : values) {
				jedis.sadd(key, (String) val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			returnConnection(jedis);
		}
	}

	public static void saveStringList(String key, List<String> values, Integer ttl) {
		Jedis jedis = getRedisConncetion();
		try {
			jedis.del(key);
			for (String val : values) {
				jedis.sadd(key, val);
			}
			if (ttl > 0) jedis.expire(key, ttl);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			returnConnection(jedis);
		}
	}

	public static List<String> getStringList(String key) {
		Jedis jedis = getRedisConncetion();
		try {
			Set<String> set = jedis.smembers(key);
			List<String> list = new ArrayList<String>(set);
			return list;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static List<Object> getList(String key) {
		Jedis jedis = getRedisConncetion();
		try {
			 Set<String> set = jedis.smembers(key);
			 List<Object> list = new ArrayList<Object>(set);
			return list;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	/**
	 * This Method Save Data to Redis Cache With ttl.
	 *
	 * @param identifier
	 * @param data
	 * @param ttl
	 */
	public static void saveData(String identifier, Map<String, Object> data, int ttl) {
		try {
			TelemetryManager.log("Saving Content Data To Redis Cache having identifier : " + identifier);
			save(identifier, mapper.writeValueAsString(data), ttl);
		} catch (Exception e) {
			TelemetryManager.error("Error while saving data to Redis for Identifier : " + identifier + " | Error is : ", e);
		}
	}

	/**
	 * This method delete the keys from Redis Cache
	 * @param keys
	 */
	public static void delete(String... keys) {
		Jedis jedis = getRedisConncetion();
		try {
			jedis.del(keys);
		} catch (Exception e) {
			TelemetryManager.error("Error while deleting data from Redis for Identifiers : " + Arrays.asList(keys) + " | Error is : ", e);
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_DELETE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static void deleteByPattern(String pattern) {
		if(StringUtils.isNotBlank(pattern) && !StringUtils.equalsIgnoreCase(pattern, "*")){
			Jedis jedis = getRedisConncetion();
			try {
                TelemetryManager.info("RedisStoreUtil: Searching for keys with pattern: " + pattern);
				Set<String> keys = jedis.keys(pattern);
                TelemetryManager.info("RedisStoreUtil: Found " + (keys != null ? keys.size() : 0) + " keys matching pattern: " + pattern);
				if (keys != null && keys.size() > 0) {
					List<String> keyList = new ArrayList<>(keys);
                    TelemetryManager.info("RedisStoreUtil: Deleting keys: " + String.join(", ", keyList));
					jedis.del(keyList.toArray(new String[keyList.size()]));
				}
			} catch (Exception e) {
                TelemetryManager.error("Error while deleteByPattern data from Redis for Identifiers : " + pattern + " | Error is : ", e);
				throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
			} finally {
				returnConnection(jedis);
			}
		}
	}

    public static void deleteByPatternSafe(String pattern) {
        Jedis jedis = getRedisConncetion();
        String cursor = ScanParams.SCAN_POINTER_START;
        ScanParams scanParams = new ScanParams().match(pattern).count(1000);
        try {
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                List<String> keys = scanResult.getResult();

                if (!keys.isEmpty()) {
                    Pipeline pipeline = jedis.pipelined();
                    for (String key : keys) {
                        pipeline.del(key);
                    }
                    pipeline.sync();
                    TelemetryManager.info("Deleted " + keys.size() + " keys for pattern: " + pattern);
                }
                cursor = String.valueOf(scanResult.getCursor());
            } while (!"0".equals(cursor));
        } catch (Exception e) {
            TelemetryManager.error("Error deleting keys for pattern: " + pattern, e);
        } finally {
            returnConnection(jedis);
        }
    }
}
