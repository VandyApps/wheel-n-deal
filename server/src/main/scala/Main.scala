
import com.twitter.finatra._

object Main extends App {

  println("=================")
  println("|  Game Server  |")
  println("=================")
  println()

  this.args match {
    case Array("help")    => println(usageGuide)
    case Array()          => bootServer()
    case Array(port)      => bootServer(port)
    case Array(port, env) => bootServer(port, env)
  }

  def bootServer(port: String = "8080", env: String = "development") {
    System.setProperty("com.twitter.finatra.config.env", env)
    System.setProperty("com.twitter.finatra.config.port", s":$port")
    System.setProperty("com.twitter.finatra.config.adminPort", "")
    System.setProperty("com.twitter.finatra.config.appName", "Game Server")
    println(s"Starting $env server on port $port")

    val server = new FinatraServer
    server.register(new GameController)
    server.start()
  }

  lazy val usageGuide = fromResource("/usage.txt")

  def fromResource(filename: String) =
    io.Source.fromInputStream(getClass.getResourceAsStream(filename)).mkString

}

class GameController extends Controller {

  get("/test") { request =>
    render.plain("Hello").toFuture
  }

}