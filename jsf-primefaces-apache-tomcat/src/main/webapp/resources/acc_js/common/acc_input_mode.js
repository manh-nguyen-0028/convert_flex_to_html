/**
 * 文字入力強制クラス
 * 
 * @version 1.0
 * @author LocDX
 * @created 2016/11/18
 *          --------------------------------------------------------------------------------------------*
 *          History YYYY/MM/DD Developer Content
 *          --------------------------------------------------------------------------------------------*
 *          2016/11/18 LocDX Create 2016/01/25 LocDX add filter character when
 *          paste, trim text when focus out
 *          --------------------------------------------------------------------------------------------*
 */

var InputMode = (function() {
	// 「住所ｶﾅ」、「事業所名ｶﾅ」、「勤務先名ｶﾅ」、「連絡先名ｶﾅ」等
	InputMode.HANKAKU_TYPE = 1;
	// 氏名カナ,代理人氏名、関係人氏名、弁護士氏名等
	InputMode.HANKAKUKANA_TYPE = 2;
	// 全角
	InputMode.ZENKAKU_TYPE = 3;

	InputMode.HANKAKU_REGEX = /[ !-~ｱ-ﾝ ｡-ｰﾞﾟﾟ]/;
	InputMode.HANKAKUKANA_REGEX = /[ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝﾞﾟｦｧｨｩｪｫｬｭｮｯｰ ]/;
	InputMode.ZENKAKU_REGEX = /[^ !-~ｱ-ﾝ ｡-ｰﾞﾟ]/;

	InputMode.prototype.inputType = null;

	/**
	 * construct function
	 */
	function InputMode(type) {
		this.inputType = type;
	}

	/**
	 * static function get hankaku text (half size) of a string
	 * 
	 * @str : input string
	 */
	InputMode.prototype.getAllowedText = function(str, type) {
		var new_str = "";
		for (var i = 0; i < str.length; i++) {
			var c = str[i];
			switch (type) {
			case InputMode.HANKAKU_TYPE:
				if (c.match(InputMode.HANKAKU_REGEX)) {
					new_str += c;
				}
				break;
			case InputMode.HANKAKUKANA_TYPE:
				if (c.match(InputMode.HANKAKUKANA_REGEX)) {
					new_str += c;
				}
				break;
			case InputMode.ZENKAKU_TYPE:
				if (c.match(InputMode.ZENKAKU_REGEX)) {
					new_str += c;
				}
				break;
			default:
				break;
			}
		}

		return new_str;
	}

	/**
	 * the function catch event when user press on the text input
	 * 
	 * @param input:
	 *            text input component
	 * @param :
	 *            key press event
	 */
	InputMode.prototype.onKeyUp = function(input, e) {
		// dont mind util press enter
		if (e.keyCode != 13) {
			return;
		}

		this.fixInput(input);
	}

	/**
	 * @input : input component
	 */
	InputMode.prototype.fixInput = function(input) {
		var old_value = input.value;

		var new_value = this.getAllowedText(old_value, this.inputType);

		// get current pointer position
		pointer_position = input.selectionStart;

		// delete zenkaku text
		input.value = new_value;
		// after delete zenkaku text, put cursor to old position
		input.focus();
		input.selectionEnd = pointer_position
				- (old_value.length - new_value.length);
	}

	return InputMode;
})();
var hankakuInputMode = new InputMode(InputMode.HANKAKU_TYPE);
var hankakuKanaInputMode = new InputMode(InputMode.HANKAKUKANA_TYPE);
var zenkakuInputMode = new InputMode(InputMode.ZENKAKU_TYPE);

/**
 * MaxByteValidate class for validate max bytes, component inputTextMaxByte uses
 * it
 */
var MaxByteValidate = (function() {
	MaxByteValidate.prototype.isCtrPress = false;

	MaxByteValidate.prototype.beforeInputValue = "";

	function MaxByteValidate() {

	}

	/**
	 * when user focus in the input text, save current value
	 * 
	 * @param input
	 * @param e
	 */
	MaxByteValidate.prototype.focusIn = function(input, e) {
		this.beforeInputValue = input.value;
	}

	MaxByteValidate.prototype.validate = function(input, e, maxBytes) {
		// check is ctr c, ctrl a
		if (this.isCtrPress && (e.keyCode == "67" || e.keyCode == "65")) {
			this.isCtrPress = false;
			return;
		}
		
		// check key
		if (input.value.length == 0) {
			this.beforeInputValue = "";
		}

		// check special key
		if (e.key === "Backspace" || e.key === "Home" || e.key === "End"
				|| e.key === "Del" || e.key === "Right" || e.key === "Left"
				|| e.key === "Up" || e.key === "Down" || e.key === "Control"
				|| e.key === "Shift") {
			return;
		}
		old_value = input.value;
		new_value = cutStringByByteLength(input.value, maxBytes);

		// if not over maxbyte yet
		if (old_value === new_value) {
			// save current value
			this.beforeInputValue = input.value;
			return;
		}

		// ↓ if over maxbyte

		// get current pointer position
		pointer_position = input.selectionStart;

		// restore to value before input
		input.value = this.beforeInputValue;

		// after delete zenkaku text, put cursor to old position
		input.focus();
		input.selectionEnd = pointer_position
				- (old_value.length - this.beforeInputValue.length);

		this.isCtrPress = false;
		return;
	}

	MaxByteValidate.prototype.onKeyDown = function(event) {
		// Ctr Press
		if (event.keyCode == "17") {
			this.isCtrPress = true;
			return;
		}
	}

	return MaxByteValidate;
})();
var maxByteValidate = new MaxByteValidate();

/* jquery function, set cursor postion for input text, text area */
$.fn.setRange = function(start, end) {
	if (end === undefined) {
		end = start;
	}
	return this.each(function() {
		if ('selectionStart' in this) {
			this.selectionStart = start;
			this.selectionEnd = end;
		} else if (this.setSelectionRange) {
			this.setSelectionRange(start, end);
		} else if (this.createTextRange) {
			var range = this.createTextRange();
			range.collapse(true);
			range.moveEnd('character', end);
			range.moveStart('character', start);
			range.select();
		}
	});
};

/* jquery function, get cursor postion */
$.fn.getSelectionStart = function() {
	var el = $(this).get(0);
	var pos = 0;
	if ('selectionStart' in el) {
		pos = el.selectionStart;
	} else if ('selection' in document) {
		el.focus();
		var Sel = document.selection.createRange();
		var SelLength = document.selection.createRange().text.length;
		Sel.moveStart('character', -el.value.length);
		pos = Sel.text.length - SelLength;
	}
	return pos;
};

/* handle event focus out for inputtext has class ui-inputfield */
$(document).ready(
		function() {
			$("#page").on(
					"focusout",
					".ui-inputfield",
					function() {
						if ($(this).attr("disabled") == true
								|| $(this).attr("disabled") == "disabled"
								|| $(this).attr("readOnly") == true
								|| $(this).hasClass("nonTrim")) {
							return;
						}

						var initValue = $(this).val();
						$(this).val(initValue.trim());
					});
		});

/**
 * PasteController class filter invalid characters when paste on text input
 */
var PasteController = (function() {
	PasteController.NUMBER_REGEX = /[-.0-9]/;// css class "acc-number-input";
	PasteController.HALF_WIDTH_NUMBER_REGEX = /[0-9]/; // css class half-with-number
	PasteController.TEL_REGEX = /[0-9\-]/; // css class: acc-tel-input
	PasteController.ALPHA_NUMBER_REGEX = /[0-9a-zA-Z]/; // css class:
	// acc-alpha-number-input

	PasteController.prototype.isCtrPress = false;
	PasteController.prototype.pointer_position = 0;
	PasteController.prototype.beforPasteValue = "";
	PasteController.prototype.highlightedText = "";
	PasteController.prototype.newValue = "";

	function PasteController() {
	}

	PasteController.prototype.onKeydownEvent = function(ele, event) {
		// when press ctrl key
		if (event.keyCode == "17") {
			this.isCtrPress = true;
			this.pointer_position = ele.getSelectionStart();
			this.beforPasteValue = ele.val();
			this.highlightedText = this.beforPasteValue.substr(
					this.pointer_position, ele.get(0).selectionEnd
							- this.pointer_position);
		// 2019/06/17 setsu マージ対応HON096 START
		} else {
			// 属性変更画面のカナ項目で半角「ｳﾞ」が入力できるように
			this.isCtrPress = false;
		}
		// 2019/06/17 setsu マージ対応HON096 END
	}

	PasteController.prototype.onKeyupEvent = function(ele, event) {
		if (!this.isCtrPress || event.key === "Ctr" || event.key !== "v") {
			return;
		}

		var keyFilter = this.getKeyFilter(ele, event);
		if (keyFilter !== "") {
			this.filter(ele, event, keyFilter);
			this.updatePointerPosition(ele, event);
		}
	}

	PasteController.prototype.onChangeEvent = function(ele, event) {
		var keyFilter = this.getKeyFilter(ele, event);
		if (keyFilter !== "") {
			this.filter(ele, event, keyFilter);
		}
	}

	/**
	 * get key filter of component
	 * 
	 * @param ele
	 * @param event
	 * @returns {String}
	 */
	PasteController.prototype.getKeyFilter = function(ele, event) {
		// get keyfilter of component
		var keyFilter = "";
		if (ele.hasClass("hankaku-mode")) {
			keyFilter = InputMode.HANKAKU_REGEX;
		} else if (ele.hasClass("hankakuKana-mode")) {
			keyFilter = InputMode.HANKAKUKANA_REGEX;
		} else if (ele.hasClass("zenkaku-mode")) {
			keyFilter = InputMode.ZENKAKU_REGEX;
		} else if (ele.hasClass("acc-number-input")) {
			keyFilter = PasteController.NUMBER_REGEX;
		} else if (ele.hasClass("acc-alpha-number-input")) {
			keyFilter = PasteController.ALPHA_NUMBER_REGEX;
		} else if (ele.hasClass("acc-tel-input")) {
			keyFilter = PasteController.TEL_REGEX;
		} else if (ele.hasClass("half-with-number")) {
			keyFilter = PasteController.HALF_WIDTH_NUMBER_REGEX;
		}

		return keyFilter;
	}
	/**
	 * exec filter invalid characters
	 * 
	 * @param ele:
	 *            text input
	 */
	PasteController.prototype.filter = function(ele, event, keyFilter) {
		// fix regex to filter all invalid characters
		keyFilter = new RegExp(keyFilter.source, "g");

		// filter inputtext value
		var oldValue = ele.val();
		var newValue = oldValue.match(keyFilter);
		if (newValue == null) {
			newValue = "";
		} else {
			newValue = newValue.join('');
		}

		// set new value for input text
		ele.val(newValue);

		// save newValue for updatePointerPosition
		this.newValue = newValue;

	}

	PasteController.prototype.updatePointerPosition = function(ele, event) {
		// update position
		ele.focus();
		ele.setRange(this.pointer_position
						+ (this.newValue.length - (this.beforPasteValue.length - this.highlightedText.length)));
	}

	return PasteController;
})();

var pasteController = new PasteController();
/* filter character after paste */
$(document).ready(function() {
	$("#page").on("keydown", ".ui-inputfield", function(event) {
		pasteController.onKeydownEvent($(this), event);
	});

	$("#page").on("keyup", ".ui-inputfield", function(event) {
		pasteController.onKeyupEvent($(this), event);
	});

	// filter invalid character when paste by
	$("#page").on("change", ".ui-inputfield", function(e) {
		pasteController.onChangeEvent($(this), e);
	});
});

// =============
// Input Number/Decimal
// =============
var inheritsFrom = function(child, parent) {
	child.prototype = Object.create(parent.prototype);
};

var isMatched = function(input, regex) {
	// http://stackoverflow.com/questions/1520800
	regex.lastIndex = 0;
	var result = input.match(regex);
	regex.lastIndex = 0;

	return result;
}
// Repeat a string with 'count' times
var repeat = function(source, count) {
	var s = '';
	for (var i = 0; i < count; i++) {
		s += source;
	}

	return s;
}

var InputNumber = function() {

	InputNumber.prototype.onKeyDown = function(event, obj) {
		var num = document.getElementById(obj.id).value;
		var inputVal = event.key;
		if (inputVal === '.' && num.indexOf('.') >= 0) {
			event.stopPropagation();
			event.preventDefault();
			return;
		}

		// Toggle "-"
		if (inputVal === '-') {
			if ($(obj).attr('data-allowNegative') == 'true') {
				this._toggleMinusSign(obj);
			}
			event.stopPropagation();
			event.preventDefault();
			return;
		}

		if (/[.0-9]/.test(inputVal)) {
			var len = $(obj).attr('maxlength');
			var curPosition = obj.selectionStart;
			// Calculate '-' in length of string
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

		// リターンキー押下かつ小数点ありの場合、整数部以上の桁数を入力されたら、整数部のみを画面に設定するようにする。
		if (event.keyCode == "13") {
			var intLen = this._integerMaxLength(obj);
			if (intLen && parseInt(intLen) < num.length) {
				num = num.substr(0, parseInt(intLen));
				document.getElementById(obj.id).value = num;
			}
		}
	}

	InputNumber.prototype.onBlur = function(obj) {
		var number = document.getElementById(obj.id).value;
		if (number) {
			document.getElementById(obj.id).value = this._formatNumber(obj,
					number, parseInt($(obj).attr('maxlength')));
		} else {
            // 2018-12-03 Fix issue 163 CuongHT START
            if(obj && obj.getAttribute("money") == 'true') {
                document.getElementById(obj.id).value = "0";
            }
            // 2018-12-03 Fix issue 163 CuongHT END
        }
	}

	InputNumber.prototype.onPaste = function(event, obj) {
		var clipboardData, pastedData;

		// Stop data actually being pasted into
		event.stopPropagation();
		event.preventDefault();

		// Get pasted data via clipboard API
		clipboardData = event.clipboardData || window.clipboardData;
		pastedData = clipboardData.getData('Text');

		var curPosition = obj.selectionStart;
		var value = document.getElementById(obj.id).value;
		var isExistsDot = value.indexOf('.') >= 0;
		var isAllowNegative = ($(obj).attr('data-allowNegative') == 'true');

		// Remove invalid characters
		var pattern = '';
		if (isExistsDot) {
			pattern = isAllowNegative ? InputNumber.REGEX_NUMBER
					: InputNumber.REGEX_NUMBER_NOT_NEGATIVE;
		} else {
			pattern = isAllowNegative ? InputNumber.REGEX_DECIMAL
					: InputNumber.REGEX_DECIMAL_NOT_NEGATIVE;
		}

		var data = isMatched(pastedData, pattern);
		if (data) {
			// Insert to current position of cursor
			var newValue = value.substring(0, curPosition) + data.join('')
					+ value.substring(curPosition);
			document.getElementById(obj.id).value = this._formatByLength(obj,
					newValue, parseInt($(obj).attr('maxlength')));
		}
	}

	InputNumber.prototype.onFocus = function(obj) {
		// pos = str.slice(0, obj.selectionStart).length;
		var number = document.getElementById(obj.id).value;
		if (!number) {
			return;
		}
		number = number.split(',').join('')
		document.getElementById(obj.id).value = number;

		caretPos = number.length;
		elem = document.getElementById(obj.id);
		if (elem.createTextRange) {
			var range = elem.createTextRange();
			range.moveStart('character', 0);
			range.moveEnd('character', caretPos);
			range.select();
		} else {
			if (elem.selectionStart) {
				elem.focus();
				elem.setSelectionRange(caretPos, caretPos);
			} else
				elem.focus();
		}
	}

	InputNumber.prototype._formatNumber = function(obj, number, maxLength) {
		if (!this._isValidNumber(number)) {
			return '';
		}

		var num = this._getNumber(number);
		var isNegative = num < 0;

		num = this._formatByLength(obj, String(num), maxLength);
		var integerPart = String(Math.abs(this._getInt(num)));
		var decimalPart = this._getFractal(num);

		// Divide by each 3 number groups
		num = this._join(integerPart.split(/(?=(?:\d{3})+$)/).join(","),
				decimalPart);
		var value = (isNegative ? "-" + num : num);
		return value;
	}

	InputNumber.prototype._toggleMinusSign = function(obj) {
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

	InputNumber.prototype._formatByLength = function(obj, input, len) {
		if (!len)
			return input;

		var s = this._cutByLength(input, len, '-.,');

		return s;
	}

	// Cut string by length
	InputNumber.prototype._cutByLength = function(input, len, excludeChars) {
		var s = '';
		for (var i = 0; i < input.length; i++) {
			var c = input[i];
			var isExcludeChar = excludeChars.indexOf(c) >= 0;
			if (isExcludeChar || /[0-9]/g.test(c)) {
				if (isExcludeChar)
					len = len + 1;

				s += c;
				if (s.length >= len)
					return s;
			}
		}

		return s;
	}

	InputNumber.prototype._join = function(intPart, decimalPart) {
		return !decimalPart ? intPart : intPart + '.' + decimalPart;
	}

	InputNumber.prototype._isValidNumber = function(input) {
		return !isNaN(this._getNumber(input));
	}

	InputNumber.prototype._isIncludeSignInLength = function(input) {
		return $(input).attr('data-includeSignInLength') == 'true';
	}

	InputNumber.prototype._getNumber = function(input) {
		input = String(input);
		input = isMatched(input, InputNumber.REGEX_NUMBER).join('');

		// Format '-'
		if (input[0] === '-') {
			input = '-' + input.replace(/\-/g, '');
		} else {
			input = input.replace(/\-/g, '');
		}

		return parseInt(input);
	}

	InputNumber.prototype._getInt = function(input) {
		return String(input).split('.')[0] || '';
	}

	InputNumber.prototype._getFractal = function(input) {
		return String(input).split('.')[1] || '';
	}

};
InputNumber.REGEX_NUMBER = /[\-0-9]/g;
InputNumber.REGEX_NUMBER_NOT_NEGATIVE = /[0-9]/g;
InputNumber.REGEX_DECIMAL = /[\.\-0-9]/g;
InputNumber.REGEX_DECIMAL_NOT_NEGATIVE = /[\.0-9]/g;

var InputDecimal = function() {
	InputDecimal.prototype._integerMaxLength = function(element) {
		return $(element).attr('data-integerMaxLength');
	}

	InputDecimal.prototype._decimalMaxLength = function(element) {
		return $(element).attr('data-decimalMaxLength');
	}

	InputDecimal.prototype._formatByLength = function(obj, input, len) {
		var dotIndex = input.lastIndexOf('.');
		if (dotIndex < 0) {
			dotIndex = input.length;
		}
		var intPart = input.substr(0, dotIndex);
		var decPart = input.substr(dotIndex);
		var intLen = this._integerMaxLength(obj);
		var decLen = this._decimalMaxLength(obj);

		if (!decPart)
			decPart = '.';
		decPart += repeat('0', decLen);

		var s = this._cutByLength(intPart, Number(intLen), '-')
				+ this._cutByLength(decPart, Number(decLen), '.');

		return s;
	}

	InputDecimal.prototype._getNumber = function(input) {
		input = String(input);
		input = isMatched(input, InputNumber.REGEX_DECIMAL).join('');

		// Format '-'
		if (input[0] === '-') {
			input = '-' + input.replace(/\-/g, '');
		} else {
			input = input.replace(/\-/g, '');
		}

		// Format '.'
		var dotIndex = input.lastIndexOf('.');
		if (dotIndex >= 0) {
			input = input.substr(0, dotIndex).replace(/[.]/g, '')
					+ input.substr(dotIndex);
		}

		return parseFloat(input);
	}
};
inheritsFrom(InputDecimal, InputNumber);

var accInputNumber = new InputNumber();
var accInputDecimal = new InputDecimal();
