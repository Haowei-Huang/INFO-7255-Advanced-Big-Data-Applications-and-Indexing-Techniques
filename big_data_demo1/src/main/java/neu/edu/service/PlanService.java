package neu.edu.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import neu.edu.dao.RedisOpr;

@Service
public class PlanService {

	@Autowired
	RedisOpr opr;

	public void saveData(JSONObject object) {
		String objectId = object.getString("objectId");
		// System.out.println(object.toString());
		opr.saveData(objectId, object.toString());
	}

	public Object getData(String key) {
		return opr.getData(key);
	}

	public Object deleteData(String key) {
		return opr.deleteData(key);
	}

	public boolean hasKey(String key) {
		return opr.hasKey(key);
	}
}
