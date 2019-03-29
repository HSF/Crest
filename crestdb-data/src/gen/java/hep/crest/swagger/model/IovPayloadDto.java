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
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-03-29T17:06:31.512+01:00")
public class IovPayloadDto   {
  @JsonProperty("since")
  private BigDecimal since = null;

  @JsonProperty("payload")
  private String payload = null;

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

  public IovPayloadDto payload(String payload) {
    this.payload = payload;
    return this;
  }

  /**
   * Get payload
   * @return payload
   **/
  @JsonProperty("payload")
  @ApiModelProperty(value = "")
  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
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
        Objects.equals(this.payload, iovPayloadDto.payload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(since, payload);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IovPayloadDto {\n");
    
    sb.append("    since: ").append(toIndentedString(since)).append("\n");
    sb.append("    payload: ").append(toIndentedString(payload)).append("\n");
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

