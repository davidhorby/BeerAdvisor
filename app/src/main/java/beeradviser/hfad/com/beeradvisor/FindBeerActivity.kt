package beeradviser.hfad.com.beeradvisor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Spinner
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_find_beer.view.*
import java.util.*


class FindBeerActivity : Activity() {


    private val TAG = "SignInActivity"
    private val RC_SIGN_IN = 9001

    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_beer)
        getId()

        // Setup Google login
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    fun onClickFindBeer(view: View) {
        val brands = findViewById<View>(R.id.brands) as TextView
        val color: Spinner = findViewById<View>(R.id.color) as Spinner
        val beerType= color.selectedItem.toString()
        brands.text = beerType
    }

    fun onClickSignInToGoogle(view: View) {
        signIn()
    }

    fun onClickSignOutOfGoogle(view: View) {
        signOut()
    }


    public override fun onStart() {
        super.onStart()

        // [START on_start_sign_in]
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)
        // [END on_start_sign_in]
    }

    // [START onActivityResult]
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val displayNameTxt = findViewById<View>(R.id.display_name) as TextView
            val email = findViewById<View>(R.id.email) as TextView
            displayNameTxt.text = account.displayName
            email.text = account.email
            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }

    }
    // [END handleSignInResult]

    // [START signIn]
    private fun signIn() {
        mGoogleSignInClient?.let {
            startActivityForResult(it.signInIntent, RC_SIGN_IN)
        }
    }
    // [END signIn]

    // [START signOut]
    private fun signOut() {
        mGoogleSignInClient?.let {
            it.signOut() .addOnCompleteListener(this) {
                updateUI(null)
            }
        }
    }
    // [END signOut]

    private fun updateUI(account: GoogleSignInAccount?) {

        account?.let {
            findViewById<View>(R.id.status).status.text = getString(R.string.signed_in_fmt, it.displayName)

            findViewById<View>(R.id.sign_in_button).visibility = View.GONE
            findViewById<View>(R.id.sign_out_button).visibility = View.VISIBLE
        } ?: run {
            findViewById<View>(R.id.sign_in_button).visibility = View.VISIBLE
            findViewById<View>(R.id.sign_out_button).visibility = View.GONE
        }

    }

    fun getId() {
        if (uniqueID == null) {
            val sharedPrefs = this.baseContext.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE)
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null)

            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString()
                val editor = sharedPrefs.edit()
                editor.putString(PREF_UNIQUE_ID, uniqueID)
                editor.commit()
            }
        }
        findViewById<View>(R.id.uuid).uuid.text = uniqueID
    }



    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    companion object {

        private var uniqueID: String? = null
        private val PREF_UNIQUE_ID = "PREF_UNIQUE_ID"

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }


    }
}
