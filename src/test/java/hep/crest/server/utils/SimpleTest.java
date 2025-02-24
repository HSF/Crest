package hep.crest.server.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import hep.crest.server.serializers.ArgTimeUnit;
import hep.crest.server.swagger.model.IovSetDto;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Slf4j
public class SimpleTest {

    public static void main(String[] args) {
        try {
            ArgTimeUnit tu = ArgTimeUnit.valueOf("ms".toUpperCase(Locale.ROOT));
            log.info("Time unit ms is: {}", tu);
            ArgTimeUnit tu1 = ArgTimeUnit.valueOf("number".toUpperCase(Locale.ROOT));
            log.info("Time unit number is: {}", tu1);

            String json = "{ \"size\": 2, \"datatype\": \"PYL\", \"format\": \"IovSetDto\", \"resources\":[ { "
                          + "\"since\" : 1001, \"payloadHash\": \"file:///tmp/newfile-01.txt\"}, { \"since\" : 2001, "
                          + "\"payloadHash\": \"file:///tmp/newfile-02.txt\"} ] }";

            ObjectMapper mapper = new ObjectMapper();

            IovSetDto dto = mapper.readValue(json.getBytes(StandardCharsets.UTF_8), IovSetDto.class);
            log.info("Created dto : {}", dto);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
