/**
 * MG3003001_00_000.js
 * @version 1.0
 * @author NEV-SangNV
 * @created 2016/11/02
 *--------------------------------------------------------------------------------------------*
 * History
 * YYYY/MM/DD	Developer	Content
 *--------------------------------------------------------------------------------------------*
 * 2016/11/02	NEV-SangNV	Create
 * 2017/02/01	LocDX		Insert setting tabindex
 * 2020/02/17	KienNT		Fix [v2.21] Kadai 17
 *--------------------------------------------------------------------------------------------*
 */

$(document).ready(function() {
	$('#MG3003001 .ui-radiobutton div').removeClass('ui-state-disabled');
	$('#MG3003001 .ui-selectoneradio label').removeClass('ui-state-disabled');

	//set focus on
	if ($(getId("frmMg3001:btnJokenYobidashi")).attr("disabled") != "disabled") {
		focusItemId("frmMg3001:btnJokenYobidashi");
	}

	updatePickListTabindex();
	updatePickList2Tabindex();
	updatePickList3Tabindex();
	updatePickList4Tabindex();

	addEventChbGroup();
});

function updatePickListTabindex() {
  ACCTabUtility.setPickListTabindex("frmMg3001:ANKEN_TABS:pickList", 22);
}

function updatePickList3Tabindex() {
  ACCTabUtility.setPickListTabindex("frmMg3001:ANKEN_TABS:pickList3", 27);
}

function updatePickList4Tabindex() {
  ACCTabUtility.setPickListTabindex("frmMg3001:ANKEN_TABS:pickList4", 81);
}

function updatePickList2Tabindex() {
  ACCTabUtility.setPickListTabindex("frmMg3001:pickList2", 105);
}

function addEventChbGroup() {
	//change checkbox 約束(履行 || 不履行)
	$(getId('frmMg3001:ANKEN_TABS:saiKeiyakuCheckBox')).find('input[type="checkbox"]').bind('change', function (v) {
		uncheckOther($(this), 'frmMg3001:ANKEN_TABS:saiKeiyakuCheckBox');
    });
	//change checkbox 入金約束(約束なし||履行 || 不履行)
	$(getId('frmMg3001:ANKEN_TABS:paymentPromiseCheckBox')).find('input[type="checkbox"]').bind('change', function (v) {
		uncheckOther($(this), 'frmMg3001:ANKEN_TABS:paymentPromiseCheckBox');
    });
}
/**
 * set check/uncheck for current check box and uncheck all other in group
 * @param chb
 * @param groupId
 */
function uncheckOther(chb, groupId) {
	displayCheckedItem($(chb));
	var curentId = $(chb).attr("id");
	$(getId(groupId)).find('input[type="checkbox"]').not(getId(curentId)).each(function(){
		var otherId = $(this).attr("id");
		var div = otherId.substring(0, otherId.length - 6);//remove end "_input"
		var box = $(this).parent().parent().children().next().children(".ui-chkbox-icon");
		if(box == null || box == undefined) {
			return;
		}
		$(box).removeClass("ui-icon-check").addClass("ui-icon-blank");
    	$(box).parent().removeClass("ui-state-active");
		$(this).prop("checked", false);
	  });
}

/**
 * set checked or unchecked item by jquery
 * @param clientId
 * @param checked
 */
function checkedItem(clientId, checked) {

	if(checked == undefined || checked == '') {
		checked = false;
	}
    //to change the checked attribute
    $(':checkbox[id="'+ clientId + '"]').attr('checked', checked);
    var div = clientId.substring(0, clientId.length - 6);//remove end "_input"
    if (checked) {
        $('div[id="'+ div + '"] > div').each(function() {
            $(this).addClass('ui-state-active');
            $(this).children('span').addClass('ui-icon ui-icon-check');
        });
    } else {
        $('div[id="'+ div + '"] > div').each(function() {
            $(this).removeClass('ui-state-active');
            $(this).children('span').removeClass('ui-icon ui-icon-check');
        });
    }
}

function removedError() {
	$(".ui-state-error").removeClass("ui-state-error");
	$(".acc-validate-error").removeClass("acc-validate-error");
}