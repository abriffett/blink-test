package org.abriffett;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Rest controller class
 */
@RestController
public class SummariserController {

	// Could use @Autowired here and configure it in.
	private Summariser summariser;
	private Gson gson;

	public SummariserController() {
		summariser = new Summariser();
		gson = new GsonBuilder().setPrettyPrinting().create();
	}

	// Get method for summariser
	@GetMapping("/summariser")
	public String summarise(@RequestParam(name="url", defaultValue="https://bbc.co.uk/news") String url,
							@RequestParam(name="nested_pages", defaultValue="0") int numNestedPages) throws IOException {
		if (numNestedPages <= 3) {
			return gson.toJson(summariser.summariseURL(url, numNestedPages));
		}
		else {
			// Throw error code because input is invalid.
			return "{status:400}";
		}
	}

}