package dev.nimesh.backend;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@SpringBootApplication
@ComponentScan(basePackages = "dev.nimesh.backend")
public class FileUploadApplication {
	@Value("${cloud.aws.credentials.accessKey}")
	private  String accessKey ;

	@Value("${cloud.aws.credentials.secretKey}")
	private  String secretKey ;
	@Value("${aws.region}")
	private String awsRegion;
	public static void main(String[] args) {
		SpringApplication.run(FileUploadApplication.class, args);
	}
	@Bean
	public AmazonS3 amazonS3Client() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		return AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(awsRegion)
				.build();
	}
}
