package com.example.springboot;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import neu.edu.validator.SchemaValidator;

public class HelloControllerTest {

	@Autowired
	SchemaValidator validator;

	@Autowired
	private MockMvc mvc;

	@Test
	public void validate() {
		String resourcePath = getClass().getResource("/").getPath();
		File file = new File(HelloControllerTest.class.getResource(resourcePath+"/useCase.json").getFile());
		try {
			String content = FileUtils.readFileToString(file, "utf-8");
			JSONObject jsonObject = new JSONObject(content);
			System.out.println(validator.validate(jsonObject));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
