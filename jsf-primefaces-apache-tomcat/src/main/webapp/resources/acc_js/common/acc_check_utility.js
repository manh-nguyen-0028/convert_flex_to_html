/**
 *  チェック関連ユーティリティObject
 *  @version 1.0
 *  @author locdx
 *  @created 2016/07/20
 *--------------------------------------------------------------------------------------------*
 * History
 * YYYY/MM/DD	Developer	Content
 *--------------------------------------------------------------------------------------------*
 * 2016/07/20	locdx		Create 
 * 2016/07/27   ThangNM add functions
 * 							daysInMonth, isWarekiDate	
 *--------------------------------------------------------------------------------------------*
 */

var ACCCheckUtility = {
	/**
	 * 空文字チェック処理
	 * @param チェック対象文字列
	 * @return true:空文字 false:空文字でない
	 */
	isEmpty:function(str) {
		if(str === null || str === "" || str === undefined) {
			return true;
		}
		return false;
	}
	,
	/**
	 * 半角数字チェック
	 * @param チェック対象文字列
	 * @return true:OK false:NG
	 */
	isHankakuNumeric:function(str) {
		var regPtn = /[^0-9]/;
		
		if (this.isEmpty(str)) {
			return true;
		}
		
		if(regPtn.test(str)) {
			return false;
		}
		
		return true;
	}
	,
	/**
	 * 半角英数字チェック
	 * @param チェック対象文字列
	 * @return true:OK false:NG
	 */
	isHankakuAlphaNumeric:function(str) {
		var regPtn = /[^a-zA-Z0-9]/;
		
		if (this.isEmpty(str)) {
			return true;
		}
		
		if(regPtn.test(str)) {
			return false;
		}
		return true;
	}
	,
	/**
	 * 時刻チェック(HHMM)
	 * @param チェック対象文字列
	 * @return true:OK false:NG
	 */
	isJikoku:function(str) {
		if (this.isEmpty(str)) {
			return true;
		}
		
		
		if (str.length != 4) {
			return false;
		}
		
		if (!this.isHankakuNumeric(str)) {
			return false;
		}
		
		hh = parseInt(str.substr(0,2));
		mm = parseInt(str.substr(2,2));
		
		if(hh === NaN || mm === NaN || hh<0 || hh>23 || mm<0 || mm>59) {
			return false;
		}
		return true;
	}
	,
	/**
	 * 西暦日付チェック(YYYYMMDD)
	 * @param チェック対象文字列
	 * @return true:OK false:NG
	 */
	isDate:function(str)
	{
		if (this.isEmpty(str)) {
			return true;
		}

		if (str.length !== 8) {
			return false;
		}
		if (!this.isHankakuNumeric(str)) {
			return false;
		}
		// First check for the pattern
	    if(!/^\d{4}\d{1,2}\d{1,2}$/.test(str)) {
	        return false;
	    }

	    // Parse the date parts to integers
	    var year 	= parseInt(str.substr(0,4));
	    var month 	= parseInt(str.substr(4,2));
	    var day 	= parseInt(str.substr(6,2));

	    // Check the ranges of month and year
	    if(year < 1000 || year > 3000 || month == 0 || month > 12) {
	        return false;
	    }

	    var monthLength = [ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 ];

	    // Adjust for leap years
	    if(year % 400 === 0 || (year % 100 !== 0 && year % 4 === 0))
	        monthLength[1] = 29;

	    // Check the range of the day
	    return day > 0 && day <= monthLength[month - 1];
	}
	,
//	/**
//	 * check wareki date
//	 * ex: = isWarekiDate("H.12.02.29") return true 
//	 * @param str
//	 * @returns {Boolean}
//	 */	
//	isWarekiDate : function(str) {
//		//eg. "H.12/02/29"
//		if (str == null || str.length != 10) {
//			return false;
//		}
//		// get year,month,day		
//		year = str.substr(2, 2);
//		month = str.substr(5, 2);
//		day = str.substr(8, 2);
//
//		if (isNaN(year) || isNaN(month)
//				|| isNaN(day)) {
//			return false;
//		}
//
//		//convert to int
//		var nYear = parseInt(year);
//		var nMonth = parseInt(month);
//		var nDay = parseInt(day);
//		//japanese year	
//		MYear = (1911 - 1867)+1;
//		TYear = (1925 - 1911)+1;
//		SYear = (1988 - 1925)+1;
//		//check year
//		c = str.substr(0, 1);
//		switch (c) {
//		case 'M':
//			if (nYear > MYear) {
//				return false;
//			}
//			nYear += 1867;
//			break;
//		case 'T':
//			if (nYear > TYear) {
//				return false;
//			}
//			nYear += 1911;
//			break;
//		case 'S':
//			if (nYear > SYear) {
//				return false;
//			}
//			nYear += 1925;
//			break;
//		case 'H':
//			nYear += 1988;
//			break;
//		default:
//			return false;
//		}
//		//check month
//		if (nMonth < 1 || nMonth > 12) {
//			return false;
//		}
//		//check day
//		nRealDay = this.daysInMonth(nMonth, nYear);
//		if (nDay < 1 || nDay > nRealDay) {
//			return false;
//		}
//		//date is valid
//		return true;
//	}
//	,
//	/**
//	 * 和暦年号チェック
//	 * @param チェック対象文字列
//	 * @return true:OK false:NG
//	 */
//	isWarekiNengo:function(str)
//	{
//		if (this.isEmpty(str)) {
//			return true;
//		}
//
//		if (str.length != 1) {
//			return false;
//		}
//
//		var NENGOR_LIST = [ 'M', 'T', 'S', 'H'];
//		for(i=0;i<NENGOR_LIST.length;i++) {
//			if(str===NENGOR_LIST[i]) {
//				return true;
//			}
//		}
//
//		return false;
//	}
//	,
	/**
	 * 入力文字列が十進数かチェック
	 * 
	 * @param value チェック対象文字列
	 */
	isDecimal:function(value) {
		// 0-9.以外は入力不可とする
		var regPtn = /[^0-9.]/;
		
		if (this.isEmpty(value)) {
			return true;
		}
		
		if(regPtn.test(value)) {
			return false;
		}
		return true;
	}
	,
	/**
	 * 入力文字列が金銭かチェックする
	 * 
	 * @param value チェック対象文字列
	 */
	isMonetary:function(value) {
		var aryTmp = null; 
		var i;
		
		if (this.isEmpty(value)) {
			return true;
		}
		
		// 小数部の数値をチェック
		aryTmp = value.split(".");
		if (aryTmp.length == 2) {
			if (!aryTmp[1].toString().match(/^\d+$/)) {
				return false;
			}
		} else if (aryTmp.length > 2) {
			return false;
		}
		
		// 整数部の数値をチェック
		aryTmp = aryTmp[0].toString().split(",");
		if (aryTmp.length == 1) {
			if (!aryTmp[0].toString().replace(/^[+-]/, "").match(/^\d+$/)) {
				return false;
			}
		} else {
			for (i = 0; i < aryTmp.length; i++) {
				if (i == 0) {
					if (!aryTmp[i].toString().replace(/^[+-]/, "").match(/^\d{1}$|^\d{2}$|^\d{3}$/)) {
						return false;
					}
				} else {
					if (!aryTmp[i].toString().match(/^\d{3}$/)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	,
	/**
	 * 入力文字列が入力可能な数値かチェックする
	 * 
	 * @param value チェック対象文字列
	 * @bol TRUE マイナス数が可、FALSE マイナススが不可
	 */
	isMinusNumber:function(value, bol) {
		// 0-9.以外は入力不可とする
		var regPtn = null;
		
		if(bol === false) {
			regPtn = /[^0-9]/;
		}else{
			regPtn = /[^0-9-]/;
		}
		
		if (this.isEmpty(value)) {
			return true;
		}
		
		if(regPtn.test(value)) {
			return false;
		}
		return true;
	}
	,
	/**
	 * 数値用マスク文字チェックを行う
	 * 
	 */
	isNumberMask:function(value) {
		var formatAryTmp = null;
		var decimalFormat = ""; // 小数部フォーマット
		var integerFormat = ""; // 整数部フォーマット
		var regPtn = /[^#0,.]/;
		
		// 値に何も入っていない場合はチェックＯＫとする。
		if (this.isEmpty(value)) {
			return true;
		}
		
		if(regPtn.test(value)) {
			return false;
		}
		
		formatAryTmp = value.split(".");
		if (formatAryTmp.length == 2) {
			// 小数点が含まれる場合
			integerFormat = formatAryTmp[0];
			decimalFormat = formatAryTmp[1];
			// 整数部小数部どちらかが入ってなければエラー
			if (this.isEmpty(integerFormat)|| this.isEmpty(decimalFormat)) {
				return false;
			}
		} else if (formatAryTmp.length == 1) {
			integerFormat = formatAryTmp[0];
			// 整数部に入ってなければエラー
			if (this.isEmpty(integerFormat)) {
				return false;
			}
		}
		
		return true;
	},
	/**
	 * daysInMonth: 特定の月の日数を取得
	 * private 関数
	 * 
	 * @param month
	 * @param year
	 * @returns
	 */	
	daysInMonth : function(month, year) {
		return new Date(year, month, 0).getDate();
	},
};
