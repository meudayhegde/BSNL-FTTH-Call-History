package history.call.ftth

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val pref = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val url = findViewById<EditText>(R.id.edit_url)
        val uname = findViewById<EditText>(R.id.edit_uname)
        val pwd = findViewById<EditText>(R.id.edit_pwd)

        url.setText(pref.getString("router_admin_page", "http://192.168.1.1"))
        uname.setText(pref.getString("username", "admin"))
        pwd.setText(pref.getString("password", "system"))

        findViewById<Button>(R.id.btn_apply).setOnClickListener {
            val editor = pref.edit()
            editor.putString("router_admin_page", url.text.toString())
            editor.putString("username", uname.text.toString())
            editor.putString("password", pwd.text.toString())
            editor.apply()

            Toast.makeText(this, "Settings Applied", Toast.LENGTH_LONG).show()
        }
    }
}