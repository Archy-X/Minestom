package fr.themode.minestom.entity;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.Main;
import fr.themode.minestom.Viewable;
import fr.themode.minestom.event.Callback;
import fr.themode.minestom.event.CancellableEvent;
import fr.themode.minestom.event.Event;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.play.SetPassengersPacket;
import fr.themode.minestom.utils.Utils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Entity implements Viewable {

    private static AtomicInteger lastEntityId = new AtomicInteger();

    // Metadata
    protected static final byte METADATA_BYTE = 0;
    protected static final byte METADATA_VARINT = 1;
    protected static final byte METADATA_FLOAT = 2;
    protected static final byte METADATA_STRING = 3;
    protected static final byte METADATA_CHAT = 4;
    protected static final byte METADATA_OPTCHAT = 5;
    protected static final byte METADATA_SLOT = 6;
    protected static final byte METADATA_BOOLEAN = 7;

    protected Instance instance;
    protected double lastX, lastY, lastZ;
    protected double x, y, z;
    protected float yaw, pitch;
    private int id;

    protected Entity vehicle;
    private Map<Class<Event>, Callback> eventCallbacks = new ConcurrentHashMap<>();
    private Set<Player> viewers = new CopyOnWriteArraySet<>();
    private Set<Entity> passengers = new CopyOnWriteArraySet<>();

    // Metadata
    protected boolean onFire;
    protected UUID uuid;
    private boolean isActive; // False if entity has only been instanced without being added somewhere
    protected boolean crouched;
    private boolean shouldRemove;
    protected boolean UNUSED_METADATA;
    protected boolean sprinting;
    protected boolean swimming;
    protected boolean invisible;
    protected boolean glowing;
    protected boolean usingElytra;
    protected int air = 300;
    protected String customName = "";
    protected boolean customNameVisible;
    protected boolean silent;
    protected boolean noGravity;
    protected Pose pose = Pose.STANDING;
    private int entityType;
    private long lastUpdate;

    public Entity(int entityType) {
        this.id = generateId();
        this.entityType = entityType;
        this.uuid = UUID.randomUUID();
    }


    private static int generateId() {
        return lastEntityId.incrementAndGet();
    }

    public abstract void update();

    @Override
    public void addViewer(Player player) {
        this.viewers.add(player);
    }

    @Override
    public void removeViewer(Player player) {
        synchronized (viewers) {
            if (!viewers.contains(player))
                return;
            this.viewers.remove(player);
            // TODO send packet to remove entity
        }
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    public void tick() {
        if (shouldUpdate()) {
            update();
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    public <E extends Event> void setEventCallback(Class<E> eventClass, Callback<E> callback) {
        this.eventCallbacks.put((Class<Event>) eventClass, callback);
    }

    public <E extends Event> Callback<E> getEventCallback(Class<E> eventClass) {
        return this.eventCallbacks.getOrDefault(eventClass, null);
    }

    public <E extends Event> void callEvent(Class<E> eventClass, E event) {
        Callback<E> callback = getEventCallback(eventClass);
        if (callback != null)
            getEventCallback(eventClass).run(event);
    }

    public <E extends CancellableEvent> void callCancellableEvent(Class<E> eventClass, E event, Runnable runnable) {
        callEvent(eventClass, event);
        if (!event.isCancelled()) {
            runnable.run();
        }
    }

    public int getEntityId() {
        return id;
    }

    public int getEntityType() {
        return entityType;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isActive() {
        return isActive;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        if (instance == null)
            throw new IllegalArgumentException("instance cannot be null!");

        if (this.instance != null) {
            this.instance.removeEntity(this);
        }

        this.isActive = true;
        this.instance = instance;
        instance.addEntity(this);
    }

    public float getDistance(Entity entity) {
        return (float) Math.sqrt(Math.pow(entity.getX() - getX(), 2) + Math.pow(entity.getY() - getY(), 2) + Math.pow(entity.getZ() - getZ(), 2));
    }

    public void addPassenger(Entity entity) {
        this.passengers.add(entity);
        entity.vehicle = this;
        if (instance != null) {
            SetPassengersPacket passengersPacket = new SetPassengersPacket();
            passengersPacket.vehicleEntityId = getEntityId();
            passengersPacket.passengersId = new int[]{entity.getEntityId()};
            sendPacketToViewers(passengersPacket);
        }
    }

    public boolean hasPassenger() {
        return !passengers.isEmpty();
    }

    public Set<Entity> getPassengers() {
        return Collections.unmodifiableSet(passengers);
    }

    public void refreshPosition(double x, double y, double z) {
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        this.x = x;
        this.y = y;
        this.z = z;

        Instance instance = getInstance();
        if (instance != null) {
            Chunk lastChunk = instance.getChunkAt(lastX, lastZ);
            Chunk newChunk = instance.getChunkAt(x, z);
            if (lastChunk != null && newChunk != null && lastChunk != newChunk) {
                synchronized (lastChunk) {
                    synchronized (newChunk) {
                        lastChunk.removeEntity(this);
                        newChunk.addEntity(this);
                    }
                }
            }
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void remove() {
        this.shouldRemove = true;
    }

    protected void sendPacketToViewers(ServerPacket packet) {
        getViewers().forEach(player -> player.getPlayerConnection().sendPacket(packet));
    }

    public Buffer getMetadataBuffer() {
        Buffer buffer = Buffer.create();
        fillMetadataIndex(buffer, 0);
        /*Utils.writeVarInt(buffer, 0);
        Utils.writeString(buffer, customName);
        buffer.putBoolean(silent);
        buffer.putBoolean(noGravity);
        Utils.writeVarInt(buffer, pose.ordinal());*/

        // Chicken test
        /*buffer.putByte((byte) 0); // Hand states
        buffer.putFloat(10f); // Health
        Utils.writeVarInt(buffer, 0); // Potion effect color
        buffer.putBoolean(false); // Potion effect ambient
        Utils.writeVarInt(buffer, 0); // Arrow count in entity
        buffer.putByte((byte) 0); // (Insentient)
        buffer.putBoolean(false); // baby (Ageable)*/
        return buffer;
    }

    private void fillMetadataIndex(Buffer buffer, int index) {
        switch (index) {
            case 0:
                fillStateMetadata(buffer);
                break;
            case 1:
                fillAirTickMetaData(buffer);
                break;
            case 2:
                fillCustomNameMetaData(buffer);
                break;
        }
    }

    private void fillStateMetadata(Buffer buffer) {
        buffer.putByte((byte) 0);
        buffer.putByte(METADATA_BYTE);
        byte index0 = 0;
        if (onFire)
            index0 += 1;
        if (crouched)
            index0 += 2;
        if (UNUSED_METADATA)
            index0 += 4;
        if (sprinting)
            index0 += 8;
        if (swimming)
            index0 += 16;
        if (invisible)
            index0 += 32;
        if (glowing)
            index0 += 64;
        if (usingElytra)
            index0 += 128;
        buffer.putByte(index0);
    }

    private void fillAirTickMetaData(Buffer buffer) {
        buffer.putByte((byte) 1);
        buffer.putByte(METADATA_VARINT);
        Utils.writeVarInt(buffer, air);
    }

    private void fillCustomNameMetaData(Buffer buffer) {
        buffer.putByte((byte) 2);
        buffer.putByte(METADATA_CHAT);
        Utils.writeString(buffer, customName);
    }

    private boolean shouldUpdate() {
        return (float) (System.currentTimeMillis() - lastUpdate) >= Main.TICK_MS * 0.9f; // Margin of error
    }

    public enum Pose {
        STANDING,
        FALL_FLYING,
        SLEEPING,
        SWIMMING,
        SPIN_ATTACK,
        SNEAKING,
        DYING;
    }

    protected boolean shouldRemove() {
        return shouldRemove;
    }
}
