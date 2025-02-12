package hep.crest.server.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom ser for JSON to offset date time using a formatter.
 * The formatter should be initialized elsewhere.
 *
 * @author formica
 */
@Slf4j
public class CustomTimeSerializer extends JsonSerializer<OffsetDateTime> {

    /**
     * The formatter.
     */
    private DateTimeFormatter formatter;

    /**
     * The ctor.
     *
     * @param formatter
     */
    public CustomTimeSerializer(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        // Write the string using the formatter provided at creation time.
        gen.writeString(value.format(this.formatter));
    }
}
