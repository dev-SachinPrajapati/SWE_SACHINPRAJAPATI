import org.apache.kafka.clients.consumer.*;
import java.time.Duration;
import java.util.*;

public class StreamConsumer {
    private final Consumer<String,String> consumer;
    private final StorageClient storage;
    private final MetadataDao metadata;
    private final Transformer transformer;

    public StreamConsumer(Consumer<String,String> consumer,
                          StorageClient storage,
                          MetadataDao metadata,
                          Transformer transformer) {
        this.consumer = consumer;
        this.storage = storage;
        this.metadata = metadata;
        this.transformer = transformer;
    }

    public void run() {
        consumer.subscribe(Arrays.asList("sat.raw"));
        while(true) {
            ConsumerRecords<String,String> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String,String> rec : records) {
                try {
                    // parse message
                    Message msg = Message.parse(rec.value());
                    if (metadata.isProcessed(msg.getStoragePath())) {
                        consumer.commitSync(Collections.singletonMap(new TopicPartition(rec.topic(), rec.partition()),
                                                                     new OffsetAndMetadata(rec.offset()+1)));
                        continue;
                    }
                    byte[] raw = storage.get(msg.getStoragePath());
                    byte[] derived = transformer.level1Transform(raw);
                    String derivedPath = msg.getStoragePath().replace("/raw/","/level1/");
                    storage.put(derivedPath, derived);
                    metadata.markProcessed(msg.getStoragePath(), derivedPath);
                    consumer.commitSync(Collections.singletonMap(new TopicPartition(rec.topic(), rec.partition()),
                                                                 new OffsetAndMetadata(rec.offset()+1)));
                } catch (Exception e) {
                    e.printStackTrace();
                    // decent production code uses DLQ and exponential backoff
                }
            }
        }
    }
}
