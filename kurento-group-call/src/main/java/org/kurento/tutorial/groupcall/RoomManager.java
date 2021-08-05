package org.kurento.tutorial.groupcall;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.kurento.client.KurentoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Ivan Gracia (izanmail@gmail.com)
 * @since 4.3.1
 */
public class RoomManager {

  private final Logger log = LoggerFactory.getLogger(RoomManager.class);

  @Autowired
  private KurentoClient kurento;

  private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();

  /**
   * Looks for a room in the active room list.
   *
   * @param roomName
   *          the name of the room
   * @return the room if it was already created, or a new one if it is the first time this room is
   *         accessed
   */
  public Room getRoom(String roomName) {
    log.debug("Searching for room {}", roomName);
    Room room = rooms.get(roomName); //room이 이미 존재할 경우 가져옴

    if (room == null) {
      log.debug("Room {} not existent. Will create now!", roomName);
      room = new Room(roomName, kurento.createMediaPipeline()); //1-5 kurentoClient::createMediaPipeline 메소드로 얻은 MediaPipeline 인스턴스로
      //kMS에 접근 가능한 room을 생성함
      rooms.put(roomName, room);
    }
    log.debug("Room {} found!", roomName);
    return room; 
  }

  /**
   * Removes a room from the list of available rooms.
   *
   * @param room
   *          the room to be removed
   */
  public void removeRoom(Room room) { ////3-8 방 삭제
    this.rooms.remove(room.getName());
    room.close();
    log.info("Room {} removed and closed", room.getName());
  }

}
