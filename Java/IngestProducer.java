// IngestProducer.java
import org.apache.kafka.clients.producer.*;
import java.util.*;
import java.nio.file.*;
import java.security.MessageDigest;

public class IngestProducer {
    private final Producer<String, String> kafka;
    private final StorageClient storage;
    private final String topic;

    public IngestProducer(Producer<String,String> kafka, StorageClient storage, String topic){
        this.kafka = kafka;
        this.storage = storage;
        this.topic = topic;
    }

    public void ingest(byte[] frame, IngestMetadata meta) throws Exception {
        String path = buildStoragePath(meta);
        byte[] compressed = Compression.lz4Compress(frame);
        storage.put(path, compressed);

        String sha = sha256Hex(compressed);
        String message = buildMessageJson(meta, path, sha);

        ProducerRecord<String,String> record = new ProducerRecord<>(topic, meta.getSatelliteId(), message);
        kafka.send(record, (metadata, ex) -> {
            if (ex != null) {
                // implement retry/backoff or write to DLQ
                ex.printStackTrace();
            } else {
                // optionally update metadata DB asynchronously
            }
        });
    }

    private String buildStoragePath(IngestMetadata meta) {
        // e.g. sat/2025/09/14/SAT1/frame_0001.lz4
        return String.format("%s/%04d/%02d/%02d/%s/frame_%d.lz4",
            meta.getSatelliteId(),
            meta.getCaptureTime().getYear(),
            meta.getCaptureTime().getMonthValue(),
            meta.getCaptureTime().getDayOfMonth(),
            meta.getSatelliteId(),
            meta.getFrameSeq()
        );
    }

    private String sha256Hex(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(data);
        return javax.xml.bind.DatatypeConverter.printHexBinary(digest).toLowerCase();
    }

    private String buildMessageJson(IngestMetadata meta, String path, String sha) {
        // build a short JSON payload; for readability keep as string
        return String.format("{\"message_id\":\"%s\",\"satellite_id\":\"%s\",\"storage_path\":\"%s\",\"checksum\":\"%s\"}",
            UUID.randomUUID().toString(),
            meta.getSatelliteId(),
            path,
            sha
        );
    }
}
