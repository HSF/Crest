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
import javax.validation.constraints.*;

/**
 * IovPayloadDto
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-12-26T18:15:33.531+01:00")
public class IovPayloadDto   {
  @JsonProperty("since")
  private BigDecimal since = null;

  @JsonProperty("version")
  private String version = null;

  @JsonProperty("objectType")
  private String objectType = null;

  @JsonProperty("size")
  private Integer size = null;

  @JsonProperty("payloadHash")
  private String payloadHash = null;

  public IovPayloadDto since(BigDecimal since) {
    this.since = since;
    return this;
  }

  /**
   * Get since
   * @return since
   **/
  @JsonProperty("since")
  @ApiModelProperty(value = "")
  public BigDecimal getSince() {
    return since;
  }

  public void setSince(BigDecimal since) {
    this.since = since;
  }

  public IovPayloadDto version(String version) {
    this.version = version;
    return this;
  }

  /**
   * Get version
   * @return version
   **/
  @JsonProperty("version")
  @ApiModelProperty(value = "")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public IovPayloadDto objectType(String objectType) {
    this.objectType = objectType;
    return this;
  }

  /**
   * Get objectType
   * @return objectType
   **/
  @JsonProperty("objectType")
  @ApiModelProperty(value = "")
  public String getObjectType() {
    return objectType;
  }

  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  public IovPayloadDto size(Integer size) {
    this.size = size;
    return this;
  }

  /**
   * Get size
   * @return size
   **/
  @JsonProperty("size")
  @ApiModelProperty(value = "")
  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public IovPayloadDto payloadHash(String payloadHash) {
    this.payloadHash = payloadHash;
    return this;
  }

  /**
   * Get payloadHash
   * @return payloadHash
   **/
  @JsonProperty("payloadHash")
  @ApiModelProperty(value = "")
  public String getPayloadHash() {
    return payloadHash;
  }

  public void setPayloadHash(String payloadHash) {
    this.payloadHash = payloadHash;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IovPayloadDto iovPayloadDto = (IovPayloadDto) o;
    return Objects.equals(this.since, iovPayloadDto.since) &&
        Objects.equals(this.version, iovPayloadDto.version) &&
        Objects.equals(this.objectType, iovPayloadDto.objectType) &&
        Objects.equals(this.size, iovPayloadDto.size) &&
        Objects.equals(this.payloadHash, iovPayloadDto.payloadHash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(since, version, objectType, size, payloadHash);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IovPayloadDto {\n");
    
    sb.append("    since: ").append(toIndentedString(since)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    objectType: ").append(toIndentedString(objectType)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    payloadHash: ").append(toIndentedString(payloadHash)).append("\n");
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

