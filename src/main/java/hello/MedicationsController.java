package hello;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MedicationsController {

  private final MedicationsRepository repository;

  MedicationsController(MedicationsRepository repository) {
    this.repository = repository;
  }

  @RequestMapping("/medications")
  String showUsers(Model model, Pageable pageable, @RequestParam(value = "search") String search) {
    List<MedicationsModel> all = repository.findByName(search);
    return all.toString();
  }
  
  @RequestMapping("/sample")
  String sample(Model model, Pageable pageable) {
    this.repository.save(new MedicationsModel("my wife is a beautiful woman"));
    return "done";
  }
  
  @RequestMapping("/migrateFromCSV")
  String migrateFromCSV(Model model) {
    InputStream resource = getClass().getResourceAsStream("/autosuggest.tbdev.csv");
    try (
        Reader reader =  new InputStreamReader(resource);
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
    ) {
        for (CSVRecord csvRecord : csvParser) {
            String id = csvRecord.get(0);
            
            if (id.startsWith("medications") && id.endsWith(".terms")) { // different record types saved in different places
              
              String created = csvRecord.get(1);
              String value = "{\"json\":"+csvRecord.get(2)+"}";
              
              System.out.println("Record No - " + csvRecord.getRecordNumber());
              System.out.println("id : " + id);
              System.out.println("created : " + created);
              System.out.println("value : " + value);
              
              Object obj = new JSONParser().parse(value);
              JSONObject jo = (JSONObject) obj;
              
              JSONArray terms = (JSONArray) jo.get("json");
              terms.forEach((termJson) -> { // probably using simple json is not the bes solution
                String term = (String) ((JSONObject) termJson).get("term");
                
                String toUUIDHash = term; //to hash string save also fid
                String uuidFromString = UUID.nameUUIDFromBytes(toUUIDHash.getBytes()).toString();
                
                System.out.println("term : " + term);
                System.out.println("uuid : " + uuidFromString);
                
                this.repository.save(new MedicationsModel(uuidFromString, toUUIDHash)); // consider to use batch processing
                
              });
              
              System.out.println("---------------\n\n");
            }
            
            
        }
    } catch (Exception e) {
      System.out.println("exc : " + e);
      return "filed";
    }
    
    return "done";
  }

}
