package es.hiiberia.simpatico.utils;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Utils {

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
	
	public static String createJSONString(String test) {
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
}
