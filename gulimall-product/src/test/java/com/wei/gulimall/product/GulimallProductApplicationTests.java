package com.wei.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wei.gulimall.product.entity.BrandEntity;
import com.wei.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.UUID;

@SpringBootTest
class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setName("一加");
//        brandEntity.setBrandId(13L);
//        brandEntity.setDescript("oppo子品牌");
//        brandService.updateById(brandEntity);
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>());
        list.forEach(System.out::println);
//        System.out.println("保存成功....");
    }

//    @Test
//    void testUpLoadByOss() throws FileNotFoundException {
//        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
//        String endpoint = OssUtils.endpoint;
//// 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        String accessKeyId = OssUtils.accessKeyId;
//        String accessKeySecret = OssUtils.accessKeySecret;
//        String bucketName = OssUtils.bucketName;
//// 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//        //文件上传流
//        InputStream inputStream=new FileInputStream("D:\\temp\\临时图片\\4.jpg");
//        ossClient.putObject(bucketName,"miaochun1.jpg",inputStream);
//
//// 关闭OSSClient。
//        ossClient.shutdown();
//    }
    @Test
    void testRedis(){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello","world_"+ UUID.randomUUID());
        System.out.println("hello的值是"+ops.get("hello"));
    }

}
