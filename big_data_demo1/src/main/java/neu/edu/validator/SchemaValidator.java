package neu.edu.validator;

import org.json.JSONObject;
import java.io.InputStream;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONTokener;
import org.springframework.stereotype.Service;

@Service
public class SchemaValidator {

	public boolean validate(JSONObject object) throws ValidationException {
		InputStream inputStream = getClass().getResourceAsStream("/schema.json");
		JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
		Schema schema = SchemaLoader.load(rawSchema);
		schema.validate(object);
		System.out.println("validate success");
		return true;
	}

}
