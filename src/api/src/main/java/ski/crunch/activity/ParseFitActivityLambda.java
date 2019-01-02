package ski.crunch.activity;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.log4j.Logger;
import ski.crunch.activity.model.ActivityOuterClass;
import ski.crunch.activity.model.ApiGatewayResponse;
import ski.crunch.activity.parser.ActivityHolderAdapter;
import ski.crunch.activity.parser.fit.FitActivityHolderAdapter;
import ski.crunch.activity.service.S3Service;
import ski.crunch.utils.ConvertibleOutputStream;


import java.io.IOException;
import java.io.InputStream;

import java.text.ParseException;
import java.util.*;

public class ParseFitActivityLambda implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    private String region;
    private String s3ActivityBucket;
    private static final Logger LOG = Logger.getLogger(ParseFitActivityLambda.class);

    public ParseFitActivityLambda() {
        this.region = System.getenv("AWS_DEFAULT_REGION");
        this.s3ActivityBucket = System.getenv("s3ActivityBucketName");
    }

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

        //1. convert input
        String bucket = null;
        String key = null;
        System.out.println("convert activity lambda");
        Iterator<String> it = input.keySet().iterator();
        while (it.hasNext()) {
            String next = it.next();
            ArrayList<Map> records = (ArrayList<Map>) input.get(next);

            for (Map r : records) {
                Iterator rit = r.keySet().iterator();
                while (rit.hasNext()) {
                    String nxt = (String) rit.next();
                    if (nxt.equals("s3")) {
                        Map s3Map = (Map) r.get(nxt);
                        Map buck = (Map) s3Map.get("bucket");
                        bucket = (String) buck.get("name");
                        Map obj = (Map) s3Map.get("object");
                        key = (String) obj.get("key");
                        System.out.println("bucket = " + bucket);
                        System.out.println("object = " + key);
                        break;
                    }
                }

            }
        }
//        if(bucket == null || key == null){
//            throw new ParseException("failed to convert raw activity bucket / key");
//        }
        assert (bucket != null);
        assert (key != null);

        String newKey = "";
        if (key != null && key.length() > 1 && key.contains(".fit")) {
            newKey = key.substring(0, key.indexOf(".fit") - 1) + ".pbf";
            LOG.debug("new key name: " + newKey);
        } else {
            // throw new ParseException("invalid key name for activity " + key);
            LOG.error("invalid key name: " + key);
        }

        // records/s3/object -> filename
        S3Service s3Service = new S3Service(region);
        InputStream is = null;
        ActivityOuterClass.Activity activity = null;
        try {
            LOG.info("attempting to read " + key + " from " + bucket);
            is = s3Service.getObjectAsInputStream(bucket, key);
            ActivityHolderAdapter fitParser = new FitActivityHolderAdapter();
//            activity = fitParser.convert(is);
            //TODO -> add calculated fields
            //TODO -> add API driven fields


            ConvertibleOutputStream cos = new ConvertibleOutputStream();
            activity.writeTo(cos);
            s3Service.putObject(bucket, newKey, cos.toInputStream());

            //TODO -> update activity table
        } catch (IOException e) {
            e.printStackTrace();
//        }catch(ParseException ex){
//            ex.printStackTrace();
//        }
            //2. fetch item from s3
            //3.


            return null;
        }
        return null;
    }
}
