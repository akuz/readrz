
var getJSON2Requests = {};
var getJSON2RequestIds = {};
var getJSON2RequestCounter = 0;

function getJSON2(freq, url, requestKey, onSuccess, onFailure) {

	if (!url || url.length == 0) {
		alert("Cannot use getJSON2 with an empty URL");
	}
	
	// abort current
	getJSON2_abort(requestKey);
	
	// generate request id
	getJSON2RequestCounter += 1;
	var requestId = getJSON2RequestCounter;
	getJSON2RequestIds[requestKey] = requestId;
	
	// start making periodic requests
	getJSON2_one(freq, url, requestKey, requestId, onSuccess, onFailure);
}

function getJSON2_one(freq, url, requestKey, requestId, onSuccess, onFailure) {

	// check curr request id		
	var currRequestId = getJSON2RequestIds[requestKey];
	if (currRequestId != requestId) {
		return;
	}
	
	getJSON2Requests[requestKey] = 
		$.getJSON(url, function(data) {
		
			// forget request handle		
			getJSON2Requests[requestKey] = null;
			
			// check curr request id		
			var currRequestId = getJSON2RequestIds[requestKey];
			if (currRequestId != requestId) {
				return;
			}
			
			// analyse data
			if (data == null) {
				onFailure("Server returned null data");
			} else if (!data.isOk) {
				onFailure(data.warning);
			} else if (data.isReady) {
				onSuccess(data.data);
			} else {
				setTimeout(function() {
					getJSON2_one(freq, url, requestKey, requestId, onSuccess, onFailure);
				}, freq);
			}
			
		})
		.fail(function() {

			// forget request handle		
			getJSON2Requests[requestKey] = null;

			// check curr request id		
			var currRequestId = getJSON2RequestIds[requestKey];
			if (currRequestId != requestId) {
				return;
			}
		
			onFailure("Error getting data from server")
		});
}

function getJSON2_abort(requestKey) {
	
	getJSON2RequestIds[requestKey] = null;
	
	var currRequest = getJSON2Requests[requestKey];
	if (currRequest != null) {
		currRequest.abort();
		getJSON2Requests[requestKey] = null;
	}
}
