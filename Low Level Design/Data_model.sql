-- # Data model (metadata)
  
sat_data_files (
  id UUID PRIMARY KEY,              -- unique record identifier (UUID ensures global uniqueness)
  satellite_id STRING,              -- which satellite produced the data (e.g. SAT-1, IRS-2)
  capture_start TIMESTAMP,          -- when data/image capture started
  capture_end TIMESTAMP,            -- when data/image capture ended
  orbit INT,                        -- orbit number of the satellite at that time
  product_type STRING,              -- data stage: raw | level1 | level2 | tile
  storage_path STRING,              -- physical location in object storage (S3/MinIO/...)
  size_bytes BIGINT,                -- file size in bytes
  checksum_sha256 STRING,           -- SHA-256 hash for file integrity verification
  ingestion_status ENUM(            -- current pipeline state of the file
     'received',                    -- data landed at gateway
     'stored',                      -- safely stored in object storage
     'processed',                   -- transformed to next level (e.g. level1)
     'archived',                    -- moved to long-term cold storage
     'failed'                       -- error occurred in processing
  ),
  partition_key STRING,             -- partition identifier for fast queries (e.g. yyyy/mm/dd/hr+satelliteId)
  created_at TIMESTAMP,             -- record creation timestamp
  updated_at TIMESTAMP,             -- last modification timestamp
  acl JSONB,                        -- access control list (who can read/write)
  spatial_bounds GEOMETRY,          -- geographical bounding polygon for the image
  tags JSONB                        -- extra metadata (mission tags, labels, annotations)
)

