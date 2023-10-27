/**
 * ACCユーティリティJavascript
 * @version 1.0
 * @author thangnm
 * @created 2016/07/15
 *--------------------------------------------------------------------------------------------*
 * History
 * YYYY/MM/DD	Developer	Content
 *--------------------------------------------------------------------------------------------*
 * 2016/07/15	thangnm		Create 
 *--------------------------------------------------------------------------------------------*
 */
var ACCUtil = {
	errNo : 0,
	errStr : "",

	//get local ip address
	getLocalIP : function(callback) {
		window.RTCPeerConnection = window.RTCPeerConnection
				|| window.mozRTCPeerConnection
				|| window.webkitRTCPeerConnection; //compatibility for firefox and chrome
		if (!window.RTCPeerConnection) {
			//alert("Not supported by IE.");  
			return;
		}
		var pc = new RTCPeerConnection({
			iceServers : []
		}), noop = function() {
		};
		pc.createDataChannel(""); //create a bogus data channel
		pc.createOffer(pc.setLocalDescription.bind(pc), noop); // create offer and set local description
		pc.onicecandidate = function(ice) { //listen for candidate events
			if (!ice || !ice.candidate || !ice.candidate.candidate)
				return;
			var myIP = /([0-9]{1,3}(\.[0-9]{1,3}){3}|[a-f0-9]{1,4}(:[a-f0-9]{1,4}){7})/
					.exec(ice.candidate.candidate)[1];
			callback(myIP);
			pc.onicecandidate = noop;
		};
	},
	
	// close current window/tab
	closeCurrentWindow : function() {
		window.opener = null;
		window.open('', '_self');
		window.close();
	}
}
