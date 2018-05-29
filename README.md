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



