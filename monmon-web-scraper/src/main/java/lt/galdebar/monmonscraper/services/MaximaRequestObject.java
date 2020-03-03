package lt.galdebar.monmonscraper.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
public class MaximaRequestObject {
    private Map<String,String> requestBody;

    public MaximaRequestObject() {
        requestBody = new HashMap<>();
    }

    public void addToMap(String propertyName, String propertyValue){
        requestBody.put(propertyName,propertyValue);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        requestBody.forEach((key, value) -> {
//            stringBuilder.append("\"");
            stringBuilder.append(key);
//            stringBuilder.append("\":\"");
            stringBuilder.append(":");
            stringBuilder.append(value);
//            stringBuilder.append("\"");
        });
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
