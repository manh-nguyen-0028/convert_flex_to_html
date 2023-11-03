$(document).ready(	function() {
	// initial chat state (default offline)
	sessionStorage.setItem(SYS_PARAM.SESSION_STORAGE_KEY_STATE_CHAT, SYS_PARAM.WINDOW_STATE.OFFLINE);
	// connect to chat server
	connetChatServer();
	
	// get context menu
	contextMenu = $("#chatMain\\:contextMenu");
	
	if (calledBy.indexOf(SYS_PARAM.CHAT_SCREEN) == -1) {
		// add event listen storage change
		window.addEventListener("storage", function onStorageChange(event) {

			// event open chat
			if (event.key == SYS_PARAM.LOCAL_STORAGE_CURRENT_CHAT_ID) {

				// get current chat ID
				var currentChatId = localStorage.getItem(SYS_PARAM.LOCAL_STORAGE_CURRENT_CHAT_ID);
				var chatType = currentChatId.split(':')[0];
				var chatId = currentChatId.split(':')[1];
				
				if (chatType==message_info.type && (chatId==message_info.fromUser || chatId == message_info.chanelId)) { 
				
					// save to localStorage
					sessionStorage.setItem("newMesage", "");
				
					$("#iconChat").removeClass("newMessageIcon");
					$("#iconChat").removeClass("flashit");
					$("#iconChat").addClass("chatIcon");
				}
			}
		});
	}
	
	// check if has unread messeges then blink chat icon
	// get list new message
	let listMessageJson = sessionStorage.getItem('newUnreadMessages');
	let lsNewMessage;
	
	try {
		lsNewMessage = JSON.parse(listMessageJson);
	} catch (e) {
		console.log('Invalid json messeges');
	}
	
	if (!lsNewMessage) {
		return;
	}
	
	var checked = '1';
	// check new message
	for (var i = 0; i < lsNewMessage.length; i++) {

		if (lsNewMessage[i].isRead == '0') {
			checked = '0';
		}
	}
	
	if (checked != '1') {
		$("#iconChat").addClass('newMessageIcon');
		$("#iconChat").addClass('flashit');
		
	}
	
	for (var i = 0; i < lsNewMessage.length; i++) {
		if (lsNewMessage[i]['isRead'] == '0') {
			// process new message coming
			user_config.listMessageNew.push({
				type : lsNewMessage[i].type,
				time : lsNewMessage[i].time,
				fromUser : lsNewMessage[i].fromUser,
				toUser : lsNewMessage[i].toUser,
				content : lsNewMessage[i].messageContent,
				chanelId : lsNewMessage[i].chanelId
			});
			// create new message icon
		}
	}
	newMsg.createNewMessageIcon();
	
});

// open chat pop up
function openChatPopUp() {
	
	console.log('--------openChatPopUp-------------Start------');
	var width = 840;
	var height = 512;

	var left = (screen.width / 2) - (width / 2);
	var top = (screen.height / 2) - (height / 2);
	var feature = "width="
			+ width
			+ ",height="
			+ height
			+ ",top="
			+ top
			+ ",left="
			+ left
			+ ",toolbar=no,menubar=no,scrollbars=no,resizable=no,location=no,status=no,scrollbars=true";
	var url = "/acc_web/jp/co/nissho_ele/acc/mg/mg1004/MG1004001_00_000.xhtml";
	CHAT_MAIN = window.open(url, "Chat", feature);
	CHAT_MAIN.focus();  
	console.log('--------openChatPopUp-------------End------');
}

// close dialog CL1028
function closeDialogTransfer(){
	PF('dlg').hide();
}

// connect to chat server
function connetChatServer() {
	
	console.log("Start Connect Chat Server. ")
		
	 // check web socket support
	 if (!webSocketSupport) {
        alert("WebSocket not supported by your Browser!");
        return;
    }
	
	user_config.screen = calledBy.indexOf(SYS_PARAM.CHAT_SCREEN) == -1 ? SYS_PARAM.MAIN : SYS_PARAM.CHAT;
	if (screenType == "popup") return;
	
	URI_SUFFIX = "/acc_web/accChat/" + user_config.screen  + "/" + currentUser + "/" + currentCorpCd;
	
	try {
		// websocket uri
		var wsURI = "ws" + secured + "://" + machine + ":" + port + URI_SUFFIX ;
	
		// create websocket connect
		ws = new WebSocket(wsURI);
	} catch (err) {
		console.log("Error creating WebSocket:" + JSON.stringify(err));
	}

	// init websocket
	accWebsocketInit(); 
};

var ChatMessage = (function() {
	
	function ChatMessage(){
		
	}
	
	// set current chat ID
	ChatMessage.prototype.setChatId = function( id , type , name) {
		
		// set user_config
		user_config.currentChatId = id;
		user_config.currentChatType = type;
		
		// set text to lblChatId
		$("#chatMain\\:lblChatId").text(name);
		
	}
	
	// update label name , status
	ChatMessage.prototype.updateLabelStatus = function(name,status){
		$("label[id='chatMain:lblName']").text(name);
		$("label[id='chatMain:lblStatus']").text(status);
	}
	
	// clear text editor when send message
	ChatMessage.prototype.clearText = function() {
		PF('editor1').clear();
	}
	
	return ChatMessage;

})();

// control new message
var NewMessage = (function() {
	
	// constructor
	function NewMessage(){
		
	}
	
	// create new message in text area message
	NewMessage.prototype.createNewMessage = function(msg_name , msg_time , msg_content , msg_type){
		
		// create message receiver/ send
		var val = (msg_type == SYS_PARAM.MESSAGE_DIRECTION.TYPE_RECEIVE_MESSAGE) ? 
						createChatObj.messageReceiver(msg_name , msg_time , msg_content)
						: createChatObj.messageSend(msg_name , msg_time , msg_content);
		
		return $.parseHTML(val);;
	}
	
	// add new record of message to text area message
	NewMessage.prototype.appendNewMessage = function(mess){
	         
		// append to taMessage
	    $("#taMessage").append(mess);
	    $('#taMessage').scrollTop($('#taMessage')[0].scrollHeight);
	}
	
	// remove all record of message from text area message
	NewMessage.prototype.removeAllMessage = function(){
		
		// remove all message from text area message
		$("#taMessage").empty();
	}
	
	// add icon new message
	NewMessage.prototype.createNewMessageIcon = function(){
		
		// get list new message group by user
		var userList = groupBy(user_config.listMessageNew, "chanelId", "fromUser");
		
		// get list user
		var arrUser = Object.keys(userList).map(function(k) { return userList[k] });
		var length = arrUser.length;
		
		for (var i = 0; i < length; i++) {
		
			// count New Message
			var newMessageCount = arrUser[i].length;
			if ( newMessageCount > 0){
				
				// type group chat/private chat
				var type = arrUser[i][0].type;
				
				// user send message
				var fromUser =  arrUser[i][0].fromUser;
				
				// id group
				var chanelId =  arrUser[i][0].chanelId;
				
				// key list contract
				var data_rk = (type == SYS_PARAM.MESSAGE_TYPE.PRIVATE_CHAT) ? type + ":" + fromUser 
																				 : type + ":" + chanelId;
				if ((type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT_DETAIL || type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT) && !chanelId) {
					data_rk = SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT + ":" +  arrUser[i][0].fromUser;
				}
				// display icon
				showNewMessageIcon(data_rk,newMessageCount);
			}		
		}
	}
	
	// remove icon new message
	NewMessage.prototype.removeNewMessageIcon = function (msg_id, msg_type , msg_fromUser) {
		
		// get key of contact list
		var data_rk = msg_type + ":" + msg_id;
		
		// hide icon
		hideNewMessageIcon(data_rk)
		
		// check length list new message
		var length =  user_config.listMessageNew.length;
		if (length == 0)  return; 
			
		// remove new message from user
		user_config.listMessageNew = user_config.listMessageNew.filter(function(el) {
		    return msg_id != el.fromUser && msg_type != el.type;
		});
		
		// reload
		this.createNewMessageIcon();
	}
	
	NewMessage.prototype.updateUnReadMessagesInSession = function(listUnreadMessages) {
		if (!listUnreadMessages) {
			listUnreadMessages = sessionStorage.getItem('newUnreadMessages');
		}
		
		if (!listUnreadMessages) {
			return;
		}
		
		let saveList = [];
		//save new messages list before count new messages from db
		if (user_config.listMessageNew.length > 0) {
			saveList = user_config.listMessageNew.slice();
		}
		
		user_config.listMessageNew = [];
		
		for (var i = 0; i < listUnreadMessages.length; i++) {
			if (listUnreadMessages[i]['isRead'] == '0') {
				// process new message coming
				user_config.listMessageNew.push({
					type : listUnreadMessages[i].type,
					time : listUnreadMessages[i].time,
					fromUser : listUnreadMessages[i].fromUser,
					toUser : listUnreadMessages[i].toUser,
					content : listUnreadMessages[i].messageContent,
					chanelId : listUnreadMessages[i].chanelId,
					receiver: listUnreadMessages[i].receiver
				});
			}
		}
		// create new message icon
		newMsg.createNewMessageIcon();
		user_config.listMessageNew = saveList;
		sessionStorage.setItem('newUnreadMessages',
				JSON.stringify(listUnreadMessages));
	}
	return NewMessage;
})();

// control list contact
var ContactList = (function(){
	
	// constructor
	function ContactList(){
		
	}
	
	// add new group in list contact
	ContactList.prototype.addNewGroup = function( groupName , groupId , adminId){
		
		// get data_ri
		var data_ri = $("#chatMain\\:datUser_data tr").length;
		 
		// get key of contact list
		var data_rk = "1:" + groupId;
		
		// type group
		var typeGroup = ( currentUser == adminId ) ?  SYS_PARAM.USER_TYPE.GROUP_USER 
												   :  SYS_PARAM.USER_TYPE.ADMIN_USER;
		
		var row = $("#chatMain\\:datUser_data tr[data-rk='"+ data_rk +"']");
		
		// check exist
		if (row.length == 0 ){
		
			var html = createChatObj.groupContact( data_rk, data_ri, typeGroup);
			var row = $.parseHTML(html);
			$("#chatMain\\:datUser_data").prepend(row);
		}
	}
	
	// remove group in contact list
	ContactList.prototype.removeGroup = function(groupId){
		
		// get key of contact list
		var data_rk = "1:"+groupId;
		
		// get row
		var row = $("#chatMain\\:datUser_data tr[data-rk='"+ data_rk +"']");
		
		// remove row
		if (row.length > 0 ){
			row.remove();		
		}
	}
	
	// control context menu by role of user
	ContactList.prototype.controlContextMenu = function(){
		
		// get type
		var type =	$("#chatMain\\:datUser tr.ui-state-highlight td:first-child").attr("class") ;		

		// check type record click
		if (type.indexOf(SYS_PARAM.USER_TYPE.NORMAL_USER) != -1) {
			
			displayContextMenu(SYS_PARAM.USER_TYPE.NORMAL_USER);
			
		}  else if (type.indexOf(SYS_PARAM.USER_TYPE.ADMIN_USER) != -1) {
			
			displayContextMenu(SYS_PARAM.USER_TYPE.ADMIN_USER);			
					
		} else if (type.indexOf(SYS_PARAM.USER_TYPE.GROUP_USER) != -1) {
			
			displayContextMenu(SYS_PARAM.USER_TYPE.GROUP_USER);
		}	
	}
	
	// push row to the first row
	ContactList.prototype.pushRow = function(type,groupId){
		
		// get key of contact list
		var data_rk = type + ":" + groupId;
		
		// get row
		var row = $("#chatMain\\:datUser_data tr[data-rk='"+ data_rk +"']");
		
		// push row to the first row
		if (row.length == 1) {
			
			row.remove();
			$("#chatMain\\:datUser_data").prepend(row);
		}	
		
	}
	
	// set icon online,offline,busy,away for contact record
	ContactList.prototype.setIconContact = function(statusId,fromUser) {
		
		// getting icon
		var icon = getIconByStatusId(statusId);
		
		// setting icon
		$("div[id='chatMain:datUser'] tr[data-rk='0:" + fromUser + "'] td span[id='iconStatus']").attr('class', icon);
	}
	
	return ContactList;
	
})();

// function send message to websocket.
function send(request, fromUser, fromUserName, toUser, type, content, chanelId, time, name, arrayUserAdd, arrayUserRemove) {
	// check browser support
	if (!webSocketSupport) {
		alert("WebSocket not supported by your Browser!");
		return;
	}

	// send message to websocket server
	// create object json
	var objectJson = new Object();
	objectJson.request = request;
	objectJson.fromUser = fromUser;
	objectJson.fromUserName = fromUserName;
	objectJson.toUser = toUser;
	objectJson.type = type;
	objectJson.content = content;
	objectJson.chanelId = chanelId;
	objectJson.time = time;
	objectJson.name = name;
	objectJson.userAdd = arrayUserAdd;
	objectJson.userRemove = arrayUserRemove;

	// convert object to json string
	var jsonStr = JSON.stringify(objectJson);

	// check request
	if (request == 'sendrq') {

		if (type == 0) {
			newMsg.appendNewMessage(newMsg.createNewMessage(name, time, content, 1));
			contactLs.pushRow(type, toUser);
			$("#chatMain\\:datUser .ui-datatable-scrollable-body").scrollTop(0);
		} else {
			newMsg.appendNewMessage(newMsg.createNewMessage(currentUserName, time, content, 1));
			contactLs.pushRow(type, chanelId);
			$("#chatMain\\:datUser .ui-datatable-scrollable-body").scrollTop(0);
		}

	}

	// send to websocket
	ws.send(jsonStr);
};

var getClass = function(obj) {
	if (obj && typeof obj === 'object' && Object.prototype.toString.call(obj) !== '[object Array]' && obj.constructor) {
		var arr = obj.constructor.toString().match(/function\s*(\w+)/);
		if (arr && arr.length === 2) {
			return arr[1];
		}
	}
	return false;
};

// function group by
function groupBy(xs, key1, key2) {
	
	return xs.reduce(function(pre, cur){
		const value1 = cur[key1];
		const value2 = cur[key2];
		value1 ? (pre[value1] = pre[value1] || []).push(cur) : (pre[value2] = pre[value2] || []).push(cur);
		return pre;
		},{})
};

// display new message icon and number of new message
function showNewMessageIcon(data_rk,newMessageCount){
	
	// get row
	var div =  $("#chatMain\\:datUser_data tr[data-rk='"+data_rk+"'] .newMsgCount");
	
	// display icon
	div.text(newMessageCount + '');
	div.css('display','block');
}

// hide new message icon and number of new message
function hideNewMessageIcon(data_rk){
	
	// get row
	var div =  $("#chatMain\\:datUser_data tr[data-rk='"+data_rk+"'] .newMsgCount");
	
	// display icon
	div.text("");
	div.css('display','none');
}

// display context menu by type user
function displayContextMenu(type) {
	

	// child 2 : leave group
	var context2 = contextMenu.find('ul li:nth-child(2)');
	
	// child 3 : delete group
	var context3 = contextMenu.find('ul li:nth-child(3)');
		
	
	// check type
	switch(type) {
    case SYS_PARAM.USER_TYPE.NORMAL_USER:
    	
    	// remove context menu if right click normal user
		$("body div#chatMain\\:contextMenu").remove();
        break;
    case SYS_PARAM.USER_TYPE.ADMIN_USER:
    	
    	// display context menu if right click admin user
		contextMenu.appendTo($("body"));
		
		context2.css('display','block');
		context3.css('display','none');
        break;
       
    case SYS_PARAM.USER_TYPE.GROUP_USER:
    	
    	// display context menu if right click group user
		contextMenu.appendTo($("body"));
		context2.css('display','none');
		context3.css('display','block');
    	
    default:
        break;
	}
}

// button hidden add group click
function clickButtonAddGroup(){
	$("#chatMain\\:btnAddGroup").click();
}

// button hidden update group click
function clickButtonUpdateGroup(){
	$("#chatMain\\:btnUpdateGroup").click();
}

// get icon class by status id
function getIconByStatusId(statusId){
	
	var icon = '';
	
	switch (statusId) {
	case SYS_PARAM.STATUS.ONLINE:
		icon = SYS_PARAM.ICON.ONLINE;
		break;
	case SYS_PARAM.STATUS.BUSY:
		icon = SYS_PARAM.ICON.BUSY;
		break;
	case SYS_PARAM.STATUS.AWAY:
		icon = SYS_PARAM.ICON.AWAY;
		break;
	case SYS_PARAM.STATUS.OFFLINE:
		icon = SYS_PARAM.ICON.OFFLINE;
		break;
	default:
		icon = SYS_PARAM.ICON.OFFLINE;
	}
	
	return icon
}


var chatMsg = new ChatMessage();
var newMsg = new NewMessage();
var contactLs = new ContactList();
var newMsgHelp = new NewMessageHelp();

// init websocket
function accWebsocketInit() {

	// on open
	ws.onopen = function() {

		// send status request
		if (calledBy.indexOf(SYS_PARAM.CHAT_SCREEN) != -1) {

			// send request to websocket server
			send(SYS_PARAM.MESSAGE_REQUEST_TYPE.STATUS_REQUEST, currentUser,"",
					"", "", user_config.statusId, "", "", "", "", "");
		}

	};

	// on error
	ws.onerror = function(error) {

		console.log("ws.onerror:" + JSON.stringify(error));
		console.log("Transport is (was) " + ws.getTransport());
	};

	// on message
	ws.onmessage = function(message) {

		// read json object
		var messageJson;
		try {
			messageJson = JSON.parse(message.data);
		} catch (e) {
			console.log(e);
			console.log('This doesn\'t look like a valid JSON: ', message.data);
			return;
		}

		// get request
		message_info.messageRequest = messageJson.request;

		// check screen working
		if (calledBy.indexOf(SYS_PARAM.CHAT_SCREEN) == -1) {

			// not screen MG1004001
			if (message_info.messageRequest == SYS_PARAM.MESSAGE_REQUEST_TYPE.SEND_REQUEST) {

				// check state window MG1004001
				var stateChat  = sessionStorage.getItem(SYS_PARAM.SESSION_STORAGE_KEY_STATE_CHAT);
				var stateWindow  = localStorage.getItem(SYS_PARAM.LOCAL_STORAGE_WINDOW_STATE);
				console.log('stateChat : ' + stateChat);
				console.log('stateWindow : ' + stateWindow);
				if (stateChat == SYS_PARAM.WINDOW_STATE.OFFLINE || (stateChat == SYS_PARAM.WINDOW_STATE.ONLINE && stateWindow == SYS_PARAM.WINDOW_STATE.BLUR )) {
				
					// setting message info
					message_info.fromUser = messageJson.fromUser;
					message_info.name = messageJson.name;
					message_info.chanelId = messageJson.chanelId;
					message_info.type = messageJson.type;
					message_info.time = messageJson.time;
					
					var newMessageContent = message_info.type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT ? msgContentGroup.replace("<%arg1%>",message_info.name) : msgContentOperator.replace("<%arg1%>",message_info.name) ;
					
					// process when got new message
					newMsgHelp.addNewMessage(message_info,newMessageContent);
				}
			}

			// check request : request close chat screen
			if (message_info.messageRequest == SYS_PARAM.MESSAGE_REQUEST_TYPE.CLOSE_REQUEST) {

				// set state offline
				sessionStorage.setItem(
						SYS_PARAM.SESSION_STORAGE_KEY_STATE_CHAT,
						SYS_PARAM.WINDOW_STATE.OFFLINE);

			}

			// check request : request open chat screen
			if (message_info.messageRequest == SYS_PARAM.MESSAGE_REQUEST_TYPE.OPEN_REQUEST) {
				// set state offline
				sessionStorage.setItem(
						SYS_PARAM.SESSION_STORAGE_KEY_STATE_CHAT,
						SYS_PARAM.WINDOW_STATE.ONLINE);

			}
			
			// check request : remind request
			if (message_info.messageRequest == SYS_PARAM.MESSAGE_REQUEST_TYPE.REMIND_REQUEST) {
				
				// setting message info
				message_info.fromUser = messageJson.fromUser;
				message_info.type = messageJson.type;
				message_info.time = messageJson.time;
				
				// get msg content
				var msgContent = messageJson.content.split('*');
				if (msgContent.length != 3) 
					return;
				if ( msgContent[2] == 'E91032') {
					
					message_info.content = msgStartNotification.replace("<%arg1%>",msgContent[0]).replace("<%arg2%>",msgContent[1]);
				} else {
					message_info.content = msgEndNotification.replace("<%arg1%>",msgContent[0]).replace("<%arg2%>",msgContent[1]);
				}
				
				// process when got new message
				newMsgHelp.addNewMessage(message_info,message_info.content);
			}

			// check request : transfer request
			if (message_info.messageRequest == SYS_PARAM.MESSAGE_REQUEST_TYPE.TRANSFER_REQUEST) {
				// Get request info
				var res = messageJson.content.split(":");
				// set data for CL1029
				$("#comfirmDialogForm\\:hiddenToUser").val(messageJson.toUser);
				$("#comfirmDialogForm\\:hiddenToUserName").val(
						messageJson.currentUserName);
				$("#comfirmDialogForm\\:hiddenFromUser").val(
						messageJson.fromUser);
				$("#comfirmDialogForm\\:hiddenToKcifno").val(res[0]);
				// Get msgContent
				var msgId = res[1];
				var msgUser = res[2];
				var userName = res[3];
				var kjCifNo = res[0];
				var msgContent = msgId == 'E91034' ? msgTranfer.replace(
						'<%arg1%>', msgUser).replace('<%arg2%>', userName) : "";
				$(
						"#comfirmDialogForm\\:messageRequest .ui-dialog-content .ui-confirm-dialog-message")
						.html(msgContent);
				// show dialog CL1029
				PF('dlg').show();
			}
			
			// check request: generic notify response
			if(message_info.messageRequest == SYS_PARAM.MESSAGE_REQUEST_TYPE.GENERIC_NOTIFY_RESPONSE){
				console.log(message_info.messageContent);
				// setting message info
				message_info.fromUser = messageJson.fromUser;
				message_info.name = messageJson.name;
				message_info.chanelId = messageJson.chanelId;
				message_info.type = messageJson.type;
				message_info.time = messageJson.time;

				// process when got new message
				newMsgHelp.addNewMessage(message_info,messageJson.content);
			}
		} else {
			// screen MG1004001

			// check request : request login
			if (message_info.messageRequest == SYS_PARAM.MESSAGE_REQUEST_TYPE.LOGIN_REQUEST) {

				// setting message info
				message_info.time = messageJson.time;
				message_info.toUser = messageJson.toUser;
				message_info.fromUser = messageJson.fromUser;
				message_info.messageContent = messageJson.content;

				// login request
				send('sttrq', currentUser, '', '', '1', '');
			}

			// check request : request send message
			if (message_info.messageRequest == SYS_PARAM.MESSAGE_REQUEST_TYPE.SEND_REQUEST) {

				// setting message info
				message_info.type = messageJson.type;
				message_info.time = messageJson.time;
				message_info.fromUser = messageJson.fromUser;
				message_info.fromUserName = messageJson.fromUserName;
				message_info.toUser = messageJson.toUser;
				message_info.messageContent = messageJson.content;
				message_info.chanelId = messageJson.chanelId;
				message_info.name = messageJson.name;

				// process new message coming
				if ((message_info.type == SYS_PARAM.MESSAGE_TYPE.PRIVATE_CHAT && user_config.currentChatId == message_info.fromUser)
						|| (message_info.type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT && user_config.currentChatId == message_info.chanelId)) {

					if ( message_info.fromUser  != currentUser) {
						// append new message to current window chat
						var newMsgFromUser = newMsg.createNewMessage(
							message_info.fromUserName  , message_info.time,
							message_info.messageContent,
							SYS_PARAM.MESSAGE_DIRECTION.TYPE_RECEIVE_MESSAGE);
						newMsg.appendNewMessage(newMsgFromUser);
						//update message status to database
						updateMessageStatus([
							{name : 'senderTtcd' ,
							 value:	message_info.fromUser},
							{name : 'sendMessage',
							 value: message_info.messageContent},
							{name : 'sendDate'   ,
							 value: message_info.time},
							{name : 'toUser',
							 value: message_info.toUser},
							{name : 'chanelId',
							 value: message_info.chanelId}
							]);
					}
				} else {

					// process new message coming
					user_config.listMessageNew.push({
						type : message_info.type,
						time : message_info.time,
						fromUser : message_info.fromUser,
						toUser : message_info.toUser,
						content : message_info.messageContent,
						chanelId : message_info.chanelId
					});
					
					//update message status to database
					updateMessageStatus([
						{name : 'senderTtcd' ,
						 value:	message_info.fromUser},
						{name : 'sendMessage',
						 value: message_info.messageContent},
						{name : 'sendDate'   ,
						 value: message_info.time},
						{name : 'toUser',
						 value: message_info.toUser
						 }
						]);
					
					//save list send messages new before
					let saveList = user_config.listMessageNew.slice();
					
					//check list new messages from db
					let listMessageJson = sessionStorage.getItem('newUnreadMessages');
					let lsNewMessage;
					
					try {
						lsNewMessage = JSON.parse(listMessageJson);
					} catch (e) {
						console.log('Invalid json messeges');
					}
					
					if (lsNewMessage) {
						for (var i = 0; i < lsNewMessage.length; i++) {
							if (lsNewMessage[i]['isRead'] == '0' && message_info.toUser.indexOf(lsNewMessage[i].receiver) > -1) {
								
								// process new message coming
								user_config.listMessageNew.push({
									type : lsNewMessage[i].type,
									time : lsNewMessage[i].time,
									fromUser : lsNewMessage[i].chanelId == '' ? lsNewMessage[i].fromUser : '',
									toUser : lsNewMessage[i].toUser,
									content : lsNewMessage[i].messageContent,
									chanelId : lsNewMessage[i].chanelId
								});
								// create new message icon
							}
						}
					}
					
					
					// create new message icon
					newMsg.createNewMessageIcon();
					
					user_config.listMessageNew = saveList;
				}

				// make contact to the first contact
				contactLs
						.pushRow(
								message_info.type,
								(message_info.type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT) ? message_info.chanelId
										: message_info.fromUser);
			}

			// check request : request change status
			if (message_info.messageRequest == SYS_PARAM.MESSAGE_REQUEST_TYPE.STATUS_REQUEST) {

				message_info.time = messageJson.time;
				message_info.fromUser = messageJson.fromUser;
				message_info.messageContent = messageJson.content;

				// setting icon
				contactLs.setIconContact(message_info.messageContent,
						message_info.fromUser)
			}

			// check request : request group
			if (message_info.messageRequest == SYS_PARAM.MESSAGE_REQUEST_TYPE.GROUP_REQUEST) {

				message_info.fromUser = messageJson.fromUser;
				message_info.chanelId = messageJson.chanelId;
				message_info.name = messageJson.name;
				message_info.adminId = messageJson.toUser;
				message_info.note = messageJson.content;

				if (message_info.note == SYS_PARAM.ADD_GROUP) {

					// create new group
					$("#remoteFrom\\:typeRemote").val('1');
					remoteReload();
				} else {

					// check current group
					if (user_config.currentChatId != message_info.chanelId) {

						// remove group
						contactLs.removeGroup(message_info.chanelId);
					}
				}
			}
			
			// check request : expired request
			if (message_info.messageRequest == SYS_PARAM.MESSAGE_REQUEST_TYPE.EXPIRED_REQUEST) {
				
				window.close();
			}
			
			// check request : protect websocket request
			if (message_info.messageRequest == SYS_PARAM.MESSAGE_REQUEST_TYPE.PROTECT_REQUEST) {
				
				console.log(msgSecurity.replace("<%arg1%>",currentUserName));
				$("#remoteFrom\\:typeRemote").val('2');
				remoteReload();
			}
			

		}

	};

	ws.onclose = function() {

		console.log('Websocket closing');
	}
};