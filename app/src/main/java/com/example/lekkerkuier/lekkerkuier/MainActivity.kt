package com.example.lekkerkuier.lekkerkuier

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.location.Location
import android.content.SharedPreferences
import android.content.pm.PackageManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import  android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import com.google.android.gms.common.internal.Objects
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_main.view.*
import org.json.JSONArray
import java.awt.font.NumericShaper
import java.io.Console
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getNumPeople();

        var location = ""

        location = DoLocation()



        sendInfo(location)

        val refreshBTN = findViewById<View>(R.id.refresgBtn)

        //refreshBTN.setOnClickListener(View.OnClieckListner())

        refreshBTN.setOnClickListener(View.OnClickListener {
            //Toast.makeText(getApplicationContext(), "Hallo", Toast.LENGTH_SHORT).show();
            location = DoLocation()
            sendInfo(location)
        })

        val DoLocationHandler = Handler()
        val GetNumberPeolpleHandler = Handler()
        val delay = 10000 //milliseconds

        //to call getNumberPeople every 10 seconds
        GetNumberPeolpleHandler.postDelayed(object : Runnable {
            override fun run() {
                //location = DoLocation()
                //sendInfo(location)
                getNumPeople();
                GetNumberPeolpleHandler.postDelayed(this, delay.toLong())
            }
        }, delay.toLong())

        //to call doLocation every 10 seconds
        DoLocationHandler.postDelayed(object : Runnable {
            override fun run() {
                location = DoLocation()
                sendInfo(location)
                DoLocationHandler.postDelayed(this, delay.toLong())
            }
        }, delay.toLong())



    }

    fun sendInfo(location: String): Unit
    {
        if(location == "")
        {
            Toast.makeText(getApplicationContext(), "Please enable GPS location", Toast.LENGTH_SHORT).show();
        }
        else
        {
            //check if file exists
            val file = baseContext.getFileStreamPath("Key.txt")
            if (file.exists())
            {
                val fin = openFileInput("Key.txt");
//
                var c = 1
                var temp = ""
                while (c != -1)
                {
                    c = fin.read()
                    temp = temp + Character.toString(c.toChar())
                }

                temp = temp.substring(0, 32)// not sure why it does not give correct output if do not throw away last char

                //string temp contains all the data of the file.
                fin.close()

                Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
                val queue = Volley.newRequestQueue(this)
                val url = "http://10.0.0.15/LekkerKuier/VirServer/LekkerKuierAPI.php?apiKey=" + temp + "&Location=" + location // -25.751693, 28.235452 location must have space according to api
                val stringRequest = StringRequest(Request.Method.GET, url, Response.Listener<String>
                {response ->
                    // Display the first 500 characters of the response string.

                    //Toast.makeText(getApplicationContext(), "Response is: $re{sponse.substring(0, 288)}",Toast.LENGTH_LONG).show()

                    //Toast.makeText(getApplicationContext(), "Response is: $response", Toast.LENGTH_SHORT).show()

                },
                    Response.ErrorListener {
                        Toast.makeText(
                            getApplicationContext(),
                            "Could not connect to server",
                            Toast.LENGTH_SHORT
                        ).show()
                    })

                // Add the request to the RequestQueue.
                queue.add(stringRequest)

                //string temp contains all the data of the file.
                //fin.close();


            }
            else
            {
                val file = File(applicationContext.filesDir, "Key.txt")

                val filename = "Key.txt"
                //val fileContents = "Hello world!"

                val queue = Volley.newRequestQueue(this)
                val url = "http://10.0.0.15/LekkerKuier/VirServer/LekkerKuierAPI.php?FirstTime=YES"
                val stringRequest = StringRequest(Request.Method.GET, url, Response.Listener<String>
                { response ->
                    // Display the first 500 characters of the response string.

                    //for testing
                    //Toast.makeText(getApplicationContext(), "Response is: ${response.substring(0, 288)}",Toast.LENGTH_LONG).show()

                    //get api key and save it in key.txt file
                    val fileContents = response.substring(1, 33)
                    applicationContext.openFileOutput(filename, Context.MODE_PRIVATE).use {
                        it.write(fileContents.toByteArray())
                    }
                    Toast.makeText(getApplicationContext(), "Created", Toast.LENGTH_SHORT).show()
                },
                    Response.ErrorListener {
                        Toast.makeText(
                            getApplicationContext(),
                            "Could not connect to server",
                            Toast.LENGTH_SHORT
                        ).show()
                    })

                // Add the request to the RequestQueue.
                queue.add(stringRequest)
            }
        }
    }


    fun getNumPeople(): Unit
    {
        val queue = Volley.newRequestQueue(this)
        val url = "http://10.0.0.15/LekkerKuier/VirServer/LekkerKuierAPI.php?People=YES" // -25.751693, 28.235452 location must have space according to api
        val stringRequest = StringRequest(Request.Method.GET, url, Response.Listener<String>
        {response ->

             val numberPeopleInfo =  JSONArray(response)



            for(i in 0 until numberPeopleInfo.length())
            {
                if(numberPeopleInfo.getJSONObject(i).getString("Name") == "Aandklas")
                    findViewById<View>(R.id.txtAandklas).txtAandklas.text = numberPeopleInfo.getJSONObject(i).getString("Number People")
                if(numberPeopleInfo.getJSONObject(i).getString("Name") == "Springbok")
                    findViewById<View>(R.id.txtSpringbok).txtSpringbok.text = numberPeopleInfo.getJSONObject(i).getString("Number People")
                if(numberPeopleInfo.getJSONObject(i).getString("Name") == "None")
                    findViewById<View>(R.id.txtNone).txtNone.text = numberPeopleInfo.getJSONObject(i).getString("Number People")
            }


            //Toast.makeText(getApplicationContext(),numberPeopleInfo.getJSONObject(0).getString("Name") , Toast.LENGTH_SHORT).show()

        },
            Response.ErrorListener {
                Toast.makeText(
                    getApplicationContext(),
                    "Could not connect to server",
                    Toast.LENGTH_SHORT
                ).show()
            })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

     fun DoLocation():  String
    {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationProvider: String = LocationManager.GPS_PROVIDER

        // Define a listener that responds to location updates
        val locationListener = object : LocationListener {

            override fun onLocationChanged(location: Location)
            {
                // Called when a new location is found by the network location provider.
                //makeUseOfNewLocation(location)
                //Toast.makeText(baseContext,location.latitude.toString(), Toast.LENGTH_LONG).show();
                //Toast.makeText(baseContext, "location is:"+location, Toast.LENGTH_SHORT).show();


            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle)
            {
            }

            override fun onProviderEnabled(provider: String)
            {
                Toast.makeText(applicationContext,"Enabled", Toast.LENGTH_LONG).show();
            }

            override fun onProviderDisabled(provider: String)
            {
                Toast.makeText(applicationContext,"Disabled", Toast.LENGTH_LONG).show();
                //Toast.makeText(applicationContext,provider, Toast.LENGTH_LONG).show();
            }
        }


        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            // Acquire a reference to the system Location Manager
            //// Register the listener with the Location Manager to receive location updates

            if(locationManager.isProviderEnabled(locationProvider))
            {
                //val location

                if(locationManager.getLastKnownLocation(locationProvider) == null)
                {

                    locationManager.requestLocationUpdates(locationProvider, 0, 0f, locationListener)


                    // not ideal
                    while (locationManager.getLastKnownLocation(locationProvider) == null)
                    {

                    }
                    //location = locationManager.getLastKnownLocation(locationProvider)
                }
                //else
                    val location = locationManager.getLastKnownLocation(locationProvider)

                //Toast.makeText(getApplicationContext(), location.toString(), Toast.LENGTH_LONG).show();

                return location.longitude.toString() + ", " + location.latitude
            }
            else
            {
                //Toast.makeText(getApplicationContext(), "Please enable location", Toast.LENGTH_LONG).show();
                return ""
            }

            /*if(locationManager.getLastKnownLocation(locationProvider) == null)
            {


                //continuously gets location even when minimized but not when closed

                while (locationManager.getLastKnownLocation(locationProvider) == null)
                {
                    locationManager.requestLocationUpdates(locationProvider,0,0f,locationListener)
                }

                val location = locationManager.getLastKnownLocation(locationProvider)

                //val location = getCurrLocation.

                //val location = locationManager.getLastKnownLocation(locationProvider)

                //val longatude = location.toString()

                Toast.makeText(getApplicationContext(), location.toString(), Toast.LENGTH_LONG).show();

            }
            else
            {
                val location = locationManager.getLastKnownLocation(locationProvider)

                //Toast.makeText(getApplicationContext(),location.toString(),Toast.LENGTH_LONG)
                // Toast.makeText(getApplicationContext(),locationManager.getLastKnownLocation(locationProvider).toString(),Toast.LENGTH_LONG)

                val longatude = location.latitude

                Toast.makeText(getApplicationContext(), longatude.toString(), Toast.LENGTH_LONG).show();
            }*/
        }
        else
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)

            DoLocation()
        }

        return ""
    }
}


