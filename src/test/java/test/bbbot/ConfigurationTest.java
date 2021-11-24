package test.bbbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.LoggerFactory;
import tech.ypsilon.bbbot.config.ButterbrotConfig;

import java.io.File;
import java.io.IOException;

public class ConfigurationTest {

    public static void main(String[] args) throws IOException {
        try {
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            ButterbrotConfig config = yamlMapper.readValue(new File("sample_settings.yml"), ButterbrotConfig.class);
            System.out.println(yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config));
        } catch (UnrecognizedPropertyException exception) {
            LoggerFactory.getLogger(ConfigurationTest.class).error(exception.getMessage());
        }
    }

}
