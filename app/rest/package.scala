import org.nullvector.api.json.JsonMapper.typeNaming
import play.api.libs.json.JsonConfiguration
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.OptionHandlers.WritesNull

package object rest {

  val defaultJsonConfiguration: JsonConfiguration = JsonConfiguration(
    SnakeCase,
    typeNaming = typeNaming,
    discriminator = "type",
    optionHandlers = WritesNull
  )

}