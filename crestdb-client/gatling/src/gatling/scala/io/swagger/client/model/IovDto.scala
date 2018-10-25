
package io.swagger.client.model

import java.util.Date

case class IovDto (
    _tagName: Option[String],
    _since: Option[Number],
    _insertionTime: Option[Date],
    _payloadHash: Option[String]
)
object IovDto {
    def toStringBody(var_tagName: Object, var_since: Object, var_insertionTime: Object, var_payloadHash: Object) =
        s"""
        | {
        | "tagName":$var_tagName,"since":$var_since,"insertionTime":$var_insertionTime,"payloadHash":$var_payloadHash
        | }
        """.stripMargin
}
