
package io.swagger.client.model

import java.util.HashMap

case class GenericMap (
    _name: Option[String]
)
object GenericMap {
    def toStringBody(var_name: Object) =
        s"""
        | {
        | "name":$var_name
        | }
        """.stripMargin
}
