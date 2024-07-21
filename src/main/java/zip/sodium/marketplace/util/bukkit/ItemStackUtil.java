package zip.sodium.marketplace.util.bukkit;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdCompressCtx;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.msgpack.core.MessagePack;
import zip.sodium.marketplace.util.ReflectionUtil;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public final class ItemStackUtil {
    private ItemStackUtil() {}

    private static final Class<?> FRIENDLY_BYTE_BUF_CLASS = Objects.requireNonNullElseGet(
            ReflectionUtil.tryGetClass("net.minecraft.network.PacketDataSerializer"),
            () -> ReflectionUtil.tryGetClass("net.minecraft.network.FriendlyByteBuf")
    );

    private static final Constructor<?> FRIENDLY_BYTE_BUF_CONSTRUCTOR = ReflectionUtil.tryGetConstructor(
            FRIENDLY_BYTE_BUF_CLASS,
            ByteBuf.class
    );

    private static final Class<?> ITEMSTACK_CLASS = ReflectionUtil.tryGetClass("net.minecraft.world.item.ItemStack");

    private static final Method WRITE_ITEM_METHOD = Objects.requireNonNullElseGet(
            ReflectionUtil.tryGetMethod(
                    FRIENDLY_BYTE_BUF_CLASS,
                    "a",
                    ITEMSTACK_CLASS
            ),
            () -> ReflectionUtil.tryGetMethod(
                    FRIENDLY_BYTE_BUF_CLASS,
                    "writeItem",
                    ITEMSTACK_CLASS
            )
    );

    private static final Method READ_ITEM_METHOD = Objects.requireNonNullElseGet(
            ReflectionUtil.tryGetMethod(
                    FRIENDLY_BYTE_BUF_CLASS,
                    "r"
            ),
            () -> ReflectionUtil.tryGetMethod(
                    FRIENDLY_BYTE_BUF_CLASS,
                    "readItem"
            )
    );

    private static final Class<?> CRAFT_ITEMSTACK_CLASS = ReflectionUtil.tryGetClass("org.bukkit.craftbukkit" + ReflectionUtil.MINECRAFT_VERSION + "inventory.CraftItemStack");

    private static final Field ITEMSTACK_NMS_HANDLE_FIELD = ReflectionUtil.tryGetField(
            CRAFT_ITEMSTACK_CLASS,
            "handle"
    );

    private static final Method ITEMSTACK_AS_CRAFT_MIRROR_METHOD = ReflectionUtil.tryGetMethod(
            CRAFT_ITEMSTACK_CLASS,
            "asCraftMirror",
            ITEMSTACK_CLASS
    );

    private static final Method ITEMSTACK_AS_NMS_COPY_METHOD = ReflectionUtil.tryGetMethod(
            CRAFT_ITEMSTACK_CLASS,
            "asNMSCopy",
            ItemStack.class
    );

    private static final Field EMPTY_ITEMSTACK_FIELD = Objects.requireNonNullElseGet(
            ReflectionUtil.tryGetField(
                    ITEMSTACK_CLASS,
                    "f"
            ),
            () -> ReflectionUtil.tryGetField(
                    ITEMSTACK_CLASS,
                    "EMPTY"
            )
    );

    private static Object unwrapToNMS(final ItemStack itemStack) throws IllegalAccessException, InvocationTargetException {
        if (CRAFT_ITEMSTACK_CLASS.isInstance(itemStack)) {
            final var handle = ITEMSTACK_NMS_HANDLE_FIELD.get(itemStack);

            return handle != null ? handle : EMPTY_ITEMSTACK_FIELD.get(null);
        } else {
            return ITEMSTACK_AS_NMS_COPY_METHOD.invoke(null, itemStack);
        }
    }

    private static byte @NotNull [] compress(final byte @NotNull [] bytes, int offset) throws IOException {
        final int bytesSize = bytes.length - offset;

        final var packer = MessagePack.newDefaultBufferPacker();
        packer.packBinaryHeader(bytes.length);

        final byte[] compressed;
        try (final var ctx = new ZstdCompressCtx()) {
            final byte[] dst = new byte[(int) Zstd.compressBound(bytesSize)];
            final int size = ctx.compressByteArray(
                    dst,
                    0,
                    dst.length,
                    bytes,
                    offset,
                    bytesSize
            );

            compressed = Arrays.copyOfRange(
                    dst,
                    0,
                    size
            );
        }

        packer.writePayload(compressed);

        return packer.toByteArray();
    }

    private static byte @NotNull [] decompress(final byte @NotNull [] bytes) throws IOException {
        try (final var unpacker = MessagePack.newDefaultUnpacker(bytes)) {
            final var decompressedLength = unpacker.unpackBinaryHeader();
            return Zstd.decompress(
                    unpacker.readPayload((int) (bytes.length - unpacker.getTotalReadBytes())),
                    decompressedLength
            );
        }
    }

    public static byte[] serialize(final @NotNull ItemStack item) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final var buf = (ByteBuf) FRIENDLY_BYTE_BUF_CONSTRUCTOR.newInstance(Unpooled.buffer());

        WRITE_ITEM_METHOD.invoke(buf, unwrapToNMS(item));

        byte[] bytes;
        int offset;

        if (buf.hasArray()) {
            bytes = buf.array();
            offset = buf.arrayOffset();
        } else {
            bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            offset = 0;
        }

        return compress(bytes, offset);
    }

    public static ItemStack deserialize(final byte @NotNull [] bytes) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final var content = decompress(bytes);
        final var buf = (ByteBuf) FRIENDLY_BYTE_BUF_CONSTRUCTOR.newInstance(Unpooled.wrappedBuffer(content));

        return (ItemStack) ITEMSTACK_AS_CRAFT_MIRROR_METHOD.invoke(null, READ_ITEM_METHOD.invoke(buf));
    }

    public static ItemStack editMeta(final ItemStack item, final Consumer<ItemMeta> consumer) {
        final var meta = item.getItemMeta();
        if (meta != null) {
            consumer.accept(meta);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack of(final Material material) {
        return new ItemStack(material);
    }

    public static ItemStack of(final Material material, final Consumer<ItemMeta> consumer) {
        return editMeta(
                of(material),
                consumer
        );
    }

    public static ItemStack of(final Material material, final String displayName) {
        return of(material, meta -> meta.setDisplayName(displayName));
    }
}
