'use strict'
/**
 * 해당 JS는 room.html에 의존하고 있습니다.
 */
const View = {
    target : document.querySelector('#viewElement'),
    log : function(data) {
        try {

            let type = data.type;

            let logger = null;

            switch (type) {
                case 'JOIN':
                case 'LEAVE':
                    logger = new SystemLog();
                    break;
                case 'FILE':
                    logger = new FileLog();
                    break;
                case 'IMAGE':
                    logger = new ImageLog();
                    break;
                case 'LINK':
                    logger = new LinkLog();
                    break;
                case 'MESSAGE':
                    logger = new MessageLog();
                    break;
                default :
                    throw new TypeError('지원하지 않는 메시지 타입');
            }

            logger.log(data);

            // scroll 처리
            View.target.scrollTop = View.target.scrollHeight;

        } catch(e) {
            console.error(e);
        }
    },
};

/*
 * 메시지타입 별 Log Function 정의
 */
const SystemLog = function () {};
const FileLog = function () {};
const ImageLog = function () {};
const MessageLog = function () {};
const LinkLog = function () {};

SystemLog.prototype.log = (data) => {
    let p           = document.createElement('p');
    let infoSpan    = document.createElement('span');

    infoSpan.innerText = `[${data.auth_ymd} ${data.auth_hms}] ${data.message}`;

    p.appendChild(infoSpan);

    // View.target.appendChild(p);
};

FileLog.prototype.log = (data) => {
    let p           = document.createElement('p');
    let infoSpan    = document.createElement('span');
    let messageSpan = document.createElement('span');

    infoSpan.innerText = `[${data.auth_ymd} ${data.auth_hms}] ${data.sender.name} 님의 말 :\n`;
    messageSpan.innerHTML = '<mark><a href="' + location.origin + '/' + data.link + '" target="_blank">' + data.message + '</a></mark>';

    p.appendChild(infoSpan);
    p.appendChild(messageSpan);

    // 내가 보낸 내역일 경우 처리
    if(data.sender.ip === Member.ip) {
        // p.setAttribute('style', 'text-align : right;')
    }

    View.target.appendChild(p);
};

ImageLog.prototype.log = (data) => {
    let p           = document.createElement('p');
    let infoSpan    = document.createElement('span');
    let messageSpan = document.createElement('span');
    let imgAnchor   = document.createElement('a');
    let imgTag      = document.createElement('img');

    infoSpan.innerText = `[${data.auth_ymd} ${data.auth_hms}] ${data.sender.name} 님의 말 :\n`;

    imgAnchor.setAttribute('href', location.origin + '/' + data.link);
    imgAnchor.setAttribute('target', '_blank');

    imgTag.setAttribute('src', data.link);
    imgTag.setAttribute('style', 'width:50%;');

    imgAnchor.appendChild(imgTag);
    messageSpan.appendChild(imgAnchor);

    p.appendChild(infoSpan);
    p.appendChild(messageSpan);

    // 내가 보낸 내역일 경우 처리
    if(data.sender.ip === Member.ip) {
        // p.setAttribute('style', 'text-align : right;')
    }

    View.target.appendChild(p);
};

MessageLog.prototype.log = (data) => {
    let p           = document.createElement('p');
    let infoSpan    = document.createElement('span');
    let messageSpan = document.createElement('span');

    infoSpan.innerText = `[${data.auth_ymd} ${data.auth_hms}] ${data.sender.name} 님의 말 :\n`;

    messageSpan.innerText = data.message;
    messageSpan.addEventListener('click', () => {
        CopyUtil.copyValue({
            value : data.message
        });
    });
    messageSpan.setAttribute('style', 'cursor : pointer');
    messageSpan.setAttribute('title', '클릭 시 복사됩니다.');

    p.appendChild(infoSpan);
    p.appendChild(messageSpan);

    // 내가 보낸 내역일 경우 처리
    if(data.sender.ip === Member.ip) {
        // p.setAttribute('style', 'text-align : right;')
    }

    View.target.appendChild(p);
};

LinkLog.prototype.log = (data) => {
    let p           = document.createElement('p');
    let infoSpan    = document.createElement('span');
    let messageSpan = document.createElement('span');

    infoSpan.innerText      = `[${data.auth_ymd} ${data.auth_hms}] ${data.sender.name} 님의 말 :\n`;
    messageSpan.innerHTML   = '<a href="' + data.message + '" target="_blank">' + data.message + '</a>';

    p.appendChild(infoSpan);
    p.appendChild(messageSpan);

    // 내가 보낸 내역일 경우 처리
    if(data.sender.ip === Member.ip) {
        // p.setAttribute('style', 'text-align : right;')
    }

    View.target.appendChild(p);
};