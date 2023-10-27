/**
 * class for counting time,
 * is used in screens: 離席中画面, ＰＤ架電中, 受電待ち画面, etc
 * @version 1.0
 * @author LocDX
 * @created 2016/11/28
 *--------------------------------------------------------------------------------------------*
 * History
 * YYYY/MM/DD	Developer	Content
 *--------------------------------------------------------------------------------------------*
 * 2016/11/28	LocDX		Create 
 *--------------------------------------------------------------------------------------------*
 */

var ACCTimer = (function() {
	
	/** date object, the time that start counting */
	ACCTimer.prototype.startTime = null;
	
	/** date object, current counting value */
	ACCTimer.prototype.nowTime = null;
	
	/** the period of time will update counting value, default after each 1s */ 
	ACCTimer.prototype.duration = 1000; //1s
	
	/** pointer of setInterval function */
	ACCTimer.prototype.intervalVar = null;
	
	/** css style class of element will show counting value */ 
	ACCTimer.prototype.styleClass = null;
	/**
	 * constructor method
	 * @param _styleClass : css tyle class of element (primefaces label element)
	 * @param _startTimeStr: begin counting from this value, format ("YYYY-MM-DD-HH-MM-SS")  
	 */
	function ACCTimer(_styleClass, _startTimeStr) {
		this.styleClass = _styleClass;
		
		var tmp = _startTimeStr.split("-");
		this.startTime = new Date(tmp[0], tmp[1], tmp[2], tmp[3], tmp[4], tmp[5]);
		this.nowTime = this.startTime;
	}

	/**
	 * start counting method
	 */
	ACCTimer.prototype.start = function() {
		var self = this;
		this.intervalVar = setInterval(function() { self.updateTime(self); }, this.duration);
	}
	
	/**
	 * stop counting method
	 */
	ACCTimer.prototype.stop = function() {
		clearInterval(this.intervalVar);
	}
	
	/**
	 * print counting value to screen
	 */
	ACCTimer.prototype.print = function() {
		var now_time = this.addZero(this.nowTime.getHours()) +
			":" + this.addZero(this.nowTime.getMinutes()) +
			":" + this.addZero(this.nowTime.getSeconds());
		$(this.styleClass).text(now_time);
	}
	
	/**
	 * utiliy function for method print
	 * @param i
	 * @returns {String}
	 * 
	 */
	ACCTimer.prototype.addZero = function(i) {
	    if (i < 10) {
	        i = "0" + i;
	    }
	    return i;
	}

	/**
	 * the function passed into setInterval method 
	 * @param timerPointer : pointer point to object ACCTimer
	 */
	ACCTimer.prototype.updateTime = function(timerPointer) {
		timerPointer.nowTime = new Date(timerPointer.nowTime.getTime() + timerPointer.duration);
		timerPointer.print();
	}
	
	return ACCTimer;
	
})();