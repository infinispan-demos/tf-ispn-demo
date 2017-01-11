var http = require('http');
var express = require('express');
var app = module.exports.app = express();
var server = http.createServer(app);
var io = require('socket.io').listen(server);

server.listen(3000);

var infinispan = require('infinispan');
var Promise = require('promise');
var connected = infinispan.client({port: 11222, host: '127.0.0.1'}, {version: '2.5', cacheName: 'nodejs'});


app.get('/', function (req, res) {
  res.sendFile(__dirname + '/index.html');
});

var ispnSocket;
io.on('connection', function (socket) {
    ispnSocket = socket;
});

var listenersAdded = connected.then(function (client) {
    
    var clientAddListenerCreate = client.addListener('create', function(key) {
	console.log('[Event] Created key: ' + key);
	client.get(key).then(function (value) {
	    ispnSocket.emit('ispn', {key: value});
	} );
    });

    var clientAddListenerModify = client.addListener('modify', function(key) {
	console.log('[Event] Modified key: ' + key);
	client.get(key).then(function (value) {
	    ispnSocket.emit('ispn', {key: value});
	});
    });

}).catch(function(error) {
  console.log("Got error: " + error.message);
});
