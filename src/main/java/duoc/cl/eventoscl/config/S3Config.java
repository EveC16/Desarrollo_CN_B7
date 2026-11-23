package duoc.cl.eventoscl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration 
public class S3Config {

    @Value("${AWS_S3_BUCKET_NAME:mibucketduocs3ec}")
    private String bucketName;

    @Value("${AWS_REGION:us-east-1}")
    private String region;

    @Value("${AWS_ACCESS_KEY_ID:}")
    private String accessKey;

    @Value("${AWS_SECRET_ACCESS_KEY:}")
    private String secretKey;

    @Value("${AWS_SESSION_TOKEN:}")
    private String sessionToken;

}
