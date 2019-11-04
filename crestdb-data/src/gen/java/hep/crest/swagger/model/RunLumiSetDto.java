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
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.GenericMap;
import hep.crest.swagger.model.RunLumiInfoDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 * An RunLumiSet containing RunLumiInfoDto objects.
 */
@ApiModel(description = "An RunLumiSet containing RunLumiInfoDto objects.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-10-30T21:45:06.690+01:00")
public class RunLumiSetDto extends CrestBaseResponse  {
  @JsonProperty("resources")
  private List<RunLumiInfoDto> resources = null;

  public RunLumiSetDto resources(List<RunLumiInfoDto> resources) {
    this.resources = resources;
    return this;
  }

  public RunLumiSetDto addResourcesItem(RunLumiInfoDto resourcesItem) {
    if (this.resources == null) {
      this.resources = new ArrayList<RunLumiInfoDto>();
    }
    this.resources.add(resourcesItem);
    return this;
  }

  /**
   * Get resources
   * @return resources
   **/
  @JsonProperty("resources")
  @ApiModelProperty(value = "")
  public List<RunLumiInfoDto> getResources() {
    return resources;
  }

  public void setResources(List<RunLumiInfoDto> resources) {
    this.resources = resources;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RunLumiSetDto runLumiSetDto = (RunLumiSetDto) o;
    return Objects.equals(this.resources, runLumiSetDto.resources) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resources, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RunLumiSetDto {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    resources: ").append(toIndentedString(resources)).append("\n");
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
