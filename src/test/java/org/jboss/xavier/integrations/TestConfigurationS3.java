package org.jboss.xavier.integrations;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfigurationS3 {
    @Value("${S3_HOST}")
    private String s3_host;

    @Bean(name = "s3client")
    public AmazonS3 getAmazonS3client() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("dummyKey", "dummySecret")))
                .withChunkedEncodingDisabled(true)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3_host, "us-east-1"))
                .build();
    }
}
