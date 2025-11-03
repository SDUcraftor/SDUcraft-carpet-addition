package top.sducraft.config.rule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class JoinMessageConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(JoinMessageConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String SAVE_FILE_NAME = "sducraft_dialog_seen_data.json";

    private static class DialogSeenData {
        String dialogFileHash;
        Set<UUID> seenPlayerUUIDs = new HashSet<>();
    }

    private static DialogSeenData data = new DialogSeenData();
    private static Path dialogFilePath;

    private static Path getSavePath(MinecraftServer server) {
        return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data").resolve("carpet-sducraft-addition").resolve(SAVE_FILE_NAME);
    }

    public static void initialize(MinecraftServer server) {
        dialogFilePath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.DATAPACK_DIR)
                .resolve("sdu/data/sdu/dialog/notice.json");

        Path path = getSavePath(server);
        if (!Files.exists(path)) {
            LOGGER.info("Dialog seen data file not found. A new one will be created.");
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            DialogSeenData loadedData = GSON.fromJson(reader, DialogSeenData.class);
            if (loadedData != null) {
                data = loadedData;
                LOGGER.info("Loaded dialog seen data. Hash: [{}], Seen players: {}", data.dialogFileHash, data.seenPlayerUUIDs.size());
            }
        } catch (IOException e) {
            LOGGER.error("Could not load dialog seen data", e);
        }
    }

    private static void save(MinecraftServer server) {
        Path path = getSavePath(server);
        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Could not save dialog seen data", e);
        }
    }

    public static boolean shouldShowDialog(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        String currentHash = calculateDialogHash();

        if (currentHash == null) {
            return false;
        }
        if (!currentHash.equals(data.dialogFileHash)) {
            LOGGER.info("Dialog file has changed (or is new). Old hash: [{}], New hash: [{}]. Resetting seen players list.", data.dialogFileHash, currentHash);
            data.dialogFileHash = currentHash;
            data.seenPlayerUUIDs.clear();
            save(server);
        }

        return !data.seenPlayerUUIDs.contains(player.getUUID());
    }

    public static void markAsSeen(ServerPlayer player) {
        if (data.seenPlayerUUIDs.add(player.getUUID())) {
            save(player.getServer());
        }
    }

    private static String calculateDialogHash() {
        if (!Files.exists(dialogFilePath)) {
            LOGGER.warn("Dialog file not found, cannot calculate hash: {}", dialogFilePath);
            return null;
        }
        try (InputStream is = new FileInputStream(dialogFilePath.toFile())) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            DigestInputStream dis = new DigestInputStream(is, md);
            // 读取文件以计算哈希
            while (dis.read() != -1) ;
            byte[] digest = md.digest();
            // 转换为十六进制字符串
            StringBuilder result = new StringBuilder();
            for (byte b : digest) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            LOGGER.error("Failed to calculate hash for dialog file", e);
            return null;
        }
    }
}