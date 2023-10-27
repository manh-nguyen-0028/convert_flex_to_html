function markGyomuDateAsCurrentDate(date) {
	var gyomuDateString = $(getId("MG5003001Form:MG5003002:mg5003002_gyomuDate")).val();
	return setGyomuDateStyle(date,gyomuDateString);
};

