package com.serverless;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(Handler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: " + input);
		String s3Bucket = System.getenv("s3ActivityBucketName");
		LOG.info("bucket name" + s3Bucket);
//		LOG.info(input.keySet().stream().map(e -> e.toString()).reduce(",",String::concat));
		Map<String,String> pathParams = (Map<String,String>) input.get("pathParameters");
		LOG.info("id = " + pathParams.get("id"));
		String id = pathParams.get("id");
		String region = System.getenv("AWS_DEFAULT_REGION");
		LOG.info("region = " + System.getenv("AWS_DEFAULT_REGION"));
		S3Service service = new S3Service(region);

		// Response responseBody = new Response("Go Serverless v1.x! Your function executed successfully!", input);
		Map<String, String> headers = new HashMap<>();
		headers.put("X-Powered-By", "AWS Lambda & Serverless");
		headers.put("Content-Type", "application/x-protobuf");
		byte[] binaryBody = null;
		try {
			binaryBody = service.getObject(s3Bucket,  id+ ".pbf");
		} catch (IOException ex ) {
			ex.printStackTrace();
			LOG.error(" error reading file " + ex.getMessage());
		}
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setBinaryBody(binaryBody)
				.setBase64Encoded(true)
				.setHeaders(headers)
				.build();
	}
}
