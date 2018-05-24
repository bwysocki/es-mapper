package hello;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MedicationsRepository extends ElasticsearchRepository<MedicationsModel, String> {
  
  @Query("{\"bool\" : {\"must\" : [ {\"match\" : {\"name\" : \"?0\"}} ]}}")
  public List<MedicationsModel> findByName(String name);
  
}
