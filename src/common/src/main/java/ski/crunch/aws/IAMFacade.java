package ski.crunch.aws;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.*;
import org.apache.log4j.Logger;

import java.util.List;

public class IAMFacade {
    private AmazonIdentityManagement iam;
    private static final Logger LOG = Logger.getLogger(IAMFacade.class);

    public IAMFacade(String region) {
        iam = AmazonIdentityManagementClientBuilder.standard().withRegion(region).build();
    }

    public CreatePolicyResult createPolicy(String policyName, String policyDocument, String description) throws EntityAlreadyExistsException {
        CreatePolicyRequest request = new CreatePolicyRequest()
                .withPolicyName(policyName)
                .withPolicyDocument(policyDocument)
                .withDescription(description);
        LOG.debug("attempting to create policy " + policyName);
        return iam.createPolicy(request);
    }

    public DeletePolicyResult deletePolicy(String arn) {
        DeletePolicyRequest request = new DeletePolicyRequest().withPolicyArn(arn);
        LOG.debug("attempting to delete policy " + arn);
        return iam.deletePolicy(request);
    }


    public GetPolicyResult getPolicy(String arn) throws NoSuchEntityException {
        GetPolicyRequest request = new GetPolicyRequest().withPolicyArn(arn);
        LOG.debug("attempting to fetch policy " + arn);
        return iam.getPolicy(request);
    }

    public AttachRolePolicyResult attachPolicyToRole(String policyArn, String roleName) {
        AttachRolePolicyRequest attach_request =
                new AttachRolePolicyRequest()
                        .withRoleName(roleName)
                        .withPolicyArn(policyArn);
        LOG.debug("attempting to attach policy " + policyArn + " to " + roleName);
        return iam.attachRolePolicy(attach_request);
    }

    public DetachRolePolicyResult detachPolicyFromRole(String policyArn, String roleName) {
        DetachRolePolicyRequest request = new DetachRolePolicyRequest()
                .withRoleName(roleName)
                .withPolicyArn(policyArn);
        LOG.debug("attempting to detacj policy " + policyArn + " from " + roleName);
        return iam.detachRolePolicy(request);


    }

    public GetRoleResult getRole(String roleName) throws NoSuchEntityException {
        GetRoleRequest request = new GetRoleRequest().withRoleName(roleName);
        return iam.getRole(request);
    }


    public ListAttachedRolePoliciesResult getRolePolicies(String roleName) throws NoSuchEntityException {
        ListAttachedRolePoliciesRequest request = new ListAttachedRolePoliciesRequest().withRoleName(roleName);
        LOG.debug("attempting to fetch  policies for role " + roleName);
        return iam.listAttachedRolePolicies(request);
    }

    public CreateRoleResult createRole(String roleName, String assumeRolePolicyDocument, String description, List<Tag> tags) throws EntityAlreadyExistsException {
        CreateRoleRequest request = new CreateRoleRequest()
                .withRoleName(roleName)
                .withAssumeRolePolicyDocument(assumeRolePolicyDocument)
                .withDescription(description)
                .withTags(tags);
        LOG.debug("attempting to create role " + roleName);
        return iam.createRole(request);
    }

    public DeleteRoleResult deleteRole(String roleName) throws DeleteConflictException {
        DeleteRoleRequest request = new DeleteRoleRequest().withRoleName(roleName);

        try {
            return iam.deleteRole(request);
        } catch (DeleteConflictException e) {
            LOG.error("Unable to delete role" + roleName + ". Verify role is not associated with any resources");
            throw e;
        }
    }

}
