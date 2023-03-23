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

	public void updateData(JSONObject newObj) {
		String objectId = newObj.getString("objectId");
		// System.out.println(object.toString());
		String oldObjStr = opr.getData(objectId).toString();
		JSONObject oldObj = new JSONObject(oldObjStr);
		for (String jsonKey : newObj.keySet()) {
			Object jsonValue = newObj.get(jsonKey);
			if (jsonValue instanceof JSONArray) {
				JSONArray jsonValueArray = (JSONArray) jsonValue;
				JSONArray oldJsonArray = (JSONArray) oldObj.getJSONArray(jsonKey);
				boolean find = false;
				for (int i = 0; i < jsonValueArray.length(); i++) {
					JSONObject arrayItem = (JSONObject) jsonValueArray.get(i);
					String id = arrayItem.getString("objectId");
					for (int j = 0; j < oldJsonArray.length(); j++) {
						JSONObject oldArrayItem = (JSONObject) oldJsonArray.get(j);
						String oldId = oldArrayItem.getString("objectId");
						if (id.equals(oldId)) {
							oldJsonArray.put(j, arrayItem);
							find = true;
							break;
						}
					}
					if (!find) {
						oldJsonArray.put(arrayItem);
					}
				}
				oldObj.put(jsonKey, oldJsonArray);
			} else {
				oldObj.put(jsonKey, jsonValue);
			}
		}
		opr.saveData(objectId, oldObj.toString());
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
