package ski.crunch.cloudformation;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ski.crunch.utils.MissingRequiredParameterException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class AbstractCustomResourceLambdaTest {


    @Mock
    private Context context = mock(Context.class);


    class CustomResourceLambda extends AbstractCustomResourceLambda {
        @Override
        public CloudformationResponse doCreate(CloudformationRequest request) throws Exception {
            return CloudformationResponse.successResponse(request);
        }

        @Override
        public CloudformationResponse doUpdate(CloudformationRequest request) throws Exception {
            return CloudformationResponse.successResponse(request);
        }

        @Override
        public CloudformationResponse doDelete(CloudformationRequest request) throws Exception {
            return CloudformationResponse.successResponse(request);
        }
    };

    @Mock
    CustomResourceLambda customResourceLambda = mock(CustomResourceLambda.class);

    @Mock
    LambdaLogger lambdaLogger = mock(LambdaLogger.class);

    @Captor()
    ArgumentCaptor<CloudformationRequest> createArgCaptor;

    private Map<String, Object> testInput;
    private Map<String, Object> resourceProperties;

    @BeforeAll()
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeEach()
    public void setUp() {

        testInput = new HashMap<>();
        resourceProperties = new HashMap<>();
        resourceProperties.put("Name", "MyIntegration");
        resourceProperties.put("Region", "ca-central-1");
        resourceProperties.put("ApiKeySSM", "my-ssm-bucket");
        resourceProperties.put("AccessibleResource", "arn:aws::12345:dynamodb:mytable/*");
        resourceProperties.put("RocksetAccountId", "12345");
        resourceProperties.put("ApiServer", "api.server.test");
        resourceProperties.put("ExternalId", "12345");

        //todo -> figure out tags


        testInput.put("RequestType", "Create");
        testInput.put("ResponseURL", "http://testurl.com");
        testInput.put("StackId", "my-stack-id");
        testInput.put("RequestId", "12345-678910");
        testInput.put("LogicalResourceId", "myLogicalResourceId");
        testInput.put("PhysicalResourceId", "myPhysicalResourceId");
        testInput.put("ResourceType", "AWS::CloudFormation::CustomResource");
        testInput.put("ResourceProperties", resourceProperties);

    }

    @Test()
    void testHandlerCallCreateMethod() throws Exception {
        CloudformationRequest expected = new CloudformationRequest(testInput);


        when(customResourceLambda.sendResponse(any(CloudformationResponse.class), any(Context.class), any(HashMap.class))).thenReturn(new Object());
        when(customResourceLambda.handle(any(CloudformationRequest.class), anyMap(), any(Context.class))).thenCallRealMethod();
        when(customResourceLambda.doCreate(any(CloudformationRequest.class))).thenCallRealMethod();

        customResourceLambda.handle(expected, testInput, context);
        Mockito.verify(customResourceLambda).doCreate(createArgCaptor.capture());
        assertEquals(expected, createArgCaptor.getValue());
    }

    ;

    @Test()
    void testHandlerFailsOnMissingParam() throws Exception {
        testInput.remove("ResourceType");
        CloudformationRequest expected = new CloudformationRequest(testInput);
        assertThrows(MissingRequiredParameterException.class, () -> {
        });
    }


}
