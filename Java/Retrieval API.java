@RestController
@RequestMapping("/api/v1/data")
public class RetrievalController {
    private final MetadataDao metadata;
    private final StorageClient storage;

    @GetMapping("/files")
    public List<FileMetadata> listFiles(@RequestParam Map<String,String> filters) {
        // apply RBAC checks, then query metadata DB
        return metadata.query(filters);
    }

    @GetMapping("/file/{id}/download")
    public ResponseEntity<?> download(@PathVariable String id, Principal user) {
        // check ACLs
        FileMetadata md = metadata.getById(UUID.fromString(id));
        if (!Auth.checkAccess(user, md)) return ResponseEntity.status(403).build();
        String url = storage.presignGet(md.getStoragePath(), 60 * 15);
        return ResponseEntity.status(302).location(URI.create(url)).build();
    }
}
