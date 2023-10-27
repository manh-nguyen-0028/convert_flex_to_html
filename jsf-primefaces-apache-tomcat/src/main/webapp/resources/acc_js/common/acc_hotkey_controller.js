var FunctionKeyController = (function() {
	FunctionKeyController.prototype.formId = "";
	FunctionKeyController.prototype.buttonIds = {};
	
	/**
	 * init Function 
	 * formId: ex "HM1010Form"
	 * buttonIds : array ex {"Esc":"escBtn", "F1":"f1Btn", "F2": "f2Btn" }
	 */
	function FunctionKeyController(formId, buttonIds) {
		this.formId = formId;
		this.buttonIds = buttonIds;
		
	}
	
	/**
	 * handle key press event
	 * 
	 */
	FunctionKeyController.prototype.onKeyPress = function(e) {
		if ($("iframe").length != 0) {
			$("iframe")[0].contentWindow.$('body').trigger(e);
			return false;
		}
		
		var key = e.key;
		//check keyCode exsist in buttonIds
		if (this.buttonIds[key] == null) {
			return true;
		}
		
		var btnId = this.formId == "" ? this.buttonIds[key]  : this.formId + ":" + this.buttonIds[key];
		var btn = document.getElementById(btnId);
		if (btn.attributes["disabled"] != undefined) {
			return false;
		}
				
		btn.click();
		return false;
	}
	
	FunctionKeyController.prototype.catchPressKeyEvent = function() {
		var self = this;
		$(document).on("keydown", function(e) { 
			disableRightMouseClick();
			return self.onKeyPress(e);
		});
	}
	

	return FunctionKeyController;
})();

//Prevent the backspace key from navigating back.
$(document).unbind('keydown').bind('keydown', function (event) {
    if (event.keyCode === 8) {
        var doPrevent = true;
        var types = ["text", "password", "file", "search", "email", "number", "date", "color", "datetime", "datetime-local", "month", "range", "search", "tel", "time", "url", "week"];
        var d = $(event.srcElement || event.target);
        var disabled = d.prop("readonly") || d.prop("disabled");
        if (!disabled) {
            if (d[0].isContentEditable) {
                doPrevent = false;
            } else if (d.is("input")) {
                var type = d.attr("type");
                if (type) {
                    type = type.toLowerCase();
                }
                if (types.indexOf(type) > -1) {
                    doPrevent = false;
                }
            } else if (d.is("textarea")) {
                doPrevent = false;
            }
        }
        if (doPrevent) {
            event.preventDefault();
            return false;
        }
    }
});