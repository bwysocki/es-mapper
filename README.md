# Elastic Search helper

## Architecture

### Nodes in cluster

- master node - is the node that is responsible for coordinating changes to the cluster such as adding or removing nodes, creating or removing indices etc. The master node updates the state of the cluster.

- data node

- client node

### Data organization

- document - data item stored in cluster in JSON format. Correspond to rows in relational DB. Documents have ID.

- index - a collection of documents that have similar characteristics

- type - removed in newest version of ElasticSearch - use default index to backward compatibility (from version 7 _doc)

instead of:

```
PUT /users/default/1
{
  "name": "Bo Andersen"
}
```

do:

```
PUT /users/_doc/1
{
  "name": "Bo Andersen"
}
```

- shard - contains a subset of and index data. It allows to split big volumes of data. Operations can also be distributed on many nodes (parallelized). Number of shards can be specified at index creation (default 5). After index creation, there is no way to change number of shards (workaround - create new index and migrate data).

- replication - replication in ElasticSearch is synchronous. By default there is 1 replica for each shard (default 5 shards + 5 replicas)

- coordinating node - node responsible for the request. Forwards request to other shards and then merges subresponses to the final response

- routing - a way to determine which shard has/should have a document: shard = hash(routing) % total_primary_shards. By default the 'routing' value will be equal a given document's ID

