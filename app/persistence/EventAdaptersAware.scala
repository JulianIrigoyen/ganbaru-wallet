package persistence

import org.nullvector.EventAdapter
import reactivemongo.api.bson.MacroConfiguration

trait EventAdaptersAware {

  def eventAdapters(implicit macroConfiguration: MacroConfiguration): Seq[EventAdapter[_]]

}
