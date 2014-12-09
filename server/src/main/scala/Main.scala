
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
    /* Returns a web page that tests the HTTP API of this server. */
    render.static("test.html").toFuture
  }

  get("/game") { request =>
    /* Returns an array of all game ids active. */
    render.plain("okay").toFuture
  }
  
  post("/game") { request =>
    /* Creates a new game session and returns the session id. Session id
     * is a randomly generated string of unspecified length. 
     */
    render.plain("okay").toFuture
  }
  
  get("/game/:gameid/player") { request =>
    /* Returns an array of all player ids active in this game session. */
    render.plain("okay").toFuture
  }
  
  post("/game/:gameid/player") { request =>
    /* Creates a new player session and returns the player id. Player id
     * is a randomly generated string of unspecified length. 
     */
    render.plain("okay").toFuture
  }
  
  delete("/game/:gameid/player/:playerid") { request =>
    /* Removes the player with the given id from this game session.
     */
    render.plain("okay").toFuture
  }
  
  get("/game/:gameid/player/:playerid/events") { request =>
    /* Returns game events.
     * 
     * Options:
     *     mode ->
     *         all    | Returns all events (default)
     *         latest | Returns only the events since the last request
     *         [0-9]+ | Returns the specified number of events
     * 
     */
    render.plain("okay").toFuture
  }
  
  post("/game/:gameid/player/:playerid/events") { request =>
    /* Registers a game event. The event should be passed in in the body
     * of the request. The event should be a valid JSON object.
     * 
     * Each event will be assigned a timestamp once it's received by the server.
     * A UUID will be assigned, which identifies all event by its content and
     * time of receipt.
     */
    render.plain("okay").toFuture
  }
  
}
