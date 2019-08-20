package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientEntityActionPacket extends ClientPlayPacket {

    public int playerId;
    public Action action;
    public int horseJumpBoost;

    @Override
    public void read(Buffer buffer) {
        this.playerId = Utils.readVarInt(buffer);
        this.action = Action.values()[Utils.readVarInt(buffer)];
        this.horseJumpBoost = Utils.readVarInt(buffer);
    }

    public enum Action {
        START_SNEAKING,
        STOP_SNEAKING,
        LEAVE_BED,
        START_SPRINTING,
        STOP_SPRINTING,
        START_JUMP_HORSE,
        STOP_JUMP_HORSE,
        OPEN_HORSE_INVENTORY,
        START_FLYING_ELYTRA;
    }

}
