// Copyright 2014 Paul Kernfeld. This file is part of bitcoin-script-explorer,
//  which is licensed under the GNU GPL v3. See LICENSE for details.

'use strict';

var bse = paulkernfeld.bse;

// Example transactions
var examples = {
  "successful": {
    "pubKey": "76a9145e4ff47ceb3a51cdf7ddd80afc4acc5a692dac2d88ac",
    "scriptSig": "483045022074f35af390c41ef1f5395d11f6041cf55a6d7dab0acdac8ee746c1f2de7a43b3022100b3dc3d916b557d378268a856b8f9a98b9afaf45442f5c9d726fce343de835a58012102c34538fc933799d972f55752d318c0328ca2bacccd5c7482119ea9da2df70a2f"
  },
  "unsuccessful": {
    "pubKey": "76a9145e4ff47ceb3a51cdf7ddd80afc4acc5a692dac2d88ac",
    "scriptSig": "51"
  }
};

// Logical state
var states;
var script;
var currentState = 0;
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

var setExample = function(id) {
  var example = examples[id];
  $("#inputPubKey").val(example.pubKey);
  $("#inputScriptSig").val(example.scriptSig);
  currentState = 0;
  parseToControl();
};

var parseToControl = function() {
  $("#parse-status")
    .removeClass()
    .addClass("alert")
    .tooltipster('disable');

  var ops;  
  try {
    script = bse.parse_full(bse.from_hex(getCombinedScriptSafe()));
  } catch (err) {
    broken = true;
    $("#parse-status")
      .addClass("alert-danger")
      .html(err.message)
      .tooltipster('content', $("<span>" + err.stack.replace(/\n/g,"<br>") + "</span>"));
    $("#parse-status").tooltipster("enable");

    console.log(err.stack);
    $("#allOps").addClass("invalid");
    return;
  }
  broken = false;

  $("#parse-status")
    .addClass("alert-success")
    .text("Parsed");

  // Script was parsed successfully, let's proceed
  ops = bse.parse_js(script);
  states = bse.execute_js(script);

  $("#allOps").empty();
  $("#allOps").removeClass("invalid");

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

    $("#allOps").append(newButton);

    newButton.click(function(eventData) {
      if (broken) { return; }
      setCurrentState(index + 1);
    });

    newButton.tooltipster({content: $("<span>" + op.description + "</span>")});
  });

  setCurrentState(currentState);
};

$("#parse-status").tooltipster({"content": ""});

$(".op.0").click(function(eventData) {
  if (broken) { return; }
  setCurrentState(0);
});

var inputRegex = /^([0-9A-Fa-f][0-9A-Fa-f])*$/;

var getCombinedScriptSafe = function() {
  var scriptSig = $("#inputScriptSig").val();
  var pubKey = $("#inputPubKey").val();

  if (!inputRegex.test(scriptSig)) {
    throw new Error("scriptSig must be a valid hex-encoded byte string");
  }
  if (!inputRegex.test(pubKey)) {
    throw new Error("pubKey must be a valid hex-encoded byte string");
  }

  return scriptSig + pubKey;
};

var getCombinedScript = function() {
  var scriptSig = $("#inputScriptSig").val();
  var pubKey = $("#inputPubKey").val();

  return scriptSig + pubKey;
};

$("#inputScriptSig").change(function() {
  parseToControl();
});

$("#inputPubKey").change(function() {
  parseToControl();
});

var events = "mouseup keyup";

$("#inputScriptSig").on(events, function() {
  if(getCombinedScript().length < 1000) {
    parseToControl();
  }
});

$("#inputPubKey").on(events, function() {
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

$("#examples .example").click(function(eventObject) {
  var target = eventObject.target;
  setExample(target.id);

  $(".example").removeClass("active");
  $(target).addClass("active");
});

// Initialize
setExample("successful");

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
