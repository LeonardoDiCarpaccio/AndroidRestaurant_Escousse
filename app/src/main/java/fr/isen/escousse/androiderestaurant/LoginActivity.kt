package fr.isen.escousse.androiderestaurant

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import fr.isen.escousse.androiderestaurant.databinding.ActivityLoginBinding
import fr.isen.escousse.androiderestaurant.models.RegisterResult
import fr.isen.escousse.androiderestaurant.models.User
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate((layoutInflater))
        setContentView(binding.root)

        binding.logButton.setOnClickListener {
            if(verifyInformation()){
                launchRequest()
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun launchRequest(){
        val queue = Volley.newRequestQueue(this)
        val url = NetworkConstant.BASE_URL + NetworkConstant.PATH_LOGIN

        val jsondata = JSONObject()
        jsondata.put(NetworkConstant.ID_SHOP, "1")
        jsondata.put(NetworkConstant.EMAIL, binding.email.text)
        jsondata.put(NetworkConstant.PASSWORD, binding.password.text)

        val loader = Loader()
        loader.show(this, "Connexion en cours")

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsondata,
            {response ->
                loader.hide(this)
                val userResult = GsonBuilder().create().fromJson(response.toString(), RegisterResult::class.java)
                if(userResult.data != null) {
                    saveUser(userResult.data)
                } else {
                    Toast.makeText(this, "Mauvais login", Toast.LENGTH_LONG).show()
                }
            },
            {error ->
                loader.hide(this)
                error.message?.let {
                    Log.d("request", it)
                }?: run {
                    Log.d("request", error.toString())
                    Log.d("request", String(error.networkResponse.data))
                }
            }
        )
        queue.add(request)
    }

    private fun verifyInformation(): Boolean {
        return (binding.email.text?.isNotEmpty() == true ||
                binding.password.text?.count() ?: 0 >= 6)
    }

    fun saveUser(user: User){
        val sharedPreferences = getSharedPreferences(RegisterActivity.USER_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(RegisterActivity.ID_USER, user.id)
        editor.apply()

        setResult(Activity.RESULT_FIRST_USER)
        finish()
    }
}