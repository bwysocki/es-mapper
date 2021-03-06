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

With curl you can perform following operation (test-data.json is attached to the project):
```
..\..\curl\bin\curl.exe -H "Content-Type: application/json" -XPOST "http://localhost:9200/product/_doc/_bulk?pretty" --data-binary "@test-data.json"
```

### Mappings - schema

Dynamic mapping - when adding new documents, ES will automatically add mappings for any fields:

```
GET /biomarkers/_doc/_mapping  - presents dynamic mappings
```

Meta fields:
- _index - name of index to which a doc belongs
- _id
- _source - orginal JSON passed to ES
- _field_names
- _routing - stores a val used to route a doc to a shard
- _version - internal version
- _meta - custom fields

Data types:
1. Core
- text (descriptions, blog posts) - analyzed
- keyword  (structured data: tags, categories, email addressed) - not analyzed
- numeric (float, long, short, byte, half_float, scaled_float, integer, double)
- date
- boolean
- binary (accepts base64 encoded)
- range (e.g. 10-20)
2. Complex
- object
- array (all values should have the same type)
- nested - (solves problem of array of objects)
3. Geo
- geo_point
- geo_shape (more complex like polygon)
4. Specialized
- IP
- completion (for autocomplete functionality)
- attachment

Adding mappings: (mappings can not be changed)

```
PUT /indexName/_doc/_mapping
{
	"properties": {
		"discount" : {
			"type": "double"
		}
	}
} - to existing indices


PUT /indexName
{
	"mappings": {
		"_doc" : {
			"dynamic": false,
			"properties": {
				"discount" : {
					"type": "double"
				}
			}
		}
	}
}  - to new indices
```

Mappings parameters:
- coerce: attempts to clean up dirty values to fit the datatype of a field
- copy_to: the values of multiple fields can be copied into a group field, which can then be queried as a single field
- dynamic
- properties
- norms: Norms store various normalization factors that are later used at query time in order to compute the score of a document relatively to a query. Although useful for scoring, norms also require quite a lot of disk (typically in the order of one byte per document per field in your index, even for documents that don’t have this specific field). As a consequence, if you don’t need scoring on a specific field, you should disable norms on that field. In particular, this is the case for fields that are used solely for filtering or aggregations
- format
- null_values: something like 'default'
- fields: it is often useful to index the same field in different ways for different purposes. This is the purpose of multi-fields. For instance, a string field could be mapped as a text field for full-text search, and as a keyword field for sorting or aggregations:


Dynamic problem mapping:
1. Disable dynamic mapping
2. Add doc to index with some 'field'
3. Add mapping for that 'field'
4. Search for that 'field'
5. No results
6. To solve that : PUT /index/_update_by_query : the simplest usage of _update_by_query just performs an update on every document in the index without changing the source. This is useful to pick up a new property or some other online mapping change.


### Cat API

```
GET /_cat/health?v - general info about cluster
GET /_cat/nodes?v - nodes info + IPs
GET /_cat/indices?v - info about indices
GET /_cat/allocation?v - how shards are allocated
GET /_cat/shards?v - show all (primary + replica shards)
```

### Cluster API


### Text analysis

When you insert a new text to ElasticSearch - it is analysed: new doc -> analysis -> stored doc (inverted index)

Analysis consists of 3 steps:
- character filter: some <strong>important</strong> value => some important value
- tokenizer: My wife's birthday => [My, wife, birthday] - tokenizer remembers also position of the words
- token filter => [My, wife, birthday] => [wife, birthday] - very popular is synonym token filter

In many scenarios the standard analyser works fine. Standard analyser has no character filter. The tokenizer breaks words mainly by whitespace (also uses some break characters). As token filter, standard analyser uses only lowercase token filter.

Analysis API:

```
POST _analyze
{
  "char_filter": ["html_strip"],
  "tokenizer": "standard",
  "filter": ["lowercase"],
  "text": "<strong>ble</strong> Ble's im, very important."
} <-- if we want to configure analyzer

POST _analyze
{
  "analyzer": "standard",
  "text": "<strong>ble</strong> Ble's im, very important."
} <-- if we want to test ready analyzer

```

Character Filters:
- html_strip
- mapping - replaces values based on a supplied list of values and their replacements
- pattern_replace - like above but uses regex

Tokenizers:
* word oriented
  * standard
  * letter - tokenize when encounter not letter: i'm in => i, m, in
  * lowercase - as above + lowercase
  * whitespace - when encounter whitespace
  * uax_url_email - like standard but treats URLs and emails as signle tokens
* partial word oriented
  * ngram - break text into words when encountering certain character and them emits N-grams of specific length. Red wine => Re, Red, ed, wi, win, wine, in, ine, ne
  * edge_ngram - like above but generates ngram only for the beginning. Red wine => Re, Red, wi, win, wine
* structured text - used for structured text such as email, zip codes, identifiers etc
  * keyword - doesnt do anything :)
  * pattern - regex to split text into terms
  * path_hierarchy - splits hierarchical values and emits a term for each component in the tree: /a/b/c => /a, /a/b, /a/b/c

  For more details [see elasticsearch manual](https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-tokenizers.html)

Token filters:
- lowercase
- uppercase
- nGream: Red => [R, Re, Red, e, ed] - the same as tokenizer - for flexibility
- edgeNGram
- stop - removes stop words
- word_delimeter - splits words into subwords Wi-Fi => [Wi, Fi]
- stemmer - drinking => drink
- keyword_marker - protects some words from being modified by stemmer
- snowball - stem words like stemmer but based on other algorithm (Snowball)
- synonym - happy -> happy/delighted
- trim

Analyzers:
- standard
- simple
- stop - like simple but also remove stop words
- language analizer - group - for instance English (stemming)
- keyword - doesnt do anything
- pattern
- whitespace

Creating analyzer/token filter during index creation - they will be available in indexName:

```
PUT /indexName
{
  "settings": {
    "analysis": {
      "analyzer": {
        "myAliasAnalyzer": {
          "type": "standard",
          "stopwords": "english" <-- configuration for standard analyzer
        },
        "myCustomAnalyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "char_filter": [
            "html_strip"
          ],
          "filter": [
            "standard", "lowercase", "trim", "myTokenFilter"
          ]
        }
      },
      "filter": { <-- for token filter
        "myTokenFilter": {
          "type": "stemmer",
          "name": english
        }
      }
    }
  }
}
```

- if we need to add analyzer/filter/etc to existing indices, we need to close index in first step.


Using analyzers in mappings:

```
PUT /indexName/_doc/_mapping
{
	"properties": {
		"someText" : {
			"type": "text",
      "analyzer": "myCustomAnalyzer"
		}
	}
}
```

### Searching

```
GET /indexName/_doc/_search
{
	"query": {
		"match" : {
			"description": {
        "value": "my brother"
      }
		}
	}
}
```

Query types:
- match
- match_all
- term - exact term in the inverted index

Match vs term queries
- match query goes through the same analyzis as indexed text
- term is checked directly in inverted index

Debugging searching
```
GET /indexName/_doc/{id}/_explain
{
	"query": {
		"match" : {
			"description": {
        "value": "my brother"
      }
		}
	}
}
``` -- the response tells why the document is / is not in the result


Context:
- query context - how well the docs match - relevance
- filter context - do the docs match - no relevance (dates, staus, ranges) - can be cached
