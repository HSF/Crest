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
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.*;

/**
 * TagDto
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-05-31T15:58:56.095+02:00")
public class TagDto   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("timeType")
  private String timeType = null;

  @JsonProperty("payloadSpec")
  private String payloadSpec = null;

  @JsonProperty("synchronization")
  private String synchronization = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("lastValidatedTime")
  private BigDecimal lastValidatedTime = null;

  @JsonProperty("endOfValidity")
  private BigDecimal endOfValidity = null;

  @JsonProperty("insertionTime")
  private Date insertionTime = null;

  @JsonProperty("modificationTime")
  private Date modificationTime = null;

  public TagDto name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   **/
  @JsonProperty("name")
  @ApiModelProperty(value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TagDto timeType(String timeType) {
    this.timeType = timeType;
    return this;
  }

  /**
   * Get timeType
   * @return timeType
   **/
  @JsonProperty("timeType")
  @ApiModelProperty(value = "")
  public String getTimeType() {
    return timeType;
  }

  public void setTimeType(String timeType) {
    this.timeType = timeType;
  }

  public TagDto payloadSpec(String payloadSpec) {
    this.payloadSpec = payloadSpec;
    return this;
  }

  /**
   * Get payloadSpec
   * @return payloadSpec
   **/
  @JsonProperty("payloadSpec")
  @ApiModelProperty(value = "")
  public String getPayloadSpec() {
    return payloadSpec;
  }

  public void setPayloadSpec(String payloadSpec) {
    this.payloadSpec = payloadSpec;
  }

  public TagDto synchronization(String synchronization) {
    this.synchronization = synchronization;
    return this;
  }

  /**
   * Get synchronization
   * @return synchronization
   **/
  @JsonProperty("synchronization")
  @ApiModelProperty(value = "")
  public String getSynchronization() {
    return synchronization;
  }

  public void setSynchronization(String synchronization) {
    this.synchronization = synchronization;
  }

  public TagDto description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   * @return description
   **/
  @JsonProperty("description")
  @ApiModelProperty(value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public TagDto lastValidatedTime(BigDecimal lastValidatedTime) {
    this.lastValidatedTime = lastValidatedTime;
    return this;
  }

  /**
   * Get lastValidatedTime
   * @return lastValidatedTime
   **/
  @JsonProperty("lastValidatedTime")
  @ApiModelProperty(value = "")
  public BigDecimal getLastValidatedTime() {
    return lastValidatedTime;
  }

  public void setLastValidatedTime(BigDecimal lastValidatedTime) {
    this.lastValidatedTime = lastValidatedTime;
  }

  public TagDto endOfValidity(BigDecimal endOfValidity) {
    this.endOfValidity = endOfValidity;
    return this;
  }

  /**
   * Get endOfValidity
   * @return endOfValidity
   **/
  @JsonProperty("endOfValidity")
  @ApiModelProperty(value = "")
  public BigDecimal getEndOfValidity() {
    return endOfValidity;
  }

  public void setEndOfValidity(BigDecimal endOfValidity) {
    this.endOfValidity = endOfValidity;
  }

  public TagDto insertionTime(Date insertionTime) {
    this.insertionTime = insertionTime;
    return this;
  }

  /**
   * Get insertionTime
   * @return insertionTime
   **/
  @JsonProperty("insertionTime")
  @ApiModelProperty(value = "")
  public Date getInsertionTime() {
    return insertionTime;
  }

  public void setInsertionTime(Date insertionTime) {
    this.insertionTime = insertionTime;
  }

  public TagDto modificationTime(Date modificationTime) {
    this.modificationTime = modificationTime;
    return this;
  }

  /**
   * Get modificationTime
   * @return modificationTime
   **/
  @JsonProperty("modificationTime")
  @ApiModelProperty(value = "")
  public Date getModificationTime() {
    return modificationTime;
  }

  public void setModificationTime(Date modificationTime) {
    this.modificationTime = modificationTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TagDto tagDto = (TagDto) o;
    return Objects.equals(this.name, tagDto.name) &&
        Objects.equals(this.timeType, tagDto.timeType) &&
        Objects.equals(this.payloadSpec, tagDto.payloadSpec) &&
        Objects.equals(this.synchronization, tagDto.synchronization) &&
        Objects.equals(this.description, tagDto.description) &&
        Objects.equals(this.lastValidatedTime, tagDto.lastValidatedTime) &&
        Objects.equals(this.endOfValidity, tagDto.endOfValidity) &&
        Objects.equals(this.insertionTime, tagDto.insertionTime) &&
        Objects.equals(this.modificationTime, tagDto.modificationTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, timeType, payloadSpec, synchronization, description, lastValidatedTime, endOfValidity, insertionTime, modificationTime);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TagDto {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    timeType: ").append(toIndentedString(timeType)).append("\n");
    sb.append("    payloadSpec: ").append(toIndentedString(payloadSpec)).append("\n");
    sb.append("    synchronization: ").append(toIndentedString(synchronization)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    lastValidatedTime: ").append(toIndentedString(lastValidatedTime)).append("\n");
    sb.append("    endOfValidity: ").append(toIndentedString(endOfValidity)).append("\n");
    sb.append("    insertionTime: ").append(toIndentedString(insertionTime)).append("\n");
    sb.append("    modificationTime: ").append(toIndentedString(modificationTime)).append("\n");
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

