

var data = [
	{"id":"88522","firstname":"Dwight","lastname":"Ford","state":"WA","registration":"04-04-2008","city":"Topeka","birth":"25-06-1957","nbCommands":"98","avgAmount":"55.4"},
	{"id":"70057","firstname":"Lyndon","lastname":"Roosevelt","state":"DE","registration":"16-09-2007","city":"Baton Rouge","birth":"15-06-1976","nbCommands":"89","avgAmount":"73.4"},
	{"id":"46052","firstname":"Ulysses","lastname":"Buchanan","state":"WI","registration":"04-11-2007","city":"Lincoln","birth":"22-01-1960","nbCommands":"16","avgAmount":"42.0"},
	{"id":"48228","firstname":"Gerald","lastname":"Roosevelt","state":"NC","registration":"14-08-2007","city":"Atlanta","birth":"10-02-1955","nbCommands":"84","avgAmount":"46.4"},
	{"id":"90803","firstname":"Calvin","lastname":"Jefferson","state":"ID","registration":"22-05-2007","city":"Bismarck","birth":"27-04-1953","nbCommands":"91","avgAmount":"42.3"},
	{"id":"20156","firstname":"Rutherford","lastname":"Harding","state":"NM","registration":"26-09-2007","city":"Augusta","birth":"06-05-1958","nbCommands":"64","avgAmount":"46.7"},
	{"id":"34331","firstname":"Calvin","lastname":"Harrison","state":"NY","registration":"13-11-2008","city":"Denver","birth":"12-09-1980","nbCommands":"7","avgAmount":"49.7"},
	{"id":"56012","firstname":"Benjamin","lastname":"McKinley","state":"IL","registration":"14-07-2007","city":"Trenton","birth":"26-11-1956","nbCommands":"46","avgAmount":"33.3"},
	{"id":"52640","firstname":"Ulysses","lastname":"Ford","state":"TN","registration":"19-03-2007","city":"Jefferson City","birth":"17-08-1976","nbCommands":"69","avgAmount":"0.8"},
	{"id":"88866","firstname":"John","lastname":"McKinley","state":"OH","registration":"04-05-2008","city":"Dover","birth":"09-06-1968","nbCommands":"74","avgAmount":"18.6"},
	{"id":"74866","firstname":"Bill","lastname":"Monroe","state":"GA","registration":"01-03-2007","city":"Dover","birth":"17-11-1948","nbCommands":"41","avgAmount":"64.8"}
];

function loadDatatables() {
	var datatable = "";
	for(d in data) {
		var line = data[d];
		datatable += "<tr>";
		datatable += "<td>"+line.id+"</td>";
		datatable += "<td>"+line.firstname+"</td>";
		datatable += "<td>"+line.lastname+"</td>";
		datatable += "<td>"+line.state+"</td>";
		datatable += "<td>"+line.registration+"</td>";
		datatable += "<td>"+line.city+"</td>";
		datatable += "<td>"+line.birth+"</td>";
		datatable += "<td>"+line.nbCommands+"</td>";
		datatable += "<td>"+line.avgAmount+"</td>";
		datatable += "</tr>";
	}
	$('#mytable tbody').append(datatable);
}
