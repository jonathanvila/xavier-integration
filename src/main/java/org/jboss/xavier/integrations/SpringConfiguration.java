package org.jboss.xavier.integrations;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class SpringConfiguration {
    @Value("${S3_ACCESS_KEY_ID}")
    String key;

    @Value("${S3_SECRET_ACCESS_KEY}")
    String secret;

    @Value("${S3_REGION}")
    String region;

    @Bean(name = "s3client")
    public AmazonS3 getAmazonS3client() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(key,secret)))
                .withRegion(region)
                .build();
    }
}
