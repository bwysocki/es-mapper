package hello;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "patient-history", type = "medication")
public class MedicationsModel {

  @Id
  private String id;
  
  private String name;
  
  public MedicationsModel() {
    super();
  }

  public MedicationsModel(String name) {
    super();
    this.name = name;
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "MedicationsModel [id=" + id + ", name=" + name + "]";
  }
  
}
