"use strict";

/*
 * 
 * WARNING : this whole thing might be 100% useless.
 * To implement a long stream, XMLHttpRequest/XDomainRequest is required...
 * 
 * Currently unfinished (by far)
 * 
 */
var JSONPStatic = {
  TRANSPORT    :   "JSONP",

  setRequiredHeader : function(xhr, clientID, transport) {
    xhr.setRequestHeader("client-id",        clientID);
    xhr.setRequestHeader("client-intent",    "WS-FallBack"); 
    xhr.setRequestHeader("client-transport", transport); 
  }
};

var scriptID = 0;

function JSONP(clientId, serverURI, instanceName) {
  // The 4 ones to override
  this.onmessage        = function(mess) {};
  this.onopen           = function() {};
  this.onclose          = function() {};
  this.onerror          = function(error){}; 

  var ping;
  var self = this;
  var clientID = clientId;
  var startJSONPPolling = function() {
    // the url of the script where we send the asynchronous call
    // The instance name is used for the callback generated by the server, 
    // like ws.parseJSONP(xx, xx), where ws IS the instance name.
    var url = serverURI + "/jsonp.json?client-id=" + clientID + 
                          "&callback=parseJSONP" + 
                          "&client-operation=wait-for-input" + 
                          "&rank=" + scriptID.toString() + 
                          "&inst-name=" + instanceName;
                  
    if (OraCloudGlobal.debugLevel > 1)
      console.log("Starting JSONP polling for " + url);
    // create a new script element
    var jsonpScript = document.createElement('script');
    jsonpScript.setAttribute('src', url);
    jsonpScript.setAttribute('defer', 'true'); // @Deprecated
    jsonpScript.setAttribute('async', 'true'); // HTML5
    jsonpScript.setAttribute('id', 'JSONP');
    jsonpScript.setAttribute('rank', (scriptID++).toString());
    // set the src attribute to that url
    jsonpScript.setAttribute('src', url);
    // insert the script in out page
    document.getElementsByTagName('head')[0].appendChild(jsonpScript);

    // Ping
    if (true) {
      // TODO reproduce the long poll one. This one is bugged.
      ping = setInterval(function() {
        var pingXhr;
        try {
          pingXhr = new XMLHttpRequest();
          pingXhr.onreadystatechange = function() {
//          try { console.log("State :" + pingXhr.readyState + ", status:" + pingXhr.status); } catch (err) {}
            if (pingXhr.readyState === OraCloudGlobal.XHR_SOME_RESPONSE_BODY_RECEIVED || 
                pingXhr.readyState === OraCloudGlobal.XHR_REQUEST_COMPLETED) {
//            console.log("Status:" + pingXhr.status);
              if (pingXhr.status === OraCloudGlobal.ERROR_INTERNET_CANNOT_CONNECT ||
                  pingXhr.status === OraCloudGlobal.ERROR_INTERNET_TIMEOUT ||
                  pingXhr.status === OraCloudGlobal.ERROR_INTERNET_NAME_NOT_RESOLVED ||
                  pingXhr.status === OraCloudGlobal.ERROR_INTERNET_CONNECTION_ABORTED ||
                  pingXhr.status === OraCloudGlobal.ERROR_INTERNET_CONNECTION_RESET ||
                  pingXhr.status === OraCloudGlobal.ERROR_HTTP_INVALID_SERVER_RESPONSE ||
                  pingXhr.status === OraCloudGlobal.HTTP_NO_DATA) {
                clearInterval(ping);
                pingXhr.abort();
                self.onerror({err: "Connection Lost (JSONP-XHR) - 1"});
                if (OraCloudGlobal.debugLevel > 1)
                  console.log("Ping failed - 1: Connection lost (" + OraCloudGlobal.GnlUtils.decodeReadyState(pingXhr.readyState) + ")");
              }
              if (pingXhr.readyState === OraCloudGlobal.XHR_REQUEST_COMPLETED && pingXhr.status === OraCloudGlobal.HTTP_OK) {
                // Should be a pong
                var framedResponse = pingXhr.responseText; // not responseText;
                var uInt8Array = framedResponse; // new Uint8Array(framedResponse);
                // console.log("Framed response:" + uInt8Array.length + " byte(s)" + 
                //             ", Fin : " + OraCloudGlobal.FramingUtils.getFin(uInt8Array) +
                //             ", OpCode : " + OraCloudGlobal.FramingUtils.readableOpCode(OraCloudGlobal.FramingUtils.getOpCode(uInt8Array))+
                //             ", Payload length : " + OraCloudGlobal.FramingUtils.getPayloadLength(uInt8Array));
                if (OraCloudGlobal.FramingUtils.getPayloadLength(uInt8Array) !== 0)
                  console.log("Payload size should be 0 for a pong.");
                else if (OraCloudGlobal.debugLevel > 20)
                  console.log("Got the right pong.")                   
              }
            }
          };
          pingXhr.open("POST", serverURI + "?uid=" + Math.random().toString(), true); // Asynchronously, uid avoids caching.
//        pingXhr.overrideMimeType("text/plain; charset=x-user-defined");
//        try { pingXhr.responseType = 'arraybuffer'; } catch (err) {} // Chrome does not like this when called Synchronously...
          JSONPStatic.setRequiredHeader(pingXhr, clientID, JSONPStatic.TRANSPORT); 
          pingXhr.setRequestHeader("client-operation", "ping"); 
          pingXhr.send(OraCloudGlobal.FramingUtils.binaryString(OraCloudGlobal.FramingUtils.frameMessage("", OraCloudGlobal.FramingUtils.PING)));
          if (OraCloudGlobal.debugLevel > 20)
            console.log(JSONPStatic.TRANSPORT + " ...ping");
        }
        catch (err) {
          self.onerror({err: "Connection Lost (JSONP-XHR) - 2"});
          if (OraCloudGlobal.debugLevel > 2) {
            console.log("Ping failed: Connection lost - 2");
            console.log("Ping failed - 2: Connection lost (" + OraCloudGlobal.GnlUtils.decodeReadyState(pingXhr.readyState) + ")");
          }
          pingXhr.abort();
          clearInterval(ping);
        }
      }, OraCloudGlobal.PING_INTERVAL); // Ping
    }
    
  };

  /*
   * This one is the callback made from JSONP (see the serverURI above)
   * 
   * @param {type} json
   * @param {type} rnk
   */
  this.parseJSONP = function(json, rank) {
    // TODO Deframe
    // 
    // Response is received in the json parameter
    console.log("JSONP response:" + JSON.stringify(json));
    
    // rebuild binary data
    if (json.bindata !== undefined) {
      var bin = OraCloudGlobal.GnlUtils.base16decode(json.bindata);
      json = bin;
    }  
    try { 
      this.onmessage(json); 
    } catch (err) { 
      console.log(err.message); 
    }
    // Do we already have the JSONP Script element ?
    var scripts = document.getElementsByTagName('script');
    var jsonpScript;
    for (var i=0; i<scripts.length; i++) {
      var jsonp = false;
      var rnk = -1;
      var attr = scripts[i].attributes;
      for (var j=0; j<attr.length; j++) {
//      console.log("Attr:" + attr.item(j).nodeName + "=" + attr.item(j).nodeValue);
        if ("id" === attr.item(j).nodeName && "JSONP" === attr.item(j).nodeValue)
          jsonp = true;
        if ("rank" === attr.item(j).nodeName)
          rnk = attr.item(j).nodeValue;
      }
      if (jsonp && rnk === rank) {
        jsonpScript = scripts[i];
        document.getElementsByTagName('head')[0].removeChild(jsonpScript);
        break;
      }
    }
    // Restart polling
    startJSONPPolling();
  };

  var postRequest = function(payload) {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {};
    xhr.open("POST", serverURI, true); // true:Async, false:Sync. We dont care about the response
//  try { xhr.responseType = 'arraybuffer'; } catch (err) {}
    JSONPStatic.setRequiredHeader(xhr, clientID, JSONPStatic.TRANSPORT); 
    xhr.setRequestHeader("client-operation", "send-message"); 
    xhr.setRequestHeader('Content-Type', 'text/plain; charset=utf-8');
    var framed = OraCloudGlobal.FramingUtils.frameMessage(payload, OraCloudGlobal.FramingUtils.UTF_8_DATA);  // Frame the payload
//  try { xhr.setRequestHeader('Content-Length', framed.length.toString()); } catch (err) {}
    xhr.setRequestHeader('cache-control', 'no-cache');
    xhr.setRequestHeader('pragma', 'no-cache');
    xhr.send(OraCloudGlobal.FramingUtils.binaryString(framed));
  };

  this.send = function(msg) {
    if (!msg) {
      return;
    }
    postRequest(msg);
  };

  this.close = function() {
    // clearInterval(ping);
  };
  
  this.quit = function() {
    this.close();
  }
  
  // Initial poll
  setTimeout(function() { startJSONPPolling(); }, 1000); // UGLY!! For the window.onload to be able to execute... FIXME
};

