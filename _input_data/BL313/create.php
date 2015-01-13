

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xml:lang="en" lang="en">
<head>
<script type="text/javascript" src="jquery-1.8.2.min.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css" media="print">
@import url("https://www.csie.ntu.edu.tw/~r00944002/fujie/TaipeiSportStar/css/style.css");
</style>


	
	<script src="https://maps.googleapis.com/maps/api/js?sensor=false"></script>
    <script type="text/javascript">
      // Enter a client ID for a web application from the Google Developer Console.
      // The provided clientId will only work if the sample is run directly from
      // https://google-api-javascript-client.googlecode.com/hg/samples/authSample.html
      // In your Developer Console project, add a JavaScript origin that corresponds to the domain
      // where you will be running the script.
      var clientId = '1010405642245';

      // Enter the API key from the Google Develoepr Console - to handle any unauthenticated
      // requests in the code.
      // The provided key works for this sample only when run from
      // https://google-api-javascript-client.googlecode.com/hg/samples/authSample.html
      // To use in your own application, replace this API key with your own.
      var apiKey = 'AIzaSyBy4U1xNMbRXeSWszu5q1lIEFUTc5LQS9Q';

      // To enter one or more authentication scopes, refer to the documentation for the API.
      var scopes = 'https://www.googleapis.com/auth/latitude.current.best';	  
	  var map;
	  var posx,posy;
	  var userpos;
	  var currentDirections = null;
	  var oldDirections = [];
	  var directionsDisplay;
	  var directionsService = new google.maps.DirectionsService();
	  var nowi=0;
	  var travel_mode=1; //0 開車 1走路
	  var elitekey;
      var maxquery=10;

	  function change_dist(dddd){
		document.getElementById("directions_panel").innerHTML =dddd;
	  };
	 

	 
	var g_stadium_name;
    var g_sport_name;
	  function map_init(){
	  /*
		console.log(posx);
		console.log(posy);
		var mapOptions = {
			  zoom: 13,
			  center: new google.maps.LatLng(posy, posx),
			  mapTypeId: google.maps.MapTypeId.ROADMAP
		};
		
		map = new google.maps.Map(document.getElementById('map_canvas'),
				mapOptions);
				
		directionsDisplay = new google.maps.DirectionsRenderer({
		'map': map,
		'preserveViewport': true,
		'draggable': false
		});
		
		//directionsDisplay.setPanel(document.getElementById('directions_panel'));
		
		google.maps.event.addListener(directionsDisplay, 'directions_changed',
		function() {
        if (currentDirections) {
          oldDirections.push(currentDirections);  
        }
        currentDirections = directionsDisplay.getDirections();
		});
		*/
		userpos= new google.maps.LatLng(posy, posx);
		
		
		//calcRoute2(userpos,'台灣大學');
		console.log('a');		
		sendpos = getAllStadiumNames();	
		
		elitekey = new Array(sendpos.length);
		var pos_weight  = new Array(sendpos.length);
		for ( i=0 ; i<sendpos.length; i++ ) {
                elitekey[i] = i;
				pos_weight[i]= (posx - sendpos[i][0])*(posx - sendpos[i][0])  + (posy- sendpos[i][1]) *(posy- sendpos[i][1]);
		}	
		
		for ( i = 0; i<sendpos.length-1 ; i++ ) {
                for ( j = i+1 ; j<sendpos.length ; j++ ){				
                     if ( pos_weight[elitekey[i]] > pos_weight[elitekey[j]] ) {
                        temp = elitekey[i];
                        elitekey[i] = elitekey[j];
                        elitekey[j] = temp; 
					}
                }
        }
		
		for ( i=0 ; i<sendpos.length; i++ ) {
               console.log(pos_weight[elitekey[i]]);
		}	
		console.log(elitekey);
		
		elitesend =new Array();
		for ( i=0 ; i<maxquery; i++ ){
			elitesend[i] =  new google.maps.LatLng(sendpos[elitekey[i]][1], sendpos[elitekey[i]][0]);
			
		}
		//console.log(elitesend);
		calcDistAll(userpos,elitesend,getSortedItems );
				
		
	  }
	  

	
		
	  function calcRoute2(pFrom,pEnd) {
		var start = pFrom;
		var end = pEnd;
		
		console.log(start);
		console.log(end);
		
		var request = {
			origin:start,
			destination:end,
			travelMode: google.maps.DirectionsTravelMode.DRIVING
		};
		
		directionsService.route(request, function(response, status){
			if (status == google.maps.DirectionsStatus.OK){
			directionsDisplay.setDirections(response);
			var distance=response.routes[0].legs[0].distance.value;
			document.getElementById("directions_panel").innerHTML =distance;
			
			}
		
		});
			

	  }
	   
	   
	  function calcDistAll(pFrom,pEnd,callback){
		var start = pFrom;
		var end = pEnd;
		var n_end = end.length;
		var travelM;
		
		//console.log(start);
		
		
		
		
		var distAll =new Array(n_end);	
		//console.log('b');
		console.log(end);
		console.log(n_end);
		
		if(travel_mode==0)
					travelM=google.maps.DirectionsTravelMode.DRIVING;
		else if(travel_mode==1)
					travelM=google.maps.DirectionsTravelMode.WALKING;
		
		
		for(var i =0 ; i < n_end ;i++){			
			var request = {
				origin:start,
				destination:end[i],
				travelMode: travelM				
			};
			
			directionsService.route(request, 
				function(response, status){
					if (status == google.maps.DirectionsStatus.OK){			
						//distAll[nowi]=response.routes[0].legs[0].distance.value;	
						distAll[nowi]=response.routes[0].legs[0].duration.value;						
					}
					else{
						
						distAll[nowi] = -2;
						
					}
					nowi++;
					
					if(nowi == n_end)
						callback(distAll);								
				
					
				}			
			
			);
		}
	  }		
		
	
	
		
	  
	  
	  
      // Use a button to handle authentication the first time.
      function handleClientLoad() {
        gapi.client.setApiKey(apiKey);
        window.setTimeout(checkAuth,1);
      }

      function checkAuth() {
        gapi.auth.authorize({client_id: clientId, scope: scopes, immediate: true}, handleAuthResult);
      }


      function handleAuthResult(authResult) {
        var authorizeButton = document.getElementById('authorize-button');
        if (authResult && !authResult.error) {
          authorizeButton.style.visibility = 'hidden';
          makeApiCall();
        } else {
          authorizeButton.style.visibility = '';
          authorizeButton.onclick = handleAuthClick;
        }
      }

      function handleAuthClick(event) {
        gapi.auth.authorize({client_id: clientId, scope: scopes, immediate: false}, handleAuthResult);
        return false;
      }

      // Load the API and make an API call.  Display the results on the screen.
      function makeApiCall() {
        gapi.client.load('latitude', 'v1', function() {
		  var tempte =gapi.client.latitude.currentLocation.insert();
			
			console.log(tempte);
		
          var request = gapi.client.latitude.currentLocation.get({
            "granularity": "best"
          });
		  
		  
          request.execute(function(loc) {
            var wp;
            if (loc.error) {
				document.getElementById("current_location").innerHTML ="Can't determine location: " + loc.error.message;
				posx=25;
				posy=121;
				map_init();
				console.log('a1');
			}
            else {
				console.log('a2');
				console.log(loc);
				posx=loc.longitude;
				posy=loc.latitude;
				
				//document.getElementById("current_location").innerHTML = "Current location: " + posx + " " + posy;
				
				map_init();
				
			}			
          });
        });
      }
	  function sportClicked (a) {
        var name = $(a).attr('name');
        g_sport_name = name;
        if ( window.confirm("運動名名稱:"+g_sport_name+"\n"+"運動場館:"+g_stadium_name) ) {
			window.location = "https://www.csie.ntu.edu.tw/~r00944002/fujie/TaipeiSportStar/routine.php?action=create&sport="+g_sport_name+"&location="+g_stadium_name;
		}
		else {
		};
      }
	  function getSortedItems ( durations ) {
			//onsole.log('show dura'+durations);
			
            var divIDs = ["stadium_0","stadium_1","stadium_2","stadium_3","stadium_4","stadium_5","stadium_6","stadium_7","stadium_8","stadium_9","stadium_10","stadium_11","stadium_12","stadium_13","stadium_14","stadium_15","stadium_16","stadium_17","stadium_18","stadium_19","stadium_20","stadium_21","stadium_22","stadium_23"];
            var stadium_weights = new Array(divIDs.length);
            var key = new Array(divIDs.length);
            var minDuraion = -1;			
            var outdoor_weight = document.getElementById('outdoor_weight').innerHTML;
			
			eachdurations =new Array(divIDs.length);
			console.log(elitekey);
			for ( i=0 ; i<durations.length; i++ ){
				eachdurations[elitekey[i]] = durations[i];
			}
			console.log(eachdurations);
			
            for ( i=0 ; i<divIDs.length; i++ ) {
                key[i] = i;
				if(isNaN(eachdurations[i]) ==true)
					eachdurations[i] = 9999999;
                if ( minDuraion<0 || minDuraion>eachdurations[i] ) {
                    minDuraion = eachdurations[i];
				
                }
            }
			console.log('min' + minDuraion);
            for ( i=0 ; i<divIDs.length; i++ ) {
                type = document.getElementById(divIDs[i]+"_type").innerHTML;
                stadium_weights[i] = minDuraion/eachdurations[i];
                if ( type == 0 ) {
                    stadium_weights[i]*=outdoor_weight;
                }
            }
			
            for ( i = 0; i<divIDs.length-1 ; i++ ) {
                for ( j = i+1 ; j<divIDs.length ; j++ ){
                    if ( stadium_weights[key[i]] < stadium_weights[key[j]] ) {
                        temp = key[i];
                        key[i] = key[j];
                        key[j] = temp; 
                    }
                }
            }
			console.log(stadium_weights);
			console.log(key);
			
            shuffleDivs(key);
        }
		
        function shuffleDivs(changedIDs){
                var divIDs = ["stadium_0","stadium_1","stadium_2","stadium_3","stadium_4","stadium_5","stadium_6","stadium_7","stadium_8","stadium_9","stadium_10","stadium_11","stadium_12","stadium_13","stadium_14","stadium_15","stadium_16","stadium_17","stadium_18","stadium_19","stadium_20","stadium_21","stadium_22","stadium_23"];
                //var changedIDs = [23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1,0];
                var divInnerHTMLs = new Array(divIDs.length);
                var divName = new Array(divIDs.length);
                for(var i =0;i<divInnerHTMLs.length;i++)
                {
                      divInnerHTMLs[i] = document.getElementById(divIDs[i]).innerHTML;
						divName[i] = document.getElementById(divIDs[i]).value;
						console.log(document.getElementById(divIDs[i]).value);
                }

                for(var i=0;i<divInnerHTMLs.length;i++)
                {
                    //alert("swap "+divIDs+" with divIDs["+changedIDs[i]+"]" );
                      document.getElementById(divIDs[i]).innerHTML = divInnerHTMLs[changedIDs[i]];
					  document.getElementById(divIDs[i]).value = divName[changedIDs[i]];
					  if(i< maxquery){
						document.getElementById(divIDs[i]).style.display=""; //釋放li
					  }
					  //document.getElementById(divIDs[i]+'_name').style.display=""; 
					  //console.log(divIDs[i]+'_name');
                }            
        }

        function getAllStadiumNames() {
                
                var divIDs = ["stadium_0","stadium_1","stadium_2","stadium_3","stadium_4","stadium_5","stadium_6","stadium_7","stadium_8","stadium_9","stadium_10","stadium_11","stadium_12","stadium_13","stadium_14","stadium_15","stadium_16","stadium_17","stadium_18","stadium_19","stadium_20","stadium_21","stadium_22","stadium_23"];
                var stadiumNames = new Array(divIDs.length);
                for ( var i=0 ; i<divIDs.length ; i++ ) {
					stadiumNames[i] =new Array(2);
                    stadiumNames[i][0] = document.getElementById(divIDs[i]+"_long").innerHTML;
					stadiumNames[i][1] = document.getElementById(divIDs[i]+"_lati").innerHTML;
                    //alert(name);
                }
                console.log(stadiumNames);
                return stadiumNames;
        }

        function showStadiumDetail ( elementId ) {
            //alert("detail_"+elementId);
            document.getElementById("detail_"+elementId).style.display="";
        }

        function hideStadiumDetail (elementId) {
            document.getElementById("detail_"+elementId).style.display="none";
        }
	  /*
	   function stadiumNameClicked ( a ) {

            var id = $(a).prop('name');
            g_stadium_name = $('li[id="'+id+'"]').prop('name');
            $('li[class="stadium"]').css('display','none');
            $('li[id="'+id+'"]').css('display','block');
       }
	   */
       function stadiumNameClicked ( a ) {

            var idnum = $(a).prop('name');
			var id = 'stadium_'+idnum; 
            g_stadium_name = $('li[id="'+id+'"]').attr('name');
			//console.log(id);
			//console.log($('li[id="'+id+'"]').attr('name'));
            $('li[class="stadium"]').css('display','none');
            //$('li[id="'+id+'"]').css('display','block');  wrong!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
			
			
			for(var i=0 ;i<24 ;i++){				 
				if(document.getElementById("stadium_"+i).value==idnum ){
				document.getElementById("stadium_"+i).style.display ="block";
				break;
				}
					
			}
			
			
			g_sport_name = document.getElementById(id+"_sport").innerHTML;
			console.log(g_sport_name);
			document.getElementById(id+"_sport").style.display="none";
			document.getElementById(id+"_sport2").style.display="block";
       }
    </script>
    <script src="https://apis.google.com/js/client.js?onload=handleClientLoad"></script>
	






</head>
<body>
<div id = "navi">
    <ul>
        <li><a href="https://www.csie.ntu.edu.tw/~r00944002/fujie/TaipeiSportStar/">Home</a></</li>
    </ul>
</div>
<div id = 'fb_profile'>
    <div id='fb_profile_name'>Welcome 曾毅修</div>
    <div id='fb_profile_pic'><img src="https://graph.facebook.com/100000124992896/picture" /></div>
</div>
    <div id = "weather_lite">
<img src="http://l.yimg.com/a/i/us/we/52/30.gif"/><br />
<b>Current Conditions:</b><br />
Partly Cloudy, 28 C<BR />
<BR /><b>Forecast:</b><BR />
Sun - Sunny. High: 29 Low: 21<br />
Mon - Sunny. High: 29 Low: 21<br />
<br />
<a href="http://us.rd.yahoo.com/dailynews/rss/weather/Taipei_City__TW/*http://weather.yahoo.com/forecast/TWXX0021_c.html">Full Forecast at Yahoo! Weather</a><BR/><BR/>
(provided by <a href="http://www.weather.com" >The Weather Channel</a>)<br/>
</div>
<button id="authorize-button" style="visibility: hidden">Authorize</button>

<div id = "stadiums">
<ul>
<li id="stadium_0" class="stadium" style="display:none;" name="大佳河濱公園" value="0">
        <div id="stadium_id" style="display:none;">0</div>
        <div id="stadium_0_type" style="display:none;">0</div>
        <div class="name" id = "stadium_0_name" onMouseOver = "showStadiumDetail(0);" onMouseOut = "hideStadiumDetail(0);"><a href='#' onClick="stadiumNameClicked(this);" name="0">大佳河濱公園</a></div>

		 <div class = "sport" id = "stadium_0_sport">籃球 排球 網球 </div>
	<div class = "sport" id = "stadium_0_sport2" style="display:none;"><a href="#" name="籃球" onClick="sportClicked(this);">籃球</a> <a href="#" name="排球" onClick="sportClicked(this);">排球</a> <a href="#" name="網球" onClick="sportClicked(this);">網球</a> </div>
	<div id = "stadium_0_long" style="display:none;">121.5317</div>
	<div id = "stadium_0_lati" style="display:none;">25.0747</div>	
    <div id = "detail_0" onMouseOver = "showStadiumDetail(0);" onMouseOut = "hideStadiumDetail(0);" style="display:none;">
    <div id = "stadium_0_addr" class = "addr" >臺北市中山區圓山橋下10號水門</div>
	<div class = "time">開放時間 : none</div>
    <div class = "bus">搭乘33、72、222、286、RL34路市公車→至大佳國小站下車→步行即可達大佳河濱公園</div>
	<div class = "mrt">搭乘捷運淡水線至圓山站下車→轉搭102.103路市車至9號水門大佳國小站下車→步行即可達大佳河濱公園</div>
</div><!--div detail-->
</li>

<li id="stadium_1" class="stadium" style="display:none;" name="迎風河濱運動公園" value="1">
        <div id="stadium_id" style="display:none;">1</div>
        <div id="stadium_1_type" style="display:none;">0</div>
        <div class="name" id = "stadium_1_name" onMouseOver = "showStadiumDetail(1);" onMouseOut = "hideStadiumDetail(1);"><a href='#' onClick="stadiumNameClicked(this);" name="1">迎風河濱運動公園</a></div>

		 <div class = "sport" id = "stadium_1_sport">足球 壘球 棒球 溜冰 曲棍球 木球 迷你高爾夫 </div>
	<div class = "sport" id = "stadium_1_sport2" style="display:none;"><a href="#" name="足球" onClick="sportClicked(this);">足球</a> <a href="#" name="壘球" onClick="sportClicked(this);">壘球</a> <a href="#" name="棒球" onClick="sportClicked(this);">棒球</a> <a href="#" name="溜冰" onClick="sportClicked(this);">溜冰</a> <a href="#" name="曲棍球" onClick="sportClicked(this);">曲棍球</a> <a href="#" name="木球" onClick="sportClicked(this);">木球</a> <a href="#" name="迷你高爾夫" onClick="sportClicked(this);">迷你高爾夫</a> </div>
	<div id = "stadium_1_long" style="display:none;">121.5463</div>
	<div id = "stadium_1_lati" style="display:none;">25.0745</div>	
    <div id = "detail_1" onMouseOver = "showStadiumDetail(1);" onMouseOut = "hideStadiumDetail(1);" style="display:none;">
    <div id = "stadium_1_addr" class = "addr" >臺北市濱江街7號水門</div>
	<div class = "time">開放時間 : none</div>
    <div class = "bus">搭乘公車286副線、棕16、72、33、222在『大佳國小站』下車，再由加油站旁的八號水門進入左轉，即可到達</div>
	<div class = "mrt">搭乘台北捷運棕線至「捷運松山機場站」和「捷運大直站」後下車轉乘計程車，告知經「塔悠路」前往「中油濱江大直橋加油加氣站」旁的水門進入即為球場</div>
</div><!--div detail-->
</li>

<li id="stadium_2" class="stadium" style="display:none;" name="百齡運動公園" value="2">
        <div id="stadium_id" style="display:none;">2</div>
        <div id="stadium_2_type" style="display:none;">0</div>
        <div class="name" id = "stadium_2_name" onMouseOver = "showStadiumDetail(2);" onMouseOut = "hideStadiumDetail(2);"><a href='#' onClick="stadiumNameClicked(this);" name="2">百齡運動公園</a></div>

		 <div class = "sport" id = "stadium_2_sport">籃球 網球 足球 壘球 橄欖球 槌球 溜冰 </div>
	<div class = "sport" id = "stadium_2_sport2" style="display:none;"><a href="#" name="籃球" onClick="sportClicked(this);">籃球</a> <a href="#" name="網球" onClick="sportClicked(this);">網球</a> <a href="#" name="足球" onClick="sportClicked(this);">足球</a> <a href="#" name="壘球" onClick="sportClicked(this);">壘球</a> <a href="#" name="橄欖球" onClick="sportClicked(this);">橄欖球</a> <a href="#" name="槌球" onClick="sportClicked(this);">槌球</a> <a href="#" name="溜冰" onClick="sportClicked(this);">溜冰</a> </div>
	<div id = "stadium_2_long" style="display:none;">121.5127</div>
	<div id = "stadium_2_lati" style="display:none;">25.0873</div>	
    <div id = "detail_2" onMouseOver = "showStadiumDetail(2);" onMouseOut = "hideStadiumDetail(2);" style="display:none;">
    <div id = "stadium_2_addr" class = "addr" >臺北市士林區百齡橋下基隆河兩岸</div>
	<div class = "time">開放時間 : none</div>
    <div class = "bus">士林岸：  026、218、288，福港街站；指南客運----北門至淡大線，陽明高中站。 社子岸： 233、302、304、601、指南客運----北門至淡大線，葫東重慶路口站。 </div>
	<div class = "mrt">士林岸：北淡線(北投---南勢角)、新店線(淡水---新店)劍潭站。步行約20分鐘。</div>
</div><!--div detail-->
</li>

<li id="stadium_3" class="stadium" style="display:none;" name="美堤河濱慢壘場" value="3">
        <div id="stadium_id" style="display:none;">3</div>
        <div id="stadium_3_type" style="display:none;">0</div>
        <div class="name" id = "stadium_3_name" onMouseOver = "showStadiumDetail(3);" onMouseOut = "hideStadiumDetail(3);"><a href='#' onClick="stadiumNameClicked(this);" name="3">美堤河濱慢壘場</a></div>

		 <div class = "sport" id = "stadium_3_sport">壘球 籃球 </div>
	<div class = "sport" id = "stadium_3_sport2" style="display:none;"><a href="#" name="壘球" onClick="sportClicked(this);">壘球</a> <a href="#" name="籃球" onClick="sportClicked(this);">籃球</a> </div>
	<div id = "stadium_3_long" style="display:none;">121.5598</div>
	<div id = "stadium_3_lati" style="display:none;">25.0756</div>	
    <div id = "detail_3" onMouseOver = "showStadiumDetail(3);" onMouseOut = "hideStadiumDetail(3);" style="display:none;">
    <div id = "stadium_3_addr" class = "addr" >臺北市中山區基隆河基16號水門</div>
	<div class = "time">開放時間 : none</div>
    <div class = "bus">藍２６、紅２、紅３</div>
	<div class = "mrt">劍南站, 走路約10分鐘即可抵達</div>
</div><!--div detail-->
</li>

<li id="stadium_4" class="stadium" style="display:none;" name="彩虹河濱公園" value="4">
        <div id="stadium_id" style="display:none;">4</div>
        <div id="stadium_4_type" style="display:none;">0</div>
        <div class="name" id = "stadium_4_name" onMouseOver = "showStadiumDetail(4);" onMouseOut = "hideStadiumDetail(4);"><a href='#' onClick="stadiumNameClicked(this);" name="4">彩虹河濱公園</a></div>

		 <div class = "sport" id = "stadium_4_sport">壘球 網球 </div>
	<div class = "sport" id = "stadium_4_sport2" style="display:none;"><a href="#" name="壘球" onClick="sportClicked(this);">壘球</a> <a href="#" name="網球" onClick="sportClicked(this);">網球</a> </div>
	<div id = "stadium_4_long" style="display:none;">121.5721</div>
	<div id = "stadium_4_lati" style="display:none;">25.0595</div>	
    <div id = "detail_4" onMouseOver = "showStadiumDetail(4);" onMouseOut = "hideStadiumDetail(4);" style="display:none;">
    <div id = "stadium_4_addr" class = "addr" >臺北市內湖區堤頂大道一段</div>
	<div class = "time">開放時間 : none</div>
    <div class = "bus">63、204、207、518、621、小2、藍7、藍26、棕1路、紅25等路公車至舊宗路1段公車「新湖一路口」站或「新湖三路口」站，再往西步行約500公尺即可到達</div>
	<div class = "mrt">板南線坐到後山俾站，再搭計程車，下成美橋後就接近彩虹河濱公園的河堤口</div>
</div><!--div detail-->
</li>

<li id="stadium_5" class="stadium" style="display:none;" name="雙園河濱公園" value="5">
        <div id="stadium_id" style="display:none;">5</div>
        <div id="stadium_5_type" style="display:none;">0</div>
        <div class="name" id = "stadium_5_name" onMouseOver = "showStadiumDetail(5);" onMouseOut = "hideStadiumDetail(5);"><a href='#' onClick="stadiumNameClicked(this);" name="5">雙園河濱公園</a></div>

		 <div class = "sport" id = "stadium_5_sport">籃球 網球 壘球 溜冰 </div>
	<div class = "sport" id = "stadium_5_sport2" style="display:none;"><a href="#" name="籃球" onClick="sportClicked(this);">籃球</a> <a href="#" name="網球" onClick="sportClicked(this);">網球</a> <a href="#" name="壘球" onClick="sportClicked(this);">壘球</a> <a href="#" name="溜冰" onClick="sportClicked(this);">溜冰</a> </div>
	<div id = "stadium_5_long" style="display:none;">121.4881</div>
	<div id = "stadium_5_lati" style="display:none;">25.0349</div>	
    <div id = "detail_5" onMouseOver = "showStadiumDetail(5);" onMouseOut = "hideStadiumDetail(5);" style="display:none;">
    <div id = "stadium_5_addr" class = "addr" >臺北市長順街水門外</div>
	<div class = "time">開放時間 : none</div>
    <div class = "bus"></div>
	<div class = "mrt"></div>
</div><!--div detail-->
</li>

<li id="stadium_6" class="stadium" style="display:none;" name="青年公園棒球場" value="6">
        <div id="stadium_id" style="display:none;">6</div>
        <div id="stadium_6_type" style="display:none;">0</div>
        <div class="name" id = "stadium_6_name" onMouseOver = "showStadiumDetail(6);" onMouseOut = "hideStadiumDetail(6);"><a href='#' onClick="stadiumNameClicked(this);" name="6">青年公園棒球場</a></div>

		 <div class = "sport" id = "stadium_6_sport">棒球 壘球 </div>
	<div class = "sport" id = "stadium_6_sport2" style="display:none;"><a href="#" name="棒球" onClick="sportClicked(this);">棒球</a> <a href="#" name="壘球" onClick="sportClicked(this);">壘球</a> </div>
	<div id = "stadium_6_long" style="display:none;">121.503</div>
	<div id = "stadium_6_lati" style="display:none;">25.0228</div>	
    <div id = "detail_6" onMouseOver = "showStadiumDetail(6);" onMouseOut = "hideStadiumDetail(6);" style="display:none;">
    <div id = "stadium_6_addr" class = "addr" >臺北市萬華區水源路199號</div>
	<div class = "time">開放時間 : 08:00~17:00</div>
    <div class = "bus">青年路站：  12、藍29、205、212直達車、532、630 </div>
	<div class = "mrt"></div>
</div><!--div detail-->
</li>

<li id="stadium_7" class="stadium" style="display:none;" name="新生公園棒球場" value="7">
        <div id="stadium_id" style="display:none;">7</div>
        <div id="stadium_7_type" style="display:none;">0</div>
        <div class="name" id = "stadium_7_name" onMouseOver = "showStadiumDetail(7);" onMouseOut = "hideStadiumDetail(7);"><a href='#' onClick="stadiumNameClicked(this);" name="7">新生公園棒球場</a></div>

		 <div class = "sport" id = "stadium_7_sport">棒球 壘球 </div>
	<div class = "sport" id = "stadium_7_sport2" style="display:none;"><a href="#" name="棒球" onClick="sportClicked(this);">棒球</a> <a href="#" name="壘球" onClick="sportClicked(this);">壘球</a> </div>
	<div id = "stadium_7_long" style="display:none;">121.5315</div>
	<div id = "stadium_7_lati" style="display:none;">25.0691</div>	
    <div id = "detail_7" onMouseOver = "showStadiumDetail(7);" onMouseOut = "hideStadiumDetail(7);" style="display:none;">
    <div id = "stadium_7_addr" class = "addr" >臺北市中山區新生北路3段105號</div>
	<div class = "time">開放時間 : 08:00~18:00</div>
    <div class = "bus">美術館站： 40、42、213、247、287、220、224、310、216、217、218、259、260、301、308、277、203、279、21、291 </div>
	<div class = "mrt"></div>
</div><!--div detail-->
</li>

<li id="stadium_8" class="stadium" style="display:none;" name="臺北網球場" value="8">
        <div id="stadium_id" style="display:none;">8</div>
        <div id="stadium_8_type" style="display:none;">0</div>
        <div class="name" id = "stadium_8_name" onMouseOver = "showStadiumDetail(8);" onMouseOut = "hideStadiumDetail(8);"><a href='#' onClick="stadiumNameClicked(this);" name="8">臺北網球場</a></div>

		 <div class = "sport" id = "stadium_8_sport">網球 </div>
	<div class = "sport" id = "stadium_8_sport2" style="display:none;"><a href="#" name="網球" onClick="sportClicked(this);">網球</a> </div>
	<div id = "stadium_8_long" style="display:none;">121.552</div>
	<div id = "stadium_8_lati" style="display:none;">25.0512</div>	
    <div id = "detail_8" onMouseOver = "showStadiumDetail(8);" onMouseOut = "hideStadiumDetail(8);" style="display:none;">
    <div id = "stadium_8_addr" class = "addr" >臺北市松山區南京東路四段10號</div>
	<div class = "time">開放時間 : 6:00~22:00</div>
    <div class = "bus">北寧路口站：0 東左、46、69 、248 、266 正、279、282 正、605、282 副、289、306、307、311 綠、311 紅、622 南京、604 寧安街口站： 277、605快 臺北市體育處站：33、262、285、292、630、905、906、909 </div>
	<div class = "mrt">木柵線（中山國中 － 動物園），南京東路站 。步行約15分鐘。</div>
</div><!--div detail-->
</li>

<li id="stadium_9" class="stadium" style="display:none;" name="新生高架橋下籃球場" value="9">
        <div id="stadium_id" style="display:none;">9</div>
        <div id="stadium_9_type" style="display:none;">0</div>
        <div class="name" id = "stadium_9_name" onMouseOver = "showStadiumDetail(9);" onMouseOut = "hideStadiumDetail(9);"><a href='#' onClick="stadiumNameClicked(this);" name="9">新生高架橋下籃球場</a></div>

		 <div class = "sport" id = "stadium_9_sport">籃球 街舞 </div>
	<div class = "sport" id = "stadium_9_sport2" style="display:none;"><a href="#" name="籃球" onClick="sportClicked(this);">籃球</a> <a href="#" name="街舞" onClick="sportClicked(this);">街舞</a> </div>
	<div id = "stadium_9_long" style="display:none;">121.53</div>
	<div id = "stadium_9_lati" style="display:none;">25.0438</div>	
    <div id = "detail_9" onMouseOver = "showStadiumDetail(9);" onMouseOut = "hideStadiumDetail(9);" style="display:none;">
    <div id = "stadium_9_addr" class = "addr" >臺北市新生高架橋下(近八德路)</div>
	<div class = "time">開放時間 : none</div>
    <div class = "bus"></div>
	<div class = "mrt"></div>
</div><!--div detail-->
</li>

<li id="stadium_10" class="stadium" style="display:none;" name="臺北體育館" value="10">
        <div id="stadium_id" style="display:none;">10</div>
        <div id="stadium_10_type" style="display:none;">1</div>
        <div class="name" id = "stadium_10_name" onMouseOver = "showStadiumDetail(10);" onMouseOut = "hideStadiumDetail(10);"><a href='#' onClick="stadiumNameClicked(this);" name="10">臺北體育館</a></div>

		 <div class = "sport" id = "stadium_10_sport">籃球 羽球 桌球 </div>
	<div class = "sport" id = "stadium_10_sport2" style="display:none;"><a href="#" name="籃球" onClick="sportClicked(this);">籃球</a> <a href="#" name="羽球" onClick="sportClicked(this);">羽球</a> <a href="#" name="桌球" onClick="sportClicked(this);">桌球</a> </div>
	<div id = "stadium_10_long" style="display:none;">121.552</div>
	<div id = "stadium_10_lati" style="display:none;">25.0512</div>	
    <div id = "detail_10" onMouseOver = "showStadiumDetail(10);" onMouseOut = "hideStadiumDetail(10);" style="display:none;">
    <div id = "stadium_10_addr" class = "addr" >臺北市松山區南京東路4段10號</div>
	<div class = "time">開放時間 : 6:00~22:00</div>
    <div class = "bus">北寧路口站： 0 東左、46、69 、248 、266 正、279、282 正、605、282 副、289、306、307、311 綠、311 紅、622 南京、604  
寧安街口站： 277、605快 臺北市體育處站： 33、262、285、292、630、905、906、909 </div>
	<div class = "mrt">木柵線（中山國中 － 動物園），南京東路站 。步行約15分鐘。</div>
</div><!--div detail-->
</li>

<li id="stadium_11" class="stadium" style="display:none;" name="臺北田徑場" value="11">
        <div id="stadium_id" style="display:none;">11</div>
        <div id="stadium_11_type" style="display:none;">0</div>
        <div class="name" id = "stadium_11_name" onMouseOver = "showStadiumDetail(11);" onMouseOut = "hideStadiumDetail(11);"><a href='#' onClick="stadiumNameClicked(this);" name="11">臺北田徑場</a></div>

		 <div class = "sport" id = "stadium_11_sport">健身中心 田徑 足球 鏈球 鐵餅 跳遠 三級跳遠 舞蹈教室 </div>
	<div class = "sport" id = "stadium_11_sport2" style="display:none;"><a href="#" name="健身中心" onClick="sportClicked(this);">健身中心</a> <a href="#" name="田徑" onClick="sportClicked(this);">田徑</a> <a href="#" name="足球" onClick="sportClicked(this);">足球</a> <a href="#" name="鏈球" onClick="sportClicked(this);">鏈球</a> <a href="#" name="鐵餅" onClick="sportClicked(this);">鐵餅</a> <a href="#" name="跳遠" onClick="sportClicked(this);">跳遠</a> <a href="#" name="三級跳遠" onClick="sportClicked(this);">三級跳遠</a> <a href="#" name="舞蹈教室" onClick="sportClicked(this);">舞蹈教室</a> </div>
	<div id = "stadium_11_long" style="display:none;">121.5493</div>
	<div id = "stadium_11_lati" style="display:none;">25.0511</div>	
    <div id = "detail_11" onMouseOver = "showStadiumDetail(11);" onMouseOut = "hideStadiumDetail(11);" style="display:none;">
    <div id = "stadium_11_addr" class = "addr" >臺北市松山區敦化北路5號</div>
	<div class = "time">開放時間 : 5:00~22:00</div>
    <div class = "bus">臺北市體育處站： 33 、 262 、 285 、 292 、 630 、 905 、 906 及 909 八德路及敦化路口站： 275 、 275 副、 275 區 </div>
	<div class = "mrt">木柵線（中山國中─動物園），南京東路站。步行約 15 分鐘。</div>
</div><!--div detail-->
</li>

<li id="stadium_12" class="stadium" style="display:none;" name="中山運動中心" value="12">
        <div id="stadium_id" style="display:none;">12</div>
        <div id="stadium_12_type" style="display:none;">1</div>
        <div class="name" id = "stadium_12_name" onMouseOver = "showStadiumDetail(12);" onMouseOut = "hideStadiumDetail(12);"><a href='#' onClick="stadiumNameClicked(this);" name="12">中山運動中心</a></div>

		 <div class = "sport" id = "stadium_12_sport">游泳 體適能 韻律教室 籃球 羽球 桌球 </div>
	<div class = "sport" id = "stadium_12_sport2" style="display:none;"><a href="#" name="游泳" onClick="sportClicked(this);">游泳</a> <a href="#" name="體適能" onClick="sportClicked(this);">體適能</a> <a href="#" name="韻律教室" onClick="sportClicked(this);">韻律教室</a> <a href="#" name="籃球" onClick="sportClicked(this);">籃球</a> <a href="#" name="羽球" onClick="sportClicked(this);">羽球</a> <a href="#" name="桌球" onClick="sportClicked(this);">桌球</a> </div>
	<div id = "stadium_12_long" style="display:none;">121.5215</div>
	<div id = "stadium_12_lati" style="display:none;">25.0549</div>	
    <div id = "detail_12" onMouseOver = "showStadiumDetail(12);" onMouseOut = "hideStadiumDetail(12);" style="display:none;">
    <div id = "stadium_12_addr" class = "addr" >臺北市中山區中山北路2段44巷2號</div>
	<div class = "time">開放時間 : 06:00~22:00</div>
    <div class = "bus">路線１：218、220、221、227、247、260、261、287、310、636、659在「國賓飯店站」下車，步行五分鐘。路線２：紅33、42、46、226、290、518、811、820在「馬階醫院站」下車，步行五分鐘即達。路線３：棕九、紅25、26、52、266、282、288、292、306、539、605 在捷運中山站下車，步行五分鐘即達</div>
	<div class = "mrt">路線１：淡水線「雙連站」下車後沿捷運地下街，往中山站方向經地下書街，至R9出口即可到達，步行約3分鐘 路線２：淡水線「中山站」下車後沿捷運地下街，往雙連站方向經地下書街，至R9出口即可到達，步行約5分鐘</div>
</div><!--div detail-->
</li>

<li id="stadium_13" class="stadium" style="display:none;" name="北投運動中心" value="13">
        <div id="stadium_id" style="display:none;">13</div>
        <div id="stadium_13_type" style="display:none;">1</div>
        <div class="name" id = "stadium_13_name" onMouseOver = "showStadiumDetail(13);" onMouseOut = "hideStadiumDetail(13);"><a href='#' onClick="stadiumNameClicked(this);" name="13">北投運動中心</a></div>

		 <div class = "sport" id = "stadium_13_sport">游泳 兒童遊戲池 SPA 男女烤箱 男女蒸汽室 棋藝 攀岩 壁球 室內跑道 桌球 網球 旱地直排輪 瑜珈 </div>
	<div class = "sport" id = "stadium_13_sport2" style="display:none;"><a href="#" name="游泳" onClick="sportClicked(this);">游泳</a> <a href="#" name="兒童遊戲池" onClick="sportClicked(this);">兒童遊戲池</a> <a href="#" name="SPA" onClick="sportClicked(this);">SPA</a> <a href="#" name="男女烤箱" onClick="sportClicked(this);">男女烤箱</a> <a href="#" name="男女蒸汽室" onClick="sportClicked(this);">男女蒸汽室</a> <a href="#" name="棋藝" onClick="sportClicked(this);">棋藝</a> <a href="#" name="攀岩" onClick="sportClicked(this);">攀岩</a> <a href="#" name="壁球" onClick="sportClicked(this);">壁球</a> <a href="#" name="室內跑道" onClick="sportClicked(this);">室內跑道</a> <a href="#" name="桌球" onClick="sportClicked(this);">桌球</a> <a href="#" name="網球" onClick="sportClicked(this);">網球</a> <a href="#" name="旱地直排輪" onClick="sportClicked(this);">旱地直排輪</a> <a href="#" name="瑜珈" onClick="sportClicked(this);">瑜珈</a> </div>
	<div id = "stadium_13_long" style="display:none;">121.5097</div>
	<div id = "stadium_13_lati" style="display:none;">25.1164</div>	
    <div id = "detail_13" onMouseOver = "showStadiumDetail(13);" onMouseOut = "hideStadiumDetail(13);" style="display:none;">
    <div id = "stadium_13_addr" class = "addr" >臺北市北投區石牌路1段39巷100號</div>
	<div class = "time">開放時間 : 06:00~22:00</div>
    <div class = "bus">石牌國小站─224、277、288、601、博愛公車石牌國中站─290、216、216(副)、216(區間車)、223石牌路站─224、277、288、601、博愛公車捷運石牌站─216、216(副)、216(區間車)、223、288、508、508(黃線)、535、536、645、665、紅12、紅15、紅19、景美女中~榮總快速公車</div>
	<div class = "mrt">自石牌站出口往承德路方向沿石牌路一段行走，至致遠二路右轉，看到天使書坊左轉進入實踐街，直行看到全家後右轉直走即可到達</div>
</div><!--div detail-->
</li>

<li id="stadium_14" class="stadium" style="display:none;" name="中正運動中心" value="14">
        <div id="stadium_id" style="display:none;">14</div>
        <div id="stadium_14_type" style="display:none;">11</div>
        <div class="name" id = "stadium_14_name" onMouseOver = "showStadiumDetail(14);" onMouseOut = "hideStadiumDetail(14);"><a href='#' onClick="stadiumNameClicked(this);" name="14">中正運動中心</a></div>

		 <div class = "sport" id = "stadium_14_sport">游泳 體適能 健身中心 武術 籃球 羽球 桌球 舞蹈 高爾夫球 射擊 羽球 射箭 </div>
	<div class = "sport" id = "stadium_14_sport2" style="display:none;"><a href="#" name="游泳" onClick="sportClicked(this);">游泳</a> <a href="#" name="體適能" onClick="sportClicked(this);">體適能</a> <a href="#" name="健身中心" onClick="sportClicked(this);">健身中心</a> <a href="#" name="武術" onClick="sportClicked(this);">武術</a> <a href="#" name="籃球" onClick="sportClicked(this);">籃球</a> <a href="#" name="羽球" onClick="sportClicked(this);">羽球</a> <a href="#" name="桌球" onClick="sportClicked(this);">桌球</a> <a href="#" name="舞蹈" onClick="sportClicked(this);">舞蹈</a> <a href="#" name="高爾夫球" onClick="sportClicked(this);">高爾夫球</a> <a href="#" name="射擊" onClick="sportClicked(this);">射擊</a> <a href="#" name="羽球" onClick="sportClicked(this);">羽球</a> <a href="#" name="射箭" onClick="sportClicked(this);">射箭</a> </div>
	<div id = "stadium_14_long" style="display:none;">121.5189</div>
	<div id = "stadium_14_lati" style="display:none;">25.0385</div>	
    <div id = "detail_14" onMouseOver = "showStadiumDetail(14);" onMouseOut = "hideStadiumDetail(14);" style="display:none;">
    <div id = "stadium_14_addr" class = "addr" >臺北市中正區信義路1段1號</div>
	<div class = "time">開放時間 : 06:00~22:00</div>
    <div class = "bus">信義林森路口站－ 0東、3、20、22、38、204、670、信義幹線、信義新幹線、1503。仁愛中山路口站－ 37、245、249、261、263、270、621、630、651 。台大醫院站－中山幹線、2、15、22、208、222、227、261捷運台大醫院站 (出口2、3)－2、5、18、20、222、224、236、295、604、648、849、信義幹線、信義新幹線、1717、2020捷運中正紀念堂站 (出口 4、5、6 )－0東、15、18、227、644、648、849、中山幹線、208、236、251、252。景福門－15、208。</div>
	<div class = "mrt">台大醫院站－自台大醫院站 2號出口往台大醫院新院區方向沿仁愛路一段行走，東門圓環人行穿越道即可看見。中正紀念堂站－自中正紀念堂站 4、5 號出口往國家戲劇院方向穿越中正紀念堂前廣場步走約3分鐘，至東門圓環人行穿越道即可進入。 </div>
</div><!--div detail-->
</li>

<li id="stadium_15" class="stadium" style="display:none;" name="南港運動中心" value="15">
        <div id="stadium_id" style="display:none;">15</div>
        <div id="stadium_15_type" style="display:none;">1</div>
        <div class="name" id = "stadium_15_name" onMouseOver = "showStadiumDetail(15);" onMouseOut = "hideStadiumDetail(15);"><a href='#' onClick="stadiumNameClicked(this);" name="15">南港運動中心</a></div>

		 <div class = "sport" id = "stadium_15_sport">游泳 棋奕 健身中心 舞蹈教室 籃球 羽球 桌球 高爾夫球 射擊教室 射箭 </div>
	<div class = "sport" id = "stadium_15_sport2" style="display:none;"><a href="#" name="游泳" onClick="sportClicked(this);">游泳</a> <a href="#" name="棋奕" onClick="sportClicked(this);">棋奕</a> <a href="#" name="健身中心" onClick="sportClicked(this);">健身中心</a> <a href="#" name="舞蹈教室" onClick="sportClicked(this);">舞蹈教室</a> <a href="#" name="籃球" onClick="sportClicked(this);">籃球</a> <a href="#" name="羽球" onClick="sportClicked(this);">羽球</a> <a href="#" name="桌球" onClick="sportClicked(this);">桌球</a> <a href="#" name="高爾夫球" onClick="sportClicked(this);">高爾夫球</a> <a href="#" name="射擊教室" onClick="sportClicked(this);">射擊教室</a> <a href="#" name="射箭" onClick="sportClicked(this);">射箭</a> </div>
	<div id = "stadium_15_long" style="display:none;">121.5816</div>
	<div id = "stadium_15_lati" style="display:none;">25.049</div>	
    <div id = "detail_15" onMouseOver = "showStadiumDetail(15);" onMouseOut = "hideStadiumDetail(15);" style="display:none;">
    <div id = "stadium_15_addr" class = "addr" >臺北市南港區玉成街69號</div>
	<div class = "time">開放時間 : 06:00~22:00</div>
    <div class = "bus">路線1.玉成裡站：203、204、205、256、276、28、306、518、53、531、605、622、629、63、668、678、711、棕1、藍7。路線2.忠孝醫院站：212、212直、207、240、257、261、270、279、281、284、284直、32、51、信義新幹線、忠孝新幹線。路線3.後山埤站：207、212、212直、240、261、270、279、281、 279、281、284、284直、32、51、信義新幹線、忠孝新幹線。</div>
	<div class = "mrt">捷運／板南線後山埤站4號出口（忠孝東路六段9巷），直走接玉成街左轉後步行約5分鐘即可到達。火車／松山火車站向東，步行約200公尺。</div>
</div><!--div detail-->
</li>

<li id="stadium_16" class="stadium" style="display:none;" name="萬華運動中心" value="16">
        <div id="stadium_id" style="display:none;">16</div>
        <div id="stadium_16_type" style="display:none;">1</div>
        <div class="name" id = "stadium_16_name" onMouseOver = "showStadiumDetail(16);" onMouseOut = "hideStadiumDetail(16);"><a href='#' onClick="stadiumNameClicked(this);" name="16">萬華運動中心</a></div>

		 <div class = "sport" id = "stadium_16_sport">游泳 SPA 健身中心 舞蹈教室 高爾夫球 桌球 攀岩 射箭 漆彈 </div>
	<div class = "sport" id = "stadium_16_sport2" style="display:none;"><a href="#" name="游泳" onClick="sportClicked(this);">游泳</a> <a href="#" name="SPA" onClick="sportClicked(this);">SPA</a> <a href="#" name="健身中心" onClick="sportClicked(this);">健身中心</a> <a href="#" name="舞蹈教室" onClick="sportClicked(this);">舞蹈教室</a> <a href="#" name="高爾夫球" onClick="sportClicked(this);">高爾夫球</a> <a href="#" name="桌球" onClick="sportClicked(this);">桌球</a> <a href="#" name="攀岩" onClick="sportClicked(this);">攀岩</a> <a href="#" name="射箭" onClick="sportClicked(this);">射箭</a> <a href="#" name="漆彈" onClick="sportClicked(this);">漆彈</a> </div>
	<div id = "stadium_16_long" style="display:none;">121.5069</div>
	<div id = "stadium_16_lati" style="display:none;">25.047</div>	
    <div id = "detail_16" onMouseOver = "showStadiumDetail(16);" onMouseOut = "hideStadiumDetail(16);" style="display:none;">
    <div id = "stadium_16_addr" class = "addr" >臺北市萬華區西寧南路6之1號</div>
	<div class = "time">開放時間 : 06:00~22:00</div>
    <div class = "bus">中華路北站：202、202(區間車)、205、206、212(夜、直達車)、218、218(直達車)、221、223、231、232、234、246、249、250、252、253、260、262、262(區間車)、265、65(經中央路、經明德路、夜、區間車)、302、304(承德線)、307、310、49、527、601、604、635、635(副)、637、640、658、659、660、662、667、670、701、702、705、06、藍29 </div>
	<div class = "mrt">西門站6號出口，沿峨嵋街右轉西寧南路方向，步行約10分鐘即可到達本中心</div>
</div><!--div detail-->
</li>

<li id="stadium_17" class="stadium" style="display:none;" name="士林運動中心" value="17">
        <div id="stadium_id" style="display:none;">17</div>
        <div id="stadium_17_type" style="display:none;">1</div>
        <div class="name" id = "stadium_17_name" onMouseOver = "showStadiumDetail(17);" onMouseOut = "hideStadiumDetail(17);"><a href='#' onClick="stadiumNameClicked(this);" name="17">士林運動中心</a></div>

		 <div class = "sport" id = "stadium_17_sport">室內高爾夫球 舞蹈教室 健身中心 游泳 SPA 籃球 攀岩 射箭 </div>
	<div class = "sport" id = "stadium_17_sport2" style="display:none;"><a href="#" name="室內高爾夫球" onClick="sportClicked(this);">室內高爾夫球</a> <a href="#" name="舞蹈教室" onClick="sportClicked(this);">舞蹈教室</a> <a href="#" name="健身中心" onClick="sportClicked(this);">健身中心</a> <a href="#" name="游泳" onClick="sportClicked(this);">游泳</a> <a href="#" name="SPA" onClick="sportClicked(this);">SPA</a> <a href="#" name="籃球" onClick="sportClicked(this);">籃球</a> <a href="#" name="攀岩" onClick="sportClicked(this);">攀岩</a> <a href="#" name="射箭" onClick="sportClicked(this);">射箭</a> </div>
	<div id = "stadium_17_long" style="display:none;">121.5214</div>
	<div id = "stadium_17_lati" style="display:none;">25.0896</div>	
    <div id = "detail_17" onMouseOver = "showStadiumDetail(17);" onMouseOut = "hideStadiumDetail(17);" style="display:none;">
    <div id = "stadium_17_addr" class = "addr" >臺北市士林區士商路一號</div>
	<div class = "time">開放時間 : 06:00~22:00</div>
    <div class = "bus">士林市場 303,41,529,68,815,紅9大南路口 218直,26,288,618,68,816,紅9,淡大到北門（客運）公教住宅 303,41,529,紅9福港街 218直,26,288,618,68,816,紅9前港公園 40,529捷運劍潭站 111,203,216,218,224,250,266,277,280,290,303,304,308,310,41,529,606,616,618,665,68陽明戲院 小15,小16,小17,小18,小19</div>
	<div class = "mrt">劍潭站－淡水線or北投線至劍潭站1號出口，右轉沿基河路直行至大南路交叉口左轉，步行約10分鐘(承德路與士商路口)，即可看到士林運動中心。</div>
</div><!--div detail-->
</li>

<li id="stadium_18" class="stadium" style="display:none;" name="內湖運動中心" value="18">
        <div id="stadium_id" style="display:none;">18</div>
        <div id="stadium_18_type" style="display:none;">1</div>
        <div class="name" id = "stadium_18_name" onMouseOver = "showStadiumDetail(18);" onMouseOut = "hideStadiumDetail(18);"><a href='#' onClick="stadiumNameClicked(this);" name="18">內湖運動中心</a></div>

		 <div class = "sport" id = "stadium_18_sport">舞蹈教室 游泳 SPA 籃球 室內迷你高爾夫 羽球 壁球 射箭 </div>
	<div class = "sport" id = "stadium_18_sport2" style="display:none;"><a href="#" name="舞蹈教室" onClick="sportClicked(this);">舞蹈教室</a> <a href="#" name="游泳" onClick="sportClicked(this);">游泳</a> <a href="#" name="SPA" onClick="sportClicked(this);">SPA</a> <a href="#" name="籃球" onClick="sportClicked(this);">籃球</a> <a href="#" name="室內迷你高爾夫" onClick="sportClicked(this);">室內迷你高爾夫</a> <a href="#" name="羽球" onClick="sportClicked(this);">羽球</a> <a href="#" name="壁球" onClick="sportClicked(this);">壁球</a> <a href="#" name="射箭" onClick="sportClicked(this);">射箭</a> </div>
	<div id = "stadium_18_long" style="display:none;">121.5751</div>
	<div id = "stadium_18_lati" style="display:none;">25.0782</div>	
    <div id = "detail_18" onMouseOver = "showStadiumDetail(18);" onMouseOut = "hideStadiumDetail(18);" style="display:none;">
    <div id = "stadium_18_addr" class = "addr" >臺北市內湖區洲子街12號</div>
	<div class = "time">開放時間 : 06:00~22:00</div>
    <div class = "bus">內湖高工－110、21、21直達車、222、222(區間車)、247、247(區間車)、267、28、286。麗山國中－藍26。台北花市－0東、222、222(區間車)、240(直達)、27、646、646(區間車)、652、紅3、棕16。台北花市－286(副)、645、902、902(區間車)、內科通勤專車-木柵線、紅31、紅3直達車、通勤專車。</div>
	<div class = "mrt">捷運文湖線，港墘捷運站 2號出口沿內湖高工至洲子街</div>
</div><!--div detail-->
</li>

<li id="stadium_19" class="stadium" style="display:none;" name="信義運動中心" value="19">
        <div id="stadium_id" style="display:none;">19</div>
        <div id="stadium_19_type" style="display:none;">1</div>
        <div class="name" id = "stadium_19_name" onMouseOver = "showStadiumDetail(19);" onMouseOut = "hideStadiumDetail(19);"><a href='#' onClick="stadiumNameClicked(this);" name="19">信義運動中心</a></div>

		 <div class = "sport" id = "stadium_19_sport">棋奕 健身中心 舞蹈教室 桌球 壁球 高爾夫球 游泳 SPA 攀岩 射箭 </div>
	<div class = "sport" id = "stadium_19_sport2" style="display:none;"><a href="#" name="棋奕" onClick="sportClicked(this);">棋奕</a> <a href="#" name="健身中心" onClick="sportClicked(this);">健身中心</a> <a href="#" name="舞蹈教室" onClick="sportClicked(this);">舞蹈教室</a> <a href="#" name="桌球" onClick="sportClicked(this);">桌球</a> <a href="#" name="壁球" onClick="sportClicked(this);">壁球</a> <a href="#" name="高爾夫球" onClick="sportClicked(this);">高爾夫球</a> <a href="#" name="游泳" onClick="sportClicked(this);">游泳</a> <a href="#" name="SPA" onClick="sportClicked(this);">SPA</a> <a href="#" name="攀岩" onClick="sportClicked(this);">攀岩</a> <a href="#" name="射箭" onClick="sportClicked(this);">射箭</a> </div>
	<div id = "stadium_19_long" style="display:none;">121.5666</div>
	<div id = "stadium_19_lati" style="display:none;">25.0318</div>	
    <div id = "detail_19" onMouseOver = "showStadiumDetail(19);" onMouseOut = "hideStadiumDetail(19);" style="display:none;">
    <div id = "stadium_19_addr" class = "addr" >臺北市信義區松勤街100號</div>
	<div class = "time">開放時間 : 06:00~22:00</div>
    <div class = "bus">路線A:搭乘信義幹線在信義區行政中心(信義路)下車，右轉松仁路再右轉松勤街即達。路線B:搭乘公車20、32、46、202、207、266、612、621、650、665、669、藍10於信義區行政中心(信義路)下車，右轉松仁路再右轉松勤街即達。路線C:搭乘公車37、1、5於莊敬路松勤街口下車，直走松勤街即可到達。</div>
	<div class = "mrt">搭乘捷運板南線(藍線)至台北市政府站，沿松仁路直行10~15分鐘，過信義路五段後右轉松勤街即可到達。台北市政府捷運站轉乘公車266或32往吳興街方向，於信義國中站下即可到達。</div>
</div><!--div detail-->
</li>

<li id="stadium_20" class="stadium" style="display:none;" name="松山運動中心" value="20">
        <div id="stadium_id" style="display:none;">20</div>
        <div id="stadium_20_type" style="display:none;">1</div>
        <div class="name" id = "stadium_20_name" onMouseOver = "showStadiumDetail(20);" onMouseOut = "hideStadiumDetail(20);"><a href='#' onClick="stadiumNameClicked(this);" name="20">松山運動中心</a></div>

		 <div class = "sport" id = "stadium_20_sport">健身中心 游泳 舞蹈教室 </div>
	<div class = "sport" id = "stadium_20_sport2" style="display:none;"><a href="#" name="健身中心" onClick="sportClicked(this);">健身中心</a> <a href="#" name="游泳" onClick="sportClicked(this);">游泳</a> <a href="#" name="舞蹈教室" onClick="sportClicked(this);">舞蹈教室</a> </div>
	<div id = "stadium_20_long" style="display:none;">121.5505</div>
	<div id = "stadium_20_lati" style="display:none;">25.0484</div>	
    <div id = "detail_20" onMouseOver = "showStadiumDetail(20);" onMouseOut = "hideStadiumDetail(20);" style="display:none;">
    <div id = "stadium_20_addr" class = "addr" >臺北市敦化北路1號</div>
	<div class = "time">開放時間 : 06:00~22:00</div>
    <div class = "bus">八德敦化路口站 ：262、275、285、292、33、521、630、902、905、906、909、敦化幹線     台視站： 0東、202、203、205、257、276</div>
	<div class = "mrt">捷運南京東路站 ：由南京東路站出口，往小巨蛋方向沿南京東路三段行走，至敦化北路右轉。步行約15分鐘。 捷運忠孝敦化站 ： 由忠孝敦化8號出口，沿敦化南路直走至敦化北路與八德路交叉口後即可抵達本中心。步行時間約為20分鐘。</div>
</div><!--div detail-->
</li>

<li id="stadium_21" class="stadium" style="display:none;" name="大同運動中心" value="21">
        <div id="stadium_id" style="display:none;">21</div>
        <div id="stadium_21_type" style="display:none;">1</div>
        <div class="name" id = "stadium_21_name" onMouseOver = "showStadiumDetail(21);" onMouseOut = "hideStadiumDetail(21);"><a href='#' onClick="stadiumNameClicked(this);" name="21">大同運動中心</a></div>

		 <div class = "sport" id = "stadium_21_sport">羽球 溜冰 有氧舞蹈 技擊 健身中心 桌球 游泳 </div>
	<div class = "sport" id = "stadium_21_sport2" style="display:none;"><a href="#" name="羽球" onClick="sportClicked(this);">羽球</a> <a href="#" name="溜冰" onClick="sportClicked(this);">溜冰</a> <a href="#" name="有氧舞蹈" onClick="sportClicked(this);">有氧舞蹈</a> <a href="#" name="技擊" onClick="sportClicked(this);">技擊</a> <a href="#" name="健身中心" onClick="sportClicked(this);">健身中心</a> <a href="#" name="桌球" onClick="sportClicked(this);">桌球</a> <a href="#" name="游泳" onClick="sportClicked(this);">游泳</a> </div>
	<div id = "stadium_21_long" style="display:none;">121.5163</div>
	<div id = "stadium_21_lati" style="display:none;">25.0646</div>	
    <div id = "detail_21" onMouseOver = "showStadiumDetail(21);" onMouseOut = "hideStadiumDetail(21);" style="display:none;">
    <div id = "stadium_21_addr" class = "addr" >臺北市大龍街51號</div>
	<div class = "time">開放時間 : 06:00~22:00</div>
    <div class = "bus">承德路方向在【大同國小】下車21 26 266 280 290 304 306 616 618 636 639 811 松江幹線民權西路方向在【民權大龍街口】下車41 111 211 225 226 227 261 292 306 520 539 616 617 618 636 638 639 801 803重慶北路方向則在【昌吉重慶路口】下車2 9 21 41 215 223 250 255 274 288 302 304 306 601 636 639 641 704 重慶幹線民族西路方向則在【蘭州國中】下車2 9 21 215 246 306 636 639</div>
	<div class = "mrt">淡水線「民權西路站」，出車站後沿民權西路朝西，往承德路方向前進，於大龍街口右轉，約五百公尺，步行約七分鐘即可到達由南京東路站出口，往小巨蛋方向沿南京東路三段行走，至敦化北路右轉。步行約15分鐘。捷運忠孝敦化站 ：由忠孝敦化8號出口，沿敦化南路直走至敦化北路與八德路交叉口後即可抵達本中心。步行時間約為20分鐘。</div>
</div><!--div detail-->
</li>

<li id="stadium_22" class="stadium" style="display:none;" name="大安運動中心" value="22">
        <div id="stadium_id" style="display:none;">22</div>
        <div id="stadium_22_type" style="display:none;">1</div>
        <div class="name" id = "stadium_22_name" onMouseOver = "showStadiumDetail(22);" onMouseOut = "hideStadiumDetail(22);"><a href='#' onClick="stadiumNameClicked(this);" name="22">大安運動中心</a></div>

		 <div class = "sport" id = "stadium_22_sport">游泳 舞蹈教室 武術教室 飛輪 桌球 健身中心 羽球 籃球 直排輪 攀岩 高爾夫球 壁球 撞球 </div>
	<div class = "sport" id = "stadium_22_sport2" style="display:none;"><a href="#" name="游泳" onClick="sportClicked(this);">游泳</a> <a href="#" name="舞蹈教室" onClick="sportClicked(this);">舞蹈教室</a> <a href="#" name="武術教室" onClick="sportClicked(this);">武術教室</a> <a href="#" name="飛輪" onClick="sportClicked(this);">飛輪</a> <a href="#" name="桌球" onClick="sportClicked(this);">桌球</a> <a href="#" name="健身中心" onClick="sportClicked(this);">健身中心</a> <a href="#" name="羽球" onClick="sportClicked(this);">羽球</a> <a href="#" name="籃球" onClick="sportClicked(this);">籃球</a> <a href="#" name="直排輪" onClick="sportClicked(this);">直排輪</a> <a href="#" name="攀岩" onClick="sportClicked(this);">攀岩</a> <a href="#" name="高爾夫球" onClick="sportClicked(this);">高爾夫球</a> <a href="#" name="壁球" onClick="sportClicked(this);">壁球</a> <a href="#" name="撞球" onClick="sportClicked(this);">撞球</a> </div>
	<div id = "stadium_22_long" style="display:none;">121.5456</div>
	<div id = "stadium_22_lati" style="display:none;">25.0207</div>	
    <div id = "detail_22" onMouseOver = "showStadiumDetail(22);" onMouseOut = "hideStadiumDetail(22);" style="display:none;">
    <div id = "stadium_22_addr" class = "addr" >臺北市辛亥路3段55號</div>
	<div class = "time">開放時間 : 06:00~22:00</div>
    <div class = "bus">路線A：237、295、298在大安運動中心下車即可直接到達。路線B：敦化幹線、新店基隆、1、207、275、294、611、650、672、905、906、909在和平高中(基隆路)下車，直走右轉辛亥路即達。路線C：和平幹線、3、15、18、72、52、211、235、284、662、663、685在國立台北教育大學(和平東路)下車，直走右轉敦南街，約步行12分鐘即達。</div>
	<div class = "mrt">木柵線(棕線)在科技大樓站下車，直行復興南路左轉辛亥路步行約12-15分鐘，或六張犁站下車，直行基隆路右轉辛亥路步行約12-15分鐘即達。</div>
</div><!--div detail-->
</li>

<li id="stadium_23" class="stadium" style="display:none;" name="文山運動中心" value="23">
        <div id="stadium_id" style="display:none;">23</div>
        <div id="stadium_23_type" style="display:none;">1</div>
        <div class="name" id = "stadium_23_name" onMouseOver = "showStadiumDetail(23);" onMouseOut = "hideStadiumDetail(23);"><a href='#' onClick="stadiumNameClicked(this);" name="23">文山運動中心</a></div>

		 <div class = "sport" id = "stadium_23_sport">游泳 健身中心 舞蹈教室 羽球 射擊 桌球 攀岩 </div>
	<div class = "sport" id = "stadium_23_sport2" style="display:none;"><a href="#" name="游泳" onClick="sportClicked(this);">游泳</a> <a href="#" name="健身中心" onClick="sportClicked(this);">健身中心</a> <a href="#" name="舞蹈教室" onClick="sportClicked(this);">舞蹈教室</a> <a href="#" name="羽球" onClick="sportClicked(this);">羽球</a> <a href="#" name="射擊" onClick="sportClicked(this);">射擊</a> <a href="#" name="桌球" onClick="sportClicked(this);">桌球</a> <a href="#" name="攀岩" onClick="sportClicked(this);">攀岩</a> </div>
	<div id = "stadium_23_long" style="display:none;">121.5596</div>
	<div id = "stadium_23_lati" style="display:none;">24.997</div>	
    <div id = "detail_23" onMouseOver = "showStadiumDetail(23);" onMouseOut = "hideStadiumDetail(23);" style="display:none;">
    <div id = "stadium_23_addr" class = "addr" >臺北市文山區興隆路3段222號</div>
	<div class = "time">開放時間 : 06:00~22:00</div>
    <div class = "bus">路線A:台北車站-公館-政大-動物園,搭乘236,237,676,530,606,671,0南,棕11。路線B:新店-景美-石碇 深坑,搭乘253,671,棕2,棕3,棕6,棕11,棕12,綠2。路線C:中永和,搭乘綠2。路線D:市政府,搭乘611。路線E:行天宮,搭乘298</div>
	<div class = "mrt">木柵線(棕線):至萬芳醫院站下車。板南線(藍線):至忠孝復興站轉搭乘木柵線至萬芳醫院站下車。淡水-新店線:至台北火車站轉搭板南線至忠孝復興站轉搭乘木柵線至萬芳醫院站下車出站後往左邊走,步行約5-6分鐘即可到達</div>
</div><!--div detail-->
</li>

  
</ul>
</div>


<div id="outdoor_weight">0.96078943915232</div>

<script>
function showStadiumBySport(sportName) {
	for (i = 0; i<24 ; i++ ) {
		$('#stadium_'+i).css('display','none');
	}
	for (i = 0; i < 24; i++) {
		names = $('#stadium_'+i).attr('name');
		//var name = new Array();
		//name = $('#stadium_'+i).attr('name').split(',');
			//alert(name.length);
		for (j = 0; j < names.split(' ').length; j++) {
			name = names.split(' ')[j];
			//console.log(name+"=="+sportName);
			if (name == sportName) {
				//alert(name[j]);
				$('#stadium_'+i).css('display','block');
				break;
			}
		}
	}
}
function sport_selectSport(a) {
	g_sport_name = $(a).attr('name');
	//alert(g_sport_name);
	showStadiumBySport(g_sport_name);

}
function sport_selectStadium(a) {
	g_stadium_name = $(a).attr('name');
	if ( window.confirm("運動名名稱:"+g_sport_name+"\n"+"運動場館:"+g_stadium_name) ) {
		window.location = "https://www.csie.ntu.edu.tw/~r00944002/fujie/TaipeiSportStar/routine.php?action=create&sport="+g_sport_name+"&location="+g_stadium_name+"";
	}
	else {
	};
}
//getAllStadiumNames();

//shuffleDivs();
</script>


</body>
</html>
