'use strict'
/**
 * 해당 JS는 room.html에 의존하고 있습니다.
 */
let stompClient 	= null;
const sendArea 		= document.querySelector('#sendArea');
const sendButton	= document.querySelector('#sendButton');
const leaveButton 	= document.querySelector('#leaveButton');

sendButton.addEventListener('click', fnSend);
leaveButton.addEventListener('click', fnLeave);

function fnConnect() {
	// hadn-shake
	const socket = new SockJS("/chat-service");

	stompClient = Stomp.over(socket);
	stompClient.connect({}, function (frame) {

		// 메시지 구독
		stompClient.subscribe(`/topic/chat/room/${ChatRoom.id}`, function (responseData) {
			let data = JSON.parse(responseData.body);
			View.log(data);
		});

		// 상태 구독
		stompClient.subscribe(`/topic/chat/room/${ChatRoom.id}/status`, function (responseData) {
			let data = JSON.parse(responseData.body);
			ChatRoom = data;
			fnChatRoomApply();
		});

		// 채팅방 입장
		stompClient.send("/chat-app/sendMessage", {}, JSON.stringify({
			type : 'JOIN',
			chatRoomId : ChatRoom.id,
			message : '',
			senderIp : Member.ip
		}));
	});
}

function fnSend() {
	let message = sendArea.value;

	if(message === '') {
		alert('메시지를 입력해주세요.');
		sendArea.focus();
		return;
	}

	sendMessage(message);
}

function fnLeave() {
	// 연결해제
	stompClient.disconnect();

	location.href = '../room';
}

function sendMessage(message) {
	let sendData = {
		type : 'MESSAGE',
		chatRoomId : ChatRoom.id,
		message : message,
		senderIp : Member.ip
	};

	sendArea.value = '';

	stompClient.send("/chat-app/sendMessage", {}, JSON.stringify(sendData));
}

function fnPrevMessage() {
	// 이전 대화 log 출력
	ChatRoom.messageList.forEach(e => {
		// 입장/퇴장 log 출력하지 않음.
		if (e.type === 'JOIN' || e.type === 'LEAVE') {
			return;
		}
		View.log(e);
	});
}

function fnKeyMapping() {
	// Enter로 전송기능
	window.addEventListener('keypress', function(event) {
		if(event.code === 'Enter') {
			event.preventDefault();
			fnSend();
		}
	});

	// Alt + Enter로 개행처리
	window.addEventListener('keydown', function (event) {
		if (event.altKey && event.code === 'Enter') {
			event.preventDefault();
			sendArea.value += '\n';
		}

		if (event.shiftKey && event.code === 'Enter') {
			event.preventDefault();
			sendArea.value += '\n';
		}
	});
}