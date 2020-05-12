package hep.crest.data.test;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import hep.crest.data.serializers.ByteArrayDeserializer;
import hep.crest.data.serializers.DateSerializer;
import hep.crest.data.serializers.TimestampDeserializer;
import hep.crest.data.serializers.TimestampSerializer;

import java.sql.Timestamp;
import java.util.Date;

public class TestItem {

    @JsonDeserialize(using = ByteArrayDeserializer.class)
    private byte[] data;
    @JsonDeserialize(using = TimestampDeserializer.class)
    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp instime;
    
    @JsonSerialize(using = DateSerializer.class)
    private Date insdate;

    private String name;

    /**
     * 
     */
    public TestItem() {
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Timestamp getInstime() {
        return instime;
    }

    public void setInstime(Timestamp instime) {
        this.instime = instime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the insdate
     */
    public Date getInsdate() {
        return insdate;
    }

    /**
     * @param insdate the insdate to set
     */
    public void setInsdate(Date insdate) {
        this.insdate = insdate;
    }
}
