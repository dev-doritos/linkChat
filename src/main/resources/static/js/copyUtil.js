const CopyUtil = {
	copyValue : function( o )
	{
		if(typeof o === 'undefined' || o == null)
			throw new TypeError('copyObject cannot be null');

		try
		{
			let copyText = o.value;

			if(copyText === '')
			{
				return;
			}

			let inp = document.createElement('textarea');
			inp.value = copyText;
			document.body.appendChild(inp);

			inp.select();
			let result = document.execCommand('copy');
			inp.remove();

			if(!result)
				throw new TypeError();

			alert('내용이 복사되었습니다.');
		}
		catch(e)
		{
			console.error(e);
			alert('내용 복사에 실패했습니다.\n'+e.toString());
		}
	}
};