var MAX_PAGE_LINES = 50;
var CURRENT_PAGE = 1; 

/* ********************************************************* */
/* DATA class : variables and methods for handling the input
 * text, parsing error data and adding markup
/* ********************************************************* */
var DATA = { 
    plainText:          '', 
    xmlErrors:          '',
    arrLineObjs:        [],
    arrMarkedLines:     ['Run check first.'],
    getTotalPages:
        function (){
            return Math.ceil(this.arrMarkedLines.length / MAX_PAGE_LINES);
        },
    getPageText:
        function (pageNumber){
            CURRENT_PAGE = pageNumber;
            var pageText = "";
            var position = (pageNumber - 1) * MAX_PAGE_LINES;

            for(var i = position; i < this.arrMarkedLines.length && i < position + MAX_PAGE_LINES; i++){
                pageText += this.arrMarkedLines[i];
            }

            return pageText;
        }
};  

function clearText(){
    $('#inputText').val('');
}

function showTab(viewId, text){

//console.log('text: ' + text);
	
    $('#tabs a[href="#'+ viewId +'"]').tab('show');
    
    if(viewId == 'markupTab'){
        
        if (typeof text === 'undefined'){  
            $('#textMarked').html(DATA.textMarked[0]);
        }else{
            if(DATA.getTotalPages() >= 1){
                $('#textMarked').html(text);
                addHighlighting();
                $('#pageXofY').text('Page ' + CURRENT_PAGE + ' of ' + DATA.getTotalPages());
                $('#pageXofY').css('display','inline');
                $('#pagination').css('display','block');
            }
        }
    }else{
        $('#pagination').css('display','none');
    }
}

function addHighlighting(){

    //Loop through the char objects and add one mousemove event per character
    for(var i = 0; i < DATA.arrLineObjs.length; i++){   
    	
    	//console.log("LINE: " + DATA.arrLineObjs[i].toString());
    	
        for(var j = 0; j < DATA.arrLineObjs[i].length; j++){
  
           //For each character grab the error ids, fetch the messages for those errors. 
           var c = DATA.arrLineObjs[i][j];
           
           //console.log("LINE CHAR: " + i + " " + c.errors + " " + c.messages);
           
           if(c.errors.length >= 1){

                var elem_id = '#' + i + '_' + j;
                $(elem_id).addClass('error-highlight');
                var tooltip = $("<div id='tip_" + i + "_" + j + "' data-error-ids='" + c.errorIds.toString()  
                                    + "' class='tip'>" + c.messages + "</div>");
                tooltip.css('display','none');
                tooltip.css('position','absolute');
                tooltip.appendTo($("#textMarked"));

                $(elem_id).mousemove( { err_id: i + "_" + j, thisErrorIds: c.errorIds.toString()  } , function(event) { 

                    var thisTipId = 'tip_' + event.data.err_id; 
                    var thisTip = '#' + thisTipId;

                    //Hide the other tooltips, BUT don't hide the tooltip at the current position.
                    $('.tip').each(
                        function(index, value){
                            if($(this).attr('data-error-ids') != event.data.thisErrorIds){
                                $(this).fadeOut(200);
                            }
                        }
                    );
                    
                    //If there is not a tip shown already for this error/position, show the relevant tooltip
                    var visibleTips = $(".tip:visible[data-error-ids*='" + event.data.thisErrorIds + "']");
                    if(visibleTips.length <= 0) {
                        $(thisTip).css('left', $(this).position().left );
                        $(thisTip).css('top', $(this).position().top + $(this).height() );
                        $(thisTip).fadeIn(400);
                        setTimeout( function(){ $(thisTip).fadeOut(1000); }, 3000 );
                    }
                });
            }
        }
    }
    
    $("#loadingDiv").fadeOut(500);
}

function getLanguageCode(){
	
	var langCode = '';
	var langText = $('#dropDownLang').text();
	if(	langText.indexOf('English (US)') >= 0 ) langCode = 'en-US';
	if(	langText.indexOf('English (GB)') >= 0 ) langCode = 'en-GB';
	return langCode;
	
}

function doTextCheck(){
    DATA.plainText = $( "#inputText" ).val();
    $("#loadingDiv").show();
    doAjaxRequest('POST', '/' + getLanguageCode() + '/checktext', 'text=' + encodeURIComponent( DATA.plainText ),
        function(response){
            /* 
            new Transformation().setXml(xmlString).setXslt("grammar_errors.xsl").transform("xslOutput");
            */
    		var json = JSON.parse(response);
    		DATA.arrMarkedLines = json.htmlLines;
    		
for(var i =0; i< json.htmlLines.length; i++){
	console.log("LINE: " + json.htmlLines[i]);
}
    		
    		DATA.arrLineObjs = json.textData;
    		showTab( 'markupTab', DATA.getPageText(1) );
        }
    );
}

function searchWordInDictionary(){
	 var word = $( "#inputTextDictSearchWord" ).val();
     var lemma = $( "#inputTextDictSearchLemma" ).val();
     var postag = $( "#inputTextDictSearchPosTag" ).val();
     if($.trim(word).length <= 0
    		 || $.trim(lemma).length <= 0
    		 || $.trim(postag).length <= 0)
    	 return;
     var content = word + "." + lemma + "." + postag;
     doAjaxRequest('GET', '/dictionary', {request : "search", line : content},
         function(response){
             $('#functionResult').text(response);
         }
     )
}

function addWordToDictionary(){
	 var word = $( "#inputTextDictAddWord" ).val();
     var lemma = $( "#inputTextDictAddLemma" ).val();
     var postag = $( "#inputTextDictAddPosTag" ).val();
     if($.trim(word).length <= 0
    		 || $.trim(lemma).length <= 0
    		 || $.trim(postag).length <= 0) 
    	 return;
     var content = word + "." + lemma + "." + postag; 
     doAjaxRequest('GET', '/dictionary', {request : "add", line : content},
         function(response){
             $('#functionResult').text(response);
         }
     )
}

function getWordInflections(){
	var lemma = $( "#inputTextDictGetInflections" ).val();
    if($.trim(lemma).length <= 0) return;
    doAjaxRequest('GET', '/dictionary', {request : "inflections", line : lemma},
        function(response){
        	var items = response.split(";");
        	var inflections = "<tr><th>Inflection</th><th>POS Tag</th></tr>";
        	for (x = 0; items.length > x; x++)
        		inflections += "<tr><td>" + items[x].split(".")[0] + "</td><td>" + items[x].split(".")[1] + "</td></tr>";
            $('#functionResult').html(inflections);
        }
    )
}

function buildDictionary(){
    doAjaxRequest('GET', '/dictionary', {request : "build"},
        function(response){
            $('#functionResult').text(response);
        }
    )
}

function runRuleTest(){
    doAjaxRequest('GET', '/test',
    {test : "test_rule", rule_id : $( "#ruleTestId" ).val(), line_start : $( "#ruleTestLineStart" ).val(), line_limit : $( "#ruleTestLineEnd" ).val()},
        function(response){
            new Transformation().setXml(response).setXslt("test_errors.xsl").transform("ruleTestResult");
        }
    )
}

function getWordContext(){
    doAjaxRequest('GET', '/test',
    	{test : "context",
    		word : $( "#wordContextWord" ).val(),
    		line_start : $( "#wordContextLineStart" ).val(),
    		line_limit : $( "#wordContextLineEnd" ).val()},
    	function(response){
    		$('#wordContextResult').html(response);
    	}
    )
}

function runRuleCompetenceTest(){
    doAjaxRequest('GET', '/test', {test : "rule_competence"},
        function(response){
            $('#ruleCompetenceResult').text(response);
        }
    )
}

function runFalsePositivesTest(){
	$("#loadingDiv").show();
    doAjaxRequest('GET', '/test', {test : "false_positives"},
        function(response){
            var entries = response.split(":");
            var html = "<tr><th>Rule ID</th><th>Matches</th></tr>";
            for (x = 0; x < entries.length; x++) {
                var column = entries[x].split(",");
                html += "<tr><td>" + column[0] + "</td><td>" + column[1] + "</td></tr>";
            }
            $("#loadingDiv").fadeOut(500);
            $('#falsePositiveResult').html(html);
        }
    )
}

function runProcessingTimeTest(){
    doAjaxRequest('GET', '/test', {test : "processing_time"},
        function(response){
            var entries = response.split(":");
            var html = "<tr><th>Lines Used</th><th>Rules Used</th><th>Matches</th><th>Time Taken</th></tr>";
            for (x = 0; x < entries.length; x++) {
                var column = entries[x].split(",");
                html += "<tr><td>" + column[0] + "</td><td>" + column[1] + "</td><td>" + column[2] + "</td><td>" + column[3] + "</td></tr>";
            }
            $('#processingTimeResult').html(html);
        }
    )
}

function setTestText(){
    $.ajax({
        type: 'GET',
        dataType: 'text',
        url: '/example.txt',
        success: function(textfile){
            $("#inputText").text(textfile);
        },
        error: function(xhr, textStatus, error){
            var errorMessage = 'Error getting text.';
            alert(errorMessage);
            console.log(errorMessage);
        }
    });
}

$( "#webSocketBroadcast" ).click(function(){
	socket.send("thisisatest");
});

function initialiseDocument(){
    setTestText();
    
    $( [ $("#ruleTestPopOver"),
    		$("#ruleCompetencePopOver"),
    		$("#falsePositivesPopOver"),
    		$("#wordContextPopOver"),
    		$("#processingTimePopOver") ] )
    		.each( function(){ $(this).popover(); } ); 
    
    $( [ $("#ruleCompetenceOption"),
    		$("#falsePositivesOption"),
    		$("#wordContextOption"),
    		$("#processingTimeOption" ) ] )
    		.each( function(){ $(this).hide(); } );
}

function hideAllOptions(){
	
    $( [ $("#ruleTestPopOver"),
    		$("#ruleCompetencePopOver"),
    		$("#falsePositivesPopOver"),
    		$("#wordContextPopOver"),
    		$("#processingTimePopOver") ] )
    		.each( function(){ $(this).popover('hide'); } );
    
    $( [ $("#testRuleOption"),
    		$("#ruleCompetenceOption"),
    		$("#falsePositivesOption"),
    		$("#wordContextOption"),
    		$("#processingTimeOption") ] )
    		.each( function(){ $(this).hide(); } );
}

function setupWebSocket(){
	var socket = new WebSocket("ws://localhost:6819/update/");
    
	socket.onopen = function(){
		console.log("connected"); 
	};
	
	socket.onmessage = function (message){
		console.log(message.data);
	};
	
	socket.onclose = function(){
		console.log("disconnected"); 
	};
}

function doAjaxRequest(type, url, data, successFunction){
    $.ajax({
        type: type,
        dataType: 'text',
        url: url,
        data: data,
        success: successFunction,
        error: function(xhr, textStatus, error){
            var errorMessage = 'Error connecting to the LanguageTool server.';
            alert(errorMessage);
            console.log(errorMessage);
        }
    });
}

$( document ).ready(function() {
    
    initialiseDocument();
    
    $( "#testOption" ).change(function(){       
        hideAllOptions(); 
        switch( parseInt($( "#testOption option:selected" ).val()) ){
	        case 0:	$( "#testRuleOption" 		).show(); 	break;
	        case 1: $( "#ruleCompetenceOption" 	).show(); 	break;
	        case 2: $( "#falsePositivesOption" 	).show(); 	break;
	        case 3: $( "#processingTimeOption" 	).show(); 	break;
	        case 4: $( "#wordContextOption" 	).show(); 	break;
        } 
    });
    
    $( "#btnSubmitPost" 		).click( doTextCheck 			);
    $( "#btnAddWord" 			).click( addWordToDictionary 	);
    $( "#btnSearchWord" 		).click( searchWordInDictionary );
    $( "#btnGetInflections" 	).click( getWordInflections 	);
    $( "#btnBuildDictionary" 	).click( buildDictionary 		);
    $( "#btnTestRule" 			).click( runRuleTest 			);
    $( "#btnRuleCompetence" 	).click( runRuleCompetenceTest 	);
    $( "#btnFalsePositives" 	).click( runFalsePositivesTest 	);
    $( "#btnProcessingTime" 	).click( runProcessingTimeTest	);
    $( "#btnWordContext"	 	).click( getWordContext			);
    
});
