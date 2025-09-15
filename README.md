# SWE_SACHINPRAJAPATI


# Satellite Data Pipeline — Design + Code

## Summary
This project describes a production-ready satellite data pipeline capable of processing, storing and serving TB→PB scale satellite data. Architecture is stream-first, decoupled via durable message bus, with object storage holding payloads and metadata catalog for discovery.

## Contents
- HLD: architecture & rationale
- LLD: data models, message schemas, partitioning, checkpointing
- Pseudocode: ingest, process, recovery flows
- Java samples: IngestProducer, StreamConsumer, StorageClient interface, Retrieval controller
- UML: PlantUML diagrams for HLD, sequence, class
- Design options: brute-force → better → optimal with trade-offs
- Readme + interview walkthrough + 10x scaling plan

## How the solution is done (approach)
1. **Decouple ingestion & processing** using a message broker (Kafka/Pulsar).
2. **Persist payloads in object store** and place references in the streaming bus to avoid huge messages in broker.
3. **Stream processors** subscribe, transform, write derived products, then update metadata and commit offsets.
4. **Metadata DB** provides search, ACLs, and checkpoint tracking.
5. **Serving layer** uses metadata to generate pre-signed URLs and apply access control.
6. **Lifecycle & cost**: automated tiering to move old objects to cold storage.

## How to run the sample modules (developer notes)
> These are conceptual stubs. To run them, wire to real dependencies (Kafka broker, object storage client, metadata DB).

1. Start Kafka cluster and topic `sat.raw`.
2. Provision object store (S3 / MinIO).
3. Set configuration: brokers, bucket names, credentials.
4. Build Java project with dependencies: Kafka clients, AWS SDK (if using S3), Spring Web (if using retrieval API).
5. Run `IngestProducer` to send frames.
6. Start `StreamConsumer` to process messages.
7. Use Retrieval API to list files and fetch a presigned URL.

## Key design choices & trade-offs
- Use object store for durability & cost; use streaming bus for decoupling.
- Aim for at-least-once processing + idempotency, move to exactly-once where business-critical.
- Use hybrid metadata approach: transactional DB for metadata + search index for queries.

## Testing & validation
- Unit tests for parsers, checksum.
- Integration tests with a local Kafka and MinIO.
- Chaos tests: kill consumers mid-process, corrupt uploads, simulate network partition.
- Performance tests: ramp producers to target throughput, measure end-to-end latency and ingestion success rate.

## Monitoring & SLOs
- Track: ingestion latency, Kafka lag, processing success rate, storage error rates, API latency.
- Alerts: Kafka ISR changes, storage error spikes, consumer lag > threshold.

## Failure recovery plan
- Ingest failures: retries + DLQ
- Consumer fail: autoscale replacement, commit slow consumers, manual reprocess via metadata queries.
- Regional failure: failover to replica region and rehydrate archived data when needed.

## Things to customize
- Partitioning scheme for Kafka (satellite_id + time vs hashed).
- Storage layout naming for fast list vs access (sharding prefixes).
- Technology choices for stream engine (Flink vs Spark Streaming vs Kafka Streams).

## Appendix
- PlantUML files for HLD and sequence diagrams.
- Pseudocode and Java snippets.
