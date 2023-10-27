/**
 * MG1001001_00_000.js
 * @version 1.0
 * @author NEV
 * @created 2016/09/14
 *--------------------------------------------------------------------------------------------*
 * History
 * YYYY/MM/DD	Developer	Content
 *--------------------------------------------------------------------------------------------*
 * 2016/09/14	HungND		Create 
 *--------------------------------------------------------------------------------------------*
 */


//2020/01/21 Fix　Kadai No534 TuTN START
var hasUnreadMessges = function(lsNewMessage) {
	// save to sessionStorage
	sessionStorage.setItem('newMesage',
			JSON.stringify(lsNewMessage));
	sessionStorage.setItem('newUnreadMessages',
			JSON.stringify(lsNewMessage));
}

//	2020/01/21 Fix　Kadai No534 TuTN END  
