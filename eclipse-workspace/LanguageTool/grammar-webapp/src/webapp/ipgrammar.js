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

    var views = ['markupTab','inputText','xmlOutput','tableTab','links'];
    //for(var i=0;i<views.length;i++){
    //    if(views[i] != viewId) $('#'+views[i]).css('display','none');
    //}
    //$('#'+viewId).css('display','block');
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
}

$( document ).ready(function() {
    
    setTestText();

    $( "#btnSubmitPost" ).click(function() {
        
        $("#loadingDiv").show();

        DATA.plainText = $( "#inputText" ).val();
        
        $.ajax({
            type: 'POST',
            dataType: 'text',
            url: '/' + $('#selectLang').val() + '/checktext',
            data: 'text=' + encodeURIComponent( DATA.plainText ),
            success: function( xml ){
                
                /* remove the <? xml version ... ?> tag */
                xml = xml.substr(xml.indexOf('?>')+2);  
                var $xmlObj = $( $.parseXML('<root>'+ xml +'</root>') );
                var xmlString = $xmlObj.find('root').html().trim();

                DATA.processErrorXml( $xmlObj.find('error') );
                new Transformation().setXml(xmlString).setXslt("grammar_errors.xsl").transform("xslOutput");
                showTab( 'markupTab', DATA.getPageText(1) );
                $("#loadingDiv").hide();
            },
            error: function(xhr, textStatus, error){
                var errorMessage = 'Error connecting to the LanguageTool server.';
                alert(errorMessage);
                console.log(errorMessage);
            }
        });
    });   

});