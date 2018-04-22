import mill._
import mill.scalalib._

object controller extends ScalaModule {
  def scalaVersion = "2.12.4"
  def mainClass = Some("org.habajca.mcpoc.controller.Server")
}
