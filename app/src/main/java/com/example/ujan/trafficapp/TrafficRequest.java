

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SAYAN on 22-04-2018.
 */

public class TrafficRequest extends StringRequest{
    private static final String SEARCH_REQUEST_URL = "https://ruchira.000webhostapp.com/traffic_search.php";
    private Map<String, String> params;

    public TrafficRequest(String traffic_id,Response.Listener<String> listener) {
        super(Request.Method.POST, SEARCH_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("traffic_id", traffic_id);

    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

}
