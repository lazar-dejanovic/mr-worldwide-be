package com.raf.mrworldwide.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@Profile("!dev-local & !test & !test_it & !integration")
public class AwsConfig {

	@Primary
	@Bean
	public S3Client s3Client(@Value("${aws.default.region}") String region,
							 @Value("${aws.access.key}") String accessKey,
							 @Value("${aws.secret.key}") String secretKey) {
		return S3Client.builder()
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(accessKey, secretKey)))
				.build();
	}

	@Bean
	public SqsClient sqsClient(@Value("${aws.default.region}") String region,
							   @Value("${aws.access.key}") String accessKey,
							   @Value("${aws.secret.key}") String secretKey) {
		return SqsClient.builder()
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(accessKey, secretKey)))
				.build();
	}

	@Bean
	public SecretsManagerClient secretsManagerClient(@Value("${aws.default.region}") String region,
													 @Value("${aws.access.key}") String accessKey,
													 @Value("${aws.secret.key}") String secretKey) {
		return SecretsManagerClient.builder()
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(accessKey, secretKey)))
				.build();
	}

}
