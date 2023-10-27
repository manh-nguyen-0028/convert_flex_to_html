/**
 * IMEモード交換Javascript
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
var ACCImeJS = {
	x : null,
	error: 0,
	zenhira: 9,
	zenkata: 11,
	hankata: 3,
	zeneisu: 8,
	haneisu: 0,	

	initIme : function() {
		try {
			x = new ActiveXObject("IMEControl.class1");
		} catch (e) {
			//alert(e.message);
			alert("This browser does not support ActiveX.");
		}		
	},
	
	setImeMode: function (mode) {
		x.ime_mode(mode);
	},
		
	setZenhira: function(){
		this.setImeMode(this.zenhira);		
	},
	
	setZenkata: function(){
		this.setImeMode(this.zenkata);	
	},
	
	setHankata: function(){
		this.setImeMode(this.hankata);		
	},
	
	setZeneisu: function(){
		this.setImeMode(this.zeneisu);		
	},
	
	setHaneisu: function(){
		this.setImeMode(this.haneisu);		
	},
	
}
