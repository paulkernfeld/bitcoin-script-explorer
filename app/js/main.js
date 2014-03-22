// Copyright 2014 Paul Kernfeld. This file is part of bitcoin-script-explorer,
//  which is licensed under the GNU GPL v3. See LICENSE for details.

'use strict';

var bse = paulkernfeld.bse;

// Logical state
var states;
var script;
var currentState;

// Change where we are in the program
var setCurrentState = function(index) {
  currentState = index;

  $(".op").removeClass("active");
  $(".op." + index).addClass("active");

  $("#state").empty();

  var state = states[index];

  for (var s in state) {
    var newStackItem = $(
      '<div class="row">' +
        state[s] +
        '</div>'
    );
    $("#state").append(newStackItem);
  }
};

var parseToControl = function() {
  $("#parse-alert").hide();
  var ops;  
  try {
    script = bse.parse_full(bse.from_hex(getCombinedScript()));
  } catch (err) {
    $("#parse-alert").show().text(err.stack);
    console.log(err.stack);
    $("#pubKeyOps").addClass("invalid");
    return;
  }

  // Script was parsed successfully, let's proceed
  ops = bse.parse_js(script);
  states = bse.execute_js(script);

  $("#pubKeyOps").empty();
  $("#pubKeyOps").removeClass("invalid");

  $.each(ops, function(index, op) {
    var newButton = $(
      '<div class="op row ' + (index + 1) + '">' +
        '<span class="program-hex">' +
        'B7' +
        '</span>' +
        '<span>' +
        op.string +
        '</span>' +
        '</div>'
    );

    $("#pubKeyOps").append(newButton);

    newButton.click(function(eventData) {
      setCurrentState(index + 1);
    });
  });
};

var getCombinedScript = function() {
  return $("#inputPubKey").val() + $("#inputScriptSig").val();
};

$("#inputPubKey").change(function() {
  parseToControl();
});

$("#inputScriptSig").change(function() {
  parseToControl();
});

var events = "mouseup keyup";

$("#inputPubKey").on(events, function() {
  if(getCombinedScript().length < 1000) {
    parseToControl();
  }
});

$("#inputScriptSig").on(events, function() {
  if(getCombinedScript().length < 1000) {
    parseToControl();
  }
});

$(document).keypress(function(eventObject) {
  // K to move up
  if (eventObject.keyCode == 107) {
    if (currentState > 0) {
      setCurrentState(currentState - 1);
    }
  }

  // J to move down
  if (eventObject.keyCode == 106) {
    if (currentState < states.length - 1) {
      setCurrentState(currentState + 1);
    }
  }
});

// Initialize
parseToControl();
setCurrentState(0);

var advancedOptionsShowing = false;
$("#advanced-options-toggle").click(function() {
  advancedOptionsShowing = !advancedOptionsShowing;

  if (advancedOptionsShowing) {
    $("#advanced-options-glyphicon").removeClass("glyphicon-collapse-down");
    $("#advanced-options-glyphicon").addClass("glyphicon-collapse-up");
    $("#advanced-options").height("80px");
  } else {
    $("#advanced-options-glyphicon").removeClass("glyphicon-collapse-up");
    $("#advanced-options-glyphicon").addClass("glyphicon-collapse-down");
    $("#advanced-options").height("0px");
  }
});
