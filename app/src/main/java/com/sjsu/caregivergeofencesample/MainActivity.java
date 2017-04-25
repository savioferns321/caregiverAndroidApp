package com.sjsu.caregivergeofencesample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sjsu.caregivergeofencesample.adapters.PatientArrayAdapter;
import com.sjsu.caregivergeofencesample.model.GeoFenceDetail;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private final String TAG = getClass().getSimpleName();

    private ListView listView;

    public static final String SELECTED_NAME_KEY =
            "com.sjsu.caregivergeofencesample.MainActivity.SELECTED_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //android.os.Debug.waitForDebugger();
        setContentView(R.layout.activity_main);

        /*
        Make Volley call to
        https://obscure-hollows-67633.herokuapp.com/gfdetailservice/rest/details
        */
        String url = getResources().getString(R.string.service_get_url);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        List<GeoFenceDetail> patientDetails = new ArrayList<>();
        PatientArrayAdapter adapter = new PatientArrayAdapter(this, 0, patientDetails);

        listView = (ListView)findViewById(R.id.patientNameList);
        listView.setAdapter(adapter);
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                response -> {
                    Log.d(TAG, response.toString());


                    //Populate ListView with the items
                    try {
                        JSONArray details = response.getJSONArray("geoFenceDetails");
                        List<GeoFenceDetail> detailList = GeoFenceDetail.getListFromJson(details);
                        adapter.addAll(detailList);
                        ((PatientArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
                        listView.setOnItemClickListener(this);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e(TAG, "Error encountered" + error);
                    /*
                     * If error response code is 404, then geofence is not found for patient
                     *
                     */



                });

        request.setRetryPolicy(new DefaultRetryPolicy(3000, 2, 2));
        queue.add(request);
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, GeofenceSelectorActivity.class);
        GeoFenceDetail selectedDetail = (GeoFenceDetail)(listView.getAdapter().getItem(position));
        Log.d(TAG, "Selected name is : "+selectedDetail.getFirstName()+" "+
                selectedDetail.getFirstName());
        intent.putExtra(SELECTED_NAME_KEY, selectedDetail);
        startActivity(intent);
    }
}
