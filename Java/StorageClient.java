public interface StorageClient {
    void put(String path, byte[] data) throws Exception;
    byte[] get(String path) throws Exception;
    String presignGet(String path, int seconds) throws Exception;
}

public class S3StorageClient implements StorageClient {
    // Pseudocode - wrap AWS SDK or other object store
    private final String bucket;
    // constructor omitted

    @Override
    public void put(String path, byte[] data) throws Exception {
        // translate path into bucket + key and upload
        // PutObjectRequest, handle retries
    }

    @Override
    public byte[] get(String path) throws Exception {
        // getObject and return bytes
        return new byte[0];
    }

    @Override
    public String presignGet(String path, int seconds) throws Exception {
        // generate presigned URL
        return "https://signed-url.example.com/" + path;
    }
}
