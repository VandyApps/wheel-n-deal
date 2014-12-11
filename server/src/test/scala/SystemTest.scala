
import org.scalatest.concurrent.ScalaFutures._
import concurrent.ExecutionContext.Implicits.global

class SystemTest extends org.scalatest.FunSuite {

  test("The game system should have no game sessions initially.") {
    val system = GameSystem.create
    
    whenReady(system.games) { gs =>
      assert(gs.isEmpty, "The list of game Ids returned should be empty")
    }
  }
  
  test("The game system should have one game session after a game is created") {
    val system = GameSystem.create
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

class GameTest extends org.scalatest.FunSuite {
  
  val system = GameSystem.create
  
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
