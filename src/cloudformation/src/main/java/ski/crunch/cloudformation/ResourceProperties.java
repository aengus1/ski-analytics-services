package ski.crunch.cloudformation;

import ski.crunch.utils.MissingRequiredParameterException;

import java.util.List;
import java.util.Map;

public abstract class ResourceProperties {


    public void checkParameter(String parameter, Map<String, Object> input) throws MissingRequiredParameterException {
        if (!input.containsKey(parameter)) {
            throw new MissingRequiredParameterException("Parameter " + parameter + " not supplied");
        }
    }

    protected void checkRequiredParameters(Map<String, Object> input, List<String> requiredParameters) throws MissingRequiredParameterException {
        if (input == null) {
            throw new MissingRequiredParameterException("No ResourceProperties found");
        }
        for (String requiredParameter : requiredParameters) {
            checkParameter(requiredParameter, input);
        }
    }
}
