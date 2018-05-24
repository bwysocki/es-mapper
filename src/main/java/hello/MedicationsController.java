package hello;

import java.util.List;

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

}
