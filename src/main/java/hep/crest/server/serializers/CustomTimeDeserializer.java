package hep.crest.server.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom deser for JSON to offset date time using a formatter.
 * The formatter should be initialized elsewhere.
 *
 * @author formica
 */
@Slf4j
public class CustomTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    /**
     * The formatter.
     */
    private DateTimeFormatter formatter;

    /**
     * Ctor.
     *
     * @param formatter
     */
    public CustomTimeDeserializer(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        // Deserialize using formatter provided at creation time.
        return OffsetDateTime.parse(parser.getText(), this.formatter);
    }
}
