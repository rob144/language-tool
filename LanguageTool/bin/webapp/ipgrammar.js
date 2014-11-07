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
    processErrorXml:
        function( errorsXml ){
            this.xmlErrors = errorsXml;
            this.xmlErrors.each( function(index){ $(this).attr('ref', index); } );
            this.arrMarkedLines = this.getMarkupLines(); 
            /* Each element in arrayLineObjs contains an array of objects
            * Each one of the those objects contains the character and
            * an array of any error ids which fall at that character position. 
            * */
            this.arrLineObjs = this.buildTextData(this.plainText);
        },
    getMarkupForLine:   
        function( arrChars, lineNumber ) {
            var lineMarkup = "";
            for(var charNum = 0; charNum < arrChars.length; charNum++ ){
                lineMarkup += "<a id='" + lineNumber + "_" + charNum + "'>" + arrChars[charNum] + "</a>";
            }
            return lineMarkup;
        },
    getMarkupLines:
        function() {
            //Return an arry containing the HTML line strings
            var arrLines = this.plainText.split(/(?:\r\n|\r|\n)/);
            var arrayHtmlLines = [];
            if(arrLines.length >= 1){
                for (var i = 0; i < arrLines.length; i++) { 
                    if((arrLines[i].trim() + "" ) == ""){
                        arrayHtmlLines.push("<br/>");
                    } else {
                        arrayHtmlLines.push("<p class='line'>" 
                        + this.getMarkupForLine( arrLines[i], i ) + "</p>");
                    }
                }
            }
            return arrayHtmlLines;
        },
    buildTextData:
        function ( plainText ){
            
            var arrLines = plainText.split(/(?:\r\n|\r|\n)/);
            var arrLinesObjs = [];

            for (var lineNum = 0; lineNum < arrLines.length; lineNum++ ){
                
                var arrChars = arrLines[lineNum].split("");
                var arrCharObjs = [];

                for(var charNum = 0; charNum < arrChars.length; charNum++){
                    
                    var objChar = { 
                        char: arrChars[charNum],
                        errors: this.getErrorsAtPosition(lineNum, charNum),
                        getErrorIds:
                            function(){  
                                var errorIdsString = '';
                                for(var k = 0; k < this.errors.length; k++){
                                    if(k >= 1) errorIdsString += ':';
                                    errorIdsString += this.errors[k].ref;
                                }
                                return errorIdsString;
                            },
                        message: ''
                    };
                    for (var i = 0; i < objChar.errors.length; i++){
                        if(i == 0) objChar.message = '<ul>';
                        if(i >= 1) objChar.message += '<br/>';
                        objChar.message += '<li>' + objChar.errors[i].message + '</li>';
                        if(i == objChar.errors.length - 1) objChar.message += '</ul>';
                    }
                    arrCharObjs.push( objChar );
                }
                arrLinesObjs.push( arrCharObjs );
            }

            return arrLinesObjs;
        },
    getErrorsAtPosition:
        function ( lineNum, charNum ){
            var errors = [];
            //Search the error xmlObj for any errors at this position.

            this.xmlErrors.filter("[fromy='" + lineNum  + "']").each(
                function(){
                    //Check if the x values overlap
                    var x1 = parseInt($($(this).get(0)).attr('fromx'));
                    var x2 = parseInt($($(this).get(0)).attr('tox')) - 1;
                    if(x1 <= charNum && x2 >= charNum){
                        var e = $($(this).get(0));
                        var objError = { 
                            ref:        $(e).attr('ref'),
                            fromy:      parseInt( $(e).attr('fromy') ),
                            toy:        parseInt( $(e).attr('toy') ),
                            fromx:      parseInt( $(e).attr('fromx') ),
                            tox:        parseInt( $(e).attr('tox') ) - 1,
                            message:    $(e).attr('msg')
                        }  
                        errors.push( objError );
                    }
                }
            );
            return errors;
        },
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

function clearText(){
    $('#inputText').val('');
}

function showTab(viewId, text){

console.log('text: ' + text);
	
    $('#tabs a[href="#'+ viewId +'"]').tab('show');
    
    if(viewId == 'markupTab'){
        
        if (typeof text === 'undefined'){  
            $('#textMarked').html(DATA.textMarked[0]);
        }else{
            if(DATA.getTotalPages() >= 1){
                $('#textMarked').html(text);
                addHighlighting( DATA.xmlErrors );
                $('#pageXofY').text('Page ' + CURRENT_PAGE + ' of ' + DATA.getTotalPages());
                $('#pageXofY').css('display','inline');
                $('#pagination').css('display','block');
            }
        }
    }else{
        $('#pagination').css('display','none');
    }
}

function getErrorAttribute(errorRefId, attributeName){
    return $(XML_ERRORS).find('error[ref="' + errorRefId  + '"]').attr(attributeName);     
}

function addHighlighting( arrXmlErrors ){

    //Loop through the char objects and add one mousemove event per character
    for(var i = 0; i < DATA.arrLineObjs.length; i++){    
        for(var j = 0; j < DATA.arrLineObjs[i].length; j++){
          
           //For each character grab the error ids, fetch the messages for those errors. 
           var c = DATA.arrLineObjs[i][j];
           if(c.errors.length >= 1){
                
                var elem_id = '#' + i + '_' + j;
                $(elem_id).addClass('error-highlight');
                var tooltip = $("<div id='tip_" + i + "_" + j + "' data-error-ids='" + c.getErrorIds()  
                                    + "' class='tip'>" + c.message + "</div>");
                tooltip.css('display','none');
                tooltip.css('position','absolute');
                tooltip.appendTo($("#textMarked"));

                $(elem_id).mousemove( { err_id: i + "_" + j, thisErrorIds: c.getErrorIds()  } , function(event) { 

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
	if(langText.indexOf('English (US)') >= 0) langCode = 'en-US';
	if(langText.indexOf('English (GB)') >= 0) langCode = 'en-GB';
	return langCode;
	
}

$( document ).ready(function() {
    
    initialiseDocument();
    
    $( "select" ).change(function(){
        
        hideAllOptions();
        
        if ($( "#testOption option:selected" ).val() == 0)
            $( "#testRuleOption" ).show();
        
        if ($( "#testOption option:selected" ).val() == 1)
            $( "#ruleCompetenceOption" ).show();
        
        if ($( "#testOption option:selected" ).val() == 2)
            $( "#falsePositivesOption" ).show();
        
        if ($( "#testOption option:selected" ).val() == 3)
            $( "#processingTimeOption" ).show();
    }).change();
    
    /*var socket = new WebSocket("ws://localhost:6819/update/");
    
	socket.onopen = function(){
		console.log("connected"); 
	};
	
	socket.onmessage = function (message){
		console.log(message.data);
	};
	
	socket.onclose = function(){
		console.log("disconnected"); 
	};*/
	
	$( "#webSocketBroadcast" ).click(function(){
		socket.send("thisisatest");
	});
    
    $( "#btnSubmitPost" ).click( doTextCheck );
    
    $( "#btnAddWord" ).click(function(){
        var word = $( "#inputTextDictAddWord" ).val();
        var lemma = $( "#inputTextDictAddLemma" ).val();
        var postag = $( "#inputTextDictAddPosTag" ).val();
        if($.trim(word).length <= 0 || $.trim(lemma).length <= 0 || $.trim(postag).length <= 0) return;
        var content = word + "." + lemma + "." + postag; 
        doAjaxRequest('GET', '/dictionary', {request : "add", line : content},
            function(response){
                $('#functionResult').text(response);
            }
        )
    });
    
    $( "#btnSearchWord" ).click(function(){
        var word = $( "#inputTextDictSearchWord" ).val();
        var lemma = $( "#inputTextDictSearchLemma" ).val();
        var postag = $( "#inputTextDictSearchPosTag" ).val();
        if($.trim(word).length <= 0 || $.trim(lemma).length <= 0 || $.trim(postag).length <= 0) return;
        var content = word + "." + lemma + "." + postag;
        doAjaxRequest('GET', '/dictionary', {request : "search", line : content},
            function(response){
                $('#functionResult').text(response);
            }
        )
    });
    
    $( "#btnGetInflections" ).click(function(){
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
    });
    
    $( "#btnBuildDictionary" ).click(function(){
        doAjaxRequest('GET', '/dictionary', {request : "build"},
            function(response){
                $('#functionResult').text(response);
            }
        )
    });
    
    $( "#btnTestRule" ).click(function(){
        doAjaxRequest('GET', '/test',
        {test : "test_rule", rule_id : $( "#ruleId" ).val(), line_start : $( "#lineStart" ).val(), line_limit : $( "#lineEnd" ).val()},
            function(response){
                new Transformation().setXml(response).setXslt("test_errors.xsl").transform("testingResults");
            }
        )
    });
    
    $( "#btnRuleCompetence" ).click(function(){
        doAjaxRequest('GET', '/test', {test : "rule_competence"},
            function(response){
                $('#testingResults').html(response);
            }
        )
    });
    
    $( "#btnFalsePositives" ).click(function(){
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
                $('#testingResults').html(html);
            }
        )
    });
    
    $( "#btnProcessingTime" ).click(function(){
        doAjaxRequest('GET', '/test', {test : "processing_time"},
            function(response){
                var entries = response.split(":");
                var html = "<tr><th>Lines Used</th><th>Rules Used</th><th>Matches</th><th>Time Taken</th></tr>";
                for (x = 0; x < entries.length; x++) {
                    var column = entries[x].split(",");
                    html += "<tr><td>" + column[0] + "</td><td>" + column[1] + "</td><td>" + column[2] + "</td><td>" + column[3] + "</td></tr>";
                }
                $('#testingResults').html(html);
            }
        )
    });
    
});

function doTextCheck(){
    DATA.plainText = $( "#inputText" ).val();
    $("#loadingDiv").show();
    doAjaxRequest('POST', '/' + getLanguageCode() + '/checktext', 'text=' + encodeURIComponent( DATA.plainText ),
        function(xml){
            /* remove the <? xml version ... ?> tag */
            xml = xml.substr(xml.indexOf('?>')+2);  
            var $xmlObj = $( $.parseXML('<root>'+ xml +'</root>') );
            var xmlString = $xmlObj.find('root').html().trim();
            
            DATA.processErrorXml( $xmlObj.find('error') );
            new Transformation().setXml(xmlString).setXslt("grammar_errors.xsl").transform("xslOutput");
            showTab( 'markupTab', DATA.getPageText(1) );
        }
    );
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

function initialiseDocument(){
    setTestText();
    $( "#ruleTestPopOver" ).popover();
    $( "#ruleCompetencePopOver" ).popover();
    $( "#falsePositivesPopOver" ).popover();
    $( "#processingTimePopOver" ).popover();
    
    $( "#ruleCompetenceOption" ).hide();
    $( "#falsePositivesOption" ).hide();
    $( "#processingTimeOption" ).hide();
}

function hideAllOptions(){
    $( "#ruleTestPopOver" ).popover('hide');
    $( "#ruleCompetencePopOver" ).popover('hide');
    $( "#falsePositivesPopOver" ).popover('hide');
    $( "#processingTimePopOver" ).popover('hide');
    
    $( "#testRuleOption" ).hide();
    $( "#ruleCompetenceOption" ).hide();
    $( "#falsePositivesOption" ).hide();
    $( "#processingTimeOption" ).hide();
}