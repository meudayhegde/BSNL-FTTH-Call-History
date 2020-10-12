package history.call.ftth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jakewharton.threetenabp.AndroidThreeTen
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {
    val callerIDList = ArrayList<CallerID>()
    private lateinit var recyclerView: RecyclerView
    lateinit var callerIDAdapter: CallerIDAdapter
    lateinit var refreshLayout: SwipeRefreshLayout
    var timeDelta: Duration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidThreeTen.init(this)

        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.caller_id_list)
        refreshLayout = findViewById(R.id.refresh_layout)

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) -> {
                Log.d("Info", "Contact permission granted.")
                startApplication()
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), PERMISSION_REQUEST_CODE)
                }
            }
        }
    }

    private fun startApplication(){
        val pref = getSharedPreferences("auth", MODE_PRIVATE)
        val okHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(AuthenticationInterceptor(pref.getString("username", "admin")!!, pref.getString("password", "system")!!))
            .build()

        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("http://${pref.getString("router_admin_page", "192.168.1.1")}")
            .build()

        callerIDAdapter = CallerIDAdapter(callerIDList)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = callerIDAdapter
        }

        val service = retrofit.create(WebService::class.java)

        loadCallHistory(service)

        refreshLayout.setOnRefreshListener {
            loadCallHistory(service)
        }
    }

    private fun loadCallHistory(service: WebService){
        refreshLayout.isRefreshing = true
        if(timeDelta != null) loadCallHistory(service, timeDelta!!)
        else service.getTimeZonePage().enqueue(object: retrofit2.Callback<ResponseBody>{
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Error", t.message?:"Error!")
                refreshLayout.isRefreshing = false
                Toast.makeText(this@MainActivity, "Error loading router admin page!", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val timePage = response.body()?.string()
                val timeValues = "<th> Current Time : </th>\\s+<td>\\s+Year<input type=\"text\" name=\"year\" value=\"(\\d+)\" size=\"4\" maxlength=\"4\">\\s+Mon<input type=\"text\" name=\"month\" value=\"(\\d+)\" size=\"2\" maxlength=\"2\">\\s+Day<input type=\"text\" name=\"day\" value=\"(\\d+)\" size=\"2\" maxlength=\"2\">\\s+<p>\\s+Hour<input type=\"text\" name=\"hour\" value=\"(\\d+)\" size=\"2\" maxlength=\"2\">\\s+Min<input type=\"text\" name=\"minute\" value=\"(\\d+)\" size=\"2\" maxlength=\"2\">\\s+Sec<input type=\"text\" name=\"second\" value=\"(\\d+)\" size=\"2\" maxlength=\"2\">\\s+</td>\\s+</tr>".toRegex()
                    .find("$timePage")?.groupValues!!
                val year = timeValues[1]; var month = timeValues[2]; var day = timeValues[3]; var hour = timeValues[4]; var min = timeValues[5]; var sec = timeValues[6]
                month = if(month.length == 1) "0$month" else month
                day = if(day.length == 1) "0$day" else day
                hour = if(hour.length == 1) "0$hour" else hour
                min = if(min.length == 1) "0$min" else min
                sec = if(sec.length == 1) "0$sec" else sec
                val sudoDateTime = LocalDateTime.parse("$year-$month-${day}T$hour:$min:$sec")

                timeDelta = Duration.between(sudoDateTime, LocalDateTime.now())
                loadCallHistory(service, timeDelta!!)
            }
        })
    }

    fun loadCallHistory(service: WebService, timeDelta: Duration){
        service.getCallHistoryPage().enqueue(object: retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Error", t.message?:"Error!")
                refreshLayout.isRefreshing = false
                Toast.makeText(this@MainActivity, "Error loading router admin page!", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val historyHtml = response.body()?.string()
                callerIDList.clear()
                for(str in arrayOf("(\\w+)", "<font color=\"#\\w+\">(\\w+)</font>"))
                "<tr><td bgColor=#\\w+>(\\d+)</td><td bgColor=#\\w+>$str</td><td bgColor=#\\w+>(\\d+)</td><td bgColor=#\\w+>(\\d+)</td><td bgColor=#\\w+>(\\w+)</td><td bgColor=#\\w+>(\\d+:\\d+:\\d+)</td><td bgColor=#\\w+>(\\w+\\s+\\w+\\s+\\d+\\s+\\d+:\\d+:\\d+\\s+\\d+)</td></tr>".toRegex()
                    .findAll("$historyHtml".replace("\n", "")).forEach {
                        callerIDList.add(CallerID(it.groupValues[1].toInt(), it.groupValues[2], it.groupValues[3], it.groupValues[4], it.groupValues[5], it.groupValues[6], it.groupValues[7], timeDelta))
                    }

                callerIDList.sortBy { callerD -> -callerD.index }
                if(callerIDList.isNotEmpty())
                    title = if(callerIDList[0].type.equals("Incoming", true))
                        callerIDList[0].to
                    else callerIDList[0].from
                callerIDAdapter.notifyDataSetChanged()
                refreshLayout.isRefreshing = false
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Contacts read permission successfully granted.", Toast.LENGTH_SHORT).show()
                    startApplication()
                } else {
                    Toast.makeText(this, "Cannot continue without contact read permission", Toast.LENGTH_LONG).show()
                    finish()
                }
                return
            }
        }
    }

    companion object{
        const val PERMISSION_REQUEST_CODE = 101
    }
}