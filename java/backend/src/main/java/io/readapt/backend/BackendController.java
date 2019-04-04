package io.readapt.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@RestController
public class BackendController {

    private static final Logger log = LoggerFactory.getLogger(BackendController.class);

    @Autowired
    private RestTemplate restTemplate;

    private Random random = new Random();

    @Value("${APP_BACKEND:#{'http://localhost:8081'}}")
    private String backendUrl;

    @Value("${APP_FRONTEND:#{'http://localhost:8080'}}")
    private String frontendUrl;

    @RequestMapping("/slow")
    public String home() throws InterruptedException {
        final String callUrl = backendUrl + "/slow-call";
        String returnValue = this.backgroundTask1();
        returnValue += this.backgroundTask2();
        returnValue +=  restTemplate.getForObject(callUrl, String.class);
        log.info("You called something slow");
        return returnValue + "slow...";
    }

    @Async
    public String backgroundTask1() throws InterruptedException {
        int millis = this.random.nextInt(1000);
        Thread.sleep(millis);
        //this.tracer.addTag("background-sleep-millis", String.valueOf(millis));
        log.info("Background task ran with a delay of {} ms", millis);
        return "This ";
    }

    public String backgroundTask2() throws InterruptedException {
        int millis = this.random.nextInt(1000);
        Thread.sleep(millis);
        //this.tracer.addTag("background-sleep-millis", String.valueOf(millis));
        log.info("Background task ran with a delay of {} ms", millis);
        return "is ";
    }

    @RequestMapping("/slow-call")
    public String call() throws InterruptedException {
        log.info("Calling another method from /slow");
        return "so ";
    }

    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private PersonRepository personRepository;

    @GetMapping(path="/add") // Map ONLY GET Requests
    public @ResponseBody
    Person addNewPerson (@RequestParam(value="name", required=false, defaultValue="") String name) throws IOException {
        Person person = PersonGenerator.personGenerator();
        if (Strings.isNotEmpty(name)) {
            person.setName(name);
        }
        return personRepository.save(person);
    }

    @GetMapping(path="/all")
    public @ResponseBody Iterable<Person> getAllUsers() {
        // This returns a JSON or XML with the users
        return personRepository.findAll();
    }

    @GetMapping("/search")
    public @ResponseBody Iterable<Person> search(@RequestParam String q) {
        // This returns a JSON or XML with the users
        return personRepository.findByNameLike("%" + q + "%");
    }
}
