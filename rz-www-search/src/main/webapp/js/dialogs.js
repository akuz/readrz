function inlineConfirmDialog(elem, message, onConfirm, onCancel, options) {

	var dialog = elem.children(".inlineConfirmDialog");
	if (dialog.length > 0) {
		dialog.remove();
	}

	var inputHtml = '';
	if (options && options.input) {
		inputHtml = inputHtml + '<input type="text" name="text" class="text"';
		if (options.inputMaxLen) {
			inputHtml = inputHtml + ' maxlength="' + options.inputMaxLen + '"';
		}
		inputHtml = inputHtml + ' value="';
		if (options.inputValue) {
			inputHtml = inputHtml + options.inputValue;
		}
		inputHtml = inputHtml + '" />';
	}
	
	var confirmText = 'Confirm';
	if (options && options.confirmText) {
		confirmText = options.confirmText;
	}

	dialog = $(
		'<div class="inlineConfirmDialog">' + 
		'	<div class="message">' + message + '</div>' + 
		inputHtml +
		'	<div class="button confirm">' + confirmText + '</div>' +
		'	<div class="button cancel">Cancel</div>' +
		'</div>');
	var input = dialog.find('input.text');
	
	dialog.click(function() {
		event.stopPropagation();
	});

	var handleConfirm = function() {
		
		// check the input
		var inputValue = null;
		if (input.length > 0) {

			inputValue = input.val().trim();
			if (inputValue.length == 0) {
				return false;
			}
			
			if (options && options.inputRegex) {
				if (!inputValue.match(options.inputRegex)) {
					var errorMessage = input.next("div.message.error");
					errorMessage.remove();
					var errorText = 'Illegal input value.';
					if (options.inputRegexDesc) {
						errorText = options.inputRegexDesc;
					}
					errorMessage = $('<div class="message error">' + errorText + '</div>');
					errorMessage.insertAfter(input);
					input.focus();
					return false;
				}
			}
		}
		
		dialog.remove();
		if (onConfirm != null) {
			onConfirm(inputValue);
		}
	}

	input.keypress(function(e) {
	    if (e.which == 13) {
	    	return handleConfirm();
	    }
	});
	
	dialog.find(".button.confirm").click(function(event) {
		event.stopPropagation();
		return handleConfirm();
	});
		
	dialog.find(".button.cancel").click(function(event) {
		event.stopPropagation();
		dialog.remove();
		if (onCancel != null) {
			onCancel();
		}
	});

	elem.mouseleave(function(event) {
		elem.unbind("mouseleave", arguments.callee);
		dialog.remove();
	});
	
	elem.append(dialog);
	
	dialog.show();
	dialog.css('top', elem.height());
	
	if (options != null && options.width) {
		dialog.css('width', options.width);
	}
	
	if (options != null && options.width) {
		dialog.css('width', options.width);
	}
	if (input.length > 0) {
		if (options != null && options.width) {
			input.css('width', options.width - 33);
		}
		input.focus();
	}
}