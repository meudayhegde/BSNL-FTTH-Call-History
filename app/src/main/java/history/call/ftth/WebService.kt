package history.call.ftth

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface WebService {
    @GET("/voip_callhistory.asp")
    fun getCallHistoryPage(): Call<ResponseBody>
    @GET("/tz.asp")
    fun getTimeZonePage(): Call<ResponseBody>
}