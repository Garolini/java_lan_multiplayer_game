package io.github.java_lan_multiplayer.common.messages.shipBuilding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.java_lan_multiplayer.common.messages.GameMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReserveTileMessage implements GameMessage {

    private int slotId;

    public ReserveTileMessage() {}

    public ReserveTileMessage(int slotId) {
       this.slotId = slotId;
    }

    public int getSlotId() {
        return slotId;
    }

    @Override
    public String getType() {
        return "reserve_tile";
    }
}
