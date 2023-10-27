/**

 * use this class when openDialog, disable tabkeys at parent screen
 * 
 * @version 1.0
 * @author LocDX
 * @created 2016/12/20
 * --------------------------------------------------------------------------------------------*
 * History YYYY/MM/DD Developer Content
 * --------------------------------------------------------------------------------------------*
 * 2016/12/20 LocDX Create
 * --------------------------------------------------------------------------------------------*
 */

var ACCTabUtility = {

  SELECTOR_EXCLUDE_INPUT: 'input[disabled=true],input[readonly],[type=hidden],table,tbody',
  SELECTOR_EXCLUDE_INPUT_1: 'input[disabled=true],input[readonly],[type=hidden]',

  /**
   * Check id input is id has max tab index.
   *
   * @Param {String} id of element.
   */
  isLastElement: function (tabindex) {
    if (!tabindex) {
      return false;
    }

    var maxTabIndex = ACCTabUtility.getMaxTabIndex();
    if (!maxTabIndex || maxTabIndex == -1) {
      return false;
    }

    return tabindex == maxTabIndex;
  },

  /**
   * Check id input is id has min tab index.
   *
   * @Param {String} id of element.
   */
  isFirstElement: function (tabindex) {
    if (!tabindex) {
      return false;
    }

    var minTabIndex = ACCTabUtility.getMinTabIndex();
    if (!minTabIndex || minTabIndex == -1) {
      return false;
    }

    return tabindex == minTabIndex;
  },
  /**
   * Find all tabable element in form
   * Return max tab index of element
   *
   * @Return {Integer} Max tab index
   */
  getMaxTabIndex: function () {

    var max = -1;
    $(':tabbable,li').not(ACCTabUtility.SELECTOR_EXCLUDE_INPUT_1).each(function () {
      max = Math.max(max, this.tabIndex);
    });
    // Check if any dialog is opening
    $(".ui-dialog").each(function() {
        if($(this).is(":visible")) {
        	max = -1;
        	$(this).find(':tabbable,li').not(ACCTabUtility.SELECTOR_EXCLUDE_INPUT_1).each(function () {
        		max = Math.max(max, this.tabIndex);
            });
        }
    });
    return max;
  },

  /**
   * Find all tabable element in form
   * Return max tab index of element
   *
   * @Return {Integer} Min tab index
   */
  getMinTabIndex: function () {

    var min = -1;
    $(':tabbable,li').not(ACCTabUtility.SELECTOR_EXCLUDE_INPUT).each(function () {
    	min = (min === -1 || min === 0) ? this.tabIndex : (this.tabIndex === -1 || this.tabIndex === 0 ? min : Math.min(min, this.tabIndex));
    });
    // Check if any dialog is opening
    $(".ui-dialog").each(function() {
        if($(this).is(":visible")) {
        	min = -1;
        	$(this).find(':tabbable,li').not(ACCTabUtility.SELECTOR_EXCLUDE_INPUT).each(function () {
            	min = (min === -1 || min === 0)  ? this.tabIndex : (this.tabIndex === -1 || this.tabIndex === 0 ? min : Math.min(min, this.tabIndex));
            });
        }
    });
    
    if (min == null || min === -1 || min === 0) {
    	min = $(":tabbable,li").not("input[disabled=true],input[readonly],[type=hidden],table,tbody")[0].tabIndex;
    }
    return min;
  },
  /**
   * Set Focus to first Element in form
   */
  focusFirstElement: function () {

    var min = ACCTabUtility.getMinTabIndex();
    setTimeout(function() {
      var firstTabIndex = $(':tabbable[tabindex=' + min + '],li[tabindex=' + min + ']');
      // tabbing for single input
      if (firstTabIndex.length == 1) {
        firstTabIndex.focus();
      } else {
        // tabbing for radiobutton
        var firstTabIndexRadio = firstTabIndex.filter('[type=radio]');
        for (var i = 0 ; i < firstTabIndexRadio.length ; i ++) {
          if(firstTabIndexRadio.eq(i).parent().parent().has('div.ui-state-active').length != 0){
            firstTabIndexRadio.eq(i).focus();
            return;
          }
        }
        // tabbing for multiple tabindex = 1 cases (non radio button)
        firstTabIndex.focus();
      }
    }, 100);
  },

  /**
   * Set Focus to last Element in form
   */
  focusLastElement: function () {

    var max = ACCTabUtility.getMaxTabIndex();
    if (max == null || max === -1) {
      return;
    }
    setTimeout(function() {
    	$(':tabbable[tabindex=' + max + '],li[tabindex=' + max + ']').focus();
	}, 100);
  },
  
  /**
   * Set Focus to next Element in form
   */
  focusPrevElement: function(e) {
	  
	var preElementIndex = -1;
	var array  = $(':tabbable,li').not(ACCTabUtility.SELECTOR_EXCLUDE_INPUT).sort(function (a,b) {return a.tabIndex - b.tabIndex}); 

	for (var i = array.length ; i > 0 ; i --) {
		if ( array[i] == e.target ) {
			preElementIndex = --i ;
			break;
		}
	}
	
	var tabIndexElement = null;
	if (preElementIndex > -1){
		tabIndexElement = array[preElementIndex].tabIndex;
	} else {
		tabIndexElement = array[array.length-1].tabIndex;
	}
			
	setTimeout(function() {
	  $(':tabbable[tabindex=' + tabIndexElement + '],li[tabindex=' + tabIndexElement + ']').first().focus();
	}, 100);

  },
  /**
   * Set Focus to next Element in form
   */
  focusNextElement: function(e) {
	  
	if (ACCTabUtility.isLastElement($(e.target).attr('tabindex'))){		
		ACCTabUtility.focusFirstElement();
	}
    
	var nextElementIndex;
	var array  = $(':tabbable,li').not(ACCTabUtility.SELECTOR_EXCLUDE_INPUT).sort(function (a,b) {return a.tabIndex - b.tabIndex}); 
	for (var i = 0 ; i < array.length ; i ++) {
		if ( array[i] == e.target ) {
			nextElementIndex = ++i ;
			break;
		}
	}

	var tabIndexElement = array[nextElementIndex].tabIndex;
	if (tabIndexElement == null || tabIndexElement === -1) {
	  return;
	}
			
	setTimeout(function() {
	  $(':tabbable[tabindex=' + tabIndexElement + '],li[tabindex=' + tabIndexElement + ']').first().focus();
	}, 100);

  },

  setPickListTabindex: function(id, tabAddAll, tabAdd, tabRemove, tabRemoveAll){
    $(getId(id)+" .ui-picklist-button-add-all").attr("tabindex", tabAddAll);
    $(getId(id)+" .ui-picklist-button-add").attr("tabindex", tabAdd ? tabAdd : tabAddAll + 1 );
    $(getId(id)+" .ui-picklist-button-remove").attr("tabindex", tabRemove ? tabRemove : tabAddAll + 2);
    $(getId(id)+" .ui-picklist-button-remove-all").attr("tabindex", tabRemoveAll ? tabRemoveAll : tabAddAll + 3);
  },

  keydownHandler: function(e) {

    // check if key press is tab key.
    if (e.which === 9) {
    	
    	if(PF('ajaxStatusDialog').jq.is(':visible')){
    		// プログレスバー表示間に、TAB禁止
    		e.preventDefault();
            return false;
    	}
    	
    	// コンボボックスフォーカスできない対応
		$(".ui-helper-hidden-accessible > input[id$='_focus'][readonly='readonly']").each(function() {
			$(this).removeAttr("readonly");
		});
		
        var selector = "input, textarea, select, a, button, li"; 
        $(selector).each(function() {
          if ( $(this).attr("readonly") == "readonly"
            || $(this).attr("readonly") == true)
          {
            $(this).attr("tabindex", "-1");
          }
          
        });
        // ページングボタンがタブ順を外す
		$(".ui-paginator-page").attr("tabindex", "-1");
		$(".ui-paginator-next").attr("tabindex", "-1");
		$(".ui-paginator-last").attr("tabindex", "-1");
		$(".ui-paginator-rpp-options").attr("tabindex", "-1");
		
        // check current screen is popup or not
        if ($('iframe').length != 0 || $('.disableTabIndex').length != 0) {
            // do focus popup
            // $('iframe').focus();
            // return true;
            if($('iframe').contents().find("#message").find(".MessageFrame1").is(':visible')){
                if ($(event.target).attr("tabindex") === undefined) {
                	if (e.shiftKey) {
                		// Shift押下の場合、最大tabindexを取得、フォーカスする
                		var maxTab = -1;
                		$('iframe').contents().find("#message").find(".MessageFrame1").find(':tabbable,li').not(ACCTabUtility.SELECTOR_EXCLUDE_INPUT_1).each(function () {maxTab = Math.max(maxTab, this.tabIndex);});
                		if (maxTab > -1) {
                			$('iframe').contents().find("#message").find(".MessageFrame1").find('[tabindex=' + maxTab + ']').focus();
                		}
                	} else {
                		$('iframe').contents().find("#message").find(".MessageFrame1").find(".dialogFrameBtn1").focus();
                	}
                } else {
                    enterKeyController.toNextComponent($("#message"), $(event.target));
                }
                event.preventDefault();
                return false;
            } else if ($('iframe').contents().find("#messageException").find(".MessageFrame2").is(':visible')) {
                if ($(event.target).attr("tabindex") === undefined) {
                    $('iframe').contents().find("#messageException").find(".MessageFrame2").find(".dialogFrameBtn2").focus();
                } else {
                    enterKeyController.toNextComponent($("#messageException"), $(event.target));
                }
                event.preventDefault();
                return false;
            } else {
            	if (e.shiftKey){
            		// Shift押下の場合、最大tabindexを取得、フォーカスする
            		var maxTab = -1;
            		$('iframe').contents().find(':tabbable,li').not(ACCTabUtility.SELECTOR_EXCLUDE_INPUT_1).each(function () {maxTab = Math.max(maxTab, this.tabIndex);});
            		if(maxTab > -1){
            			$('iframe').contents().find('[tabindex=' + maxTab + ']').focus();
            			event.preventDefault();
                        return false;	
            		}
            	}  else {
            		$('iframe').focus();
            	}
                return true;
            }
        } else {
            if($("#message").find(".MessageFrame1").is(':visible')){
                if ($(event.target).attr("tabindex") === undefined) {
                	if (e.shiftKey){
	                	// Shift押下の場合、最大tabindexを取得、フォーカスする
	            		var maxTab = -1;
	            		$("#message").find(':tabbable,li').not(ACCTabUtility.SELECTOR_EXCLUDE_INPUT_1).each(function () {maxTab = Math.max(maxTab, this.tabIndex);});
	            		if(maxTab > -1){
	            			$("#message").find('[tabindex=' + maxTab + ']').focus();
	            		} 
                	} else {
            			 $("#message").find(".dialogFrameBtn1").focus();
            		}
                } else {
                    enterKeyController.toNextComponent($("#message"), $(event.target));
                }
                event.preventDefault();
                return false;
            } else if($("#messageException").find(".MessageFrame2").is(':visible')){
                if ($(event.target).attr("tabindex") === undefined) {
                    $("#messageException").find(".dialogFrameBtn2").focus();
                } else {
                    enterKeyController.toNextComponent($("#messageException"), $(event.target));
                }
                event.preventDefault();
                return false;
            }
        }
        
      
      if (e.shiftKey){
    	  if (ACCTabUtility.isFirstElement($(e.target).attr('tabindex'))){
    		  ACCTabUtility.focusLastElement();
			  return false;
    	  } else if (ACCTabUtility.isLastElement($(e.target).attr('tabindex'))) {
    		  // remove selection
    		  if (e.target.tagName === 'INPUT' && (e.target.getAttribute('type').length != 0 && e.target.getAttribute('type') === 'text')) {
    			  e.target.setSelectionRange(0, 0);
    		  }
			  ACCTabUtility.focusPrevElement(e);
			  return false;
		  } 
    	  
      } else {
    	  if (ACCTabUtility.isLastElement($(e.target).attr('tabindex'))) {
    		 // remove selection
       		 if (e.target.tagName === 'INPUT' && (e.target.getAttribute('type').length != 0 && e.target.getAttribute('type') === 'text')) {
       			 e.target.setSelectionRange(0, 0);
       		 }
       		 
       		 ACCTabUtility.focusFirstElement();
       		 return false;
    	  }
      }
      return true;
    }
    
  }
}

$(function() {

	$(document).on('keydown', ACCTabUtility.keydownHandler);

	// set focus on first element
	setTimeout(function() {
		if ($(":tabbable").length > 0 && $(":tabbable:focus").length == 0) {
			setTimeout(function() {
				// コンボボックスフォーカスできない対応(初期表示の場合)
				$(".ui-helper-hidden-accessible > input[id$='_focus'][readonly='readonly']").each(function() {
					$(this).removeAttr("readonly");
				});
				ACCTabUtility.focusFirstElement();
			}, 250);
		}
	}, 100);

	//unfocus datatable elements
	$(document).on("focus", "div.ui-datatable-scrollable", function(e) {
	  if($(e.target).filter("input, textarea, select, a, button, li").length == 0){
	    $(e.target).blur();
	  }
	});
});
 
