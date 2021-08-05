var ws = new WebSocket("wss://" + location.host + "/groupcall");
var participants = {};
var name;

window.onbeforeunload = function () {
  ws.close();
};

// 2 FE에서 방 Join관련 이벤트 메시지 처리
ws.onmessage = function (message) { //WebRtc 시그널링 서버로 받는 이벤트 메시지 처리
  var parsedMessage = JSON.parse(message.data);
  console.info("Received message: " + message.data);

  switch (parsedMessage.id) {
    case "existingParticipants": // 2-1 클라이언트가 현재 새로운 참가자
      onExistingParticipants(parsedMessage);
      break;
    case "newParticipantArrived": // 1-9 BE에서 sendMessage()로 받은 메시지, 2-3 클라이언트가 기존 참여자인 경우
      onNewParticipant(parsedMessage); 
      break;
    case "participantLeft": // 3-9 BE->참여자가 나간 경우 로직이 모두 실행되고 방 Exit관련 이벤트 메시지 처리
      onParticipantLeft(parsedMessage);
      break;
    case "receiveVideoAnswer":
      receiveVideoResponse(parsedMessage);
      break;
    case "iceCandidate":
      participants[parsedMessage.name].rtcPeer.addIceCandidate(
        parsedMessage.candidate,
        function (error) {
          if (error) {
            console.error("Error adding candidate: " + error);
            return;
          }
        }
      );
      break;
    default:
      console.error("Unrecognized message", parsedMessage);
  }
};

function register() {
  name = document.getElementById("name").value;
  var room = document.getElementById("roomName").value;

  document.getElementById("room-header").innerText = "ROOM " + room;
  document.getElementById("join").style.display = "none";
  document.getElementById("room").style.display = "block";

  var message = { 
    id: "joinRoom",
    name: name,
    room: room,
  };
  sendMessage(message); //1-1. 방 JOIN - FE - sendMessage - 시그널링 서버에 전송
}

function onNewParticipant(request) { // 2-4 클라이언트가 기존 참여자인 경우
  receiveVideo(request.name); //kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly생성자를 통해 기존 참여자에 영상을 전달받을 수신용 WebRtcPeer를 생성함
}

function receiveVideoResponse(result) {
  participants[result.name].rtcPeer.processAnswer(result.sdpAnswer, function (error) {
    if (error) return console.error(error);
  });
}

function callResponse(message) {
  if (message.response != "accepted") {
    console.info("Call not accepted by peer. Closing call");
    stop();
  } else {
    webRtcPeer.processAnswer(message.sdpAnswer, function (error) {
      if (error) return console.error(error);
    });
  }
}

function onExistingParticipants(msg) { 
  // 2-2 클라이언트가 현재 새로운 참가자
  /*  kurentoUtils.WebRtcPeer.WebRtcPeerSendonly생성자를 통해 자신의 영상을 KMS에 전달할 송신용 WebRtcPeer를 생성함,
      kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly생성자를 통해 기존 참여자에 영상을 전달받을 수신용 WebRtcPeer를 생성함
   */
  var constraints = {
    audio: true,
    video: {
      mandatory: {
        maxWidth: 320,
        maxFrameRate: 15,
        minFrameRate: 15,
      },
    },
  };
  console.log(name + " registered in room " + room);
  var participant = new Participant(name);
  participants[name] = participant;
  var video = participant.getVideoElement(); //2-2-1

  var options = {
    localVideo: video,
    mediaConstraints: constraints,
    onicecandidate: participant.onIceCandidate.bind(participant), //2-2-2
  };
  participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendonly(
    options,
    function (error) {
      if (error) {
        return console.error(error);
      }
      this.generateOffer(participant.offerToReceiveVideo.bind(participant));
    }
  );

  msg.data.forEach(receiveVideo);  
}

function leaveRoom() { // 3 방 Exit. index.html  버큰 클릭으로 이벤트 핸들  
  sendMessage({
    // 3-1 WebSocker인스턴스를 통해 메시지를 시그널링 서버에 전송한다. ->BE
    id: "leaveRoom",
  });

  for (var key in participants) {
    participants[key].dispose();
  }

  document.getElementById("join").style.display = "block";
  document.getElementById("room").style.display = "none";

  ws.close();
}

function receiveVideo(sender) { //2-5 기존 참가자인 경우
  //kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly생성자를 통해 기존 참여자에 영상을 전달받을 수신용 WebRtcPeer를 생성함
  var participant = new Participant(sender);
  participants[sender] = participant;
  var video = participant.getVideoElement();

  var options = {
    remoteVideo: video,
    onicecandidate: participant.onIceCandidate.bind(participant),
  };

  participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options, function (error) {
    if (error) {
      return console.error(error);
    }
    this.generateOffer(participant.offerToReceiveVideo.bind(participant));
  });
}

function onParticipantLeft(request) { //3-10 나간 참여자에 연결을 종료 및 참여자 리스트에서 삭제
  console.log("Participant " + request.name + " left");
  var participant = participants[request.name]; 
  participant.dispose(); // 참여자리스트에서 찾아 WebRtc연결을 종료
  delete participants[request.name];  //참여자 리스트에서 삭제한다.
}

function sendMessage(message) {
  var jsonMessage = JSON.stringify(message);
  console.log("Sending message: " + jsonMessage);
  ws.send(jsonMessage);
}
