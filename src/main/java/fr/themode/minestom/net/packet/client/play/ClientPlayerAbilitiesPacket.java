package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerAbilitiesPacket extends ClientPlayPacket {

    public byte flags;
    public float flyingSpeed;
    public float walkingSpeed;

    @Override
    public void read(Buffer buffer) {
        this.flags = buffer.getByte();
        this.flyingSpeed = buffer.getFloat();
        this.walkingSpeed = buffer.getFloat();
    }
}
