
package io.swagger.client.model


case class GroupDto (
    _groups: Option[List[Number]]
)
object GroupDto {
    def toStringBody(var_groups: Object) =
        s"""
        | {
        | "groups":$var_groups
        | }
        """.stripMargin
}
