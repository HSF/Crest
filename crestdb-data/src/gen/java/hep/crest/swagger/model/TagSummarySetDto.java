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
import hep.crest.swagger.model.TagSummaryDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 * An Set containing TagSummaryDto objects.
 */
@ApiModel(description = "An Set containing TagSummaryDto objects.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2019-10-03T10:49:50.724+02:00")
public class TagSummarySetDto extends CrestBaseResponse  {
  @JsonProperty("resources")
  private List<TagSummaryDto> resources = null;

  public TagSummarySetDto resources(List<TagSummaryDto> resources) {
    this.resources = resources;
    return this;
  }

  public TagSummarySetDto addResourcesItem(TagSummaryDto resourcesItem) {
    if (this.resources == null) {
      this.resources = new ArrayList<TagSummaryDto>();
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
  public List<TagSummaryDto> getResources() {
    return resources;
  }

  public void setResources(List<TagSummaryDto> resources) {
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
    TagSummarySetDto tagSummarySetDto = (TagSummarySetDto) o;
    return Objects.equals(this.resources, tagSummarySetDto.resources) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resources, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TagSummarySetDto {\n");
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

