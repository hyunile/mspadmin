/**
 * 
 */

var mspBackgroundColor =[
		                'rgba(255, 99, 132, 0.2)',
		                'rgba(54, 162, 235, 0.2)',
		                'rgba(255, 206, 86, 0.2)',
		                'rgba(75, 192, 192, 0.2)',
		                'rgba(153, 102, 255, 0.2)',
		                'rgba(255, 159, 64, 0.2)',
		                'rgba(179,181,198,0.2)'
		            ];
var mspBorderColor = [
		                'rgba(255,99,132,1)',
		                'rgba(54, 162, 235, 1)',
		                'rgba(255, 206, 86, 1)',
		                'rgba(75, 192, 192, 1)',
		                'rgba(153, 102, 255, 1)',
		                'rgba(255, 159, 64, 1)',
		                'rgba(179,181,198,1)'
		            ];

function getMspBackgroundColor(cnt){
	var result = [];
	for(var i=0; i < cnt; i++){
		result.push(mspBackgroundColor[i%7]);
	}
	return result;
}

function getMspBorderColor(cnt){
	var result = [];
	for(var i=0; i < cnt; i++){
		result.push(mspBorderColor[i%7]);
	}
	return result;
}


function getMspCharBarOptions(){
	var options = {
	        scales: {
	            xAxes: [{
	                stacked: true
	                
	            }],
	            yAxes: [{
	                stacked: true,
	                ticks: {
	                    beginAtZero:true
	                }
	            }]
	        },
	        responsive: false,
	        
	    }
	return options;
}