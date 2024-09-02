package node.connection.service;

import node.connection.entity.ConfigData;
import node.connection.repository.ConfigDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfigDataService {

    @Autowired
    private ConfigDataRepository configDataRepository;

    // key로 value 조회
    public Optional<String> getValueByKey(String key) {
        return configDataRepository.findByKey(key).map(ConfigData::getValue);
    }

    // key로 value 업데이트
    public void updateValueByKey(String key, String newValue) {
        Optional<ConfigData> configDataOpt = configDataRepository.findByKey(key);
        if (configDataOpt.isPresent()) {
            ConfigData configData = configDataOpt.get();
            configData.setValue(newValue);
            configDataRepository.save(configData);
        } else {
            throw new RuntimeException("ConfigData with key " + key + " not found");
        }
    }
}
