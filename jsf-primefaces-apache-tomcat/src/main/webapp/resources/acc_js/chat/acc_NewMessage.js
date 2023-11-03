$(document).ready(function() {

	var newMsgHelp = new NewMessageHelp();
	var createChatObj = new CreateChatObj();

	$("#iconChat").click(showNewMessagePanel);

	/* $('#messageNotify #messageNotifyContent').click(newMessageContentClick); */

	newMessageIcon();

});

$(document).mouseup(
		function(e) {
			var messagePanel = $("#messageNotifyPanel");
			var iconChatPanel = $("#iconChat");
			var iconChatSpan = $("#iconChat span");
			var iconChatLabel = $("#iconChat span i");

			// check active
			var isActive = $("#messageNotifyPanel.active").length;
			if (isActive != 1) {
				return;
			}

			if (!messagePanel.is(e.target)
					&& messagePanel.has(e.target).length === 0) {
				if (iconChatPanel.is(e.target) || iconChatLabel.is(e.target)
						|| iconChatSpan.is(e.target))
					return;

				hideNewMessagePanel();
			}
		});

// function for new message use in main screen
var NewMessageHelp = (function() {

	function NewMessageHelp() {

	}

	// set current chat ID
	NewMessageHelp.prototype.addNewMessage = function(messageInfo,
			messageContent) {

		// display screen when got new message
		$("#iconChat").addClass('newMessageIcon');
		$("#iconChat").addClass('flashit');

		// Check browser support
		if (typeof (Storage) !== "undefined") {

			// add new message to list message
			// check object new message
			if (messageInfo == null) {
				return;
			}

			// get list message
			var lsNewMessage = newMsgHelp.getListMessageObject();

			// check list new message
			var result = checkExistMessageInfo(lsNewMessage, messageInfo);
			if (result != null && result != -1) {

				// update new message in list
				lsNewMessage[result].time = messageInfo.time;
				lsNewMessage[result].isRead = '0';
			} else {
				// add to list new message
				lsNewMessage.push({
					fromUser : message_info.fromUser,
					name : message_info.name,
					chanelId : message_info.chanelId,
					type : message_info.type,
					time : message_info.time,
					messageContent : messageContent,
					isRead : '0'
				});
			}

			// sort
			lsNewMessage.sort(newMsgSort);

			// check messageNotifyPanel
			if ($("#messageNotifyPanel").hasClass("active")) {

				// get id
				var idContent = 'div_'
						+ (message_info.type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT ? message_info.chanelId
								+ '_1'
								: message_info.fromUser + '_0');

				// add to list queue
				listNewMessageQueu.push({
					id : idContent,
					time : message_info.time
				});

				// update new msg notify panel
				// empty list
				$('#messageNotifyPanel').empty();

				for (var i = 0; i < lsNewMessage.length; i++) {

					if (lsNewMessage[i].type == SYS_PARAM.MESSAGE_TYPE.SYSTEM_CHAT) {

						var html = createChatObj.newMessageNotificationSystem(
								lsNewMessage[i].time,
								lsNewMessage[i].messageContent,
								lsNewMessage[i].fromUser, lsNewMessage[i].type);
						var row = $.parseHTML(html);

						$('#messageNotifyPanel').append(row);

						// update list new message
						lsNewMessage[i].isRead = '1';

					} else if (lsNewMessage[i].type == SYS_PARAM.MESSAGE_TYPE.TRANSFER_CHAT) {
						var html = createChatObj.newMessageNotificationTranfer(
								lsNewMessage[i].time,
								lsNewMessage[i].messageContent,
								lsNewMessage[i].fromUser, lsNewMessage[i].type);
						var row = $.parseHTML(html);

						$('#messageNotifyPanel').append(row);

						// update list new message
						lsNewMessage[i].isRead = '1';
					} else if (lsNewMessage[i].type == SYS_PARAM.MESSAGE_TYPE.TRANSFER_RESPONSE_CHAT) {
						var html = createChatObj
								.newMessageNotificationResponse(
										lsNewMessage[i].time,
										lsNewMessage[i].messageContent,
										lsNewMessage[i].fromUser,
										lsNewMessage[i].type);
						var row = $.parseHTML(html);

						$('#messageNotifyPanel').append(row);

						// update list new message
						lsNewMessage[i].isRead = '1';
					} else if (lsNewMessage[i].type == SYS_PARAM.MESSAGE_TYPE.TRANSFER_APPROVAL) {
						var html = createChatObj
								.newMessageNotificationResponse(
										lsNewMessage[i].time,
										lsNewMessage[i].messageContent,
										lsNewMessage[i].fromUser,
										lsNewMessage[i].type);
						var row = $.parseHTML(html);

						$('#messageNotifyPanel').append(row);

						// update list new message
						lsNewMessage[i].isRead = '1';
					} else {

						var id = lsNewMessage[i].type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT ? lsNewMessage[i].chanelId
								: lsNewMessage[i].fromUser;
						var html = createChatObj.newMessageNotificationUser(
								lsNewMessage[i].time,
								lsNewMessage[i].messageContent, id,
								lsNewMessage[i].type);
						var row = $.parseHTML(html);

						$('#messageNotifyPanel').append(row);

						// update list new message
						lsNewMessage[i].isRead = '1';
					}
				}

				// add event
				$(".closeIcon span i").click(deleteNewMessage);
				$("#messageNotifyPanel #messageNotifyContent").click(
						newMessageContentClick);

				// set background
				for (var i = 0; i < listNewMessageQueu.length; i++) {

					$('#' + listNewMessageQueu[i].id).addClass('flashitNewMsg');
				}

			}

			// save to sessionStorage
			sessionStorage.setItem(SYS_PARAM.SESSION_STORAGE_KEY_NEW_MESSAGE,
					JSON.stringify(lsNewMessage));
		}

	}

	NewMessageHelp.prototype.deleteNewMessage = function(e) {

		// get id
		var id = e.target.attributes.id.value;
		var res = id.split("_");
		var msgInfo = new Object();
		if (res.length > 2) {
			msgInfo.fromUser = res[1];
			msgInfo.type = res[2];
		}
		var lsNewMessage = newMsgHelp.getListMessageObject();

		if (msgInfo.type != SYS_PARAM.MESSAGE_TYPE.SYSTEM_CHAT) {

			// remove div
			$(e.target).closest('div.messageNotifyDetail').remove()

			// remove from list new message
			lsNewMessage = lsNewMessage
					.filter(function(el) {
						return ((msgInfo.type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT && el.chanelId != msgInfo.fromUser) || (msgInfo.type == SYS_PARAM.MESSAGE_TYPE.PRIVATE_CHAT && el.fromUser != msgInfo.fromUser));
					});

		} else {

			// get time message
			var time = $(e.target).closest('div.messageNotifyDetailSystem')
					.find("#timeMessage").html();

			// remove div
			$(e.target).closest('div.messageNotifyDetailSystem').remove()

			// remove from list new message
			lsNewMessage = lsNewMessage
					.filter(function(el) {
						return (msgInfo.type == SYS_PARAM.MESSAGE_TYPE.SYSTEM_CHAT && compareTime(
								el.time, time) != 0);
					});
		}

		// check lsNewMessage
		if (lsNewMessage.length == 0) {
			newMsgHelp.hideNewMessagePanel()
		}

		// update session
		sessionStorage.setItem(SYS_PARAM.SESSION_STORAGE_KEY_NEW_MESSAGE, JSON
				.stringify(lsNewMessage));
	}

	NewMessageHelp.prototype.getListMessageObject = function() {

		// get list new message
		var listMessageJson = sessionStorage
				.getItem(SYS_PARAM.SESSION_STORAGE_KEY_NEW_MESSAGE);
		var lsNewMessage = [];
		if (listMessageJson != null && listMessageJson.length != 0) {
			lsNewMessage = JSON.parse(listMessageJson);
		}
		return lsNewMessage;
	}

	NewMessageHelp.prototype.showNewMessagePanel = function() {

		// check screen working
		if (calledBy.indexOf(SYS_PARAM.CHAT_SCREEN) == -1) {

			// get list new message
			var lsNewMessage = newMsgHelp.getListMessageObject();
			var checked = checkNewMessage();

			// check new message
			if (checked == '1') {
				openChatPopUp();

			} else {

				// show new message content
				$('#messageNotifyPanel').toggleClass('active');

				// hidden button
				$("#iconChat").css("display", "none");

				// empty list
				$('#messageNotifyPanel').empty();

				for (var i = 0; i < lsNewMessage.length; i++) {

					if (lsNewMessage[i].type == SYS_PARAM.MESSAGE_TYPE.SYSTEM_CHAT) {
						var html = createChatObj.newMessageNotificationSystem(
								lsNewMessage[i].time,
								lsNewMessage[i].messageContent,
								lsNewMessage[i].fromUser, lsNewMessage[i].type);
						var row = $.parseHTML(html);

						$('#messageNotifyPanel').append(row);

						// update list new message
						lsNewMessage[i].isRead = '1';
					}
					// check type tranfer request CL1028
					else if (lsNewMessage[i].type == SYS_PARAM.MESSAGE_TYPE.TRANSFER_CHAT) {

						var res = lsNewMessage[i].messageContent.split(':');
						if (res.length != 4)
							return;

						var msgId = res[1];
						var msgUser = res[2];
						var userName = res[3];
						var kjCifNo = res[0];

						var msgContent = msgId == 'E91034' ? msgTranfer
								.replace('<%arg1%>', msgUser).replace(
										'<%arg2%>', userName) : "";

						var html = createChatObj.newMessageNotificationTranfer(
								lsNewMessage[i].time, msgContent,
								lsNewMessage[i].fromUser, lsNewMessage[i].type,
								kjCifNo, userName);
						var row = $.parseHTML(html);
						$('#messageNotifyPanel').append(row);

						// update list new message
						lsNewMessage[i].isRead = '1';
					}
					// check type transfer response CL1029
					else if (lsNewMessage[i].type == SYS_PARAM.MESSAGE_TYPE.TRANSFER_RESPONSE_CHAT) {

						var res = lsNewMessage[i].messageContent.split(':');
						if (res.length != 3)
							return;

						var userName = res[0];
						var msgId = res[1];
						var msgUser = res[2];
						var msgContentResponse = msgId == 'E91035' ? msgTranferResponse
								.replace('<%arg1%>', userName).replace(
										'<%arg2%>', msgUser)
								: "";
						var html = createChatObj
								.newMessageNotificationResponse(
										lsNewMessage[i].time,
										msgContentResponse,
										lsNewMessage[i].fromUser,
										lsNewMessage[i].type, userName);
						var row = $.parseHTML(html);
						$('#messageNotifyPanel').append(row);

						// update list new message
						lsNewMessage[i].isRead = '1';
					}
					// check type transfer approval response CL1029
					else if (lsNewMessage[i].type == SYS_PARAM.MESSAGE_TYPE.TRANSFER_APPROVAL) {

						var res = lsNewMessage[i].messageContent.split(':');
						if (res.length != 3)
							return;

						var userName = res[0];
						var msgId = res[1];
						var msgUser = res[2];
						var msgContentResponse = msgId == 'E91036' ? msgTranferApprovalResponse
								.replace('<%arg1%>', userName).replace(
										'<%arg2%>', msgUser)
								: "";
						var html = createChatObj
								.newMessageNotificationResponse(
										lsNewMessage[i].time,
										msgContentResponse,
										lsNewMessage[i].fromUser,
										lsNewMessage[i].type, userName);
						var row = $.parseHTML(html);
						$('#messageNotifyPanel').append(row);

						// update list new message
						lsNewMessage[i].isRead = '1';
					} else {

						var id = lsNewMessage[i].type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT ? lsNewMessage[i].chanelId
								: lsNewMessage[i].fromUser;
						var html = createChatObj.newMessageNotificationUser(
								lsNewMessage[i].time,
								lsNewMessage[i].messageContent, id,
								lsNewMessage[i].type);
						var row = $.parseHTML(html);

						$('#messageNotifyPanel').append(row);

						// update list new message
						lsNewMessage[i].isRead = '1';
					}
				}

				// add event
				$(".closeIcon span i").click(deleteNewMessage);
				$("#messageNotifyPanel #messageNotifyContent").click(
						newMessageContentClick);

				// save to sessionStorage
				sessionStorage.setItem(
						SYS_PARAM.SESSION_STORAGE_KEY_NEW_MESSAGE, JSON
								.stringify(lsNewMessage));

			}
		}

	}

	NewMessageHelp.prototype.hideNewMessagePanel = function() {

		// close message content
		$('#messageNotifyPanel').toggleClass('active');

		// show
		$("#iconChat").css("display", "block");
		$("#iconChat").removeClass("newMessageIcon");
		$("#iconChat").removeClass("flashit");
		$("#iconChat").addClass("chatIcon");

		// remove list message queue
		listNewMessageQueu = [];
	}

	NewMessageHelp.prototype.newMessageContentClick = function(e) {

		// get msg_info
		var id = e.target.parentNode.attributes.id.value;
		var res = id.split("_");
		if (res.length < 3)
			return;
		var msgInfo = new Object();
		msgInfo.type = res[2];
		msgInfo.chanelId = msgInfo.type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT ? res[1]
				: "";
		msgInfo.fromUser = msgInfo.type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT ? ""
				: res[1];

		// check msg system
		if (msgInfo.type == SYS_PARAM.MESSAGE_TYPE.SYSTEM_CHAT) {
			return;

		} else if (msgInfo.type == SYS_PARAM.MESSAGE_TYPE.TRANSFER_CHAT) {
			// get values toUser,toUserName,toKcjfno
			var dataCIF = e.target.parentNode.attributes.data.value;
			var dataUser = e.target.parentNode.attributes.row.value;
			var message = e.target.parentNode.lastChild.attributes.title.value;
			// set values
			$("#comfirmDialogForm\\:hiddenToUser").val(res[1]);
			$("#comfirmDialogForm\\:hiddenToUserName").val(dataUser);
			$("#comfirmDialogForm\\:hiddenToKcifno").val(dataCIF);
			$(
					"#comfirmDialogForm\\:messageRequest .ui-dialog-content .ui-confirm-dialog-message")
					.html(message)
			// clear message
			$('#messageNotifyPanel').toggleClass('active');
			// hide message
			hideNewMessagePanel();
			// delete message
			newMsgHelp.deleteNewMessage(e);
			// show dialog CL1029
			PF('dlg').show();

		} else if (msgInfo.type == SYS_PARAM.MESSAGE_TYPE.TRANSFER_RESPONSE_CHAT) {
			// clear message
			$('#messageNotifyPanel').toggleClass('active');
			// hide message
			hideNewMessagePanel();
			// delete message
			newMsgHelp.deleteNewMessage(e);
		} else if (msgInfo.type == SYS_PARAM.MESSAGE_TYPE.TRANSFER_APPROVAL) {
			// clear message
			$('#messageNotifyPanel').toggleClass('active');
			// hide message
			hideNewMessagePanel();
			// delete message
			newMsgHelp.deleteNewMessage(e);
		} else {
			// get session storage state chat screen
			var state = sessionStorage
					.getItem(SYS_PARAM.SESSION_STORAGE_KEY_STATE_CHAT);
			if (state != SYS_PARAM.WINDOW_STATE.ONLINE) {
				openChatPopUp();
			}
			// 2020/03/03 TuTN Fix [v2.21] Kadai 534 START
			// action open chat screen and focus to user send message
			localStorage
					.setItem(
							SYS_PARAM.ACTION_OPENCHAT,
							msgInfo.type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT ? msgInfo.type
									+ ':' + msgInfo.chanelId
									: msgInfo.type + ':' + msgInfo.fromUser);
			// 2020/03/03 TuTN Fix [v2.21] Kadai 534 END
			// clear message
			$('#messageNotifyPanel').toggleClass('active');

			// show
			$("#iconChat").css("display", "block");
			$("#iconChat").removeClass("newMessageIcon");
			$("#iconChat").removeClass("flashit");
			$("#iconChat").addClass("chatIcon");

		}
	}

	return NewMessageHelp;

})();

// click new message content
function newMessageContentClick(e) {

	newMsgHelp.newMessageContentClick(e);
	// 2020/02/20 Fix　課題No534 NEV-TUTN START
	let listUnreadMessages = sessionStorage.getItem('newUnreadMessages');
	if (!listUnreadMessages) {
		return;
	}
	newMsg.updateUnReadMessagesInSession(listUnreadMessages);
	// 2020/02/20 Fix　課題No534 NEV-TUTN END
}

// show new message content
function showNewMessagePanel() {

	newMsgHelp.showNewMessagePanel();
}

// hide new message panel
function hideNewMessagePanel() {

	newMsgHelp.hideNewMessagePanel();
}

// remove new message
function deleteNewMessage(e) {

	newMsgHelp.deleteNewMessage(e);
}

// check exit message info
function checkExistMessageInfo(lsNewMessage, messageInfo) {

	for (var i = 0; i < lsNewMessage.length; i++) {
		if ((messageInfo.type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT
				&& lsNewMessage[i].type == SYS_PARAM.MESSAGE_TYPE.GROUP_CHAT && lsNewMessage[i].chanelId == messageInfo.chanelId)
				|| (messageInfo.type == SYS_PARAM.MESSAGE_TYPE.PRIVATE_CHAT
						&& lsNewMessage[i].type == SYS_PARAM.MESSAGE_TYPE.PRIVATE_CHAT && lsNewMessage[i].fromUser == messageInfo.fromUser)) {
			return i;
		}
	}

	return -1;

}

// show new message icon
function newMessageIcon() {

	var checked = checkNewMessage();

	if (checked == '0') {
		$("#iconChat").addClass("newMessageIcon");
		$("#iconChat").addClass('flashit');

	} else {
		// case no message income
		$("#iconChat").addClass("chatIcon");
	}
}

// check new message
function checkNewMessage() {

	// get list new message
	var lsNewMessage = newMsgHelp.getListMessageObject();

	var checked = '1';
	// check new message
	for (var i = 0; i < lsNewMessage.length; i++) {

		if (lsNewMessage[i].isRead == '0') {
			checked = '0';
		}
	}

	return checked;
}

// sort new message
function newMsgSort(a, b) {

	var time1 = a.time.split(' ');
	var time2 = b.time.split(' ');
	if (time1.length != 2 || time2.length != 2)
		return 0;
	return compareTime(time1[1], time2[1]);
}

function compareTime(time1, time2) {

	if (time1 == time2) {
		return 0;
	} else {
		return time1 < time2 ? 1 : -1;
	}
}