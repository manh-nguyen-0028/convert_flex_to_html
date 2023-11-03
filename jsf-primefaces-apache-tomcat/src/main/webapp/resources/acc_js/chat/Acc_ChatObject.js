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
		CLOSE_REQUEST : 'closerq',
		// request open screen
		OPEN_REQUEST : 'openrq',
		// expire request
		EXPIRED_REQUEST : 'expiredrq',
		// protect request
		PROTECT_REQUEST : 'protectrq',
		// remind request
		REMIND_REQUEST : 'remindrq',
		// transfer request
		TRANSFER_REQUEST: 'transferrq',
		// transfer response
		TRANSFER_RESPONSE:'transferrp',
		// approval transfer response
		TRANSFER_APPROVAL_RESPONSE: 'transferaprp',
		// 2020/04/06 KienNT (v2.22) Fix Kadai 672 START
		//generic notify response
		GENERIC_NOTIFY_RESPONSE: 'genericnotifyrp'
		// 2020/04/06 KienNT (v2.22) Fix Kadai 672 END
	},

	MESSAGE_TYPE : {

		// group chat
		GROUP_CHAT : '1',
		//group_chat_detail
		// 2020/02/20 Fix　課題No534 NEV-TUTN START
		GROUP_CHAT_DETAIL : '9',
		// 2020/02/20 Fix　課題No534 NEV-TUTN END
		// private chat
		PRIVATE_CHAT : '0',
		// private chat
		SYSTEM_CHAT : '2',
		// transfer
		TRANSFER_CHAT : '3',
		// transfer response
		TRANSFER_RESPONSE_CHAT: '4',
		// transfer approval response
		TRANSFER_APPROVAL: '5'
	},

	MESSAGE_DIRECTION : {

		// send Message
		TYPE_SEND_MESSAGE : '1',
		// receiver Message
		TYPE_RECEIVE_MESSAGE : '0',
	},

	USER_TYPE : {

		// normal user
		NORMAL_USER : 'typeUser',
		// normal user in group
		GROUP_USER : 'typeGroup',
		// user admin in group
		ADMIN_USER : 'typeAdminGroup'
	},

	STATUS : {

		// status online
		ONLINE : "1",
		// status busy
		BUSY : "2",
		// status away
		AWAY : "3",
		// status offline
		OFFLINE : "4"
	},

	ICON : {

		// status online
		ONLINE : "pi pi-check-circle iconOnline icon",
		// status busy
		BUSY : "pi pi-minus-circle iconBusy icon",
		// status away
		AWAY : "pi pi-clock iconAway icon",
		// status offline
		OFFLINE : "pi pi-circle-fill iconOffline icon"
	},

	WINDOW_STATE : {

		// chat screen state online
		ONLINE : "online",

		// chat screen state offline
		OFFLINE : "offline",

		BLUR : "blur",

		FOCUS : "focus",
	},

	CHAT_SCREEN : 'MG1004001',

	// session storage key
	SESSION_STORAGE_KEY_NEW_MESSAGE : 'newMesage',

	// state chat screen
	SESSION_STORAGE_KEY_STATE_CHAT : "chatScreenState",

	// note delete group
	DELETE_GROUP : "delete",

	// note add group
	ADD_GROUP : "add",

	// main screen parameter
	MAIN : "main",

	// chat screen parameter
	CHAT : "chat",

	// action open chat screen
	ACTION_OPENCHAT : "action_openChat",

	// action close chat screen
	ACTION_CLOSECHAT : "action_closeChat",

	// state chat screen focus or blur
	LOCAL_STORAGE_WINDOW_STATE : "chat_screen_state",

	LOCAL_STORAGE_CURRENT_CHAT_ID : "current_chat_id"

}

var user_config = {

	// main or chat screen
	screen : "",

	// status Id
	statusId : "",

	// status Name
	statusName : "",

	// currentChatId
	currentChatId : "",

	// current Type Chat : private/ group
	currentChatType : "",

	// new message
	newMessageContent : '',

	// list message new
	listMessageNew : []

}

var message_info = {

	// request type of message
	messageRequest : "",

	// content of message
	messageContent : "",

	// user send message
	fromUser : "",

	// user name/ group name
	name : "",

	// time send message
	time : "",

	// user receive message
	toUser : "",

	// type of chat ( 1: group chat , 2: private chat )
	type : "",

	// group id
	chanelId : "",

	// admin group
	adminId : "",

	// note
	note : "",

	// user send message name
	fromUserName : ""

}

var CreateChatObj = (function() {

	// constructor
	function CreateChatObj() {

	}

	// create message receiver html
	CreateChatObj.prototype.messageReceiver = function(msg_name, msg_time,
			msg_content) {

		// create message receiver
		var msgRec = "";
		msgRec += " <div class=\"messageReceiver\">";
		msgRec += "		<div style='float:left;font-weight:bold'>" + msg_name + "</div>";
		msgRec += "		<div style='float:right'> " + msg_time + " </div> ";
		msgRec += " </div> ";
		msgRec += " <div class='messageContain'> " + msg_content + "</div>";

		return msgRec;
	}

	// craete message send html
	CreateChatObj.prototype.messageSend = function(msg_name, msg_time,
			msg_content) {

		// create message send
		var msgSend = "";
		msgSend += " <div class=\"messageSender\">";
		msgSend += "		<div style='float:left;font-weight:bold'>" + msg_name + "</div>";
		msgSend += "		<div style='float:right'> " + msg_time + " </div> ";
		msgSend += " </div> ";
		msgSend += " <div class='messageContain'> " + msg_content + "</div>";

		return msgSend;
	}

	CreateChatObj.prototype.groupContact = function(i_data_rk, i_data_ri,
			i_typeGroup) {

		// create group contact
		var groupContact = "";
		groupContact += " <tr class='ui-widget-content ui-datatable-odd ui-datatable-selectable' role='row' aria-selected='false' data-rk='"+ i_data_rk + "' data-ri='" + i_data_ri + "'>";
		groupContact += " 	<td class='text-left " + i_typeGroup + "' role='gridcell'>";
		groupContact += " 		<span class='icon pi pi-users' id='iconStatus'></span>";
		groupContact += " 		<span style='margin-left: 15px;'>" + i_groupName + "</span>";
		groupContact += " 	</td>";
		groupContact += " 	<td role='gridcell'>"
		groupContact += " 		<div class='newMsgCount' style='display: none;'/>"
		groupContact += " 	</td>";
		groupContact += " </tr>";

		return groupContact;
	}

	CreateChatObj.prototype.newMessageNotificationUser = function(time,
			messageContent , fromUser , type) {

		// create newMessageNotification
		var newMsgNotification = "";
		newMsgNotification += "<div id='div_" + fromUser + "_" + type +  "' class='messageNotifyDetail'>"
		newMsgNotification += "		<div id='timeMessage'>" + time + "</div> "
		newMsgNotification += "		<div class='closeIcon' id='iconClose'>"
		newMsgNotification += "			<span><i id='i_" + fromUser + "_" + type +  "' class='pi pi-times'></i></span>"
		newMsgNotification += "		</div>"
		newMsgNotification += "		<div id='messageNotifyContent' title = '" + messageContent + "' >"	+ messageContent + "</div>"
		newMsgNotification += "</div>"
		return newMsgNotification;
	}	
	
	CreateChatObj.prototype.newMessageNotificationSystem = function(time,
			messageContent , fromUser , type) {

		// create newMessageNotification
		var newMsgNotification = "";
		newMsgNotification += "<div id= 'div_" + fromUser + "_" + type +  "' class='messageNotifyDetailSystem'>"
		newMsgNotification += "		<div id='timeMessage'>" + time + "</div> "
		newMsgNotification += "		<div class='closeIcon' id='iconClose'>"
		newMsgNotification += "			<span><i id='i_" + fromUser + "_" + type + "' class='pi pi-times'></i></span>"
		newMsgNotification += "		</div>"
		newMsgNotification += "		<div id='messageNotifyContent' title = '" + messageContent + "'>"	+ messageContent + "</div>"
		newMsgNotification += "</div>"
		return newMsgNotification;
	}
	
	CreateChatObj.prototype.newMessageNotificationTranfer = function(time,
			messageContent , fromUser , type , kjCifNo, userName) {

		// create newMessageNotification
		var newMsgNotification = "";
		newMsgNotification += "<div id= 'div_" + fromUser + "_" + type +  "' class='messageNotifyDetailTranfer' data = '"+ kjCifNo +"' row = '"+ userName +"' >"
		newMsgNotification += "		<div id='timeMessage'>" + time + "</div> "
		newMsgNotification += "		<div class='closeIcon' id='iconClose'>"
		newMsgNotification += "			<span><i id='i_" + fromUser + "_" + type + "' class='pi pi-times'></i></span>"
		newMsgNotification += "		</div>"
		newMsgNotification += "		<div id='messageNotifyContent' title = '" + messageContent + "'>"	+ messageContent + "</div>"
		newMsgNotification += "</div>"
		return newMsgNotification;
	}
	
	CreateChatObj.prototype.newMessageNotificationResponse = function(time,
			messageContent , fromUser , type, kjCifNo) {

		// create newMessageNotification
		var newMsgNotification = "";
		newMsgNotification += "<div id= 'div_" + fromUser + "_" + type +  "' class='messageNotifyDetailResponse' data = '"+ kjCifNo +"' >"
		newMsgNotification += "		<div id='timeMessage'>" + time + "</div> "
		newMsgNotification += "		<div class='closeIcon' id='iconClose'>"
		newMsgNotification += "			<span><i id='i_" + fromUser + "_" + type + "' class='pi pi-times'></i></span>"
		newMsgNotification += "		</div>"
		newMsgNotification += "		<div id='messageNotifyContent' title = '" + messageContent + "'>"	+ messageContent + "</div>"
		newMsgNotification += "</div>"
		return newMsgNotification;
	}

	return CreateChatObj;
})();

// web socket server
var ws;

// get full URL and split
var calledBy = document.location.toString();

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
var listNewMessageQueu = [];
if ("WebSocket" in window) {
	webSocketSupport = true;
}

var contextMenu;

var createChatObj = new CreateChatObj();
