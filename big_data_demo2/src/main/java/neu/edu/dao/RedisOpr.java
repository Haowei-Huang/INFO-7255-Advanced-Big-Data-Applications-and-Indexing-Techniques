package neu.edu.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisOpr {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	public void saveData(String key, Object value) {
		redisTemplate.opsForValue().set(key, value);
	}

	public Object getData(String key) {
		Object obj = redisTemplate.opsForValue().get(key);
		return obj;
	}
	
	public boolean deleteData(String key) {
		return redisTemplate.delete(key);
	}

	public boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}

	
}
