/*
 * Generic Client Side WebSocket
 */
"use strict";

/**
 * Some debugging utilities, Watch Functions.
 * @namespace wf
 */
var wf = {
  // Debug Utilities, to use as watches
  /**
   * Return a Readable Operation Code
   * @function oc
   * @memberof wf
   * @param {uInt8Array} framedData The full data; may contain several frames
   * @param {integer} currentFrameIndex The index of the frame with the data above, zero based.
   * @returns The readable Operation Code within frame identified by its index (above).
   * @inner
   */
  oc : function(framedData, currentFrameIndex) {
    // Readable Op Code
    return OraSocketGlobal.FramingUtils.readableOpCode(OraSocketGlobal.FramingUtils.getOpCode(framedData, currentFrameIndex)); 
  },
  /**
   * Display Mask
   * @function dm
   * @memberof wf
   * @param {uInt8Array} framedData The full data; may contain several frames
   * @param {integer} currentFrameIndex The index of the frame with the data above, zero based.
   * @returns The string containing the 4 element mask, displayed in hexadecimal, within frame identified by its index (above).
   * @inner
   */
  dm : function(framedData, currentFrameIndex) {
    // Display Mask
    return OraSocketGlobal.FramingUtils.displayMask(OraSocketGlobal.FramingUtils.getMaskingKey(framedData, currentFrameIndex)); 
  },
  /**
   * Dump Frame Header
   * @function dh
   * @memberof wf
   * @param {uInt8Array} framedData The full data; may contain several frames
   * @param {integer} currentFrameIndex The index of the frame with the data above, zero based.
   * @returns The string containing the frame header (whatever is before the payload), displayed in hexadecimal, within frame identified by its index (above).
   * @inner
   */
  dh : function(framedData, currentFrameIndex) {
    // Dump Frame Header
    return OraSocketGlobal.FramingUtils.dumpHeader(framedData, currentFrameIndex); 
  },
  /**
   * Dump Payload
   * <br>
   * Might not be suitable for non-text data.
   * @function dp
   * @memberof wf
   * @param {uInt8Array} framedData The full data; may contain several frames
   * @param {integer} currentFrameIndex The index of the frame with the data above, zero based.
   * @returns The string containing the decrypted payload, displayed in hexadecimal, within frame identified by its index (above).
   * @inner
   */
  dp : function(framedData, currentFrameIndex) { 
    // Display Payload (good for utf8)
    return OraSocketGlobal.FramingUtils.getUnframedTextData(framedData, currentFrameIndex);
  }
};

var OraSocketGlobal = {
  debugLevel         :    0, // Tunes the verbose level. 0 means mute.
  
  PING_INTERVAL      : 1000, // in ms. Overriden by orasocket.config
  PING_ENABLED       : true, // Does the client ping the server? Overriden by orasocket.config
  encodingBase       :   16, // Used for IE, to encode the frames (so they fit in a real string). Must be in sync with the server side encoding.
  ENCODE_FOR_IE_BELOW:   10, // BaseXX encoding required for IE, below (strictly, < ) this version number. Encoding base (XX) is encodingBase, above. Overriden by orasocket.config
  
  WEBSOCKET_CREATION_TIMEOUT: 1000, // Consider that the WS creation has failed if not completed within this amount of ms
  NB_TRY_FOR_EACH_TRANSPORT :    2, // Overriden by orasocket.config
  /*
   * If the connection has failed after less than this value (in ms), then try a different transport.
   * If the connection has failed after being connected long enough (ie beyond that value), then try to connect again, with the same transport.
   */
  TRY_AGAIN_INTERVAL : 10000, // 10s. Overriden by orasocket.config
  ENFORCE_ENCODING   : false, // Overriden by orasocket.config

  wsAvailable  : false,
  xhrAvailable : false, 

  $nativeWS : {},

// XHR readyState codes
  XHR_UNINITIALIZED               : 0, // UNSENT
  XHR_LOADING                     : 1, // OPENED
  XHR_RESPONSE_HEADERS_RECEIVED   : 2, // HEADERS_RECEIVED
  XHR_SOME_RESPONSE_BODY_RECEIVED : 3, // LOADING
  XHR_REQUEST_COMPLETED           : 4, // DONE

// XHR (HTTP) Status codes
  HTTP_NO_DATA     :   0,
  HTTP_OK          : 200,
  HTTP_ERROR_1     : 403,
  HTTP_ERROR_2     : 404,
  HTTP_SERVER_ERROR: 500,
  HTTP_ERROR_RETRY : 503,

  ERROR_INTERNET_TIMEOUT             : 12002,
  ERROR_INTERNET_NAME_NOT_RESOLVED   : 12007,
  ERROR_INTERNET_CANNOT_CONNECT      : 12029,
  ERROR_INTERNET_CONNECTION_ABORTED  : 12030,
  ERROR_INTERNET_CONNECTION_RESET    : 12031,
  ERROR_HTTP_INVALID_SERVER_RESPONSE : 12152,

  NATIVE_WEB_SOCKET : "WebSocket",
  XML_HTTP_REQUEST  : "XMLHttpRequest",
  JSONP             : "JSONP",
  
  fallbackOrder       : [],  // Populated at startup with the above
  availableTransports : [],  // Populated at the first POST request

  clientID : {},
  requestQueue : {},

  XMLHttpRequest : function() {
    // Always return an XMLHttpRequest. Distinction will be made in LongPoll.js
    if (false && navigator.appName === "Microsoft Internet Explorer" && OraSocketGlobal.getIEVersion() < 10) {
      console.log("Instantiating an XDomainRequest");
      return new XDomainRequest();
    } else {
      return new XMLHttpRequest();
    }
  },

  // Returns the version of Windows Internet Explorer or a -1
  // (indicating the use of another browser).
  getIEVersion : function() {
    var rv = -1; // Return value assumes failure.
    if (navigator.appName === 'Microsoft Internet Explorer') {
      var ua = navigator.userAgent;
      var re  = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
      if (re.exec(ua) !== null)
        rv = parseFloat( RegExp.$1 );
    }
     return rv;
  },
  
  Queue : function() {
    var queue = [];

    this.getSize = function() {
      return queue.length;
    };

    this.isEmpty = function() {
      return queue.length === 0;
    };

    this.dequeue = function() {
      var obj = null;
      if (!this.isEmpty()) {
        obj = queue[0];
        queue.splice(0, 1);
      }
      return obj;
    };

    this.enqueue = function(obj) {
      queue.push(obj);
    };
  },

  FramingUtils : {
  /*
  +-+-+-+-+-------+-+-------------+-------------------------------+
   0                   1                   2                   3
   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
  +-+-+-+-+-------+-+-------------+-------------------------------+
  |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
  |I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
  |N|V|V|V|       |S|             |   (if payload len==126/127)   |
  | |1|2|3|       |K|             |                               |
  +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
  |     Extended payload length continued, if payload len == 127  |
  + - - - - - - - - - - - - - - - +-------------------------------+
  |                               | Masking-key, if MASK set to 1 |
  +-------------------------------+-------------------------------+
  | Masking-key (continued)       |          Payload Data         |
  +-------------------------------- - - - - - - - - - - - - - - - +
  :                     Payload Data continued ...                :
  + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
  |                     Payload Data continued ...                |
  +---------------------------------------------------------------+
   */

  /*-----------------------------------------------
     Masking for arrays of 8 bit words (aka byte)
   ------------------------------------------------*/

    // Frames opcodes
    PAYLOAD_CONTINUES_FROM_LAST:   0x0,
    UTF_8_DATA                 :   0x1,
    BINARY_DATA                :   0x2,
    TERMINATES_CONNECTION      :   0x8,
    PING                       :   0x9,
    PONG                       :   0xA,

    formatHexa : function(str, len) {
      return OraSocketGlobal.FramingUtils.lpad(str, len, '0');
    },

    lpad : function(str, upto, filler) {
      var s = str;
      while (s.length < upto)
        s = filler + s;
      return s;
    },

    getFin : function(data, fromIdx) {
      if (fromIdx === undefined)
        fromIdx = 0;
      return ((data[fromIdx + 0] & 0x80) >>> 7) & 0xFF;
    },

    getOpCode : function(data, fromIdx) {
      if (fromIdx === undefined)
        fromIdx = 0;
      return (data[fromIdx + 0] & 0x0F) & 0xFF;
    },

    getMask : function(data, fromIdx) {
      // Is there a mask?
      if (fromIdx === undefined)
        fromIdx = 0;
      return ((data[fromIdx + 1] & 0x80) >>> 7) & 0xFF;
    },

    getPayloadOffset : function(data, fromIdx) {
      if (fromIdx === undefined)
        fromIdx = 0;

      var offset = 2;
      var payloadLength = data[fromIdx + 1] & 0x7F;
      var mask = OraSocketGlobal.FramingUtils.getMask(data, fromIdx);
      if (payloadLength === 126)
        offset += 2;
      else if (payloadLength === 127)
        offset += 8;

      if (mask === 1)
        offset += 4;

      return offset;
    },

    getMaskOffset : function(data, fromIdx) {
      if (fromIdx === undefined)
        fromIdx = 0;

      var offset = 2;
      var payloadLength = data[fromIdx + 1] & 0x7F;
      if (payloadLength === 126)
        offset += 2;
      else if (payloadLength === 127)
        offset += 8;

      return offset;
    },

    getPayloadLength : function(data, fromIdx) {
      if (fromIdx === undefined)
        fromIdx = 0;

      var payloadLength = data[fromIdx + 1] & 0x7F;
      if (payloadLength === 126) {
        // 16 bits
        var part1 = data[fromIdx + 2];
        var part2 = data[fromIdx + 3];
    //  console.log(formatHexa(part1.toString(16), 2) + " | " + formatHexa(part2.toString(16), 2))
        payloadLength = (part1 << 8) | part2;
      } else if (payloadLength === 127) {
        // 64 bits
        var part1 = data[fromIdx + 2];
        var part2 = data[fromIdx + 3];
        var part3 = data[fromIdx + 4];
        var part4 = data[fromIdx + 5];
        var part5 = data[fromIdx + 6];
        var part6 = data[fromIdx + 7];
        var part7 = data[fromIdx + 8];
        var part8 = data[fromIdx + 9];
        var fullString = OraSocketGlobal.FramingUtils.formatHexa(part1.toString(16), 2) +   
                         OraSocketGlobal.FramingUtils.formatHexa(part2.toString(16), 2) + 
                         OraSocketGlobal.FramingUtils.formatHexa(part3.toString(16), 2) + 
                         OraSocketGlobal.FramingUtils.formatHexa(part4.toString(16), 2) + 
                         OraSocketGlobal.FramingUtils.formatHexa(part5.toString(16), 2) + 
                         OraSocketGlobal.FramingUtils.formatHexa(part6.toString(16), 2) + 
                         OraSocketGlobal.FramingUtils.formatHexa(part7.toString(16), 2) + 
                         OraSocketGlobal.FramingUtils.formatHexa(part8.toString(16), 2);
        if (OraSocketGlobal.debugLevel > 4)
          console.debug("0x" + fullString);
        payloadLength = parseInt(fullString, 16);
      }
      return payloadLength;
    },

    /* returns an array of 4 bytes */
    getMaskingKey : function(data, fromIdx) {
      if (fromIdx === undefined)
        fromIdx = 0;

      var offset = OraSocketGlobal.FramingUtils.getMaskOffset(data, fromIdx);
      var part1 = data[offset + fromIdx];
      var part2 = data[offset + fromIdx + 1];
      var part3 = data[offset + fromIdx + 2];
      var part4 = data[offset + fromIdx + 3];
      if (OraSocketGlobal.debugLevel > 100) {
        var displayString =  "0x" + OraSocketGlobal.FramingUtils.formatHexa(part1.toString(16), 2) +
                            " 0x" + OraSocketGlobal.FramingUtils.formatHexa(part2.toString(16), 2) +
                            " 0x" + OraSocketGlobal.FramingUtils.formatHexa(part3.toString(16), 2) + 
                            " 0x" + OraSocketGlobal.FramingUtils.formatHexa(part4.toString(16), 2);
        console.debug("Mask : " + displayString);
      }
      return [part1, part2, part3, part4];
    },

    dumpHeader : function(data, fromIdx) {
      var po = OraSocketGlobal.FramingUtils.getPayloadOffset(data, fromIdx);
      var str = "";
      for (var i=0; i<po; i++)
        str += "[" + OraSocketGlobal.FramingUtils.formatHexa(data[fromIdx + i].toString(16), 2) + "] ";
      return str;
    },

    getFrameLength : function(data, fromIdx) {
      if (fromIdx === undefined)
        fromIdx = 0;

      var payloadOffset = OraSocketGlobal.FramingUtils.getPayloadOffset(data, fromIdx);
      var payloadLength = OraSocketGlobal.FramingUtils.getPayloadLength(data, fromIdx);

      return payloadOffset + payloadLength; // - fromIdx;
    },

    displayMask : function(mask) {
      var str = "";
      for (var i=0; i<mask.length; i++)
        str += ("0x" + OraSocketGlobal.FramingUtils.formatHexa(mask[i].toString(16), 2) + " ");
      return str;
    },

    readableOpCode : function(opcode) {
      var meaning;
      switch (opcode) {
        case OraSocketGlobal.FramingUtils.PAYLOAD_CONTINUES_FROM_LAST:
          meaning = "Payload continues";
          break;
        case OraSocketGlobal.FramingUtils.UTF_8_DATA:
          meaning = "UTF8 data";
          break;
        case OraSocketGlobal.FramingUtils.BINARY_DATA:
          meaning = "Binary data";
          break;
        case OraSocketGlobal.FramingUtils.TERMINATES_CONNECTION:
          meaning = "Connection terminated";
          break;
        case OraSocketGlobal.FramingUtils.PING:
          meaning = "Ping";
          break;
        case OraSocketGlobal.FramingUtils.PONG:
          meaning = "Pong";
          break;
        default:
          meaning = "Unkonwn opcode [0x" + opcode.toString(16) + "]";
          break;
      }
      return meaning;
    },

    generateMask : function() {
      var maskKey = [0x10, 0x20, 0x30, 0x40]; // Hard coded
      for (var i=0; i<4; i++)
        maskKey[i] = Math.floor(Math.random() * 128); // Random generated
      return maskKey;
    },

    /*
     * Encoding
     */
    frameMessage : function(response, opcode, mask, fin) {
      if (response.length > 0 && OraSocketGlobal.debugLevel > 1)
        console.debug("Message to Frame:[" + response + "]");

      if (mask === undefined)
        mask = 1;
      if (fin === undefined)
        fin  = 1; 

      var maskKey = [ 0x0, 0x0, 0x0, 0x0 ];
      if (mask === 1)
        maskKey = OraSocketGlobal.FramingUtils.generateMask();

      var payloadLength = response.length;
      if (payloadLength > 0 && OraSocketGlobal.debugLevel > 1) // Not a ping or pong
        console.debug("Message length:" + payloadLength + " :" + response);

      var frame = [];

      var byte = (fin << 7) | opcode;
    //console.log("fin | opcode :" + formatHexa(byte.toString(16), 2));
      frame.push(byte);
      if (payloadLength < 126) {
        // 7E 
        byte = (mask << 7) | (payloadLength & 0x7F);
    //  console.log("mask | len :" + formatHexa(byte.toString(16), 2));
        frame.push(byte);
  //    for (var i=0; i<8; i++)
  //      frame.push(0x0);
      } else if (payloadLength < 0xFFFF) {
        // 615535) // 16 bits
        var part1 = (payloadLength & 0xFF00) >> 8;
        var part2 = payloadLength & 0xFF;
        frame.push(0x7E | (mask << 7)); // 126 + mask
        frame.push(part1);
        frame.push(part2);
  //    for (var i=0; i<6; i++)
  //      frame.push(0x0);
      } else {
        // 64 bits
        var hexLen = OraSocketGlobal.FramingUtils.formatHexa(payloadLength.toString(16), 16);    
        frame.push(0x7F | (mask << 7)); // 127
        for (var i=0; i<hexLen.length; i+=2) {
          var XX = hexLen.substring(i, i+2);
          frame.push(parseInt(XX, 16));
        }
      }
      // Maskink key
      for (var i=0; (mask === 1) && i<maskKey.length; i++)
        frame.push(maskKey[i]);

      // Append actual payload
      var framedResponse = frame; // frame.concat(response); // Nope.. Multitype array...
      for (var i=0; i<response.length; i++) {
        framedResponse.push(response.charCodeAt(i) ^ maskKey[i % 4]); // apply mask, encode
      }
      // Dump it
      if (payloadLength > 0 && OraSocketGlobal.debugLevel > 4) {
        // Not a pong or a ping
        var all = "";
        for (var i=0; i<framedResponse.length; i++) {
          all += ("[" + OraSocketGlobal.FramingUtils.formatHexa(framedResponse[i].toString(16), 2) + "] ");
        }
        console.debug("Framed (" + framedResponse.length + ") :" + all);
      }
      return framedResponse;
    },

    unframedMessage : function(data, fromIdx) {
      if (fromIdx === undefined)
        fromIdx = 0;

      var maskingKey = [0x00, 0x00, 0x00, 0x00]; // identity, default.
      if (OraSocketGlobal.FramingUtils.getMask(data) === 1)
        maskingKey = OraSocketGlobal.FramingUtils.getMaskingKey(data, fromIdx);          
      var payload = [];
      var offset = fromIdx + OraSocketGlobal.FramingUtils.getPayloadOffset(data, fromIdx);
      var end    = offset + OraSocketGlobal.FramingUtils.getPayloadLength(data, fromIdx);
      for (var i=offset; i<end; i++) {
        var decoded = (data[i] ^ maskingKey[(i - offset) % 4]);
        payload.push(decoded);
      }
      return payload;
    },
            
    getUnframedTextData : function(data, fromIdx) {
      var payload = "";
      if (fromIdx === undefined)
        fromIdx = 0;
      
      if (OraSocketGlobal.FramingUtils.getOpCode(data, fromIdx) === OraSocketGlobal.FramingUtils.UTF_8_DATA) {
        var maskingKey = [0x00, 0x00, 0x00, 0x00]; // identity, default.
        if (OraSocketGlobal.FramingUtils.getMask(data) === 1)
          maskingKey = OraSocketGlobal.FramingUtils.getMaskingKey(data, fromIdx);          
        var offset = fromIdx + OraSocketGlobal.FramingUtils.getPayloadOffset(data, fromIdx);
        var end    = offset + OraSocketGlobal.FramingUtils.getPayloadLength(data, fromIdx);
        for (var i=offset; i<end; i++) {
          var decoded = (data[i] ^ maskingKey[(i - offset) % 4]);
          payload += String.fromCharCode(decoded);
        }
      }
      return payload;
    },

    binaryString : function(dataArray) {
//      var bs = ""; // The name of this variable is right.
//      for (var i=0; i<dataArray.length; i++)
//        bs += (String.fromCharCode(dataArray[i]));
//      return bs;
      
      var uInt8Array = new Uint8Array(dataArray.length);
      for (var i=0; i<dataArray.length; i++)
        uInt8Array[i] = dataArray[i] & 0xFF;
      return uInt8Array.buffer;
    },

    string2ArrayBuffer : function(charSequence) {
      var ab = [];
      for (var i=0; i<charSequence.length; i++) 
        ab.push(charSequence.charCodeAt(i) & 0xFF);
      return ab;
    },
            
    /**
     * Decodes the string containing "binary" data.
     * The string looks like "ff;d8;ff;e0;00;10;4a;46;49;46;00;01;01;00;00;01;00;01;00;00;ff;db;00;84;00;09;06;06;14;10;10;10;11;10;..."
     * Will be turned into a byte array containing [0xff, 0xd8, 0xff, 0xe0, 0x00, 0x10, 0x4a, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0x00, ...]
     * 
     * @param jsonEncoded
     * @returns {Array}
     */
    binJson2ArrayBuffer_v1 : function(jsonEncoded) {
      var ab = [];
      var hx = jsonEncoded.split(";");
      for (var i=0; i<hx.length; i++) {
        if (hx[i].length > 0)
          ab.push(parseInt("0x" + hx[i]) & 0xFF);
      }
      return ab;
    }            
  },
          
  GnlUtils : {         
    /**
     * Decodes the string containing "binary" data.
     * The string looks like "ffd8ffe000104a46494600010100000100010000ffdb008400090606141010101110..."
     * Will be turned into a byte array containing [0xff, 0xd8, 0xff, 0xe0, 0x00, 0x10, 0x4a, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0x00, ...]
     * 
     * @param {string} encoded
     * @returns {Array}
     */
    base16decode : function(encoded) {
      var ab = [];
      for (var i=0; i<encoded.length; i+=2) {
        var twoChar = encoded.substring(i, i+2);
        ab.push(parseInt("0x" + twoChar) & 0xFF);
      }
      return ab;
    },

    /**
     * Encoding.
     * Turn a byte array like [0xFF, 0xCA, 0xFE, 0xBA, 0xBE, ...]
     * into a string like "ffcafebabe..."
     * 
     * @param {Array} dataArray
     * @returns {string}
     */        
    base16encode : function(dataArray) {
      var str = ""; // will contain the hexa value on two characters.
      for (var i=0; i<dataArray.length; i++)
        str += (OraSocketGlobal.FramingUtils.formatHexa(dataArray[i].toString(16), 2));
      return str;    
    },
    
  //          0         1         2         3         4         5         6
  //          01234567890123456789012345678901234567890123456789012345678901234                                                                 
    base64 : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
    
    base64encode : function(str) { 
      // Encode
      var chars = OraSocketGlobal.GnlUtils.base64;
      var encoded = [];
      var c = 0;
      var b0, b1, b2;
      while (c < str.length) { 
        if (typeof(str) === 'string') {
          b0 = str.charCodeAt(c++);
          b1 = str.charCodeAt(c++);
          b2 = str.charCodeAt(c++);
        } else {
          b0 = str[c++]; 
          b1 = str[c++]; 
          b2 = str[c++]; 
        }
        var buf = (b0 << 16) + ((b1 || 0) << 8) + (b2 || 0);
        var i0 = (buf & (63 << 18)) >> 18;
        var i1 = (buf & (63 << 12)) >> 12;
        var i2 = isNaN(b1) ? 64 : (buf & (63 << 6)) >> 6;
        var i3 = isNaN(b2) ? 64 : (buf & 63);
        encoded[encoded.length] = chars.charAt(i0);
        encoded[encoded.length] = chars.charAt(i1);
        encoded[encoded.length] = chars.charAt(i2);
        encoded[encoded.length] = chars.charAt(i3);
      }
      return encoded.join('');
    },

    base64decode : function(str) { 
      // decode
      var chars = OraSocketGlobal.GnlUtils.base64;
      var invalid = {
        strlen: (str.length % 4 !== 0),
        chars:  new RegExp('[^' + chars + ']').test(str),
        equals: (/=/.test(str) && (/=[^=]/.test(str) || /={3}/.test(str)))
      };
      if (invalid.strlen || invalid.chars || invalid.equals)
        throw new Error('Invalid base64 data');
      var decoded = [];
      var c = 0;
      while (c < str.length) {
        var i0 = chars.indexOf(str.charAt(c++));
        var i1 = chars.indexOf(str.charAt(c++));
        var i2 = chars.indexOf(str.charAt(c++));
        var i3 = chars.indexOf(str.charAt(c++));
        var buf = (i0 << 18) + (i1 << 12) + ((i2 & 63) << 6) + (i3 & 63);
        var b0 = (buf & (255 << 16)) >> 16;
        var b1 = (i2 === 64) ? -1 : (buf & (255 << 8)) >> 8;
        var b2 = (i3 === 64) ? -1 : (buf & 255);
        decoded[decoded.length] = String.fromCharCode(b0);
        if (b1 >= 0) decoded[decoded.length] = String.fromCharCode(b1);
        if (b2 >= 0) decoded[decoded.length] = String.fromCharCode(b2);
      }
      return decoded.join('');
    },

    decodeReadyState : function(status) {
      var code = "unknown";
      switch (status) {
        case OraSocketGlobal.XHR_UNINITIALIZED:
          code = "UNINITIALIZED";
          break;
        case OraSocketGlobal.XHR_LOADING:
          code = "LOADING";
          break;
        case OraSocketGlobal.XHR_RESPONSE_HEADERS_RECEIVED:
          code = "RESPONSE HEADERS RECEIVED";
          break;
        case OraSocketGlobal.XHR_SOME_RESPONSE_BODY_RECEIVED:
          code = "SOME RESPONSE BODY RECEIVED";
          break;
        case OraSocketGlobal.XHR_REQUEST_COMPLETED:
          code = "REQUEST COMPLETED";
          break;
        default:
          code = "unknown";
      }
      return code;
    },

    decodeStatus : function(status) {
      var code = "unknown";
      try {
        switch (status) {
          case OraSocketGlobal.HTTP_NO_DATA:
            code = "NO DATA/" + status.toString();
            break;
          case OraSocketGlobal.HTTP_OK:
            code = "OK/" + status.toString();
            break;
          case OraSocketGlobal.HTTP_ERROR_1:
            code = "ERROR/" + status.toString();
            break;
          case OraSocketGlobal.HTTP_ERROR_2:
            code = "ERROR/" + status.toString();
            break;
          case OraSocketGlobal.HTTP_ERROR_RETRY:
            code = "ERROR RETRY/" + status.toString();
            break;
          case OraSocketGlobal.ERROR_INTERNET_TIMEOUT:
            code = "ERROR_INTERNET_TIMEOUT/" + status.toString();
            break;
          case OraSocketGlobal.ERROR_INTERNET_NAME_NOT_RESOLVED:
            code = "ERROR_INTERNET_NAME_NOT_RESOLVED/" + status.toString();
            break;
          case OraSocketGlobal.ERROR_INTERNET_CANNOT_CONNECT:
            code = "ERROR_INTERNET_CANNOT_CONNECT/" + status.toString();
            break;
          case OraSocketGlobal.ERROR_INTERNET_CONNECTION_ABORTED:
            code = "ERROR_INTERNET_CONNECTION_ABORTED/" + status.toString();
            break;
          case OraSocketGlobal.ERROR_INTERNET_CONNECTION_RESET:
            code = "ERROR_INTERNET_CONNECTION_RESET/" + status.toString();
            break;
          case OraSocketGlobal.ERROR_HTTP_INVALID_SERVER_RESPONSE:
            code = "ERROR_HTTP_INVALID_SERVER_RESPONSE/" + status.toString();
            break;
          default:
            code = status.toString();
        }
      } catch (err) { }
      return code;
    },

    getClass : function(obj) {
      if (obj && typeof obj === 'object' &&
          Object.prototype.toString.call(obj) !== '[object Array]' &&
          obj.constructor) {
        var arr = obj.constructor.toString().match(/function\s*(\w+)/);
        if (arr && arr.length === 2) {
          return arr[1];
        }
      }
      return false;
    }
  },
          
  baseXXencode : function(prm) { 
    if (OraSocketGlobal.encodingBase === 16) 
//    return OraSocketGlobal.FramingUtils.binaryString(OraSocketGlobal.GnlUtils.base16encode(prm)); 
      return OraSocketGlobal.GnlUtils.base16encode(prm); 
    else if (OraSocketGlobal.encodingBase === 64) 
      return OraSocketGlobal.GnlUtils.base64encode(prm); 
    else
      throw new Error("Unsupported encoding");
  },
  
  baseXXdecode : function(prm) { 
    if (OraSocketGlobal.encodingBase === 16) 
      return OraSocketGlobal.FramingUtils.binaryString(OraSocketGlobal.GnlUtils.base16decode(prm)); 
    else if (OraSocketGlobal.encodingBase === 64) 
      return OraSocketGlobal.GnlUtils.base64decode(prm); 
    else
      throw new Error("Unsupported encoding");
  }
};

/**
 * Configuration utilities
 * @namespace OraSocket
 */
var OraSocket = {
  ORASOCKET_PATH     : "scripts/orasocket.js",
  ORASOCKET_MIN_PATH : "scripts/orasocket.min.js",

  baseUrl: ".", // Default location, where is the 'scripts' directory
  enforcedTransport: "", // Used to enforce a transport. Can be WebSocket or XMLHttpRequest.
  
  /**
   * Setup the WebSocket configuration. 
   * <br>
   * To be invoked <b>before</b> creating the WebSocket object, within a try-catch block (to keep the app working, even if the script import of <code>orasocket.js</code> has been removed or is missing).
   * 
   * @function configure
   * @memberof OraSocket
   * @param {object} configObject This is a json structure containing the following supported members
   * <table>
   *   <tr><th>Name</th><th>Type</th><th>Default</th><th>Description</th></tr>
   *   <tr><td><code>baseUrl</code></td><td>string</td><td>&quot;.&quot;</td><td>The location of the <code>scripts</code> directory, relatively to the HTML context of the page.</td></tr>
   *   <tr><td><code>PING_INTERVAL</code></td><td>integer</td><td>1000</td><td>Interval in ms between each ping to the server</td></tr>
   *   <tr><td><code>ENCODE_FOR_IE_BELOW</code></td><td>integer</td><td>10</td><td>If the browser is IE, and below (stricly) this version number, then use Base16 encoding for the framed data</td></tr>
   *   <tr><td><code>NB_TRY_FOR_EACH_TRANSPORT</code></td><td>integer</td><td>2</td><td>Number of consecutive trials when a connection fails on a given transport</td></tr>
   *   <tr><td><code>TRY_AGAIN_INTERVAL</code></td><td>integer</td><td>10000</td><td>If the transport fails beyond this amout of time (in ms), then try it again. Switch to the next transport ortherwise</td></tr>
   *   <tr><td><code>ENFORCE_ENCODING</code></td><td>boolean</td><td>false</td><td>Should Base16 encoding be enforced? Mostly for debugging</td></tr>
   *   <tr><td><code>SERVER_PING_ENABLED</code></td><td>boolean</td><td>true</td><td>Set it to false to disable the ping from the client to the server</td></tr>
   *   <tr><td><code>WEBSOCKET_CREATION_TIMEOUT</code></td><td>integer</td><td>1000</td><td>Consider the WebSocket creation has failed if not back after this amount of time (in ms).</td></tr>
   *   <tr><td><code>debug</code></td><td>integer</td><td>0</td><td>Debug level. 0 to +&infin;</td></tr>
   *   <tr><td><code>transport</code></td><td>string</td><td>none</td><td>Enforced transport. Can be "WebSocket" or "XMLHttpRequest"</td></tr>
   * </table>
   * All the members have default values, none of them is mandatory.
   * @returns {void}
   * @inner
   */
  configure: function(configObject) {
    if (configObject.baseUrl !== undefined)
      OraSocket.baseUrl = configObject.baseUrl;
    if (configObject.PING_INTERVAL !== undefined)
      OraSocketGlobal.PING_INTERVAL = configObject.PING_INTERVAL;
    if (configObject.ENCODE_FOR_IE_BELOW !== undefined)
      OraSocketGlobal.ENCODE_FOR_IE_BELOW = configObject.ENCODE_FOR_IE_BELOW;
    if (configObject.WEBSOCKET_CREATION_TIMEOUT !== undefined)
      OraSocketGlobal.WEBSOCKET_CREATION_TIMEOUT = configObject.WEBSOCKET_CREATION_TIMEOUT;
    if (configObject.NB_TRY_FOR_EACH_TRANSPORT !== undefined)
      OraSocketGlobal.NB_TRY_FOR_EACH_TRANSPORT = configObject.NB_TRY_FOR_EACH_TRANSPORT;
    if (configObject.TRY_AGAIN_INTERVAL !== undefined)
      OraSocketGlobal.TRY_AGAIN_INTERVAL = configObject.TRY_AGAIN_INTERVAL;
    if (configObject.SERVER_PING_ENABLED !== undefined)
      OraSocketGlobal.PING_ENABLED = configObject.SERVER_PING_ENABLED;
    if (configObject.ENFORCE_ENCODING !== undefined)
      OraSocketGlobal.ENFORCE_ENCODING = configObject.ENFORCE_ENCODING;
    if (configObject.debug !== undefined)
       OraSocketGlobal.debugLevel = configObject.debug;
    if (configObject.transport !== undefined)
      OraSocket.enforcedTransport = configObject.transport;      
  }
};

var RequestParameter = {
  names: {
    WS_ATTEMPT       : "tyrus-ws-attempt",
    REQUIRE_ENCODING : "tyrus-encoding-required",
    CLIENT_OPERATION : "tyrus-client-operation",
    CLIENT_ID        : "tyrus-connection-id",  
    CLIENT_TRANSPORT : "tyrus-client-transport",
    CORS_REQUIRED    : "tyrus-cors-headers",

    UNIQUE_REQ_ID    : "unique-req-id",
    CHAR_ENCODED     : "char-encoded"
  },
  values: {
    WS_HAND_SHAKE    : "Hand-Shake", // Value for WS_ATTEMPT
    NONE             : "none",   // value of the CHAR_ENCODED
    BASE_16          : "base16", // value of the CHAR_ENCODED
    BASE_64          : "base64"  // value of the CHAR_ENCODED
  }
};

var WSOperation = {
  WAIT_FOR_INPUT  : "tyrus-wait-for-input",
  SEND_MESSAGE    : "tyrus-send-message" // ,
};

var SUPPORTED_TRANSPORTS = "tyrus-fallback-transports";

var getScriptPath = function(scriptPath) {
  var path = OraSocket.baseUrl;
  if (scriptPath.startsWith('/') && path.endsWith('/'))
    path = path + '.' + scriptPath;
  else if (!scriptPath.startsWith('/') && !path.endsWith('/'))
    path = path + '/' + scriptPath;
  else
    path += scriptPath;
  
  if (OraSocketGlobal.debugLevel > 10)
    console.log("[" + scriptPath  + "] becomes\n[" + path + "] with\n[" + OraSocket.baseUrl + "]");
  
  return path;
};
/*
 * That one is pretty ugly. 
 * This is the last resort...
 */
var getScriptPathFromDOM = function() {
  var scriptPath = "";
  if (document.head !== undefined) {
    var scripts = document.head.getElementsByTagName("script");
    for (var i=0; i<scripts.length; i++) {
      if (scripts[i].src !== undefined) {
        if (scripts[i].src.endsWith(OraSocket.ORASOCKET_PATH)) {
          var src = scripts[i].src;
          scriptPath = src.substring(0, src.length - OraSocket.ORASOCKET_PATH.length);
          break;
        } else if (scripts[i].src.endsWith(OraSocket.ORASOCKET_MIN_PATH)) {
          var src = scripts[i].src;
          scriptPath = src.substring(0, src.length - OraSocket.ORASOCKET_MIN_PATH.length);
          break;
        }
      }
    }    
  } else {
    var scripts = document.getElementsByTagName('head')[0].getElementsByTagName("script");
    for (var i=0; i<scripts.length; i++) {
      if (scripts[i].src !== undefined) {
        if (scripts[i].src.endsWith(OraSocket.ORASOCKET_PATH)) {
          var src = scripts[i].src;
          scriptPath = src.substring(0, src.length - OraSocket.ORASOCKET_PATH.length);
          break;
        } else if (scripts[i].src.endsWith(OraSocket.ORASOCKET_MIN_PATH)) {
          var src = scripts[i].src;
          scriptPath = src.substring(0, src.length - OraSocket.ORASOCKET_MIN_PATH.length);
          break;
        }
      }
    }
  }
  return scriptPath;
};

var importScript = function(scriptURL, callback) {
  var script = document.createElement('script');
  var src = "http"+secured +"://"+machine+":"+port+"/acc_web/javax.faces.resource/chat/NativeWebSocket.js.xhtml?ln=acc_js";
  //var src = getScriptPath(scriptURL);
  
  if (script.readyState) { 
    // IE 
    script.onreadystatechange = function() {
      if (script.readyState === "loaded" || script.readyState === "complete") {
        script.onreadystatechange = null;
        if (OraSocketGlobal.debugLevel > 2)
          console.log(src + " loaded.");
        if (callback) {
          callback();
          if (OraSocketGlobal.debugLevel > 2)
            console.log("Callback after " + src + " completed.");
        }
      }
    };
  } else { 
    // Others
    script.onload = function() {
      if (OraSocketGlobal.debugLevel > 10)
        console.log(src + " loaded.");
      if (callback) {
        callback();
        if (OraSocketGlobal.debugLevel > 10)
          console.log("Callback after " + src + " completed.");
      }
    };
    script.onerror = function(err) {
      console.log(err.toString());
    };
  }
  script.src  = src; 
  script.type = "text/javascript";
  if (document.head !== undefined) {
    // 1 - Do we have one already?
    var scripts = document.head.getElementsByTagName("script");
    for (var i=0; i<scripts.length; i++) {
//    console.log("Comparing [" + scriptURL + "] with [" + scripts[i].src + "]");
      if (scripts[i].src !== undefined && scripts[i].src.endsWith(scriptURL)) {
//      console.log("--- " + scriptURL + " is already there.");
        document.head.removeChild(scripts[i]);
        break;
      }
    }    
    document.head.appendChild(script);
  } else {
    // 1 - Do we have one already?
    var scripts = document.getElementsByTagName('head')[0].getElementsByTagName("script");
    for (var i=0; i<scripts.length; i++) {
//    console.log("Comparing [" + scriptURL + "] with [" + scripts[i].src + "]");
      if (scripts[i].src !== undefined && scripts[i].src.endsWith(scriptURL)) {
        console.log("--- Removing " + scriptURL + ", already there.");
        document.getElementsByTagName('head')[0].removeChild(scripts[i]);
        break;
      }
    }    
    document.getElementsByTagName('head')[0].appendChild(script); // IE8...
  }
};

var loadJSON = function(filePath) {
  var json;
  try { 
    json = loadJSONFile(getScriptPath(filePath), "application/json"); 
  } catch (err) {
    // Misplaced?
    var pathTrial = OraSocket.baseUrl;
    OraSocket.configure({ baseUrl: getScriptPathFromDOM() });
    try { 
      json = loadJSONFile(getScriptPath(filePath), "application/json"); 
      console.log("Enforced baseUrl to [" + OraSocket.baseUrl + "], was [" + pathTrial + "]");
    } catch (err) {
      // Definitely not...
      alert("Cannot find " + filePath + "\nTried:\n - " + pathTrial + "\n - " + OraSocket.baseUrl);
    }
  }
  return JSON.parse(json);
};  

var loadJSONFile = function(filePath, mimeType) {
//console.log("Loading JSON data from " + filePath);
  var xmlhttp = new XMLHttpRequest();
  xmlhttp.open("GET", filePath, false);
  if (mimeType !== null && mimeType !== undefined) {
    if (xmlhttp.overrideMimeType) {
      xmlhttp.overrideMimeType(mimeType);
    }
  }
  xmlhttp.send(); 
  if (xmlhttp.status === OraSocketGlobal.HTTP_OK) {
    return xmlhttp.responseText;
  } else {
    // Throw exception
    if (xmlhttp.status === OraSocketGlobal.HTTP_SERVER_ERROR || 
        xmlhttp.status === OraSocketGlobal.HTTP_ERROR_2)
      throw {err: "Not Found"};
    return null; // ta mere.
  }
};

// Check availabilities, initializations, etc.
var orasocketInit = function() {
//console.log("Initializing orasocket");
  if (typeof String.prototype.startsWith !== 'function') {
    String.prototype.startsWith = function (str) {
      return this.indexOf(str) === 0;
    };
  }

  if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(suffix) {
      return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };
  }
  
  OraSocketGlobal.wsAvailable = window.WebSocket || window.MozWebSocket;
  if (OraSocketGlobal.wsAvailable) {
    OraSocketGlobal.$nativeWS = window.WebSocket;
    if (!OraSocketGlobal.$nativeWS)
      OraSocketGlobal.$nativeWS = window.MozWebSocket;
  }

  OraSocketGlobal.xhrAvailable = window.XMLHttpRequest;
  
  // For IE (when the debug console is closed, sucker)...
  var alertFallback  = false;
  var statusFallback = true;
  if (typeof console === "undefined" || typeof console.log === "undefined" || 
      typeof console.debug === "undefined" || typeof console.warn === "undefined") {
    if (typeof console === "undefined" || typeof console.log === "undefined") {
      console = {};
      if (alertFallback || statusFallback) {
        console.log = function(msg) {
          if (alertFallback)
            alert(msg);
          if (statusFallback) {
            if (window.status !== undefined)
              window.status= msg;
            else
              alert("window.status not available (display your status bar...), switching to alert.\n" + msg);
          }
        };
      } else {
        console.log = function() {}; // Noting. No error, but just nothing.
      }
    }
    if (typeof console.debug === "undefined" || typeof console.warn === "undefined") {
      console.warn = console.log;
      console.debug = console.log;        
    }
  }

  if (window.Uint8Array === undefined) {
    var subarray = function(start, end) {
      return this.slice(start, end);
    };

    var set_ = function(array, offset) {
      if (arguments.length < 2) offset = 0;
      for (var i = 0, n = array.length; i < n; ++i, ++offset)
        this[offset] = array[i] & 0xFF;
    };

    var TypedArray = function(arg1) {
      var result;
      if (typeof arg1 === "number") {
        result = new Array(arg1);
        for (var i = 0; i < arg1; ++i)
          result[i] = 0;
      } else {
        result = arg1.slice(0);
      }
      result.subarray = subarray;
      result.buffer = result;
      result.byteLength = result.length;
      result.set = set_;
      if (typeof arg1 === "object" && arg1.buffer)
        result.buffer = arg1.buffer;

      return result;
    };

    window.Uint8Array  = TypedArray;
    window.Uint32Array = TypedArray;
    window.Int32Array  = TypedArray;  
  }
   
  // if (typeof btoa === 'undefined') {
  //   var btoa = function(str) { return OraSocketGlobal.GnlUtils.base64encode(str); }
  // }

  // if (typeof atob === 'undefined') {
  //   var atob = function(str) { return OraSocketGlobal.GnlUtils.base64decode(str); }
  // }  
  
// Populate the dynamic fallback order sequence, and other config stuff.
  OraSocketGlobal.fallbackOrder = [OraSocketGlobal.NATIVE_WEB_SOCKET, OraSocketGlobal.XML_HTTP_REQUEST]; // , OraSocketGlobal.JSONP];
  
  OraSocketGlobal.requestQueue = new OraSocketGlobal.Queue();
};

(function() { orasocketInit(); })();

/**
 * WebSocket constructor.
 * <br>
 * This object is the one the client instantiates.
 * It has the same features as a native WebSocket.
 * All the fallback mechanism is taken care of transparently.
 * <br>
 * A configuration function named <code>OraSocket.configure</code> is driving the behavior of the JavaScript client.<br>
 * The following code:
 *<pre>
    var customConfig =
    { 
      baseUrl:                   "ws",
      PING_INTERVAL:             1000,
      ENCODE_FOR_IE_BELOW:         10,
      WEBSOCKET_CREATION_TIMEOUT:1000,
      NB_TRY_FOR_EACH_TRANSPORT:    2,
      TRY_AGAIN_INTERVAL:       10000,
      SERVER_PING_ENABLED:       true,
      ENFORCE_ENCODING:         false
    };
    OraSocket.configure(customConfig);
    var ws = new WebSocket("ws://machine:8080/websocket/mywsapp");
 *</pre>
 * is semantically equivalent to:
 *<pre>
    var customConfig =
    { 
      baseUrl:                   "ws",
      PING_INTERVAL:             1000,
      ENCODE_FOR_IE_BELOW:         10,
      WEBSOCKET_CREATION_TIMEOUT:1000,
      NB_TRY_FOR_EACH_TRANSPORT:    2,
      TRY_AGAIN_INTERVAL:       10000,
      SERVER_PING_ENABLED:       true,
      ENFORCE_ENCODING:         false
    };
    var ws = new WebSocket("ws://machine:8080/websocket/mywsapp", customConfig);
 *</pre>
 *<b><i>Important</i></b>: When used, <code>OraSocket.configure</code> must be invoked <b>before</b> the WebSocket constructor.
 * @see {@link OraSocket} for <code>OraSocket.configure</code>
 * @constructor
 * @param {string} serverURI Mandatory, the server URI. Generic shape is <code>ws[s]://machine:port/wsapplication/uri</code>
 * @param {object} optionalConfig connection options (JSON Object), optional parameter, used for debugging purpose, mostly. Can contain the same object as the one used for <code>OraSocket.configure</code>.
 *
 * @returns {WebSocket}
 *
 */
var WebSocket = function(serverURI) {
  var preferredTransport, scheme, secured, machine, port, query;
  var uri = serverURI;
  
  var self = this;
  var connectionTrialStatus;


  this.setDebugLevel = function(dl) {
    OraSocketGlobal.debugLevel = dl;
  };

  var constructorOptions = {};  
  
  this.getConstructorOptions = function() {
    return constructorOptions;
  };

  this.getServerURI = function() {
    return uri;
  };
  
  this.getConnectionTrialStatus = function() {
    return connectionTrialStatus;
  };
  
  this.setConnectionTrialStatus = function(cts) {
    connectionTrialStatus = cts;
  };
  
  var manageDynamicFallbackStatus = function() {
    if (connectionTrialStatus !== undefined) {
      if (self.getTransport() !== undefined && connectionTrialStatus.transport === self.getTransport()) {
        var now = (new Date().getTime());
        if (OraSocketGlobal !== undefined && OraSocketGlobal.debugLevel > 10)
          console.debug('... Connection (' + self.getTransport() + ') re-requested after ' + (now - connectionTrialStatus.timestamp) + " ms (" + OraSocketGlobal.TRY_AGAIN_INTERVAL + "). nbshot=" + connectionTrialStatus.nbshot);
        if ((now - connectionTrialStatus.timestamp) < OraSocketGlobal.TRY_AGAIN_INTERVAL) {
          connectionTrialStatus.nbshot++;
        } else {
          connectionTrialStatus.nbshot = 0; // Reset, to give more subsequent chances. That was just a hiccup.
        }
        connectionTrialStatus.actual++;
        connectionTrialStatus.timestamp = (new Date().getTime());
      } else {
        connectionTrialStatus.timestamp = (new Date().getTime());
      }
    } else { // then create the object
      connectionTrialStatus = { 
                         transport: (self.getTransport() !== undefined ? self.getTransport() : "WebSocket"),
                         timestamp: (new Date().getTime()),
                         nbshot: 1,
                         actual: 1
                       };
    }
  };

  var dynamicFallbackOnError = function(error) {
    if (OraSocketGlobal.debugLevel > 10)

    console.debug("Transport is (was) " + self.getTransport() + " -> " + JSON.stringify(error));
  //  console.log("Fallback Order:");
  //  for (var i=0; i<OraSocketGlobal.fallbackOrder.length; i++)
  //    console.log("-> " + OraSocketGlobal.fallbackOrder[i]);
  //  console.log("Available transports:");
  //  for (var i=0; i<OraSocketGlobal.availableTransports.length; i++)
  //    console.log("-> " + OraSocketGlobal.availableTransports[i]);

    // Find index of current transport in the fallback list
    var currentTransportIdx = -1;
    for (var i=0; i<OraSocketGlobal.fallbackOrder.length; i++) {
      if (OraSocketGlobal.fallbackOrder[i] === self.getTransport()) {
        currentTransportIdx = i;
        break;
      }
    }
    if (currentTransportIdx > -1) {
      // Find next transport for fallback
      if (currentTransportIdx < OraSocketGlobal.fallbackOrder.length) {
        var next = 0;
        var goodToGo = true;
        if (connectionTrialStatus !== undefined) {
          if (connectionTrialStatus.nbshot < OraSocketGlobal.NB_TRY_FOR_EACH_TRANSPORT)
            next = 0; // retry the same
          else
            next = 1; // Next one.
        } else {
          console.warn(".....................connectionTrialStatus is not defined (weird).");
        }
        var newTransport = OraSocketGlobal.fallbackOrder[currentTransportIdx + next];
        if  (newTransport === undefined) {
          // Last transport, beyond last chance
          newTransport = "no more.";
          goodToGo = false;
        }

        var errMess = (self.getTransport() + " transport just failed [" + error.err + "] (" + 
                      connectionTrialStatus.nbshot.toString() + " time" + (connectionTrialStatus.nbshot === 1?"":"s") + 
                      ((connectionTrialStatus.actual === connectionTrialStatus.nbshot) ? "" :  ", actually " + connectionTrialStatus.actual.toString()) +
                      "), " + (next === 0 ? ("retrying (after " + ((new Date().getTime() - connectionTrialStatus.timestamp) / 1000) + "s)") : "trying " +
                      newTransport));
        self.onerror({ err: errMess });

        if (goodToGo) {
          if (next > 0) {
            connectionTrialStatus.nbshot = 0; // Reset. Will be incremented to 1 in manageDynamicFallbackStatus
            connectionTrialStatus.transport = newTransport;
            self.setTransport(newTransport);
          }
          var constructorOptions = self.getConstructorOptions();
//          var serverURI = self.getServerURI();
          constructorOptions.transport = newTransport;
          var webSocketCreated = false;

          var watcherID, workerID;

          var watcher = function(cb) {
            if (webSocketCreated) {
              // This one should actually never show up. If successfully created, the worker cancels the watcher.
              cb("WebSocket (" + newTransport + ") re-created Ok");
            } else {
              cb("WebSocket (" + newTransport + ") NOT re-created within timeout...");  
              clearTimeout(workerID);
              dynamicFallbackOnError({ err: "Creation timeout [" + OraSocketGlobal.WEBSOCKET_CREATION_TIMEOUT +
                     " ms], at " + (new Date().getTime()).toString() }); 
            }
          };
          var worker = function(cb, onok) {
            cb("... Re-creating WebSocket");
            try {
              manageDynamicFallbackStatus();
              try {
                self.quit(); // Mostly to stop the ping
              } catch (err) {
                console.warn("Quit errored:" + JSON.stringify(err));
              }
              loadInterface();
              webSocketCreated = true;
              clearTimeout(watcherID);
              cb("WebSocket re-created successfully with transport " + self.getTransport());
              onok(self);
            } catch (err) {
              cb(" ---> Error re-creating WebSocket:" + JSON.stringify(err));
            }
          };

          var workAndWatch = function(work, watch, timeout, callback, onSuccessfulCreation) {
            watcherID = setTimeout(function() { watch(callback); }, timeout);
            workerID  = setTimeout(function() { work(callback, onSuccessfulCreation); }, 0);
          };

          var callback = function(str) { 
            if (OraSocketGlobal.debugLevel > 10)
              console.log(str); 
          };
          var onSuccess = function(prm) { 
            self.onopen(); 
            if (OraSocketGlobal.debugLevel > 10)
              console.debug("  onopen (re)invoked successfully");
          };

          workAndWatch(worker, watcher, OraSocketGlobal.WEBSOCKET_CREATION_TIMEOUT, callback, onSuccess);

          if (constructorOptions.debug !== undefined)
            self.setDebugLevel(constructorOptions.debug);
        } else {
          self.onerror({ err: "All transport options exhausted. You are not connected to the server." });
          self.onclose();
        }
      }
    } else {
      self.onerror({ err: "Current transport NOT FOUND in the available transport list"}); 
    } 
  };

  var transport, instanceName;
  for (var i=0; i<arguments.length; i++) {
    //  console.log("Type of Arg[" + i + "] is " + typeof(arguments[i]));
    if (i > 0) {
      if (typeof(arguments[i]) === 'object') {  
        OraSocket.configure(arguments[i]);
        if (OraSocket.enforcedTransport !== undefined && OraSocket.enforcedTransport.length > 0) {
          transport = OraSocket.enforcedTransport;
          preferredTransport = transport;
        }

        // if (arguments[i].transport !== undefined) //  enforcing a transport
        // {
        //   transport = arguments[i].transport;
        //   preferredTransport = transport;
        // }
        // if (arguments[i].debug !== undefined)    // debug level, beginning st once.
        //   this.setDebugLevel(arguments[i].debug);  
        // if (arguments[i].instName !== undefined) // instance name in the main script (for JSON)
        //   instanceName = arguments[i].instName;
        
        constructorOptions = arguments[i]; // TODO Make sure there is only one.        
      }
    }
  }
  if (OraSocketGlobal.debugLevel > 1)
    console.debug("WebSocket constructor invoked, transport is " + transport);
  
  /**
   * Register this callback to define what to do when the client receives a message from the server.
   * @function
   * @param {object} mess The message received by the client.
   * @returns {void}
   */
  this.onmessage = function(mess) { console.log("Default onMessage:"); console.log(mess); };
  /**
   * Called when a connection with the server is established.
   * <br>
   * Takes no parameter.
   * @function
   * @returns {void}
   */
  this.onopen    = function() { console.log('*** Default onopen'); };
  /**
   * Called when a connection with the server is closed.
   * <br>
   * Takes no parameter.
   * @returns {void}
   */
  this.onclose   = function() {};
  /**
   * Called when a connection error happens
   * @function
   * @param {object} error The error that was raised.
   * @returns {void}
   */
  this.onerror   = function(error) {}; 
  
  if (OraSocketGlobal.debugLevel > 10) {
    console.debug("Your browser " + (OraSocketGlobal.wsAvailable? "supports":"doesn't support") + " WebSockets.");
    console.debug("Your browser " + (OraSocketGlobal.xhrAvailable?"supports":"doesn't support") + " XMLHttpRequest.");
  }

  /*
   * Invoked at the end of the handShake function, itself invoked after the first POST request (HandShake).
   * This is the one loading the WebSocket object when possible, or flipping back to HTTPLongPoll (and others, when available) 
   * if necessary.
   *
   * Important: the onopen method must be call on completion.
   */
  var loadInterface = function() {
    if (OraSocketGlobal.debugLevel > 10)
      console.debug("Loading prototype functions...");

    if (preferredTransport === OraSocketGlobal.NATIVE_WEB_SOCKET) {
    	console.log('Load WebSocket scripts.......................');
      // Load scripts/transports/native/NativeWebSocket.js here
      importScript("scripts/transports/native/NativeWebSocket.js",
        function() {
          var backEndWebSocket = new NativeWebSocket(OraSocketGlobal.$nativeWS, 
                                                     "ws" + secured + "://" + machine + ":" + port + "/" + query);
          manageDynamicFallbackStatus();
//        backEndWebSocket.onopen    = function() { self.onopen(); };
//        backEndWebSocket.onclose   = function() { self.onclose(); };
          backEndWebSocket.onopen    = self.onopen;
          backEndWebSocket.onclose   = self.onclose;
          backEndWebSocket.onerror   = function(error) { 
            dynamicFallbackOnError(error);
//          self.onerror(error); 
          };
//        backEndWebSocket.onmessage = function(message) { self.onmessage(message); };
//        self.send   = function(mess) { backEndWebSocket.send(mess); };

          backEndWebSocket.onmessage = self.onmessage;
          self.send   = backEndWebSocket.send;

          self.close  = function() { 
        	  backEndWebSocket.close(); 
        	  console.log('WebSocket closed.......................');
          };
          self.quit   = function() { backEndWebSocket.quit(); console.log('WebSocket exited.......................');};
          if (OraSocketGlobal.debugLevel > 10)
            console.debug("End of Native WebSocket callback");
          setTimeout(function() { self.onopen(); }, 50);
       });
    } else if (preferredTransport === OraSocketGlobal.XML_HTTP_REQUEST) {
      // Load scripts/transports/http/xhr/LongPoll.js here
      importScript("scripts/transports/http/xhr/LongPoll.js",
        function() {
          var backEndWebSocket = new LongPoll(OraSocketGlobal.clientID, 
                                              "http" + secured + "://" + machine + ":" + port + "/" + query);
          manageDynamicFallbackStatus();
//        backEndWebSocket.onopen    = function() { self.onopen(); };
//        backEndWebSocket.onclose   = function() { self.onclose(); };
          backEndWebSocket.onopen    = self.onopen;
          backEndWebSocket.onclose   = self.onclose;
          backEndWebSocket.onerror   = function(error) { 
            dynamicFallbackOnError(error);
//          self.onerror(error); 
          };
//        backEndWebSocket.onmessage = function(message) { self.onmessage(message); };
//        self.send   = function(mess) { backEndWebSocket.send(mess); };

          backEndWebSocket.onmessage = self.onmessage;
          self.send   = backEndWebSocket.send;
          
          self.close  = function() { backEndWebSocket.close(); };
          self.quit   = function() { backEndWebSocket.quit(); };
          if (OraSocketGlobal.debugLevel > 10)
            console.debug("End of XHR callback");
          setTimeout(function() { self.onopen(); }, 50);
       });
    } else if (preferredTransport === OraSocketGlobal.JSONP) {
      // Load scripts/transports/http/xhr/LongPoll.js here
      importScript("scripts/transports/http/jsonp/JSONP.js",
        function() {
          var backEndWebSocket = new JSONP(OraSocketGlobal.clientID, 
                                           "http" + secured + "://" + machine + ":" + port + "/" + query, 
                                           instanceName);
          manageDynamicFallbackStatus();
          backEndWebSocket.onopen    = self.onopen;
          backEndWebSocket.onclose   = self.onclose;
          backEndWebSocket.onerror   = function(error) { 
            dynamicFallbackOnError(error);
//          self.onerror(error); 
          };
          backEndWebSocket.onmessage = self.onmessage;
          WebSocket.prototype.send   = backEndWebSocket.send;

          WebSocket.prototype.close  = function() { backEndWebSocket.close(); };          
          WebSocket.prototype.parseJSONP = function(json, rnk) { backEndWebSocket.parseJSONP(json, rnk); };
          WebSocket.prototype.quit   = function() { backEndWebSocket.quit(); };
          if (OraSocketGlobal.debugLevel > 10)
            console.debug("End of JSONP callback");
          setTimeout(function() { self.onopen(); }, 50);
       });
    } else {
      if (OraSocketGlobal.debugLevel > 0) {
        console.warn('Unmanaged transport:' + preferredTransport);
        alert('Unmanaged transport:' + preferredTransport);
      }
    }

    // Extra functions
    self.getTransport = function() { return preferredTransport; };
    self.setTransport = function(str) { preferredTransport = str; };
    // Just for tests...
    self.onInitCompleted = function(mess) {
      console.log("WebSocket.onInitCompleted:" + mess);
    };
    if (OraSocketGlobal.debugLevel > 10)
      console.debug("... Prototype functions loaded.");  
  };

  /*
   * Makes the first POST request (HandShake) that returns the list of the available transports and unique client-id.
   * Will itself eventually invoke the loadInterface function.
   * @param {string} serverURI the full http URL od the app on the server
   * @returns {void}
   */
  var handShake = function(serverURI) {
    if (OraSocketGlobal.debugLevel > 10) {
      console.debug("Sending first (POST) request to " + serverURI + "..." + 
        (transport !== undefined ? ", enforcing " + transport : ""));
    }
//  var xhr = new XMLHttpRequest();
    var xhr = new OraSocketGlobal.XMLHttpRequest();
    var completed = false;
    xhr.onreadystatechange = function() {
      if (xhr.readyState === OraSocketGlobal.XHR_REQUEST_COMPLETED && xhr.status === OraSocketGlobal.HTTP_OK) {
        // No response expected (empty string in xhr.responseText)
        var headers = xhr.getAllResponseHeaders();
        if (OraSocketGlobal.debugLevel > 10)
          console.debug("All headers:\n" + headers);

        var supportedTransports = xhr.getResponseHeader(SUPPORTED_TRANSPORTS);
        if (OraSocketGlobal.debugLevel > 10)
          console.debug("Fallback Transports:" + supportedTransports);
        
        var transports = supportedTransports.split(",");
        // Do we have WebSocket is the supported transport list? Add it if not.
        var containsWS = false;
        for (var i=0; i<transports.length; i++) {
          if (transports[i] === OraSocketGlobal.NATIVE_WEB_SOCKET) {
            containsWS = true;
            break;
          }
        }
        if (!containsWS)
          transports.splice(0, 0, OraSocketGlobal.NATIVE_WEB_SOCKET);
        
        OraSocketGlobal.availableTransports = transports;
        for (var i=0; i<transports.length; i++) {
          if (OraSocketGlobal.wsAvailable && transports[i] === OraSocketGlobal.NATIVE_WEB_SOCKET &&
              (transport === undefined || (transport !== undefined && transport === OraSocketGlobal.NATIVE_WEB_SOCKET))) {
            preferredTransport = transports[i];
            break;
          }                
          else if (OraSocketGlobal.xhrAvailable && transports[i] === OraSocketGlobal.XML_HTTP_REQUEST &&
                   (transport === undefined || (transport !== undefined && transport === OraSocketGlobal.XML_HTTP_REQUEST))) {
            preferredTransport = transports[i];
            break;
          }                
          else if (transports[i] === OraSocketGlobal.JSONP &&
                   (transport === undefined || (transport !== undefined && transport === OraSocketGlobal.JSONP))) {
            preferredTransport = transports[i];
            break;
          }                
        }

        if (preferredTransport === undefined || preferredTransport.length === 0) {
          console.warn("No available transport can be used...");
          alert("No available transport can be used...");
        } else if (OraSocketGlobal.debugLevel > 10) {         
          console.debug("Preferred transport is " + preferredTransport);
        }

//      console.log("First POST: clientID = " + OraSocketGlobal.clientID);
        if (JSON.stringify(OraSocketGlobal.clientID) === "{}") // Then create new, otherwise re-use
          OraSocketGlobal.clientID = xhr.getResponseHeader(RequestParameter.names.CLIENT_ID);
        if (OraSocketGlobal.debugLevel > 10)
          console.debug("Client ID:" + OraSocketGlobal.clientID);
        
        completed = true; // successfully
        loadInterface();
      }
    };
    
    // WARNING: a synchronous request on "localhost" would NEVER come back if the server is down...
//    serverURI = serverURI.replace("localhost", "127.0.0.1");
    
    if (OraSocketGlobal.debugLevel > 10)
      console.debug("First POST request (handshake) to:" + serverURI);    
    xhr.open("POST", serverURI, false); // false:Synchronously!!!
    try { 
      // Means return the transport list, and my unique ID
      xhr.setRequestHeader(RequestParameter.names.WS_ATTEMPT, RequestParameter.values.WS_HAND_SHAKE);  
    } catch (err) { 
      console.warn(err.toString()); 
    }
    try { 
      xhr.send(); 
      completed = true;
//    clearTimeout(to);
    } catch (err) { 
      var errMess = (err.stack !== undefined ? err.stack : JSON.stringify(err));
      console.warn("First POST error:" + errMess);
      self.onerror(err); // The onerror is the default one, client ones not set yet...
      throw err;
    }
  };

  if (OraSocketGlobal.debugLevel > 10)
    console.debug("Parsing server URI: [" + serverURI + "]");
  // open connection. Use document.baseURI for the machine and port, http://machine:port/etc/etc...
  var regExp = new RegExp("(http|ws)(.?):[/]{2}([^/|^:]*):?(\\d*)/(.*)");  
  var matches = regExp.exec(serverURI);
  scheme  = matches[1];
  secured = matches[2];
  machine = matches[3];
  port    = matches[4];
  query   = matches[5];
  if (port === null || port.length === 0)
    port = "80";

  handShake("http" + secured + "://" + machine + ":" + port + "/" + query);
};

// Date formatting
// Provide month names
Date.prototype.getMonthName = function() {
  var month_names = [
                      'January',
                      'February',
                      'March',
                      'April',
                      'May',
                      'June',
                      'July',
                      'August',
                      'September',
                      'October',
                      'November',
                      'December'
                  ];

  return month_names[this.getMonth()];
};

// Provide month abbreviation
Date.prototype.getMonthAbbr = function() {
  var month_abbrs = [
                      'Jan',
                      'Feb',
                      'Mar',
                      'Apr',
                      'May',
                      'Jun',
                      'Jul',
                      'Aug',
                      'Sep',
                      'Oct',
                      'Nov',
                      'Dec'
                  ];

  return month_abbrs[this.getMonth()];
};

// Provide full day of week name
Date.prototype.getDayFull = function() {
  var days_full = [
                    'Sunday',
                    'Monday',
                    'Tuesday',
                    'Wednesday',
                    'Thursday',
                    'Friday',
                    'Saturday'
                  ];
  return days_full[this.getDay()];
};

// Provide full day of week name
Date.prototype.getDayAbbr = function() {
  var days_abbr = [
                    'Sun',
                    'Mon',
                    'Tue',
                    'Wed',
                    'Thur',
                    'Fri',
                    'Sat'
                  ];
  return days_abbr[this.getDay()];
};

// Provide the day of year 1-365
Date.prototype.getDayOfYear = function() {
  var onejan = new Date(this.getFullYear(),0,1);
  return Math.ceil((this - onejan) / 86400000);
};

// Provide the day suffix (st,nd,rd,th)
Date.prototype.getDaySuffix = function() {
  var d = this.getDate();
  var sfx = ["th", "st", "nd", "rd"];
  var val = d % 100;

  return (sfx[(val-20)%10] || sfx[val] || sfx[0]);
};

// Provide Week of Year
Date.prototype.getWeekOfYear = function() {
  var onejan = new Date(this.getFullYear(),0,1);
  return Math.ceil((((this - onejan) / 86400000) + onejan.getDay()+1)/7);
};

// Provide if it is a leap year or not
Date.prototype.isLeapYear = function() {
  var yr = this.getFullYear();
  if ((parseInt(yr) % 4) === 0) {
    if (parseInt(yr) % 100 === 0) {
      if (parseInt(yr) % 400 !== 0)
        return false;
      if (parseInt(yr) % 400 === 0)
        return true;
    }
    if (parseInt(yr) % 100 !== 0)
      return true;
  }
  if ((parseInt(yr) % 4) !== 0)
    return false;
};

// Provide Number of Days in a given month
Date.prototype.getMonthDayCount = function() {
  var month_day_counts = [
                            31,
                            this.isLeapYear() ? 29 : 28,
                            31,
                            30,
                            31,
                            30,
                            31,
                            31,
                            30,
                            31,
                            30,
                            31
                         ];

  return month_day_counts[this.getMonth()];
}; 

// format provided date into this.format format
Date.prototype.format = function(dateFormat) {
  // break apart format string into array of characters
  dateFormat = dateFormat.split("");

  var date = this.getDate(),
      month = this.getMonth(),
      hours = this.getHours(),
      minutes = this.getMinutes(),
      seconds = this.getSeconds(),
      milli = this.getTime() % 1000,
      tzOffset = - (this.getTimezoneOffset() / 60);

  var lpad = function(s, w, len) {
    var str = s;
    while (str.length < len)
      str = w + str;
    return str;
  };

  // get all date properties ( based on PHP date object functionality )
  var date_props = {
    d: date < 10 ? '0'+date : date,
    D: this.getDayAbbr(),
    j: this.getDate(),
    l: this.getDayFull(),
    S: this.getDaySuffix(),
    w: this.getDay(),
    z: this.getDayOfYear(),
    W: this.getWeekOfYear(),
    F: this.getMonthName(),
    m: month < 10 ? '0'+(month+1) : month+1,
    M: this.getMonthAbbr(),
    n: month+1,
    t: this.getMonthDayCount(),
    L: this.isLeapYear() ? '1' : '0',
    Y: this.getFullYear(),
    y: this.getFullYear()+''.substring(2,4),
    a: hours > 12 ? 'pm' : 'am',
    A: hours > 12 ? 'PM' : 'AM',
    g: hours % 12 > 0 ? hours % 12 : 12,
    G: hours > 0 ? hours : "12",
    h: hours % 12 > 0 ? hours % 12 : 12,
    H: hours < 10 ? '0' + hours : hours,
    i: minutes < 10 ? '0' + minutes : minutes,
    s: seconds < 10 ? '0' + seconds : seconds,
    Z: "UTC" + (tzOffset > 0 ?"+":"") + tzOffset,
    _: lpad(milli, '0', 3)
  };

  // loop through format array of characters and add matching data else add the format character (:,/, etc.)
  var date_string = "";
  for (var i=0; i<dateFormat.length; i++) {
    var f = dateFormat[i];
    if (f.match(/[a-zA-Z|_]/g))
      date_string += date_props[f] ? date_props[f] : '';
    else 
      date_string += f;
  }
  return date_string;
};

