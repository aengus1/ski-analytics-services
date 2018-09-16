package com.serverless;

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

		Response responseBody = new Response("Go Serverless v1.x! Your function executed successfully!", input);
		Map<String, String> headers = new HashMap<>();
		headers.put("X-Powered-By", "AWS Lambda & Serverless");
		headers.put("Content-Type", "application/x-protobuf");
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
//				.setBinaryBody()
				.setObjectBody(responseBody)
				.setBase64Encoded(true)
				.setHeaders(headers)
				.build();
	}
}
