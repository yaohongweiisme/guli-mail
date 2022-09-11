package com.wei.gulimall.thirdparty;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class GulimallThirdPartyApplicationTests {
    @Value("${aliyunOss.endpoint}")
    public  String endpoint ;
    @Value("${aliyunOss.accessKeyId}")
    public  String accessKeyId ;
    @Value("${aliyunOss.accessKeySecret}")
    public  String accessKeySecret ;
    @Value("${aliyunOss.bucketName}")
    public   String bucketName;

    @Test
    void contextLoads() {
    }
    @Test
    void testUpLoadByOss() throws FileNotFoundException {
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
// 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        System.out.println(endpoint+ "  "+accessKeyId+ "  "+accessKeySecret+ "  "+bucketName);
// 创建OSSClient实例。d
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        //文件上传流
        InputStream inputStream=new FileInputStream("D:\\temp\\临时图片\\3.jpg");
        ossClient.putObject(bucketName,"miaochun1.jpg",inputStream);
// 关闭OSSClient。
        ossClient.shutdown();
    }

}
