package node.connection;

import node.connection.service.ConfigDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigDataExample {

    @Autowired
    private ConfigDataService configDataService;

    public void exampleUsage() {
        // Key로 value 조회
        String key = "1";
        configDataService.getValueByKey(key).ifPresent(value -> {
            System.out.println("Value for key " + key + ": " + value);
        });

        // Key로 value 업데이트
        String newValue = "new value";
        configDataService.updateValueByKey(key, newValue);

        // 업데이트된 value 조회
        configDataService.getValueByKey(key).ifPresent(value -> {
            System.out.println("Updated value for key " + key + ": " + value);
        });
    }
}
