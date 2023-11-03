const
SYS_PARAM = {

	// type request of message
	MESSAGE_REQUEST_TYPE : {

		// request change status
		STATUS_REQUEST : 'sttrq',
		// request send/receive message
		SEND_REQUEST : 'sendrq',
		// request login
		LOGIN_REQUEST : 'lgrq',
		// request add/remove group
		GROUP_REQUEST : 'grouprq',
		// request close screen
		CLOSE_REQUEST : 'closerq'

	}

}

var user_config = {

	// status Id
	statusId : "",

	// status Name
	statusName : "",

	// currentChatId
	currentChatId : "admin",

	// current Type Chat : private/ group
	currentChatType : ""

}

var server_config = {

}

// web socket server
var ws;

// get full URL and split
var calledBy = document.location.toString();

// user send message
var fromUser = "";

// user receive message
var toUser = "";

// content of message
var message = "";

// type of chat ( 1: group chat , 2: private chat )
var type = "";

var regExp = new RegExp("(http|ws)(.?):[/]{2}([^/|^:]*):?(\\d*)/(.*)");
var matches = regExp.exec(calledBy);
var enforceTransport, machine, port, secured;

// http/https
secured = matches[2];

// ip address
machine = matches[3];

// port
port = matches[4];

// url
query = matches[5];
var URI_SUFFIX;

// check websocket support or not
var webSocketSupport = false
if ("WebSocket" in window) {
	webSocketSupport = true;
}

// content field
contentFld = $("textarea[id='chatMain:taMessage']");

// list message new
var listMessageNew = [];

// new message
var newMessageContent = '';

const
ACTION_OPENCHAT = "action_openChat";

var userSendMessage = '';

var contextMenu;

var chatMsg = new chatMessage();