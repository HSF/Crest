package hep.crest.server.utils;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.pojo.IovId;
import hep.crest.server.swagger.model.GenericMap;
import hep.crest.server.swagger.model.StoreDto;
import hep.crest.server.swagger.model.StoreSetDto;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Java program to demonstrate how to use Jackson Streaming API to read and * write
 */
@Slf4j
public class JsonParserTest {


    public static void main(String args[]) {
        System.out.println("Creating JSON file by using Jackson Streaming API in Java");
        createJSON("jacksondemo.json");
        System.out.println("done");
        System.out.println("Parsing JSON file by using Jackson Streaming API");
        parseJSON("jacksondemo.json");
        System.out.println("done");
        System.out.println("Creating JSON file by using Jackson Streaming API in Java");
        StoreSetDto setdto = buildStoreSet("test-tag");
        try {
            JsonFactory jsonfactory = new JsonFactory();
            File jsonDoc = new File("/tmp/jacksondemo_storeset.json");
            OutputStream out = new java.io.FileOutputStream(jsonDoc);
            log.info("...writing to file: {}", jsonDoc.getAbsolutePath());
            JsonGenerator generator = jsonfactory.createJsonGenerator(out, JsonEncoding.UTF8);
            generator.writePOJO(setdto);
            generator.close();

        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    } /* * This method create JSON String by using Jackson Streaming API. */

    public static void createJSON(String path) {
        try {
            JsonFactory jsonfactory = new JsonFactory();
            File jsonDoc = new File(path);
            OutputStream out = new java.io.FileOutputStream(jsonDoc);
            JsonGenerator generator = jsonfactory.createJsonGenerator(out, JsonEncoding.UTF8);
            generator.writeStartObject();
            generator.writeStringField("firstname", "Garrison");
            generator.writeStringField("lastname", "Paul");
            generator.writeNumberField("phone", 847332223);
            generator.writeFieldName("address");
            generator.writeStartArray();
            generator.writeString("Unit - 232");
            generator.writeString("Sofia Streat");
            generator.writeString("Mumbai");
            generator.writeEndArray();
            generator.writeEndObject();
            generator.close();
            System.out.println("JSON file created successfully");
        }
        catch (IOException ioex) {
            ioex.printStackTrace();
        }
    } /* * This method parse JSON String by using Jackson Streaming API example. */

    public static void parseJSON(String filename) {
        try {
            JsonFactory jsonfactory = new JsonFactory();
            File source = new File(filename);
            JsonParser parser = jsonfactory.createParser(source);
            //
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String token = parser
                        .getCurrentName();
                if ("firstname".equals(token)) {
                    parser.nextToken(); //next token contains value
                    String fname = parser.getText(); //getting text field
                    log.info("firstname : {}", fname);
                }
                if ("lastname".equals(token)) {
                    parser.nextToken();
                    String lname = parser.getText();
                    log.info("lastname : {}", lname);
                }
                if ("phone".equals(token)) {
                    parser.nextToken();
                    int phone = parser
                            .getIntValue(); // getting numeric field
                    log.info("phone : {}", phone);
                }
                if ("address".equals(token)) {
                    System.out.println("address :");
                    parser.nextToken(); // next token will be '[' which means JSON array //
                    // parse tokens until
                    // you find ']'
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        log.info(parser.getText());
                    }
                }
            }
            parser.close();
        }
        catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    public static StoreSetDto buildStoreSet(String tagname) {
        // Upload batch iovs
        final IovId id = new IovId().setTagName(tagname)
                .setSince(BigInteger.valueOf(4000000L * 1000000000L)).setInsertionTime(new Date());
        final Iov miov = new Iov();
        miov.setId(id);
        miov.getId().setInsertionTime(null);
        log.info("...created iov via random gen: {}", miov);
        final IovId id2 = new IovId().setTagName(tagname)
                .setSince(BigInteger.valueOf(5000000L * 1000000000L)).setInsertionTime(new Date());
        final Iov miov2 = new Iov();
        miov2.setId(id2);
        miov2.getId().setInsertionTime(null);
        log.info("...created iov2 via random gen: {}", miov2);

        final StoreSetDto setdto = new StoreSetDto();
        setdto.size(2L);
        setdto.format("StoreSetDto");
        final GenericMap filters = new GenericMap();
        filters.put("tagName", tagname);
        setdto.datatype("payloads").filter(filters);

        StoreDto sdto = new StoreDto();
        sdto.streamerInfo("{\"filename\": \"test-inline-5-this is a large payload\"}");
        sdto.setSince((miov.getId().getSince()).longValue());
        sdto.hash("somehashjson1");
        sdto.setData("{ \"key\": \"an inline very large payload as a json\"}");

        StoreDto sdto1 = new StoreDto();
        sdto1.streamerInfo("{\"filename\": \"test-inline-2- this is another large payload\"}");
        sdto1.since((miov2.getId().getSince()).longValue());
        sdto1.hash("somehashjson2");
        sdto1.setData("{ \"key\": \"an inline very large payload as a json 2 should have "
                      + "different hash\"}");
        setdto.addresourcesItem(sdto).addresourcesItem(sdto1);
        return setdto;
    }

}