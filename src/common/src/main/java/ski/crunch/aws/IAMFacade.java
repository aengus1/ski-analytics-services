package ski.crunch.aws;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class IAMFacade {
    private AmazonIdentityManagement iam;
    private static final Logger logger = LoggerFactory.getLogger(IAMFacade.class);

    public IAMFacade(String region) {
        iam = AmazonIdentityManagementClientBuilder.standard().withRegion(region).build();
    }

    //POLICY
    public CreatePolicyResult createPolicy(String policyName, String policyDocument, String description) throws EntityAlreadyExistsException {
        CreatePolicyRequest request = new CreatePolicyRequest()
                .withPolicyName(policyName)
                .withPolicyDocument(policyDocument)
                .withDescription(description);
        logger.debug("attempting to create policy " + policyName);
        return iam.createPolicy(request);
    }

    public DeletePolicyResult deletePolicy(String arn) {
        DeletePolicyRequest request = new DeletePolicyRequest().withPolicyArn(arn);
        logger.debug("attempting to delete policy " + arn);
        return iam.deletePolicy(request);
    }


    public GetPolicyResult getPolicy(String arn) throws NoSuchEntityException {
        GetPolicyRequest request = new GetPolicyRequest().withPolicyArn(arn);
        logger.debug("attempting to fetch policy " + arn);
        return iam.getPolicy(request);
    }

    //ROLE
    public AttachRolePolicyResult attachPolicyToRole(String policyArn, String roleName) {
        AttachRolePolicyRequest attach_request =
                new AttachRolePolicyRequest()
                        .withRoleName(roleName)
                        .withPolicyArn(policyArn);
        logger.debug("attempting to attach policy " + policyArn + " to " + roleName);
        return iam.attachRolePolicy(attach_request);
    }

    public DetachRolePolicyResult detachPolicyFromRole(String policyArn, String roleName) {
        DetachRolePolicyRequest request = new DetachRolePolicyRequest()
                .withRoleName(roleName)
                .withPolicyArn(policyArn);
        logger.debug("attempting to detacj policy " + policyArn + " from " + roleName);
        return iam.detachRolePolicy(request);
    }

    public GetRoleResult getRole(String roleName) throws NoSuchEntityException {
        GetRoleRequest request = new GetRoleRequest().withRoleName(roleName);
        return iam.getRole(request);
    }


    public ListAttachedRolePoliciesResult getRolePolicies(String roleName) throws NoSuchEntityException {
        ListAttachedRolePoliciesRequest request = new ListAttachedRolePoliciesRequest().withRoleName(roleName);
        logger.debug("attempting to fetch  policies for role " + roleName);
        return iam.listAttachedRolePolicies(request);
    }

    public CreateRoleResult createRole(String roleName, String assumeRolePolicyDocument, String description, List<Tag> tags) throws EntityAlreadyExistsException {
        CreateRoleRequest request = new CreateRoleRequest()
                .withRoleName(roleName)
                .withAssumeRolePolicyDocument(assumeRolePolicyDocument)
                .withDescription(description);
            if(!tags.isEmpty()) {
                request.withTags(tags);
            }
            logger.debug("create role tags..");
        for (Tag tag : tags) {
            logger.debug(tag.getKey() + ": "  + tag.getValue());
        }

        logger.debug("attempting to create role " + roleName);
        return iam.createRole(request);
    }

    public DeleteRoleResult deleteRole(String roleName) throws DeleteConflictException {
        DeleteRoleRequest request = new DeleteRoleRequest().withRoleName(roleName);

        try {
            return iam.deleteRole(request);
        } catch (DeleteConflictException e) {
            logger.error("Unable to delete role" + roleName + ". Verify role is not associated with any resources");
            throw e;
        }
    }

    //USERS

    public CreateUserResult createUser(String userName, String path, List<Tag> tags) {
        CreateUserRequest createUserRequest = new CreateUserRequest()
                .withUserName(userName)
                .withPath(path)
                .withTags(tags);
        logger.debug("attempting to create user " + userName);
        return iam.createUser(createUserRequest);

    }

    public DeleteUserResult deleteUser(String userName) {
        DeleteUserRequest deleteUserRequest = new DeleteUserRequest()
                .withUserName(userName);

        return iam.deleteUser(deleteUserRequest);
    }

    public GetUserResult getUser(String userName) {
        GetUserRequest getUserRequest = new GetUserRequest()
                .withUserName(userName);
        return iam.getUser(getUserRequest);
    }

    public AttachUserPolicyResult attachPolicyToUser(String policyArn, String userName) {
        AttachUserPolicyRequest attach_request =
                new AttachUserPolicyRequest()
                        .withUserName(userName)
                        .withPolicyArn(policyArn);
        logger.debug("attempting to attach policy " + policyArn + " to " + userName);
        return iam.attachUserPolicy(attach_request);
    }

    public DetachUserPolicyResult detachPolicyFromUser(String policyArn, String userName) {
        DetachUserPolicyRequest request = new DetachUserPolicyRequest()
                .withUserName(userName)
                .withPolicyArn(policyArn);
        logger.debug("attempting to detach policy " + policyArn + " from " + userName);
        return iam.detachUserPolicy(request);
    }

    public ListAttachedUserPoliciesResult getUserPolicies(String userName) throws NoSuchEntityException {
        ListAttachedUserPoliciesRequest request = new ListAttachedUserPoliciesRequest()
                .withUserName(userName);
        logger.debug("attempting to fetch  policies for user " + userName);
        return iam.listAttachedUserPolicies(request);
    }


    //  ACCESS KEYS

    public CreateAccessKeyResult createAccessKey(String userName) {
        CreateAccessKeyRequest request = new CreateAccessKeyRequest()
                .withUserName(userName);
        logger.info("attempting to create access key for user " + userName);
        return iam.createAccessKey(request);
    }

    public ListAccessKeysResult listAccessKeys(String userName) {
        ListAccessKeysRequest request = new ListAccessKeysRequest()
                .withUserName(userName);

        return iam.listAccessKeys(request);
    }


    public DeleteAccessKeyResult deleteAccessKey(String userName, String accessKeyId) {
        DeleteAccessKeyRequest request = new DeleteAccessKeyRequest()
                .withUserName(userName)
                .withAccessKeyId(accessKeyId);

        return iam.deleteAccessKey(request);
    }


}
