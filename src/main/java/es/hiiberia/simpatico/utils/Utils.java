package es.hiiberia.simpatico.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Utils {

	/**
	 * Check if a string is a valid JSON
	 * @param test
	 * @return
	 */
	public static boolean isJSONValid(String test) {
	    try {
	        new JSONObject(test);
	    } catch (JSONException ex) {
	        try {
	            new JSONArray(test);
	        } catch (JSONException ex1) {
	            return false;
	        }
	    }
	    return true;
	}
	
	/**
	 * Create a valid JSON string from a string. For example {a: "hello"} (not valid JSON) returns {"a": "hello"} (valid JSON)
	 * @param test
	 * @return
	 */
	public static String createJSONStringIfValid(String test) {
	    try {
	        return new JSONObject(test).toString();
	    } catch (JSONException ex) {
	        try {
	            return new JSONArray(test).toString();
	        } catch (JSONException ex1) {
	            return "";
	        }
	    }
	}
	
	/**
	 * Create a valid JSON Object
	 * @param test
	 * @return
	 */
	public static JSONObject createJSONObjectIfValid(String test) {
	    try {
	        return new JSONObject(test);
	    } catch (JSONException ex) {
	        return null;
	    }
	}
	
	/**
	 * Create a valid JSON Array
	 * @param test
	 * @return
	 */
	public static JSONArray createJSONArrayIfValid(String test) {

        try {
            return new JSONArray(test);
        } catch (JSONException ex1) {
            return null;
        }
	    
	}
	
	/* Json2Map */
	public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
		
	    Map<String, Object> retMap = new HashMap<String, Object>();

	    if (json != JSONObject.NULL) {
	        retMap = toMap(json);
	    }
	    
	    return retMap;
	}
	
	/* Json2Map */
	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
		
	    Map<String, Object> map = new HashMap<String, Object>();

	    @SuppressWarnings("unchecked")
		Iterator<String> keysItr = object.keys();
	    while (keysItr.hasNext()) {
	        String key = keysItr.next();
	        Object value = object.get(key);

	        if (value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        } else if (value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        
	        map.put(key, value);
	    }
	    
	    return map;
	}
	
	/* Json2Map */
	public static List<Object> toList(JSONArray array) throws JSONException {
		
	    List<Object> list = new ArrayList<Object>();
	    for (int i = 0; i < array.length(); i++) {
	        Object value = array.get(i);
	        
	        if (value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        } else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        
	        list.add(value);
	    }
	    
	    return list;
	}
	
	/* Check if integer */
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    // only got here if we didn't return false
	    return true;
	}
}
