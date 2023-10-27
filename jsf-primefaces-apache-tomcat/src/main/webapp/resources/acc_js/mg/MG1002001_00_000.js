/**
 * MG1002001_00_000.js
 * @version 1.0
 * @author NEV
 * @created 2016/09/14
 *--------------------------------------------------------------------------------------------*
 * History
 * YYYY/MM/DD	Developer	Content
 *--------------------------------------------------------------------------------------------*
 * 2016/09/14	HungND		Create 
 * 2016/11/16	LocDX		use js to insert icon for primefaces's submenu tag
 *--------------------------------------------------------------------------------------------*
 */
$(document).ready(function () {
   document.getElementById("portal:changePass").click();
   
   
   /*↓ use js to set icon for submenu tag because primefaces dont support icon for submenu tags  ↓*/
   submenus = $(".acc-icon-folder h3");
   
   
   $(submenus).each( function(index, submenu) {
	   $(submenu).children(0).after('<span class="ui-menuitem-icon ui-icon pi pi-folder-open" style="margin-left: 20px;"></span>');
   })
   
   
   $('#portal\\:panel_menu li a').on('click',function(){
	   ws.close();
   })
   
});

