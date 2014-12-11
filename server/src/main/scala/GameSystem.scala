
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor._
import akka.pattern.{ask, pipe}
import akka.util.Timeout

trait GameSystem {
  
  def games: Future[Seq[String]]
  
  def createGame(): Future[String]
  
  def players(game: String): Future[Seq[String]]
  
  def addPlayer(game: String): Future[String]
  
  def removePlayer(game: String, player: String): Future[Unit]
  
  def postEvent(game: String, event: String): Future[Unit]
  
  def getEvents(game: String, player: String, opts: EventOption): Future[String]
  
}

object GameSystem {
  
  def create: GameSystem = new AkkaGameSystem
  
  def uuid = java.util.UUID.randomUUID.toString
  
  implicit val timeout: Timeout = 5.second
  import concurrent.ExecutionContext.Implicits.global
  
  
  
  private class Game extends Actor {
    import Game._
    
    private var players = Map.empty[String, ActorRef]
    
    def receive = {
      case Players => sender ! Players(players.keys.toSeq)
      
      case AddPlayer =>
        val newPlayer = uuid
        players += (newPlayer -> context.actorOf(Props[Player], newPlayer))
        sender ! AddPlayer(newPlayer)
        
      case Remove(player) =>
        players -= player
        
      case PostEvent(event) =>
        players foreach { case (_, p) => p ! PostEvent(event) }
      
      case FetchEvent(player, ops) =>
        (players(player) ? Player.FetchEvent(ops)) pipeTo sender
    }
  }
  
  private object Game {
    
    case class Players(ids: Seq[String])
    case object Players
    
    case object AddPlayer
    case class AddPlayer(id: String)
    
    case class Remove(player: String)
    
    case class PostEvent(event: String)
    
    case class Events(events: String)
    case class FetchEvent(player: String, ops: EventOption)
    
  }
  
  
  
  private final class Player extends Actor {
    import Player._
    
    private var events = Vector.empty[String]
    private var lastPosition = 0
    
    def receive = {
      case Game.PostEvent(event) => 
        events = event +: events
        lastPosition += 1
        
      case FetchEvent(ops) =>
        val requested = ops match {
          case All => 
            events
          case SinceLast =>
            val fresh = lastPosition
            lastPosition = 0
            events.take(fresh)
          case Fixed(n) =>
            events.take(n)
            
        }
        sender ! Game.Events(s"""[${requested.mkString(",")}]""")
    }
  }
  
  private object Player {
    case class FetchEvent(ops: EventOption)
  }
  
  
  
  private final class AkkaGameSystem extends GameSystem {
    val system = ActorSystem("GameSystem")
  
    var gamesRegister = Map.empty[String, ActorRef]
    
    def games = Future.successful(gamesRegister.keys.toSeq)
    
    def createGame() = {
      val newGame = uuid
      gamesRegister += (newGame -> system.actorOf(Props[Game], newGame))
      Future.successful(newGame)
    }
    
    def players(game: String) =
      gamesRegister.get(game) match {
        case Some(g) => (g ? Game.Players).mapTo[Game.Players].map(_.ids)
        case None    => Future.failed(noGame)
      }
    
    def addPlayer(game: String) = {
      val newPlayer = uuid
      gamesRegister.get(game) match {
        case Some(g) => (g ? Game.AddPlayer).mapTo[Game.AddPlayer].map(_.id)
        case None    => Future.failed(noGame)
      }
    }
    
    def removePlayer(game: String, player: String) = {
      gamesRegister.get(game) match { 
        case Some(g) => 
          g ! Game.Remove(player) 
          (g ? Game.Players).mapTo[Game.Players] map { ps =>
            if (ps.ids.isEmpty) {
              g ! PoisonPill
              gamesRegister -= game
              ()
            }
          }
        case None => Future.successful(())
      }
    }
    
    def postEvent(game: String, event: String) = {
      gamesRegister.get(game) foreach { g =>
        g ! Game.PostEvent(event)
      }
      Future.successful(())
    }
    
    def getEvents(game: String, player: String, opts: EventOption) = {
      gamesRegister.get(game) match {
        case Some(g) => 
          (g ? Game.FetchEvent(player, opts))
            .mapTo[Game.Events].map(_.events)
        case None => 
          Future.failed(noGame)
      }
    }
    
    def noGame = 
      new IllegalArgumentException("No game with that session id exist")
    
  }
  
}

sealed trait EventOption
case object All extends EventOption
case object SinceLast extends EventOption
case class Fixed(count: Int) extends EventOption

object EventOption {
  val numberMatcher = "[0-9]+".r
  
  def unapply(s: String): Option[EventOption] = s match {
    case "all"            => Some(All)
    case "latest"         => Some(SinceLast)
    case numberMatcher(n) => Some(Fixed(n.toInt))
    case _                => None
  }
}

object GameId {
  def unapply(map: Map[String, String]): Option[String] = map.get("gameid")
}

object PlayerId {
  def unapply(map: Map[String, String]): Option[String] = map.get("playerid")
}
