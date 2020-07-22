package timifeoluwa.example.contentproviderexample

import android.Manifest.permission.READ_CONTACTS
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"
private const val REQUEST_CODE_READ_CONTACTS = 1

class MainActivity : AppCompatActivity() {

//    private var readGranted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val hasReadContactPermission = ContextCompat.checkSelfPermission(
            this,
            READ_CONTACTS
        )  //better imported from the android manifest
        //rather than having to type manifest.permission.READ_CONTACTS every time. returns denied or granted.
        Log.d(TAG, "onCreate: checkSelfPermission returned $hasReadContactPermission")

//        if (hasReadContactPermission == PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "onCreate: permission granted")
////            readGranted = true  //TODO don't do this!!
//        } else {
//            Log.d(TAG, "onCreate: requesting permission")
//            ActivityCompat.requestPermissions(
//                this, arrayOf(READ_CONTACTS),
//                REQUEST_CODE_READ_CONTACTS
//            )
//        }
        if (hasReadContactPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(READ_CONTACTS),
                REQUEST_CODE_READ_CONTACTS
            )
        }
        fab.setOnClickListener { view ->
            Log.d(TAG, "fab onClick: starts")
//            if (readGranted) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

                //Main activity uses the URI to specify the kind of data it wants, the contentResolver then uses that URI to decide which contentProvider it should
                //ask to supply the data.there are levels of abstraction, which means separation of main activity here from the source of data i.e.database.
                //contentResolver extracts an Authority that's included in the URI to decide which content provider to direct data/query requests to.

                val cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    projection,
                    null,  //like the filter to determine which row should be returned.like the WHERE statement in SQL.
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY  //like the SQL ORDER BY clause.
                )//contentResolver then executes the query and returns the cursor

                val contacts = ArrayList<String>()          //create a list to hold the contacts
                cursor?.use {
                    //loop through the cursor.
                    while (it.moveToNext()) {
                        contacts.add(it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)))
                    }
                }
                val adapter =
                    ArrayAdapter<String>(this, R.layout.contact_details, R.id.name, contacts)
                contact_names.adapter = adapter

            } else {
                Snackbar.make(
                    view,
                    "Please grant access to your contacts",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Grant Access") {
                        Log.d(TAG, "Snackbar onClick: starts")
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                READ_CONTACTS
                            )
                        ) {  //returns false if the user clicks the don't ask me again button.
                            Log.d(TAG, "Snackbar onClick: calling requestPermission")
                            ActivityCompat.requestPermissions(
                                this, arrayOf(READ_CONTACTS),
                                REQUEST_CODE_READ_CONTACTS
                            )
                        } else {
                            //The user has permanently denied the permission,take them directly to the settings.
                            Log.d(TAG, "Snackbar onClick: launching Settings")
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", this.packageName, null)
                            Log.d(TAG, "Snackbar onClicked: Uri is $uri")
                            intent.data = uri
                            this.startActivity(intent)
                        }
                        Log.d(TAG, "Snackbar onClick: Ends")
                    }.show()
            }

            Log.d(TAG, "fab onClick: ends")


        }
        Log.d(TAG, "onCreate: ends")
    }       //no need to open or close the database since the it wasn't accessed directly. and also the content resolver's object
    //is global and doesn't get destroyed when my activity does. However, the content resolver and provider handle the activity lifecycle for me,
    //so i don't need to coordinate the data access with the activity lifecycle. also using a cursor loader , i don't need to worry about creating
    // a background thread.

//    override fun onRequestPermissionsResult(
//        requestCode: Int,  //request code needs to be unique in cases of large apps where more than one permission might be requested e.g. camera.
//        permissions: Array<out String>,  //this is the permission requested
//        grantResults: IntArray    //this is the result for each permission requested. to check which permission was granted and which was denied.
//    ) {
//        Log.d(TAG, "onRequestPermissionsResult: starts")
//        when (requestCode) {  //request code needs to be unique in cases of large apps where more than one permission might be requested e.g. camera.
//            REQUEST_CODE_READ_CONTACTS -> {
////                readGranted =
//                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {  //only one request was made, hence the first result was checked.
//                        //permission was granted, Yey! Do the
//                        //contacts-related task we need to do.
//                        Log.d(TAG, "onRequestPermissionResult: permission granted")
//
////                        true
//                    } else {
//                        //permission denied,boo!! Disable the
//                        //functionality that depends on this permission
//                        Log.d(TAG, "onRequestPermissionResult: permission refused")
////                        false
//                    }
//
////                fab.isEnabled = readGranted  //this changes depending on whether we have the required permission or not.if the readGranted is true then fab
//                //is enabled else it is disabled.
//            }
//
//        }
//        Log.d(TAG, "onRequestPermissionsResult: end")
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
