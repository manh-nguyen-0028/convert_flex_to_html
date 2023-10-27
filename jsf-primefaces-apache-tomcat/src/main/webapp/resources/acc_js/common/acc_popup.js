/**
 * ACCPopup
 * 
 * @version 1.0
 * @author NEJ
 * @created 2016/07/15
 *          --------------------------------------------------------------------------------------------*
 *          History YYYY/MM/DD Developer Content
 *          --------------------------------------------------------------------------------------------*
 *          2016/07/15 NEJ Create
 *          --------------------------------------------------------------------------------------------*
 */
function openPopupBrowser(url, width, height) {
	var left = (screen.width / 2) - (width / 2);
	var top = (screen.height / 2) - (height / 2);
	var feature = "width="
			+ width
			+ ",height="
			+ height
			+ ",top="
			+ top
			+ ",left="
			+ left
			+ ",toolbar=no,menubar=no,scrollbars=no,resizable=yes,location=no,status=no,scrollbars=yes";
	window.open(url, "", feature);
}