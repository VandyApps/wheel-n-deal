
import org.scalatest.concurrent.ScalaFutures._
import concurrent.ExecutionContext.Implicits.global

class SystemTest 
    extends org.scalatest.FunSuite 
    with org.scalatest.BeforeAndAfter {

  var system: GameSystem = _

  before {
    system = GameSystem.create
  }
  
  after {
    system.shutdown()
    system = null
  }

  test("The game system should have no game sessions initially.") {
    whenReady(system.games) { gs =>
      assert(gs.isEmpty, "The list of game Ids returned should be empty")
    }
  }
  
  test("The game system should have one game session after a game is created") {
    val gameIdFut = system.createGame
    val gamesFut = system.games
    
    val result = for {
      gameId <- gameIdFut
      games <- gamesFut
    } yield (gameId, games)
    
    whenReady(result) { case (gameId, games) =>
      assert(games.length == 1, "The list of game Ids returned should has 1 item")
      assert(games(0) == gameId, 
          "The one item in the game Id list should equal the Id returned earlier")
    }
  }
  
}

class GameTest extends org.scalatest.FunSuite
    with org.scalatest.BeforeAndAfter {
  
  var system: GameSystem = _
  
  before {
    system = GameSystem.create
  }
  
  after {
    system.shutdown()
    system = null
  }
  
  test("A game session should have zero players initially") {
    val playersFut = for {
      game <- system.createGame()
      players <- system.players(game)
    } yield players
    
    whenReady(playersFut) { ps =>
      assert(ps.isEmpty)
    }
  }
  
  test("A game session should have one player after a player is registered") {
    val playersFut = for {
      game <- system.createGame()
      player <- system.addPlayer(game)
      players <- system.players(game)
    } yield (player, players)
    
    whenReady(playersFut) { case (player, players) =>
      assert(players.length == 1)
      assert(player == players(0))
    }
  }
  
  test("A game session should have one less player after one is removed") {
    val playersFut = for {
      game <- system.createGame()
      p <- system.addPlayer(game)
      _ <- system.addPlayer(game)
      _ <- system.addPlayer(game)
      _ <- system.addPlayer(game)
      _ <- system.removePlayer(game, p)
      players <- system.players(game)
    } yield players
    
    whenReady(playersFut) { players =>
      assert(players.length == 3)
    }
  }
  
  test("A game session should be terminated after it has no players anymore") {
    val gamesFut = for {
      gamesBefore <- system.games
      game <- system.createGame()
      p1 <- system.addPlayer(game)
      p2 <- system.addPlayer(game)
      _ <- system.removePlayer(game, p1)
      _ <- system.removePlayer(game, p2)
    } yield gamesBefore
    
    val afterFut = gamesFut.flatMap {
      before => system.games.map(after => (before, after))
    }
    
    whenReady(afterFut) { case (gamesBefore, gamesAfter) =>
      assert(gamesBefore.length == gamesAfter.length)
    }
  }
  
}

class EventTest extends org.scalatest.FunSuite
    with org.scalatest.BeforeAndAfter {
  
  import scala.concurrent.Await
  import scala.concurrent.duration.Duration
  
  var system: GameSystem = _
  var gameSession: String = ""
  
  val sampleEvents = Seq(
    "{event: 5}",
    "{event: 6}",
    "{event: 7}",
    "{event: 8}",
    "{event: 9}",
    "{event: 10}")
  
  before {
    system = GameSystem.create
    val f = system.createGame()
    gameSession = Await.result(f, Duration.Inf)
  }
  
  after {
    system.shutdown()
    system = null
  }
  
  test("A posted event should be returned in an array in the get events call") {
    val sampleEvent = """{name: "Banana Event", message: "Hello toffy"}"""
    val eventsFut = for {
      p1     <- system.addPlayer(gameSession)
      _      <- system.postEvent(gameSession, sampleEvent)
      events <- system.getEvents(gameSession, p1, All)
    } yield events
    
    whenReady(eventsFut) { events =>
      assert(events === s"[$sampleEvent]")
    }
  }
  
  test("Fetching events with the SinceLast flag should only " ++
      "return the events since the last call") {
    
    val eventsFut = for {
      p1     <- system.addPlayer(gameSession)
      _      <- system.addPlayer(gameSession)
      _      <- system.postEvent(gameSession, sampleEvents(0))
      _      <- system.postEvent(gameSession, sampleEvents(1))
      _      <- system.postEvent(gameSession, sampleEvents(2))
      _      <- system.getEvents(gameSession, p1, SinceLast)
      _      <- system.postEvent(gameSession, sampleEvents(3))
      _      <- system.postEvent(gameSession, sampleEvents(4))
      _      <- system.postEvent(gameSession, sampleEvents(5))
      events <- system.getEvents(gameSession, p1, SinceLast)
    } yield events
    
    whenReady(eventsFut) { events =>
      assert(events.containsSlice(sampleEvents(3)))
      assert(events.containsSlice(sampleEvents(4)))
      assert(events.containsSlice(sampleEvents(5)))
      
      assert(!events.containsSlice(sampleEvents(0)))
      assert(!events.containsSlice(sampleEvents(1)))
      assert(!events.containsSlice(sampleEvents(2)))
    }
  }
  
  test("Fetching events with the Fixed flag should only " ++
      "return the specified number of events") {
    
    val eventsFut = for {
      p1     <- system.addPlayer(gameSession)
      _      <- system.addPlayer(gameSession)
      _      <- system.postEvent(gameSession, sampleEvents(0))
      _      <- system.postEvent(gameSession, sampleEvents(1))
      _      <- system.postEvent(gameSession, sampleEvents(2))
      _      <- system.postEvent(gameSession, sampleEvents(3))
      _      <- system.postEvent(gameSession, sampleEvents(4))
      events <- system.getEvents(gameSession, p1, Fixed(3))
    } yield events
    
    whenReady(eventsFut) { events =>
      assert(events.containsSlice(sampleEvents(2)))
      assert(events.containsSlice(sampleEvents(3)))
      assert(events.containsSlice(sampleEvents(4)))
      
      assert(!events.containsSlice(sampleEvents(0)))
      assert(!events.containsSlice(sampleEvents(1)))
    }
  }  
  
}
