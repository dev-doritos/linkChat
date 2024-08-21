Date.prototype.getyyyyMMdd = function() {
	return new Date().toJSON().substring(0, 10).replace('-', '').replace('-', '');
}

Date.prototype.getNow = function() {
	// 2022-11-11 10:03:43

	let seed = new Date();
	
	let yyyyMMddDash = seed.toJSON().substring(0, 10);
	let timeString = seed.toTimeString().substring(0, 8);
	
	return yyyyMMddDash + ' ' + timeString;
}