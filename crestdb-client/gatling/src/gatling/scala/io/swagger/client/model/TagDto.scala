
package io.swagger.client.model

import java.util.Date

case class TagDto (
    _name: Option[String],
    _timeType: Option[String],
    _objectType: Option[String],
    _synchronization: Option[String],
    _description: Option[String],
    _lastValidatedTime: Option[Number],
    _endOfValidity: Option[Number],
    _insertionTime: Option[Date],
    _modificationTime: Option[Date]
)
object TagDto {
    def toStringBody(var_name: Object, var_timeType: Object, var_objectType: Object, var_synchronization: Object, var_description: Object, var_lastValidatedTime: Object, var_endOfValidity: Object, var_insertionTime: Object, var_modificationTime: Object) =
        s"""
        | {
        | "name":$var_name,"timeType":$var_timeType,"objectType":$var_objectType,"synchronization":$var_synchronization,"description":$var_description,"lastValidatedTime":$var_lastValidatedTime,"endOfValidity":$var_endOfValidity,"insertionTime":$var_insertionTime,"modificationTime":$var_modificationTime
        | }
        """.stripMargin
}
