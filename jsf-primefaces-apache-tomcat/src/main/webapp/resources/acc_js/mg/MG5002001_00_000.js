/**
 * MG5002001_00_000.js
 * @version 1.0
 * @author NEV
 * @created 2016/12/09
 *--------------------------------------------------------------------------------------------*
 * History
 * YYYY/MM/DD	Developer	Content
 *--------------------------------------------------------------------------------------------*
 * 2016/12/09	DuanTN		Create 
 *--------------------------------------------------------------------------------------------*
 */
/*
 * click move up button
 */
function clickMoveUp() {
	document.getElementsByClassName("ui-picklist-button-move-up")[0].click();
}

/*
 * click move down button
 */
function clickMoveDown() {
	document.getElementsByClassName("ui-picklist-button-move-down")[0].click();
}

function removeHiglightClass(){
	var list = document.getElementById("MG5002001Form:panel-left:datKomokumei_data")
	.getElementsByClassName("ui-state-highlight");

	if(list.length > 0){
		list[0].classList.remove("ui-state-highlight");
	}
}

