"use strict";

var NativeWebSocketStatic = {
  TRANSPORT : "WebSocket",
  
  WebSocketReadyState : {
    CONNECTING : 0,
    OPEN       : 1,
    CLOSING    : 2,
    CLOSED     : 3
  }
};

/*
 * NativeWebSocket constructor
 * <br>
 * This should <b>NEVER</b> be invoked nor used by the client application.
 * <br>
 * It will be invoked by the WebSocket object when required or necessary.
 * @constructor 
 * @param {WebSocket} proto
 * @param {string} serverURI
 * @returns {NativeWebSocket}
 */
function NativeWebSocket(proto, serverURI) {
  var self = this;

  // The ones to override
  this.onmessage        = function(mess) {};
  this.onopen           = function() {};
  this.onclose          = function() {};
  this.onerror          = function(error){}; 

  var ws = new proto(serverURI);

  ws.onmessage = function(mess) { self.onmessage(mess); };
  ws.onopen    = function() { self.onopen; };
  ws.onclose   = function() { self.onclose; };
  ws.onerror   = function(error) {
                   self.onerror(error);
                   clearInterval(ping);
                 };

  this.send = function(msg) {
    if (!msg) {
      return;
    }
    ws.send(msg);
  };

  this.close = function() {
    ws.onclose();
    ws.close();
    clearInterval(ping);
  };

  var ping = setInterval(function() {
//	console.log(NativeWebSocketStatic.TRANSPORT + " ...ping.")
    if (ws.readyState !== NativeWebSocketStatic.WebSocketReadyState.OPEN) {
      clearInterval(ping);
      self.onerror({ err : '(Native, ping) Connection lost' });
      if (OraSocketGlobal !== undefined && OraSocketGlobal.debugLevel > 10)
        console.log("   Native WebSocket ping readyState " + ws.readyState);
    }
    else {
//    	console.log('WebSocket is living....');
    }
  }, OraSocketGlobal.PING_INTERVAL); // Ping
  
  this.quit = function() {
    try { 
    clearInterval(ping); 
    } catch (err) {
      console.log("(Native) quit:" + JSON.stringify(err));
    }
  }
};

