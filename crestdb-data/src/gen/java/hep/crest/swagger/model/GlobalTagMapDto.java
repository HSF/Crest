/*
 * CrestDB REST API
 * Crest Rest Api to manage data for calibration files.
 *
 * OpenAPI spec version: 2.0
 * Contact: andrea.formica@cern.ch
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package hep.crest.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * GlobalTagMapDto
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-09-05T16:23:23.401+02:00")
public class GlobalTagMapDto   {
  @JsonProperty("globalTagName")
  private String globalTagName = null;

  @JsonProperty("record")
  private String record = null;

  @JsonProperty("label")
  private String label = null;

  @JsonProperty("tagName")
  private String tagName = null;

  public GlobalTagMapDto globalTagName(String globalTagName) {
    this.globalTagName = globalTagName;
    return this;
  }

   /**
   * Get globalTagName
   * @return globalTagName
  **/
  @JsonProperty("globalTagName")
  @ApiModelProperty(value = "")
  public String getGlobalTagName() {
    return globalTagName;
  }

  public void setGlobalTagName(String globalTagName) {
    this.globalTagName = globalTagName;
  }

  public GlobalTagMapDto record(String record) {
    this.record = record;
    return this;
  }

   /**
   * Get record
   * @return record
  **/
  @JsonProperty("record")
  @ApiModelProperty(value = "")
  public String getRecord() {
    return record;
  }

  public void setRecord(String record) {
    this.record = record;
  }

  public GlobalTagMapDto label(String label) {
    this.label = label;
    return this;
  }

   /**
   * Get label
   * @return label
  **/
  @JsonProperty("label")
  @ApiModelProperty(value = "")
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public GlobalTagMapDto tagName(String tagName) {
    this.tagName = tagName;
    return this;
  }

   /**
   * Get tagName
   * @return tagName
  **/
  @JsonProperty("tagName")
  @ApiModelProperty(value = "")
  public String getTagName() {
    return tagName;
  }

  public void setTagName(String tagName) {
    this.tagName = tagName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GlobalTagMapDto globalTagMapDto = (GlobalTagMapDto) o;
    return Objects.equals(this.globalTagName, globalTagMapDto.globalTagName) &&
        Objects.equals(this.record, globalTagMapDto.record) &&
        Objects.equals(this.label, globalTagMapDto.label) &&
        Objects.equals(this.tagName, globalTagMapDto.tagName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(globalTagName, record, label, tagName);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GlobalTagMapDto {\n");
    
    sb.append("    globalTagName: ").append(toIndentedString(globalTagName)).append("\n");
    sb.append("    record: ").append(toIndentedString(record)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    tagName: ").append(toIndentedString(tagName)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

