package org.kurento.tutorial.groupcall;

import java.io.IOException;

import org.kurento.client.IceCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * 
 * @author Ivan Gracia (izanmail@gmail.com)
 * @since 4.3.1
 */
public class CallHandler extends TextWebSocketHandler {

  private static final Logger log = LoggerFactory.getLogger(CallHandler.class);

  private static final Gson gson = new GsonBuilder().create();

  @Autowired
  private RoomManager roomManager;

  @Autowired
  private UserRegistry registry;

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);

    final UserSession user = registry.getBySession(session);

    if (user != null) {
      log.debug("Incoming message from user '{}': {}", user.getName(), jsonMessage);
    } else {
      log.debug("Incoming message from new user: {}", jsonMessage);
    }

    switch (jsonMessage.get("id").getAsString()) {
      case "joinRoom":
        joinRoom(jsonMessage, session); // 1-2 joinRoom()
        break;
      case "receiveVideoFrom":
        final String senderName = jsonMessage.get("sender").getAsString();
        final UserSession sender = registry.getByName(senderName);
        final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
        user.receiveVideoFrom(sender, sdpOffer);
        break;
      case "leaveRoom": //3-2 FE에서 메시지 전달 받음
        leaveRoom(user); //3-3
        break;
      case "onIceCandidate":
        JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();

        if (user != null) {
          IceCandidate cand = new IceCandidate(candidate.get("candidate").getAsString(),
              candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
          user.addCandidate(cand, jsonMessage.get("name").getAsString());
        }
        break;
      default:
        break;
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    UserSession user = registry.removeBySession(session);
    roomManager.getRoom(user.getRoomName()).leave(user);
  }

  private void joinRoom(JsonObject params, WebSocketSession session) throws IOException { // 1-3 전달받은 joinRoom  메시지에서 accessToken 값과 conferenceID를 통해 유저를 인증
    final String roomName = params.get("room").getAsString();
    final String name = params.get("name").getAsString();
    log.info("PARTICIPANT {}: trying to join room {}", name, roomName);

    Room room = roomManager.getRoom(roomName); //1-4 RoomManager의 getRoom()에서 방 생성하거나 가져옴. 1-5 getRoom
    final UserSession user = room.join(name, session); //1-6 해당 room의 MediaPipeline 인스턴스를 가지는 webRTC 유저세션을 생성
    registry.register(user);
  }

  private void leaveRoom(UserSession user) throws IOException { 
    final Room room = roomManager.getRoom(user.getRoomName()); //3-3 세션값을 확인하여, 메시지를 전달한 유저를 식별하여 이전에 생성한 유저세션을 통해
    room.leave(user); // 3-4 KMS에서 release되도록 처리.
    if (room.getParticipants().isEmpty()) { 
      roomManager.removeRoom(room); //3-7 방 삭제
    }
  }
}
