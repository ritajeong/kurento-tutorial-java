/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.kurento.tutorial.magicmirror;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.kurento.client.*;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Magic Mirror handler (application and media logic).
 *
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @since 5.0.0
 */
public class MagicMirrorHandler extends TextWebSocketHandler {

  private static final Gson gson = new GsonBuilder().create();
  private final Logger log = LoggerFactory.getLogger(MagicMirrorHandler.class);

  private final ConcurrentHashMap<String, UserSession> users = new ConcurrentHashMap<>();

  @Autowired
  private KurentoClient kurento;

  private WebRtcEndpoint webRtcEndpoint;

  private ImageOverlayFilter imageOverlayFilter;
  private int imageIndex;
  private String[] imageUris = 
    {"/home/ubuntu/images/Presentation/Presentation01.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation02.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation03.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation04.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation05.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation06.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation07.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation08.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation09.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation10.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation11.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation12.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation13.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation14.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation15.jpeg",
    "/home/ubuntu/images/Presentation/Presentation16.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation17.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation18.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation19.jpeg", 
    "/home/ubuntu/images/Presentation/Presentation20.jpeg",
    "/home/ubuntu/images/Presentation/Presentation21.jpeg",
    "/home/ubuntu/images/Presentation/Presentation22.jpeg",
    "/home/ubuntu/images/Presentation/Presentation23.jpeg",
    "/home/ubuntu/images/Presentation/Presentation24.jpeg",
    "/home/ubuntu/images/Presentation/Presentation25.jpeg",
    "/home/ubuntu/images/Presentation/Presentation26.jpeg",
    "/home/ubuntu/images/Presentation/Presentation27.jpeg",
    "/home/ubuntu/images/Presentation/Presentation28.jpeg",
    "/home/ubuntu/images/Presentation/Presentation29.jpeg",
    "/home/ubuntu/images/Presentation/Presentation30.jpeg"};
  private float offsetXPercent = 0.03f;
  private float offsetYPercent = 0.18f;
  private float widthPrecent = 0.65f;
  private float heightPrecent = 0.65f;
  private boolean keepAspectRatio = false;
  private boolean imageCenter = true;
  private boolean isFullScreen = false;
  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);

    log.debug("Incoming message: {}", jsonMessage);

    switch (jsonMessage.get("id").getAsString()) {
      case "start":
        start(session, jsonMessage);
        break;
      case "stop": {
        UserSession user = users.remove(session.getId());
        if (user != null) {
          user.release();
        }
        break;
      } case "prev":{
        System.out.println("prev");
        prev(session, jsonMessage);
        break;
      }
      case "next":{
        System.out.println("next");
        next(session, jsonMessage);
        break;
      }
      case "fullScreen": {
        System.out.println("fullScreen");
        fullScreen(session, jsonMessage);
        break;
      }
      case "onIceCandidate": {
        JsonObject jsonCandidate = jsonMessage.get("candidate").getAsJsonObject();

        UserSession user = users.get(session.getId());
        if (user != null) {
          IceCandidate candidate = new IceCandidate(jsonCandidate.get("candidate").getAsString(),
              jsonCandidate.get("sdpMid").getAsString(),
              jsonCandidate.get("sdpMLineIndex").getAsInt());
          user.addCandidate(candidate);
        }
        break;
      }
      default:
        sendError(session, "Invalid message with id " + jsonMessage.get("id").getAsString());
        break;
    }
  }

  private void start(final WebSocketSession session, JsonObject jsonMessage) {
    try {
      // User session
      UserSession user = new UserSession();
      System.out.println("[start] kurento "+kurento);
      MediaPipeline pipeline = kurento.createMediaPipeline();
      user.setMediaPipeline(pipeline);
      //WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
      webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
      user.setWebRtcEndpoint(webRtcEndpoint);
      users.put(session.getId(), user);

      // ICE candidates
      webRtcEndpoint.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {

        @Override
        public void onEvent(IceCandidateFoundEvent event) {
          JsonObject response = new JsonObject();
          response.addProperty("id", "iceCandidate");
          response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
          try {
            synchronized (session) {
              session.sendMessage(new TextMessage(response.toString()));
            }
          } catch (IOException e) {
            log.debug(e.getMessage());
          }
        }
      });

      // // Media logic
      // FaceOverlayFilter faceOverlayFilter = new FaceOverlayFilter.Builder(pipeline).build();

      // //String appServerUrl = System.getProperty("app.server.url",
      // //    MagicMirrorApp.DEFAULT_APP_SERVER_URL);
      // String appServerUrl = "http://files.openvidu.io";
      // faceOverlayFilter.setOverlayedImage(appServerUrl + "/img/mario-wings.png", -0.35F, -1.2F,
      //     1.6F, 1.6F);

      // webRtcEndpoint.connect(faceOverlayFilter);
      // faceOverlayFilter.connect(webRtcEndpoint);

      //image 필터 씌우기
      imageOverlayFilter = new ImageOverlayFilter.Builder(pipeline).build();
      String imageId = "testImage" + Integer.toString(imageIndex);
      String imageUri = imageUris[imageIndex];
      System.out.println("image start imageId: "+imageId+" imageUri: "+imageUri);
      //imageOverlayFilter.removeImage(imageId);
      imageOverlayFilter.addImage(imageId, imageUri, offsetXPercent, offsetYPercent, widthPrecent, heightPrecent, keepAspectRatio, imageCenter);
      webRtcEndpoint.connect(imageOverlayFilter);
      imageOverlayFilter.connect(webRtcEndpoint);
      // console.log("imageIndex : " + imageIndex);



      // SDP negotiation (offer and answer)
      String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
      String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);

      JsonObject response = new JsonObject();
      response.addProperty("id", "startResponse");
      response.addProperty("sdpAnswer", sdpAnswer);

      synchronized (session) {
        session.sendMessage(new TextMessage(response.toString()));
      }

      webRtcEndpoint.gatherCandidates();

    } catch (Throwable t) {
      sendError(session, t.getMessage());
    }
  }

  private void prev(final WebSocketSession session, JsonObject jsonMessage) {
    try {
      if (imageIndex > 0) {
        String removeImageId = "testImage" + Integer.toString(imageIndex);
        String removeImageUri = imageUris[imageIndex];
        imageIndex--;
        String addImageId = "testImage" + Integer.toString(imageIndex);
        String addImageUri = imageUris[imageIndex];

        imageOverlayFilter.addImage(addImageId, addImageUri, offsetXPercent, offsetYPercent, widthPrecent, heightPrecent, keepAspectRatio, imageCenter);
        imageOverlayFilter.removeImage(removeImageId);
        webRtcEndpoint.connect(imageOverlayFilter);
        imageOverlayFilter.connect(webRtcEndpoint);
      } else {
        JsonObject response = new JsonObject();
        response.addProperty("prev", "맨 처음 사진입니다.");


        synchronized (session) {
          session.sendMessage(new TextMessage(response.toString()));
        }
      }
    } catch (Throwable t) {
      sendError(session, t.getMessage());
    }
  }

  private void next(final WebSocketSession session, JsonObject jsonMessage) {
    try{
      if(imageIndex < imageUris.length){
        String removeImageId = "testImage" + Integer.toString(imageIndex);
        String removeImageUri = imageUris[imageIndex];
        imageIndex++;
        String addImageId = "testImage" + Integer.toString(imageIndex);
        String addImageUri = imageUris[imageIndex];

        imageOverlayFilter.addImage(addImageId, addImageUri, offsetXPercent, offsetYPercent, widthPrecent, heightPrecent, keepAspectRatio, imageCenter);
        imageOverlayFilter.removeImage(removeImageId);
        webRtcEndpoint.connect(imageOverlayFilter);
        imageOverlayFilter.connect(webRtcEndpoint);
      }else{
        JsonObject response = new JsonObject();
        response.addProperty("next", "마지막 사진입니다.");


        synchronized (session) {
          session.sendMessage(new TextMessage(response.toString()));
        }
      }
    }catch (Throwable t) {
      sendError(session, t.getMessage());
    }
  }

  private void fullScreen(final WebSocketSession session, JsonObject jsonMessage) {
    if (isFullScreen) {
      offsetXPercent = 0.03f;
      offsetYPercent = 0.18f;
      widthPrecent = 0.65f;
      heightPrecent = 0.65f;
      isFullScreen = false;
    } else {
      offsetXPercent = 0.0f;
      offsetYPercent = 0.0f;
      widthPrecent = 1.0f;
      heightPrecent = 1.0f;
      isFullScreen = true;
    }
    String imageId = "testImage" + Integer.toString(imageIndex);
    String imageUri = imageUris[imageIndex];
    imageOverlayFilter.removeImage(imageId);
    imageOverlayFilter.addImage(imageId, imageUri, offsetXPercent, offsetYPercent, widthPrecent, heightPrecent, keepAspectRatio, imageCenter);
    webRtcEndpoint.connect(imageOverlayFilter);
    imageOverlayFilter.connect(webRtcEndpoint);
  }


  private void sendError(WebSocketSession session, String message) {
    try {
      JsonObject response = new JsonObject();
      response.addProperty("id", "error");
      response.addProperty("message", message);
      session.sendMessage(new TextMessage(response.toString()));
    } catch (IOException e) {
      log.error("Exception sending message", e);
    }
  }
}
