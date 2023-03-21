package neu.edu.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import neu.edu.service.PlanService;
import neu.edu.validator.SchemaValidator;

@RestController
public class HelloController {

	@Autowired
	PlanService planService;

	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@RequestMapping(path = "/v1/plan", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> createPlan(@RequestBody String plan) {

		if (plan == null || plan.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new JSONObject().put("Error", "Body is empty").toString());
		}

		SchemaValidator validator = new SchemaValidator();
		JSONObject json = new JSONObject(plan);
		try {
			validator.validate(json);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new JSONObject().put("Error", "Format is not correct").toString());
		}

		if (planService.hasKey(json.getString("objectId"))) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new JSONObject().put("Error", "Object is existed already").toString());
		}

		planService.saveData(json);
//		System.out.println("json saved!");
		System.out.println(json.toString().equals(plan));
//		System.out.println("plan "+plan);
//		System.out.println("JSON "+json.toString());
		return ResponseEntity.status(HttpStatus.CREATED).eTag(DigestUtils.md5Hex(json.toString()))
				.body(new JSONObject().put("objectId", json.getString("objectId")).toString());
	}

	@RequestMapping(path = "/v1/plan/{id}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getPlan(@PathVariable("id") String id, HttpServletRequest request) {

		if (!planService.hasKey(id)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new JSONObject().put("Error", "Object does not exist").toString());
		}

		String ifNoneMatch = request.getHeader("If-None-Match");
		String objectStr = planService.getData(id).toString();
		String jsonHash = DigestUtils.md5Hex(objectStr);

		// compare the ifNodeMatch header with current stored record's hash
		if (ifNoneMatch != null && ifNoneMatch.equals(jsonHash)) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(DigestUtils.md5Hex(objectStr)).body(objectStr);
		}

		return ResponseEntity.status(HttpStatus.OK).eTag(DigestUtils.md5Hex(objectStr)).body(objectStr);
	}

	@RequestMapping(path = "/v1/plan/{id}", method = RequestMethod.DELETE, produces = "application/json")
	public ResponseEntity<Object> deletePlan(@PathVariable("id") String id, HttpServletRequest request) {

		if (!planService.hasKey(id)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new JSONObject().put("Error", "Object does not exist").toString());
		}

		planService.deleteData(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
