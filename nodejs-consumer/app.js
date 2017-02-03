var http = require('http');
var express = require('express');
var app = module.exports.app = express();
var server = http.createServer(app);
var io = require('socket.io').listen(server);

server.listen(3000);

var infinispan = require('infinispan');
var Promise = require('promise');
// client which loads JPG representation of incomming data to show user also the input
// as JS client currently doesn't support convertes, we need to load this data in separate get request
var connected1 = infinispan.client({port: 11222, host: '127.0.0.1'}, {version: '2.5', cacheName: 'mnistJpgImgs'});
// client which listens to ISPN result cache for data created and modified events
var connected2 = infinispan.client({port: 11222, host: '127.0.0.1'}, {version: '2.5', cacheName: 'mnistResults'});


app.get('/', function (req, res) {
  res.sendFile(__dirname + '/index.html');
});

var ispnSocket;
io.on('connection', function (socket) {
    ispnSocket = socket;
});

var pngListenerAdded = connected1.then(function (clientPng) {
    var listenersAdded = connected2.then(function (clientRes) {

	// listens to result cache and once new result arrives, load also original image from JPG cache
	var clientAddListenerCreate = clientRes.addListener('create', function(key) {
	    console.log('[Event] Created key: ' + key);
	    clientRes.get(key).then(function (imgRes) {
		clientPng.get(key).then(function (jpgImg) {
		    ispnSocket.emit('ispn', {id: key.substring(3), jpg: jpgImg.substring(4), number: imgRes.substring(3)});
		});
	    });
	});

	// same as above methond, but for modified data. Can be squeeze to previous promise, but for clarity implemented as separate promise
	var clientAddListenerModify = clientRes.addListener('modify', function(key) {
            console.log('[Event] Modified key: ' + key);
    	    clientRes.get(key).then(function (imgRes) {
		clientPng.get(key).then(function (jpgImg) {
		    ispnSocket.emit('ispn', {id: key.substring(3), jpg: jpgImg.substring(4), number: imgRes.substring(3)});
		});
    	    });
	});
	
    }).catch(function(error) {
	console.log("Got error: " + error.message);
    });
});
