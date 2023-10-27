 
/*
 This common for fix kadai 512.
 Reference Primefaces 5.3 source code:
 https://github.com/primefaces/primefaces-5_3/primefaces-5_3/src/main/resources/META-INF/resources/primefaces/mobile/widgets/datepicker.js
*/
$.datepicker._gengoList = [ {
		name : 'M',
		from : 'M.01/09/08',
		to : 'M.45/07/29',
		startDay : 08,
		startMonth : 09,
		startYear : 1868,
		startTime : '1868/09/08',
		endTime : '1912/07/29'
		
	}, // Meiji (1868/09/08 ~ 1912/07/29)
	{
		name : 'T',
		from : 'T.01/07/30',
		to : 'T.15/12/24',
		startDay : 30,
		startMonth : 07,
		startYear : 1912,
		startTime : '1912/07/30',
		endTime : '1926/12/24'
		
	}, // Taisho (1912/07/30 ~ 1926/12/24)
	{
		name : 'S',
		from : 'S.01/12/25',
		to : 'S.64/01/07',
		startDay : 25,
		startMonth : 12,
		startYear : 1926,
		startTime : '1926/12/25',
		endTime : '1989/01/07'
	}, // Showa (1926/12/25 ~ 1989/01/07)
	{
		name : 'H',
		from : 'H.01/01/08',
		to : 'H.31/04/30',
		startDay : 08,
		startMonth : 01,
		startYear : 1989,
		startTime : '1989/01/08',
		endTime : '2019/04/30'
	}, // Heisei (1989/01/08 ~ 2019/04/30)
	{
		name : 'R',
		from : 'R.01/05/01',
		to : 'R.99/12/31',
		startDay : 05,
		startMonth : 01,
		startYear : 2019,
		startTime : '2019/05/01',
		endTime : '2119/04/30'
	} // Reiwa (2019/05/01 ~ )
	]

//class to clasify input is inputWarekidateByCalendar or others
$.datepicker._ENABLE_WAREKI_CONVERT = 'acc_input_wareki'

//save previous label for replace error message to show error dialog
$.datepicker._PREVIOUS_INPUT_LABEL = ''
	
//Override press ENTER key event for validate input wareki date
$.datepicker._doKeyDown = function(event) {
	var onSelect, dateStr, sel, inst = $.datepicker._getInst(event.target), handled = true, isRTL = inst.dpDiv
			.is(".ui-datepicker-rtl");

	inst._keyEvent = true;
	if ($.datepicker._datepickerShowing) {
		switch (event.keyCode) {
		// TAB
		case 9:
			$.datepicker._hideDatepicker();
			handled = false;
			break; // hide on tab out

		// ENTER
		case 13:
			sel = $("td." + $.datepicker._dayOverClass + ":not(."
					+ $.datepicker._currentClass + ")", inst.dpDiv);
			
			// 2020/01/01 TuTN ADD START
			// This function use to check when press enter
			$.datepicker._checkOnEnter(inst);
			// 2020/01/01 TuTN ADD END
			
			// 2020/01/01 TuTN DELETE START
			// This check make error sometime -> delete
			/*if (sel[0]) {
				$.datepicker._selectDay(event.target, inst.selectedMonth, inst.selectedYear, sel[0]);
			}*/
			// 2020/01/01 TuTN DELETE END
			
			onSelect = $.datepicker._get(inst, "onSelect");
			
			if (onSelect) {
				dateStr = $.datepicker._formatDate(inst);

				// trigger custom callback
				onSelect.apply((inst.input ? inst.input[0] : null), [ dateStr,
						inst ]);
			} else {
				$.datepicker._hideDatepicker();
			}

			return false; // don't submit the form
		case 27:
			$.datepicker._hideDatepicker();
			break; // hide on escape
		case 33:
			$.datepicker._adjustDate(event.target,
					(event.ctrlKey ? -$.datepicker._get(inst, "stepBigMonths")
							: -$.datepicker._get(inst, "stepMonths")), "M");
			break; // previous month/year on page up/+ ctrl
		case 34:
			$.datepicker._adjustDate(event.target,
					(event.ctrlKey ? +$.datepicker._get(inst, "stepBigMonths")
							: +$.datepicker._get(inst, "stepMonths")), "M");
			break; // next month/year on page down/+ ctrl
		case 35:
			if (event.ctrlKey || event.metaKey) {
				$.datepicker._clearDate(event.target);
			}
			handled = event.ctrlKey || event.metaKey;
			break; // clear on ctrl or command +end
		case 36:
			if (event.ctrlKey || event.metaKey) {
				$.datepicker._gotoToday(event.target);
			}
			handled = event.ctrlKey || event.metaKey;
			break; // current on ctrl or command +home
		case 37:
			if (event.ctrlKey || event.metaKey) {
				$.datepicker._adjustDate(event.target, (isRTL ? +1 : -1), "D");
			}
			handled = event.ctrlKey || event.metaKey;
			// -1 day on ctrl or command +left
			if (event.originalEvent.altKey) {
				$.datepicker._adjustDate(event.target,
						(event.ctrlKey ? -$.datepicker._get(inst,
								"stepBigMonths") : -$.datepicker._get(inst,
								"stepMonths")), "M");
			}
			// next month/year on alt +left on Mac
			break;
		case 38:
			if (event.ctrlKey || event.metaKey) {
				$.datepicker._adjustDate(event.target, -7, "D");
			}
			handled = event.ctrlKey || event.metaKey;
			break; // -1 week on ctrl or command +up
		case 39:
			if (event.ctrlKey || event.metaKey) {
				$.datepicker._adjustDate(event.target, (isRTL ? -1 : +1), "D");
			}
			handled = event.ctrlKey || event.metaKey;
			// +1 day on ctrl or command +right
			if (event.originalEvent.altKey) {
				$.datepicker._adjustDate(event.target,
						(event.ctrlKey ? +$.datepicker._get(inst,
								"stepBigMonths") : +$.datepicker._get(inst,
								"stepMonths")), "M");
			}
			// next month/year on alt +right
			break;
		case 40:
			if (event.ctrlKey || event.metaKey) {
				$.datepicker._adjustDate(event.target, +7, "D");
			}
			handled = event.ctrlKey || event.metaKey;
			break; // +1 week on ctrl or command +down
		default:
			handled = false;
		}
	} else if (event.keyCode === 36 && event.ctrlKey) { // display the date
														// picker on ctrl+home
		$.datepicker._showDatepicker(this);
	} else {
		handled = false;
	}

	if (handled) {
		event.preventDefault();
		event.stopPropagation();
	}
}


/* Synchronise manual entry and field/alternate field. */
$.datepicker._doKeyUp = function(event) {
	var date,
		inst = $.datepicker._getInst(event.target);

	// 2020/01/01 TuTN ADD START
	if (inst.input.val() !== inst.lastVal) {
		if (!inst.input) {
			return;
		}
		
		const inputId = $(PrimeFaces.escapeClientId(inst.id.replace(/\\/g, '')));
		const inputIdSpan = inputId.parent();
		const inputValue = inst.input.val();
		
		if (inputIdSpan.hasClass($.datepicker._ENABLE_WAREKI_CONVERT)
				&& (!$.datepicker._isWarekiDate(inputValue)) && (inputValue.indexOf('_') == -1)) {
			
			inputId.val('');
			
			let label = $.datepicker._getLabel(event);
			
			if (label !=='') {
				$.datepicker._showErrorValidationMessage(label);
				return;
			}
		}
		
		if ($.datepicker._isWarekiDate(inst.input.val())) {
			
			$.datepicker._setDateFromField(inst);
			$.datepicker._updateAlternate(inst);
			$.datepicker._updateDatepicker(inst);
		}
		// 2020/01/01 TuTN ADD END
		
		try {
			date = $.datepicker.parseDate($.datepicker._get(inst, "dateFormat"),
				(inst.input ? inst.input.val() : null),
				$.datepicker._getFormatConfig(inst));

			if (date) { // only if valid
				$.datepicker._setDateFromField(inst);
				$.datepicker._updateAlternate(inst);
				$.datepicker._updateDatepicker(inst);
			}
		}
		catch (err) {
		}
	}
	return true;
}

/* Update the input field with the selected date. */
$.datepicker._selectDate = function(id, dateStr) {

	var onSelect, target = $(id), inst = this._getInst(target[0]);
	
	// 2020/01/01 TuTN ADD START
	const inputId = $(PrimeFaces.escapeClientId(inst.id.replace(/\\/g, '')));
	const inputIdSpan = inputId.parent();

	
	if (dateStr != null && dateStr[1] !== '.' && inputIdSpan.hasClass($.datepicker._ENABLE_WAREKI_CONVERT)) {
		dateStr = this._convertNormalDateToWarekiDate(inst)
	} else {
		  dateStr = (dateStr != null ? dateStr : this._formatDate(inst));
	}
	// 2020/01/01 TuTN ADD END
	
	if (inst.input) {
		inst.input.val(dateStr);
	}
	
	this._updateAlternate(inst);
	// 2020/01/01 TuTN DELETE START
	/*onSelect = this._get(inst, "onSelect");*/
	// 2020/01/01 TuTN DELETE END
	if (onSelect) {
		onSelect.apply((inst.input ? inst.input[0] : null), [ dateStr, inst ]); // trigger
																				// custom
																				// callback
	} else if (inst.input) {
		inst.input.trigger("change"); // fire the change event
	}

	if (inst.inline) {
		this._updateDatepicker(inst);
	} else {
		this._hideDatepicker();
		this._lastInput = inst.input[0];
		if (typeof (inst.input[0]) !== "object") {
			inst.input.focus(); // restore focus
		}
		this._lastInput = null;
	}
}

$.datepicker._setDateFromField = function(inst, noDefault) {
	if (inst.input.val() === inst.lastVal) {
		return;
	}
	
	// 2020/01/01 TuTN ADD START
	const currentInputValue = inst.input.val();
	const inputId = $(PrimeFaces.escapeClientId(inst.id.replace(/\\/g, '')));
	const inputIdSpan = inputId.parent();
	
	if (currentInputValue == '' && inputIdSpan.hasClass($.datepicker._ENABLE_WAREKI_CONVERT)) {
		const currentTime = (new Date);
		inst.selectedDay = currentTime.getDate();
		inst.drawMonth = inst.selectedMonth = currentTime.getMonth();
		inst.drawYear = inst.selectedYear = currentTime.getFullYear();
		inst.currentDay = (currentTime ? currentTime.getDate() : 0);
		inst.currentMonth = (currentTime ? currentTime.getMonth() : 0);
		inst.currentYear = (currentTime ? currentTime.getFullYear() : 0);
		this._adjustInstDate(inst);
		
		return;
	}

	if (inputIdSpan.hasClass($.datepicker._ENABLE_WAREKI_CONVERT) 
			&& ('MSTHR'.indexOf(currentInputValue[0]) > -1) 
			&& ('.'.indexOf(currentInputValue[1])) > -1
			&& (currentInputValue).indexOf('_') == -1) {
		date = this._convertWarikiDateToNormalDate(currentInputValue);
		
		inst.selectedDay = date.getDate();
		inst.drawMonth = inst.selectedMonth = date.getMonth();
		inst.drawYear = inst.selectedYear = date.getFullYear();
		inst.currentDay = (date ? date.getDate() : 0);
		inst.currentMonth = (date ? (date.getMonth()) : 0);
		inst.currentYear = (date ? date.getFullYear() : 0);
		this._adjustInstDate(inst);
		
     // 2020/01/01 TuTN ADD END
	} else {
		var dateFormat = this._get(inst, "dateFormat"),
		dates = inst.lastVal = inst.input ? inst.input.val(): null,
		defaultDate = this._getDefaultDate(inst),
		date = defaultDate, settings = this._getFormatConfig(inst);

		try {
			date = this.parseDate(dateFormat, dates, settings) || defaultDate;
		} catch (event) {
			dates = (noDefault ? "" : dates);
		}

		inst.selectedDay = date.getDate();
		inst.drawMonth = inst.selectedMonth = date.getMonth();
		inst.drawYear = inst.selectedYear = date.getFullYear();
		inst.currentDay = (dates ? date.getDate() : 0);
		inst.currentMonth = (dates ? date.getMonth() : 0);
		inst.currentYear = (dates ? date.getFullYear() : 0);
		this._adjustInstDate(inst);
	}

	
}

/*
 * This use to convert from normal date with patterm Warekidate to YYYY/MM/DD  
 * input is input date G.99/99/99 to YYYY/MM/DD
 * this one is new function add to instance not override function 
 * */ 
$.datepicker._convertWarikiDateToNormalDate = function(warekiDateString) {

	if (!warekiDateString || warekiDateString.length != 10) {
		return;
	}

	// get year,month,day
	const yearString = warekiDateString.substr(2, 2);
	const monthString = warekiDateString.substr(5, 2);
	const dayString = warekiDateString.substr(8, 2);
	const era = warekiDateString.substr(0, 1).toUpperCase();

	if (yearString === '__' && monthString === '__' && dayString === '__') {
		return; // R.__/__/__ is okay
	}

	if (isNaN(yearString) || isNaN(monthString) || isNaN(dayString)) {
		return;
	}

	var gengo = this._gengoList.filter(function(item) {
		return item.name === era
	})[0];
	if (gengo.from <= warekiDateString.toUpperCase()
			&& warekiDateString.toUpperCase() <= gengo.to) {
		const year = parseInt(yearString);
		const month = parseInt(monthString);
		const day = parseInt(dayString);

		// R.01/06/31(Date(2019,5,31)) is invalid date
		var checkedDate = new Date(gengo.startYear + year - 1, month - 1, day);

		// Date(2019,5,31) automatically converts to Date(2019,6,1), and the
		// dates are not the same
		return checkedDate;
	}

}

/*
 * This use to convert from normal date with patterm YYYY/MM/DD to Warekidate
 * input is input date YYYY/MM/DD to G.99/99/99
 * this one is new function add to instance not override function 
 * */ 
$.datepicker._convertNormalDateToWarekiDate = function(inst) {

	if (!inst) {
		return;
	}

	// get year,month,day
	const selectedYear = inst.selectedYear;
	const selectedMonth = inst.selectedMonth + 1; 
	const selectedDay = inst.selectedDay;

	if (selectedYear === '__' && selectedMonth === '__' && selectedDay === '__') {
		return; // R.__/__/__ is okay
	}

	if ((selectedYear == '') || (selectedMonth == '') || (selectedDay == '')) {
		return;
	}
	//because in js month start from 0 then when need to + 1
	const fullSelectedYear = selectedYear + '/'
			+ (selectedMonth < 10 ? ('0' + selectedMonth ) : selectedMonth  ) + '/'
			+ (selectedDay < 10 ? ('0' + selectedDay) : selectedDay);
	let gengo;
	for (var i = this._gengoList.length - 1; i >= 0; i--) {
		if (this._gengoList[i].startTime <= fullSelectedYear
				&& fullSelectedYear <= this._gengoList[i].endTime) {
			gengo = this._gengoList[i];
			break;
		}
	}

	let WarekiConverted;
	let yearWarekiConverted = selectedYear - gengo.startYear + 1;

	WarekiConverted = gengo.name
			+ '.'
			+ (yearWarekiConverted < 10 ? ('0' + yearWarekiConverted)
					: yearWarekiConverted) + '/'
			+ (selectedMonth < 10 ? ('0' + selectedMonth) : selectedMonth)
			+ '/' + (selectedDay < 10 ? ('0' + selectedDay) : selectedDay);

	return WarekiConverted;

}
/*
 * This function use to check when press Enter
 * Input is inputWarekiDate component
 * this one is new function add to instance not override function 
 * */
$.datepicker._checkOnEnter = function(inst) {
	const inputId = $(PrimeFaces.escapeClientId(inst.id.replace(/\\/g, '')));
	const inputWarekiDate = inputId.val().toUpperCase();
	const inputIdSpan = inputId.parent();
	
	if (inputWarekiDate && inputIdSpan.hasClass($.datepicker._ENABLE_WAREKI_CONVERT)) {
		if (inputWarekiDate.replace(/\/|\./g, '').length < 7) {
			inputId.val('');
			return false;
		}
		if (!$.datepicker._isWarekiDate(inputWarekiDate)) {
			inputId.val('');
			const label = $('label[for=\"' + inst.id.replace(/\\/g, '') + '\"]')[0].innerText;

			$.datepicker._showErrorValidationMessage(label);
			return;
		} else {
			$.datepicker._selectDate(inputId, inputWarekiDate);
		}
	}
	inputId.focusout();
}


/**
 * Check the input string is Japanese date with era or not
 * 
 * @param warekiDateString
 *            {string} Format [R/H/S/T/M].NN/MM/DD
 * @return {boolean} true if the input string is valid Japanese date
 * 
 * this one is new function add to instance not override function 
 */
$.datepicker._isWarekiDate = function(warekiDateString) {
	// eg. "H.12/02/29"
	if (!warekiDateString || warekiDateString.length != 10) {
		return false;
	}

	// get year,month,day
	var yearString = warekiDateString.substr(2, 2);
	var monthString = warekiDateString.substr(5, 2);
	var dayString = warekiDateString.substr(8, 2);
	var era = warekiDateString.substr(0, 1).toUpperCase();
	
	if (yearString === '__' && monthString === '__' && dayString === '__') {
		return true; // R.__/__/__ is okay
	}
	
	if ('MSTHR'.indexOf(era) < 0) {
		return false;
	}

	if (isNaN(yearString) || isNaN(monthString) || isNaN(dayString)) {
		return false;
	}

	var gengo = $.datepicker._gengoList.filter(function(item) {
		return item.name === era
	})[0];
	if (gengo.from <= warekiDateString.toUpperCase()
			&& warekiDateString.toUpperCase() <= gengo.to) {
		var year = parseInt(yearString);
		var month = parseInt(monthString);
		var day = parseInt(dayString);
		
		// Check the date is valid or not.
		// R.01/05/32 is invalid date
		if (month > 12 || month <= 0 || day > 31 || day <= 0) {
			return false;
		}

		// R.01/06/31(Date(2019,5,31)) is invalid date
		var checkedDate = new Date(gengo.startYear + year - 1, month - 1,
				day);

		// Date(2019,5,31) automatically converts to Date(2019,6,1), and the
		// dates are not the same
		return checkedDate.getDate() === day;
	}
	
	return false;
}

/*
 * This one to show Error messege whenever input invalid Warekidate.
 * this one is new function add to instance not override function.
 * */ 
$.datepicker._showErrorValidationMessage = function(inputLabel) {
	
	let dialogId;
	let currentHtmlContext;
	let newHtmlContext;
	if (screenType == 'popup') {
		dialogId = PrimeFaces.escapeClientId(parent.PF('warekiDateConfirm').id);
		currentHtmlContext = $(dialogId + ' div.ui-dialog-content',parent.document)[0].outerHTML;
	} else {
		dialogId = PrimeFaces.escapeClientId(PF('warekiDateConfirm').id);
		currentHtmlContext = $(dialogId + ' div.ui-dialog-content')[0].innerHTML;
	}
	
	//clear mess dialog before show
	const previousValidationContext = warekiDateValidationMessage.replace('<%arg1%>',
			$.datepicker._PREVIOUS_INPUT_LABEL);
	
	//create new context validation then show validation message
	const newValidationContext = warekiDateValidationMessage.replace(
			'<%arg1%>', inputLabel);


	if (screenType == 'popup') {
		$(dialogId + ' div.ui-dialog-content span',parent.document)[1].innerText = newValidationContext;
		parent.PF('warekiDateConfirm').show();
	} else {
		$(dialogId + ' div.ui-dialog-content span')[1].innerText = newValidationContext;
		PF('warekiDateConfirm').show();
	}
	
	$.datepicker._PREVIOUS_INPUT_LABEL = inputLabel;
	
}

/*
 * 
 * this function use to get label of input for _showErrorValidationMessage
 * this one is new function add to instance not override function.
 * */
$.datepicker._getLabel = function(event) {
	const inputId = event.target.id;
	let label = $('label[for=\"' + inputId + '\"]')[0] != undefined ? $('label[for=\"' + inputId + '\"]')[0].innerText : '' ;
	
	if (label !='') {
		return label;
	}
	
	const subBelongLabel = $(PrimeFaces.escapeClientId(inputId)).parent()[0].id;
	const belongLabel = subBelongLabel.substr(0, subBelongLabel.lastIndexOf(':'))
	
	const matchingLabel = $('label').toArray().filter(function(item) {
		const relativeLabel = $(PrimeFaces.escapeClientId(item.id)).attr('for');
		if(relativeLabel == belongLabel || relativeLabel == subBelongLabel) {
			return item;
		}
	})
	
	
	if (matchingLabel.length > 0) {
		return matchingLabel[0].innerText;
	}

	const input = $(PrimeFaces.escapeClientId(inputId));
	const labelArray = input.parent().siblings().toArray().filter(function(item) {
		if (item.nodeName === 'LABEL') {
			return item;
		}
	})
	
	for (var i = 0; i < labelArray.length; i++) {
		if (label = labelArray[i] != '') {
			label = labelArray[i].innerText;
			break;
		}
	}
	
	
	return label;
	
}