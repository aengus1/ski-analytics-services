package ski.crunch.cloudformation.rockset.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class FieldMapping {

    private String name;
    private List<InputField> inputFields  = new ArrayList<>();
    private OutputField outputField = new OutputField();

    public static List<FieldMapping> parse(List input) {
        List<FieldMapping> fieldMappings = new ArrayList<>();
        FieldMapping fieldMapping = new FieldMapping();
        for (Object obj : input) {
            Map<String, Object> mapping = (Map<String, Object>)  obj;
            fieldMapping.name = (String) mapping.get("Name");
            List<Map<String, Object>> inputFields = (List<Map<String, Object>>) mapping.get("InputFields");
            for (Map<String, Object> inputField : inputFields) {
                fieldMapping.addInputField(
                        (String) inputField.get("FieldName"),
                        parseActionString((String)inputField.get("IfMissing")),
                        (Boolean) inputField.get("IsDrop"),
                        (String) inputField.get("Param")
                );
            }
            Map<String, Object> output = (Map<String, Object>) mapping.get("OutputField");
            fieldMapping.outputField.fieldName = (String) output.get("FieldName");
            fieldMapping.outputField.onError = parseActionString((String)output.get("OnError"));
            fieldMapping.outputField.value = (String) output.get("Value");
            fieldMappings.add(fieldMapping);
        }
        return fieldMappings;
    }

    static List<Action> parseActionString(String ifMissing) {
            ifMissing = ifMissing.replaceAll(Pattern.quote("'\\''"), "");
            ifMissing = ifMissing.replace("[","")
                    .replace("]", "")
                    .replace("\"", "")
                    .replace(" ", "");
            String[] actions = ifMissing.split(",");
            List<Action> res = new ArrayList<>();
        for (String action : actions) {
            res.add(Action.valueOf(action));
        }
        return res;
    }

    static String formatActionString(List<Action> actions) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Action action : actions) {
            sb.append("'\\''").append(action.name()+"'\\'',");
        }
        String res = sb.toString();
        if(res.length()>0){
            return res.substring(0, res.length() - 1);
        }else {
            return res;
        }
    }


    public void addInputField(String fieldName, List<Action> ifMissing, boolean isDrop, String param) {
        InputField inputField = new InputField();
        inputField.fieldName = fieldName;
        inputField.ifMissing = ifMissing;
        inputField.isDrop = isDrop;
        inputField.param = param;
        inputFields.add(inputField);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<InputField> getInputFields() {
        return inputFields;
    }

    public void setInputFields(List<InputField> inputFields) {
        this.inputFields = inputFields;
    }

    public OutputField getOutputField() {
        return outputField;
    }

    public void setOutputField(String fieldName, String value, List<Action> onError){
        this.outputField = new OutputField();
        this.outputField.onError = onError;
        this.outputField.fieldName =fieldName;
        this.outputField.value = value;
    }

   public  class InputField {
        private String fieldName;
        private List<Action> ifMissing;
        private boolean isDrop;
        private String param;

        public String getFieldName() {
            return fieldName;
        }

        public List<Action> getIfMissing() {
            return ifMissing;
        }

        public boolean isDrop() {
            return isDrop;
        }

        public String getParam() {
            return param;
        }
    }

    public class OutputField{
        private String fieldName;
        private String value;
        private List<Action> onError;

        public String getFieldName() {
            return fieldName;
        }

        public String getValue() {
            return value;
        }

        public List<Action> getOnError() {
            return onError;
        }
    }

    public enum Action {
        SKIP, PASS, FAIL;
    }

    public JsonNode toJson(ObjectMapper objectMapper) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("Name", name);
        ArrayNode inputs = objectMapper.createArrayNode();

        for (InputField inputField : inputFields) {
            ObjectNode field = objectMapper.createObjectNode();
            field.put("field_name", inputField.fieldName );
            field.put("if_missing", formatActionString(inputField.ifMissing));
            field.put("is_drop", inputField.isDrop);
            field.put("param", inputField.param);
            inputs.add(field);
        }
        root.set("InputFields", inputs);
        ObjectNode output = objectMapper.createObjectNode();
        output.put("field_name", outputField.fieldName);
        output.put("value", outputField.value);
        output.put("on_error", formatActionString(outputField.onError));
        root.set("OutputField", output);

        return root;
    }


}
