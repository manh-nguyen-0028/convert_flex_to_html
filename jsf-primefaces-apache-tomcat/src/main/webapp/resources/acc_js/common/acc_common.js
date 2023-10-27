/**
 * 共通Javascript
 * @version 1.0
 * @author NEV
 * @created 2016/07/15
 */

var if_ctrl = 0;
var if_r = 0;
window.document.onkeydown = function() {
	// F5 キー押下
    if (event.keyCode == 116) {
    	// F5 キー押下を無効にする
        return false;
	}
    //2019/11/11 TuTN Fix [v2.21]Kadai625 START
    if (event.ctrlKey && event.keyCode == 82) {
		return false;
	}
}

/**
 * 右クリックを無効にする
 */
function disableRightMouseClick() {
	// ヘルプを禁止
	document.onhelp = function() { // for ie
		return false;
	};
	// 右クリックのメニューを禁止
	document.oncontextmenu = function() {
		return false;
	};
}

$(document).ready(function() {
	
	disableRightMouseClick();
});

/**
 * Function Button enum list
 */
var FunctionButton = {
	F1 : "btnF1Id",
	F2 : "btnF2Id",
	F3 : "btnF3Id",
	F4 : "btnF4Id",
	F5 : "btnF5Id",
	F6 : "btnF6Id",
	F7 : "btnF7Id",
	F8 : "btnF8Id",
	F9 : "btnF9Id",
	F10 : "btnF10Id",
	F11 : "btnF11Id",
	F12 : "btnF12Id"
}

/**
 * When user press F1->F12 key, click the corresponding button on the screen
 *
 * @param event
 * @param formId
 */
function setBtnClickToKey(event, formId) {
	// read the input keycode
	switch (event.keyCode) {
	// if user presses the "F1" key
	case 112:
		document.getElementById(formId + ":" + FunctionButton.F1).click();
		break;
	// if user presses the "F2" key
	case 113:
		document.getElementById(formId + ":" + FunctionButton.F2).click();
		break;
	case 114:
		document.getElementById(formId + ":" + FunctionButton.F3).click();
		break;
	case 115:
		document.getElementById(formId + ":" + FunctionButton.F4).click();
		break;
	case 116:
		document.getElementById(formId + ":" + FunctionButton.F5).click();
		break;
	case 117:
		document.getElementById(formId + ":" + FunctionButton.F6).click();
		break;
	case 118:
		document.getElementById(formId + ":" + FunctionButton.F7).click();
		break;
	case 119:
		document.getElementById(formId + ":" + FunctionButton.F8).click();
		break;
	case 120:
		document.getElementById(formId + ":" + FunctionButton.F9).click();
		break;
	case 121:
		document.getElementById(formId + ":" + FunctionButton.F10).click();
		break;
	case 122:
		document.getElementById(formId + ":" + FunctionButton.F11).click();
		break;
	case 123:
		document.getElementById(formId + ":" + FunctionButton.F12).click();
		break;
	}
}

/**
 * Alt, Ctrl キーを無効にする
 *
 * @param event
 * @returns {Boolean}
 */
function disableAltCtrl(event) {
	var pressedKey = String.fromCharCode(event.keyCode).toLowerCase();

	if (((event.altKey == true) || (event.ctrlKey == true))
			&& (((event.keyCode >= 65) && (event.keyCode <= 90)) || ((event.keyCode >= 97) && (event.keyCode <= 122)))) {
		// Alt+F4
		if (event.keyCode == 115) {
			event.cancel = true;
			event.keyCode = 116;
			event.stopPropagation();
			event.prevententDefault();
			event.returnValue = false;
		}

		if (pressedKey == 'f') {
			// alert("Alt+F");
		}

		event.returnValue = false;
		return false;
	}

	return true;
}

/**
 * params:
 *
 * @formId : id of the form
 * @tabIndexTotal : number of elements input in the form
 * @btnArray : array of button's id
 * @firstButtonIndex : index of first button
 */
function setTabIndex(formId, tabIndexTotal, btnArray, firstButtonIndex) {
	$('#' + formId + ' input[type=text],[type=password]').keydown(
			function(e) {
				if (e.keyCode == 13 || e.keyCode == 9) {
					ctab = Number(this.getAttribute('tabindex'));
					ctab = ctab + 1;
					if (ctab == tabIndexTotal) {
						ctab = 0;
					}
					if (ctab == firstButtonIndex) {
						nextButtonIndex = ctab - firstButtonIndex;
						document.getElementById(
								formId + ':' + btnArray[nextButtonIndex])
								.focus();
					} else {
						inputs = $('#' + formId
								+ ' input[type=text],[type=password]');
						inputs[ctab].focus();
					}
					return false;
				}
			});
}

/*******************************************************************************
 * acc_componentに使用するjavascript
 ******************************************************************************/
/**
 * <acc:inputTextDate> javascript Primefacesのカレンダーを日本語化
 */
PrimeFaces.locales['ja'] = {
	closeText : '閉じる',
	prevText : '先月',
	nextText : '翌月',
	currentText : '今日',
	monthNames : [ '1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月',
			'11月', '12月' ],
	monthNamesShort : [ '1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月',
			'10月', '11月', '12月' ],
	dayNames : [ '日曜', '月曜', '火曜', '水曜', '木曜', '金曜', '土曜' ],
	dayNamesShort : [ '日', '月', '火', '水', '木', '金', '土' ],
	dayNamesMin : [ '日', '月', '火', '水', '木', '金', '土' ],
	firstDay : 7,
	showMonthAfterYear : true,
	yearSuffix : '年',
	timeText : '時間',
	hourText : '　時',
	minuteText : '　分',
	secondText : '　秒'
};

/**
 * <acc:inputTextWareki> javascript
 *
 * @param obj
 *            inputTextWareki
 */
function setInputMask(obj) {
	var selector = document.getElementById(obj.id);
	var im = new Inputmask("a{1}.9{2}.9{2}.9{2}", {
		definitions : {
			"a" : {
				validator : "[mMsStThH]",
				cardinality : 1,
				casing : "upper"
			}
		}
	});
	im.mask(selector);
	selector.onfocus = null;
}

/**
 * バイト数 Validate用関数
 *
 * @param str:
 * @param bytes :
 *            ex: cutStringByByteLength('ＨｅＬlＯ', 2) = 'Ｈ';
 */
function cutStringByByteLength(str, bytes) {
	var r = 0;
	for (var i = 0; i < str.length; i++) {
		var c = str.charCodeAt(i);

		// when input component is textarea, it's value can include new line
		// character has byte length = 2;
		if (str[i] === "\n") {
			r += 2;
			// Shift_JIS: 0x0 ～ 0x80, 0xa0 , 0xa1 ～ 0xdf , 0xfd ～ 0xff
			// Unicode : 0x0 ～ 0x80, 0xf8f0, 0xff61 ～ 0xff9f, 0xf8f1 ～ 0xf8f3
		} else if ((c >= 0x0 && c < 0x81) || (c == 0xf8f0)
				|| (c >= 0xff61 && c < 0xffa0) || (c >= 0xf8f1 && c < 0xf8f4)) {
			r += 1; // half size
		} else {
			r += 2; // full size
		}

		if (r > bytes) {
			return str.substr(0, i);
		}
	}

	return str;
}

/**
 * JSFのコンポIDを取得用関数
 *
 * @param myid:
 *            ex: getId('form:btn01')
 */
function getId(myid) {
	return '#' + myid.replace(/(:|\.)/g, '\\$1');
}

/**
 * PrimefacesのAJAX全般処理監視
 *
 */
var originalPrimeFacesAjaxUtilsSend = PrimeFaces.ajax.Request.send;
PrimeFaces.ajax.Request.send = function(cfg) {
	var originalOncomplete = cfg.oncomplete;
	cfg.oncomplete = function() {
		ajaxStatusOncomplete.apply(this, arguments);

		if (originalOncomplete) {
			originalOncomplete.apply(this, arguments);
		}
	};
	originalPrimeFacesAjaxUtilsSend.apply(this, arguments);
};

/**
 * セッションが切れる場合、自動的にログイン画面に遷移する
 *
 */
function ajaxStatusOncomplete(xhr, status, args) {
	var sessionstatus = xhr.getResponseHeader("sessionstatus");
	if (sessionstatus == "TIMEOUT") {
		var win = window;
		while (win != win.top) {
			win = win.top;
		}
		win.location.href = xhr.getResponseHeader("CONTEXTPATH");
	}
}

Date.prototype.Format = function(fmt) {
	var o = {
		"M+" : this.getMonth() + 1,
		"d+" : this.getDate(),
		"h+" : this.getHours(),
		"m+" : this.getMinutes(),
		"s+" : this.getSeconds(),
		"q+" : Math.floor((this.getMonth() + 3) / 3),
		"S" : this.getMilliseconds()
	};
	if (/(y+)/.test(fmt))
		fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "")
				.substr(4 - RegExp.$1.length));
	for ( var k in o)
		if (new RegExp("(" + k + ")").test(fmt))
			fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k])
					: (("00" + o[k]).substr(("" + o[k]).length)));
	return fmt;
}

function getNow() {
	return new Date().Format("yyyyMMddhhmmssS");
}

function getNowCTI() {
	return new Date().Format("yyyy/MM/dd hh:mm:ss  ");
}

var enterKeyController = {
	/**
	 * for elements has same tabindex
	 */
	currentIndex : 0,
	createSelector : function(next_tabindex) {
		return "input[tabindex=" + next_tabindex + "], select[tabindex="
				+ next_tabindex + "], button[tabindex=" + next_tabindex
				+ "], a[tabindex=" + next_tabindex + "], textarea[tabindex="
				+ next_tabindex + "]";
	},
	toNextComponent : function(form, element) {
		var tabKeyIndexs = [];

		form
				.find(
						"input[tabindex], textarea[tabindex], select[tabindex], a[tabindex], button[tabindex]")
				.each(
						function(index) {
							var ele = $(this);
							if (ele.attr("tabindex") != undefined
									&& Number(ele.attr("tabindex")) > 0
									&& ele.attr("readonly") != "readonly"
									&& ele.attr("readonly") != true
									&& ele.attr("disabled") != "disabled"
									&& ele.attr("disabled") != true) {

								if (tabKeyIndexs.indexOf(Number(ele
										.attr("tabindex"))) == -1) {
									tabKeyIndexs.push(Number(ele
											.attr("tabindex")));
								}
							}
						});
		tabKeyIndexs.sort(function(a, b) {
			return a - b
		});
		var ele_tabindex = Number(element.attr("tabindex"));
		if (ele_tabindex == NaN || tabKeyIndexs.indexOf(ele_tabindex) == -1) {
			return;
		}

		var next_index = 0;
		var selector = this.createSelector(ele_tabindex);

		// for have many components having same tabindex
		if (form.find(selector).size() > (this.currentIndex + 1)) {
			next_index = tabKeyIndexs.indexOf(ele_tabindex);
			this.currentIndex++;
			form.find(selector)[this.currentIndex].focus();
		} else {
			next_index = tabKeyIndexs.indexOf(ele_tabindex) + 1;
			if (next_index == tabKeyIndexs.length) {
				next_index = 0;
			}

			var next_tabindex = tabKeyIndexs[next_index];
			selector = this.createSelector(next_tabindex);
			console.log(next_tabindex);
			form.find(selector)[0].focus();
			this.currentIndex = 0;
		}
	}
}

$(document).on("keypress", "form", function(event) {
	// enter key
	if (event.keyCode == 13) {
		var tagName = event.target.tagName;
		if (tagName === "BUTTON" || tagName === "A" || tagName === "TEXTAREA" || $(event.target).hasClass("noneChangeNext")) {
			return;
		}

		enterKeyController.toNextComponent($(this), $(event.target));
		return false;
	}
});

function highlightPickList(id) {
	$(getId(id) + " .ui-picklist-list-wrapper:eq(1)").addClass(
			"acc-validate-error");
}

function offHighlightPickList(id) {
	$(getId(id) + " .ui-picklist-list-wrapper:eq(1)").removeClass(
			"acc-validate-error");
}

function highlightItem(id) {
	$(getId(id)).addClass("ui-state-error");

	// for select one menu
	if ($(getId(id)).hasClass("ui-selectonemenu")) {
		$(getId(id) + " .ui-selectonemenu-trigger:eq(0)").addClass("ui-state-error");
	}
}

function offHighlightItem(id) {
	$(getId(id)).removeClass("ui-state-error");

	// for select one menu
	if ($(getId(id)).hasClass("ui-selectonemenu")) {
		$(getId(id) + " .ui-selectonemenu-trigger:eq(0)").removeClass("ui-state-error");
	}
}

function focusItemId(id) {
	if ($(getId(id)).get(0).tagName === "INPUT") {
		$(getId(id)).select();
	} else {
		$(getId(id)).focus();
	}
}

function focusItemClass(classItem) {
	tagName = $("." + classItem + ":eq(0)").get(0).tagName;

	if (tagName === "INPUT") {
		$("." + classItem + ":eq(0)").select();
	} else if (tagName === "BUTTON") {
		setTimeout(function() {
			$("." + classItem + ":eq(0)").focus();
		}, 250);
	} else {
		$("." + classItem + ":eq(0)").focus();
	}
}



function selectOneMenuCodeDefaultDeleteBlank(component) {
	var id = component.id.replace("_input", "");
	var labelId = id + "_label";
	var itemsId = id+"_items";
	var selectedValue =  $(getId(labelId)).text();

	if (selectedValue !== "" && $(getId(itemsId) + " li:eq(0)").text().trim() === "" ) {
		$(getId(itemsId) + " li:eq(0)").css("display", "none");
	}
}


//fix bug about checkbox's label when click on it
$(document).on('change', "input[type=checkbox]", function (v) {
	displayCheckedItem($(this));
});

function displayCheckedItem(chb) {
	var box = $(chb).parent().parent().children().next().children(".ui-chkbox-icon");
	if(box == null || box == undefined) {
		return;
	}
    if ($(chb).prop("checked")) {
    	$(box).removeClass("ui-icon-blank").addClass("ui-icon-check");
    } else {
    	$(box).removeClass("ui-icon-check").addClass("ui-icon-blank");
    	$(box).parent().removeClass("ui-state-active");
    }
}

//util function to check if an element has a scrollbar present
if (typeof jQuery.fn.hasScrollBar !== "function") {
    jQuery.fn.hasScrollBar = function (direction) {
        if (direction === 'vertical') {
            return this.get(0).scrollHeight > this.innerHeight();
        }
        else if (direction === 'horizontal') {
            return this.get(0).scrollWidth > this.innerWidth();
        }
        return false;
    }
}

function removeCalendarHighlightClass() {
	$(".calendar-remove-highlight input").focus(function() {
		// remove highlight class
		$(".acc-system-date-overwrite .ui-datepicker-calendar .ui-datepicker-today a.ui-state-highlight").removeClass('ui-state-highlight');
	})
	
}

// set style for gyomuDate
function setGyomuDateStyle(date, gyomuDateVal){
	var status = [];
	var currentDate = moment(new Date()).format("YYYY/MM/DD")
	if (date.getTime() == new Date(gyomuDateVal).getTime()) {
		status = [ true,
				'ui-datepicker-days-cell-over ui-datepicker-today gyomu-date',
				'' ];
	} else if (date.getTime() == new Date(currentDate).getTime()){
		status = [ true, 'acc-system-date-overwrite', '' ];
	} else {
		status = [ true, '', '' ];
	}
	return status;
}

var InputDecimalVar = function() {
	InputDecimalVar.prototype.onKeyDown = function(event, obj) {
		var num = document.getElementById(obj.id).value;
		var keyCode = event.keyCode;
		var inputVal = event.key;
		if ((keyCode === 110 || keyCode === 190) && num.indexOf('.') >= 0) {
			event.stopPropagation();
			event.preventDefault();
			return;
		}

		// Toggle "-"
		if (keyCode === 109 || keyCode === 189) {
			if ($(obj).attr('data-allowNegative') == 'true') {
				this._toggleMinusSign(obj);
			}
			event.stopPropagation();
			event.preventDefault();
			return;
		}

		if (keyCode === 8 || keyCode === 46) {
			var beginCursor = obj.selectionStart;
			var endCursor = obj.selectionEnd;
			var strDel;
			if (endCursor - beginCursor > 0) {
				strDel = num.substring(beginCursor, endCursor);
			} else if (endCursor - beginCursor === 0) {
				if (keyCode === 8) {
					strDel = num.substring(beginCursor - 1, beginCursor);
				} else {
					strDel = num.substring(endCursor, endCursor + 1);
				}
			}

			if (strDel && strDel.indexOf('.') >=0) {
				num = num.replace(strDel, '');
				var maxValue = parseFloat(this._decimalMaxValue(obj));
				var minValue = parseFloat(this._decimalMinValue(obj));
				if (parseFloat(num) > maxValue || parseFloat(num) < minValue) {
					event.stopPropagation();
					event.preventDefault();
					return;
				}
			}
		}

		if (/[.0-9]/.test(inputVal)) {
			var len = $(obj).attr('maxlength');
			var curPosition = obj.selectionStart;
			num = num.substring(0, curPosition) + inputVal
					+ num.substring(curPosition);
			if (num.indexOf('-') >= 0 && num[0] !== '-') {
				event.stopPropagation();
				event.preventDefault();
				return;
			}
			var maxValue = parseFloat(this._decimalMaxValue(obj));
			var minValue = parseFloat(this._decimalMinValue(obj));
			if (parseFloat(num) > maxValue || parseFloat(num) < minValue) {
				event.stopPropagation();
				event.preventDefault();
				return;
			}
			// Calculate '-' in length of string
			num = document.getElementById(obj.id).value;
			if (!len
					|| (num[0] === '-' && num.length < parseInt(len)
							+ (this._isIncludeSignInLength(obj) ? 0 : 1))) {
				num = num.substring(0, curPosition) + inputVal
						+ num.substring(curPosition);
				event.stopPropagation();
				event.preventDefault();
				document.getElementById(obj.id).value = num;
				// Keep the original position of cursor
				obj.selectionStart = curPosition + 1;
				obj.selectionEnd = curPosition + 1;
			}
		}
	}

	InputDecimalVar.prototype._toggleMinusSign = function(obj) {
		var num = document.getElementById(obj.id).value;
		var curPosition = obj.selectionStart;
		var newCurPos = curPosition + 1;
		if (num[0] === '-') {
			num = num.substring(1, num.length);
			newCurPos = newCurPos - 2;
		} else {
			if (this._isIncludeSignInLength(obj)
					&& num.length >= parseInt($(obj).attr('maxlength'))) {
				return;
			}
			num = '-' + num;
		}

		document.getElementById(obj.id).value = num;
		// Keep the original position of cursor
		obj.selectionStart = newCurPos;
		obj.selectionEnd = newCurPos;
	}

	InputDecimalVar.prototype._decimalMaxValue = function(element) {
		return $(element).attr('data-decimalMaxValue');
	}

	InputDecimalVar.prototype._decimalMinValue = function(element) {
		return $(element).attr('data-decimalMinValue');
	}

	InputDecimalVar.prototype._isIncludeSignInLength = function(input) {
		return $(input).attr('data-includeSignInLength') == 'true';
	}

};
var inputDecimalVar = new InputDecimalVar();

$(document).on("focusin", ".inputMarkColorOnChange", function(){
	if (typeof this.stableValue == "undefined") {
		var id = "#" + this.id.replace(/:/g, "\\\:");
        switch (this.nodeName.toLowerCase()) {
            case "input" :
                this.stableValue = $(id).val();
                break;
            case "select" :
                this.stableValue = $(id).val();
                break;
            case "textarea" :
                this.stableValue = $(id).val();
                break;
            case "div" :
                id = id + "_input";
                this.stableValue = $(id).val();
                break;
            default:
        }
	}
});

$(document).on("change", ".inputMarkColorOnChange", function(){
    var id = "#" + this.id.replace(/:/g, "\\\:");
    switch (this.nodeName.toLowerCase()) {
		case "input" :
            if(this.stableValue != $(id).val()) {
            	markColor(id);
			} else {
                resetMarkColor(id);
			}
            break;
        case "select" :
            if(this.stableValue != $(id).val()) {
                markColor(id);
            } else {
                resetMarkColor(id);
            }
            break;
        case "textarea" :
            if(this.stableValue != $(id).val()) {
                markColor(id);
            } else {
                resetMarkColor(id);
            }
            break;
        case "div" :
            if(this.stableValue != $(id + "_input").val()) {
                markColor(id + "_label");
            } else {
                resetMarkColor(id + "_label");
            }
            break;
        default:
    }
});

function markColor(id) {
    resetMarkColor(id);
    $(id).addClass("ui_input_dataChange");
}

function resetMarkColor(id) {
    $(id).removeClass("ui_input_dataChange");
}

function doRedirectLogicScreen(){
	var windowLocation = document.location.toString();
	if (windowLocation.indexOf("hm") == -1)
		return;
	var parentScreen = screenType == 'popup' ? window.parent.opener
			: window.opener;
	if (!parentScreen)
		return;
	parentScreen.closeHoumuKihonWindows();
	parentScreen.location.reload();
}

function cancelTooltip() {
	$('.ui-tooltip.ui-widget.ui-tooltip-right').css('display', 'none');
	$('.ui-tooltip.ui-widget.ui-tooltip-left').css('display', 'none');
	$('.ui-tooltip.ui-widget.ui-tooltip-center').css('display', 'none');
}