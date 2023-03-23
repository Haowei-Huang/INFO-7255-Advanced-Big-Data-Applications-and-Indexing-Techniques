package neu.edu.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import neu.edu.security.TokenVerifier;
import neu.edu.service.PlanService;
import neu.edu.validator.SchemaValidator;

@RestController
public class HelloController {

	@Autowired
	PlanService planService;

	@Autowired
	TokenVerifier tokenVerifier;

	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@RequestMapping(path = "/v1/plan", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> createPlan(@RequestBody String plan, HttpServletRequest request) {

		String idToken;
		// verify token
		idToken = request.getHeader("Authorization");
		if (idToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new JSONObject().put("Error", "Authorization header missing").toString());
		}
		idToken = idToken.replace("Bearer ", "");
		try {
			tokenVerifier.authorize(idToken);
		} catch (GeneralSecurityException | IOException e) {
			System.out.println(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new JSONObject().put("Error", "Token invalid").toString());
		}

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

	@RequestMapping(path = "/v1/plan/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> putPlan(@PathVariable("id") String id, @RequestBody String plan,
			HttpServletRequest request) {

		String idToken;
		// verify token
		idToken = request.getHeader("Authorization");
		if (idToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new JSONObject().put("Error", "Authorization header missing").toString());
		}
		idToken = idToken.replace("Bearer ", "");
		try {
			tokenVerifier.authorize(idToken);
		} catch (GeneralSecurityException | IOException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new JSONObject().put("Error", "Token invalid").toString());
		}

		// check if plan existed
		if (!planService.hasKey(id)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new JSONObject().put("Error", "Object does not exist").toString());
		}

		// check if data is provided
		if (plan == null || plan.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new JSONObject().put("Error", "Body is empty").toString());
		}

		// validate schema
		SchemaValidator validator = new SchemaValidator();
		JSONObject json = new JSONObject(plan);
		try {
			validator.validate(json);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new JSONObject().put("Error", "Format is not correct").toString());
		}

		// check if object id in json and the object id in url match
		String objectId = json.getString("objectId");
		if (!objectId.equals(id)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new JSONObject().put("Error", "Id does not match in url and json").toString());
		}

		// compare the ifNodeMatch header with current stored record's hash
		String ifMatch = request.getHeader("If-Match");
		String objectStr = planService.getData(id).toString();
		String jsonHash = DigestUtils.md5Hex(objectStr);

		if (ifMatch != null && !ifMatch.equals(jsonHash)) {
			return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).eTag(jsonHash) // return the actual E-tag
					.build();
		}

		planService.saveData(json);
		String updateObjStr = planService.getData(id).toString();
		// return the update e-tag
		return ResponseEntity.status(HttpStatus.OK).eTag(DigestUtils.md5Hex(updateObjStr.toString()))
				.body(updateObjStr);
	}

	@RequestMapping(path = "/v1/plan/{id}", method = RequestMethod.PATCH, consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> patchPlan(@PathVariable("id") String id, @RequestBody String plan,
			HttpServletRequest request) {

		String idToken;
		// verify token
		idToken = request.getHeader("Authorization");
		if (idToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new JSONObject().put("Error", "Authorization header missing").toString());
		}
		idToken = idToken.replace("Bearer ", "");
		// verify token
		try {
			tokenVerifier.authorize(idToken);
		} catch (GeneralSecurityException | IOException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new JSONObject().put("Error", "Token invalid").toString());
		}

		// check if plan existed
		if (!planService.hasKey(id)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new JSONObject().put("Error", "Object does not exist").toString());
		}

		// check if data is provided
		if (plan == null || plan.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new JSONObject().put("Error", "Body is empty").toString());
		}

		// validate schema
		SchemaValidator validator = new SchemaValidator();
		JSONObject json = new JSONObject(plan);
		try {
			validator.validate(json);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new JSONObject().put("Error", "Format is not correct").toString());
		}

		// check if object id in json and the object id in url match
		String objectId = json.getString("objectId");
		if (!objectId.equals(id)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new JSONObject().put("Error", "Id does not match in url and json").toString());
		}

		// compare the ifNodeMatch header with current stored record's hash
		String ifMatch = request.getHeader("If-Match");
		String objectStr = planService.getData(id).toString();
		String jsonHash = DigestUtils.md5Hex(objectStr);

		if (ifMatch != null && !ifMatch.equals(jsonHash)) {
			return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).eTag(jsonHash) // return the actual E-tag
					.build();
		}

		// The patch process
		planService.updateData(json);
		String updateObjStr = planService.getData(id).toString();
		// return the update e-tag
		return ResponseEntity.status(HttpStatus.OK).eTag(DigestUtils.md5Hex(updateObjStr.toString()))
				.body(updateObjStr);
	}

	@RequestMapping(path = "/v1/plan/{id}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getPlan(@PathVariable("id") String id, HttpServletRequest request) {

		String idToken;
		// verify token
		idToken = request.getHeader("Authorization");
		if (idToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new JSONObject().put("Error", "Authorization header missing").toString());
		}

		idToken = idToken.replace("Bearer ", "");
		try {
			tokenVerifier.authorize(idToken);
		} catch (GeneralSecurityException | IOException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new JSONObject().put("Error", "Token invalid").toString());
		}

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

		String idToken;
		// verify token
		idToken = request.getHeader("Authorization");
		if (idToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new JSONObject().put("Error", "Authorization header missing").toString());
		}
		
		idToken = idToken.replace("Bearer ", "");
		try {
			tokenVerifier.authorize(idToken);
		} catch (GeneralSecurityException | IOException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new JSONObject().put("Error", "Token invalid").toString());
		}

		if (!planService.hasKey(id)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new JSONObject().put("Error", "Object does not exist").toString());
		}

		planService.deleteData(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
