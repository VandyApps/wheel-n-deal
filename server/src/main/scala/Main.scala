
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
    server.register(new GameController(GameSystem.create))
    server.start()
  }

  lazy val usageGuide = fromResource("/usage.txt")

  def fromResource(filename: String) =
    io.Source.fromInputStream(getClass.getResourceAsStream(filename)).mkString

}

final class GameController(val gameSystem: GameSystem) 
    extends Controller with AkkaTwitterBridge {

  import concurrent.ExecutionContext.Implicits.global

  get("/test") { request =>
    /* Returns a web page that tests the HTTP API of this server. */
    render.static("test.html").toFuture
  }

  get("/game") { request =>
    /* Returns an array of all game ids active. */
    gameSystem.games.map( render.json )
  }
  
  post("/game") { request =>
    /* Creates a new game session and returns the session id. Session id
     * is a randomly generated string of unspecified length. 
     */
    gameSystem.createGame().map( render.plain )
  }
  
  get("/game/:gameid/player") { request =>
    /* Returns an array of all player ids active in this game session. */
    val gameid = request.routeParams("gameid")
    gameSystem.players(gameid).map( render.json )
  }
  
  post("/game/:gameid/player") { request =>
    /* Creates a new player session and returns the player id. Player id
     * is a randomly generated string of unspecified length. 
     */
    val gameid = request.routeParams("gameid")
    gameSystem.addPlayer(gameid).map( render.plain )
  }
  
  delete("/game/:gameid/player/:playerid") { request =>
    /* Removes the player with the given id from this game session. */
    val gameid = request.routeParams("gameid")
    val playerid = request.routeParams("playerid")
    gameSystem.removePlayer(gameid, playerid)
    render.plain("Okay").toFuture
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
    val gameid = request.routeParams("gameid")
    val playerid = request.routeParams("playerid")
    gameSystem.getEvents(gameid, playerid, All).map( render.json )
  }
  
  post("/game/:gameid/events") { request =>
    /* Registers a game event. The event should be passed in in the body
     * of the request. The event should be a valid JSON object.
     * 
     * Each event will be assigned a timestamp once it's received by the server.
     * A UUID will be assigned, which identifies all event by its content and
     * time of receipt.
     */
    val gameid = request.routeParams("gameid")
    gameSystem.postEvent(gameid, request.contentString)
    render.plain("Okay").toFuture
  }
  
}

trait AkkaTwitterBridge {
  type SFuture[A] = scala.concurrent.Future[A]
  type TFuture[A] = com.twitter.util.Future[A]
  
  /* Transforms the Future returned by Akka to Twitter's Future. */
  implicit def futurePipe[A](sfuture: SFuture[A]): TFuture[A] = {
    import concurrent.ExecutionContext.Implicits.global
    import util.{Success, Failure}
    
    val p = com.twitter.util.Promise[A]()
    sfuture.onComplete {
      case Success(re) => p.setValue(re)
      case Failure(ex) => p.raise(ex)
    }
    p
  }  
}
