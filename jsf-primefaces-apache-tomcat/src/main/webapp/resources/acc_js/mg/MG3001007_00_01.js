/**
 * MG3001007_00_000.js
 * @version 1.0
 * @author NEV
 * @created 2017/03/22
 *--------------------------------------------------------------------------------------------*
 * History
 * YYYY/MM/DD    Developer    Content
 *--------------------------------------------------------------------------------------------*
 * 2017/03/22    ThemNV        Create
 * 2020/02/17    KienNT       Fix [v2.21] Kadai 17
 *--------------------------------------------------------------------------------------------*
 */
var pickListId = "#frmMg3001\\:frameChild\\:pickList5";
$( document ).ready(function() {
    $(pickListId +" div.ui-picklist-buttons div.ui-picklist-buttons-cell button.ui-picklist-button-add").click(function(){
    	addToSource();
    });
    $(pickListId +" div.ui-picklist-buttons div.ui-picklist-buttons-cell button.ui-picklist-button-remove").click(function(){
    	removeAllTarget();
    });
    $(pickListId +" div.ui-picklist-list-wrapper ul.ui-picklist-target").droppable({
        drop: function( event, ui ) {
            var source = pickListId +" div.ui-picklist-list-wrapper ul.ui-picklist-source";
            var target = pickListId +" div.ui-picklist-list-wrapper ul.ui-picklist-target";
            var newItem= ui.draggable;//$(newItem).attr('data-item-value')
            var dataItem = $(newItem).attr('data-item-value');
            if($(target).children('li').length > 1) {
                //$(target).children('li').detach().appendTo($(source));
                var listItems = $(target).children('li');
                var ind, len, item, oldDataItem;

                for ( ind = 0, len = listItems.length; ind < len; ind++ ) {
                    item = $(listItems[ind]);
                    oldDataItem = $(item).attr('data-item-value');
                    if(oldDataItem != undefined && oldDataItem != dataItem ) {
                        $(item).detach().appendTo($(source));
                    }

                }
            }
      }
    });
    //dblclick source
    $(pickListId +" div.ui-picklist-list-wrapper ul.ui-picklist-source li").dblclick(function(){
    	addToSource();
    });

    // 2020/02/17 NEV-KienNT Fix [v2.21] Kadai 17 START
    //update tab index for picklist5 buttons
    updatePickList5Tabindex()
    // 2020/02/17 NEV-KienNT Fix [v2.21] Kadai 17 END
  });

/**
 * moved old items of target back to source and add selected item from target to source
 */
function addToSource() {
	var source = pickListId
			+ " div.ui-picklist-list-wrapper ul.ui-picklist-source";
	var target = pickListId
			+ " div.ui-picklist-list-wrapper ul.ui-picklist-target";
	//removed multiple selection
	var listItems = $(source).children('li.ui-state-highlight');
	var ind, len;
	for (ind = 1, len = listItems.length; ind < len; ind++) {
		$(listItems[ind]).removeClass('ui-state-highlight');
	}
	if ($(target).children('li').length > 0) {
		$(target).children('li').detach().appendTo($(source));
	}
}

function removeAllTarget() {
	var target = pickListId
			+ " div.ui-picklist-list-wrapper ul.ui-picklist-target";
	if ($(target).children('li.ui-state-highlight').length > 0) {
		return
	}
	if ($(target).children('li').length > 0) {
		$(target).children('li').addClass('ui-state-highlight');
	}
}

// 2020/02/17 NEV-KienNT Fix [v2.21] Kadai 17 START
function updatePickList5Tabindex() {
  $(getId("frmMg3001:frameChild:pickList5")+" .ui-picklist-button-add").attr("tabindex", "167");
  $(getId("frmMg3001:frameChild:pickList5")+" .ui-picklist-button-remove").attr("tabindex", "168");
}
// 2020/02/17 NEV-KienNT Fix [v2.21] Kadai 17 END