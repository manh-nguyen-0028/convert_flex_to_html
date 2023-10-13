$( document ).ready(function() {
    console.log( "ready!" );
	openTabPosition(0)
});

function openTab(tabName) {
  var i, tabContent;
  tabContent = document.getElementsByClassName("tab-content");
  for (i = 0; i < tabContent.length; i++) {
    tabContent[i].style.display = "none";
  }
  document.getElementById(tabName).style.display = "block";
}

function openTabPosition(position) {
  var i, tabContent;
  tabContent = document.getElementsByClassName("tab-content");
  for (i = 0; i < tabContent.length; i++) {
	if(i == position) {
		tabContent[i].style.display = "block";
	} else {
		tabContent[i].style.display = "none";
	}
  }
}