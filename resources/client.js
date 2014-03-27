(function() {
  var history = document.getElementById('history');
  var pushToRepl = function(message) {
    // render the original input that was eval'd
    var inputView = '<p class="input ' + message.sender + '">' +
                    '<span class="prompt">' + message.sender + '</span>' +
                    '<code>' + message.code + '</code></p>';

    // render any print statements that occurred as a result of eval
    var printedView = '';
    var printed = message.printed || [];
    for (var i in printed) {
      printedView += '<p class="printed">' + printed[i] + '</p>';
    }

    // render the actual result
    var error = message.error;
    var resType, resText;
    if (error) {
      resType = 'failure';
      resText = error;
    } else {
      resType = 'success';
      resText = message.value;
    }
    var resultView = '<p class="result ' + resType + '">' + resText + '</p>';

    // concat everything together and append to view
    var view = document.createElement('div');
    view.classList.add('entry');
    view.innerHTML = inputView + printedView + resultView;
    history.appendChild(view);
    history.scrollTop = history.scrollHeight;
  };

  var host = location.origin.replace(/^http/, 'ws') + '/ws';
  var conn = new WebSocket(host);
  conn.onmessage = function(ev) {
    var message = JSON.parse(ev.data);
    pushToRepl(message);
  };

  var input = document.getElementById('input');
  input.addEventListener('keyup', function(ev) {
    if ((ev.key || ev.keyCode || ev.which) === 13) {
      // enter key pressed, send input to server
      var message = {
        op: 'eval',
        code: input.value
      };
      conn.send(JSON.stringify(message));
      input.value = '';
    }
  });
})();
