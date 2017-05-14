package logging

import com.typesafe.scalalogging.Logger

trait Log {
  val log = Logger(this.getClass)
}
