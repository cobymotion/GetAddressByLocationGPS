package mx.edu.tecmm.getaddressfromlocation

import android.Manifest
import android.R.attr.country
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {


    lateinit var mLocationProviderClient: FusedLocationProviderClient
    val PERMISSION_ID = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(allPermissionGrantedGps())
        {
            mLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            getLocation()
        }else
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID)
        }
    }

    private fun getLocation(){


            if(isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationProviderClient.lastLocation.addOnCompleteListener(this)
                    { task ->
                        var location: Location? = task.result
                        if (location == null) {
                            requestNewLocationData();
                        } else
                        {
                            showLocation(location)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Activar GPS", Toast.LENGTH_LONG).show()
            }

    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }



    private fun requestNewLocationData() {
        var mLocationRequest = com.google.android.gms.location.LocationRequest()
        mLocationRequest.priority = LocationRequest.QUALITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mLocationProviderClient  = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        mLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, android.os.Looper.myLooper())
    }

    private val mLocationCallBack = object: LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            val mLastLocation: Location = p0.lastLocation
            showLocation(mLastLocation)
        }
    }

    companion object{
        private val REQUIRED_PERMISSION_GPS = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun allPermissionGrantedGps() = REQUIRED_PERMISSION_GPS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun showLocation(location: Location){
        Toast.makeText(this, "${location.latitude} ${location.altitude}", Toast.LENGTH_LONG).show()
        val address = getAdressInfo(location)

        val addressDirection = address?.getAddressLine(0)
        Log.e("DIRECCIOn", "La direccion detectada es ${addressDirection}")
    }


    /**
     * Aqui se obtiene la calle que se quiere obtener
     */
    private fun getAdressInfo(location: Location): Address? {
        var address: Address? = null
        val geocoder = Geocoder(this, Locale.getDefault())
        val errorMessage: String
        var addresses: List<Address?>? = null
        try {
            addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
        } catch (ioException: IOException) {
            // Catch network or other I/O problems.
            errorMessage = "IOException>>" + ioException.message
        } catch (illegalArgumentException: IllegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "IllegalArgumentException>>" + illegalArgumentException.message
        }
        if (addresses != null && !addresses.isEmpty()) {
            address = addresses[0]
        }
        return address
    }
}