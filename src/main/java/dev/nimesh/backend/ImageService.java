package dev.nimesh.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

    private final RedisTemplate<String, byte[]> redisTemplate;

    @Autowired
    public ImageService(RedisTemplate<String, byte[]> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public byte[] getImageDataFromCache(String userEmail) {
        return redisTemplate.opsForValue().get("image:" + userEmail);
    }

    public void cacheImageData(String userEmail, byte[] fileData) {
        redisTemplate.opsForValue().set("image:" + userEmail, fileData);
    }

}
