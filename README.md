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

### ElasticSearch queries


- CREATION

```
PUT /indexName
```

- INSERTING

```
POST /indexName/_doc
{}

//with specified ID / replacing: 
PUT /indexName/_doc/{id} 
{}
```

- RETRIVING: 

```
GET /indexName/_doc/{id}
```

- SEARCHING

```
GET /indexName/_doc/{api} 
//Example: GET /biomarkers/_doc/_search
```

- UPDATING 

```
POST biomarkers/_doc/BIO-1/_update
{
  "doc": {
    "description": "Super important Biomarker"
  }
}
or with scripting

POST biomarkers/_doc/BIO-1/_update
{
  "script": "ctx._source.description += '!!!'"
}
```

- UPSERT

```
POST biomarkers/_doc/BIO-1/_update
{
    "script" : {
        "source": "ctx._source.counter += params.count",
        "lang": "painless",
        "params" : {
            "count" : 4
        }
    },
    "upsert" : {
        "counter" : 1
    }
}
```

- DELETION

```
DELETE biomarkers/_doc/BIO-1
POST biomarkers/_delete_by_query 
{
	"query" : {
		"match" : {
			"category": "xyz"
		}
	}
}
DELETE biomarkers <-- deletes the whole index
```

### Batch processing (importing data)


- BATCH INSERT
```
POST biomarkers/_doc/_bulk 
{ "index": { "_id": "BIO-1" } }
{ "name": "My biomarker" }
{ "index": { "_id": "BIO-2" } }
{ "name": "My second biomarker" }
```

- BATCH UPDATE
```
POST biomarkers/_doc/_bulk 
{ "update": { "_id": "BIO-1" } }
{ "doc": { "importance": "HIGH" }}
{ "update": { "_id": "BIO-2" } }
{ "doc": { "importance": "LOW" }}
{ "delete": { "_id": "BIO-3" } }
```

With curl youy can perform following operation (test-data.json is attached to the project):
```
..\..\curl\bin\curl.exe -H "Content-Type: application/json" -XPOST "http://localhost:9200/product/_doc/_bulk?pretty" --data-binary "@test-data.json"
```

