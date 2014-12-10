'use strict';

function listGames() {
  $.ajax({
    url: "game",
    type: 'get',
    contentType: 'application/json',
    dataType: 'json',
    success: function(json) {
      console.log(json);
    },
    error: function( xhr, status, errorThrown ) {
      console.log( "Error: " + errorThrown );
      console.log( "Status: " + status );
    }
  });
}

function createGame() {
  $.ajax({
    url: "game",
    type: 'post',
    contentType: 'application/json',
    dataType: 'text',
    success: function(json) {
      console.log(json);
    },
    error: function( xhr, status, errorThrown ) {
      console.log( "Error: " + errorThrown );
      console.log( "Status: " + status );
    }
  });
}

function listPlayers(game) {
  $.ajax({
    url: "game/" + game + "/player",
    type: 'get',
    contentType: 'application/json',
    dataType: 'json',
    success: function(json) {
      console.log(json);
    },
    error: function( xhr, status, errorThrown ) {
      console.log( "Error: " + errorThrown );
      console.log( "Status: " + status );
    }
  });  
}

function registerPlayer(game) {
  $.ajax({
    url: "game/" + game + "/player",
    type: 'post',
    contentType: 'application/json',
    dataType: 'text',
    success: function(json) {
      console.log(json);
    },
    error: function( xhr, status, errorThrown ) {
      console.log( "Error: " + errorThrown );
      console.log( "Status: " + status );
    }
  });  
}

function removePlayer(game, player) {
  $.ajax({
    url: "game/" + game + "/player/" + player,
    type: 'delete',
    contentType: 'application/json',
    dataType: 'text',
    success: function(json) {
      console.log(json);
    },
    error: function( xhr, status, errorThrown ) {
      console.log( "Error: " + errorThrown );
      console.log( "Status: " + status );
    }
  });  
}

function fetchEvents(game, player, mode) {
  $.ajax({
    url: "game/" + game + "/player/" + player + "/events?mode=all",
    type: 'get',
    contentType: 'application/json',
    dataType: 'json',
    success: function(json) {
      console.log(json);
    },
    error: function( xhr, status, errorThrown ) {
      console.log( "Error: " + errorThrown );
      console.log( "Status: " + status );
    }
  });  
}

function postEvent(game, event) {
  $.ajax({
    url: "game/" + game + "/events",
    type: 'post',
    data: JSON.stringify(event),
    contentType: 'application/json',
    dataType: 'text',
    success: function(json) {
      console.log(json);
    },
    error: function( xhr, status, errorThrown ) {
      console.log( "Error: " + errorThrown );
      console.log( "Status: " + status );
    }
  });  
}

