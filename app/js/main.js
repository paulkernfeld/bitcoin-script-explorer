// Copyright 2014 Paul Kernfeld. This file is part of bitcoin-script-explorer,
//  which is licensed under the GNU GPL v3. See LICENSE for details.

'use strict';

var bse = paulkernfeld.bse;

// Logical state
var states;
var script;
var currentState = 0
var broken = false;

// Change where we are in the program
var setCurrentState = function(index) {
  currentState = index;

  $(".op").removeClass("active");
  $(".op." + index).addClass("active");

  $("#stack").empty();

  var state = states[index];

  var stack = state.stack;

  $("#result").removeClass().addClass("alert").text(state.result);
  if (state.result == "unfinished") $("#result").addClass("alert-warning");
  if (state.result == "success") $("#result").addClass("alert-success");
  if (state.result == "failure") $("#result").addClass("alert-danger");


  for (var s in stack) {
    var newStackItem = $(
      '<div class="row frame">' +
        stack[s] +
        '</div>'
    );
    $("#stack").append(newStackItem);
  }
};

var parseToControl = function() {
  $("#parse-status").removeClass().addClass("alert");
  
  var ops;  
  try {
    script = bse.parse_full(bse.from_hex(getCombinedScript()));
  } catch (err) {
    broken = true;
    $("#parse-status").addClass("alert-danger").text(err.message);
    console.log(err.stack);
    $("#pubKeyOps").addClass("invalid");
    return;
  }
  broken = false;

  $("#parse-status").addClass("alert-success").text("Parsed");

  // Script was parsed successfully, let's proceed
  ops = bse.parse_js(script);
  states = bse.execute_js(script);

  $("#pubKeyOps").empty();
  $("#pubKeyOps").removeClass("invalid");

  $.each(ops, function(index, op) {
    var newButton = $(
      '<div class="op row frame ' + (index + 1) + '">' +
        '<span class="opcode program-hex">' +
        op.opcode.toString(16) +
        '</span>' +
        '<span>' +
        op.name + " >" +
        '</span>' +
        '</div>'
    );

    $("#pubKeyOps").append(newButton);

    newButton.click(function(eventData) {
      if (broken) { return; }
      setCurrentState(index + 1);
    });
  });

  setCurrentState(currentState);
};

$(".op.0").click(function(eventData) {
  if (broken) { return; }
  setCurrentState(0);
});

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
  if (broken) {
    return;
  }

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

var advancedOptionsShowing = false;
$("#advanced-options-toggle").click(function() {
  advancedOptionsShowing = !advancedOptionsShowing;

  if (advancedOptionsShowing) {
    $("#advanced-options-glyphicon").removeClass("glyphicon-collapse-down");
    $("#advanced-options-glyphicon").addClass("glyphicon-collapse-up");
    $("#advanced-options").height("100%");
  } else {
    $("#advanced-options-glyphicon").removeClass("glyphicon-collapse-up");
    $("#advanced-options-glyphicon").addClass("glyphicon-collapse-down");
    $("#advanced-options").height("0px");
    $("#advanced-options").text("");
  }
});
