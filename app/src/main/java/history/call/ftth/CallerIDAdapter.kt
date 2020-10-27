package history.call.ftth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView


class CallerIDAdapter(private val idList: ArrayList<CallerID>) : RecyclerView.Adapter<CallerIDAdapter.MyViewHolder>() {

    class MyViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView){
        val callerIDIcon: ImageView = cardView.findViewById(R.id.icon_status)
        val callerNumber: TextView = cardView.findViewById(R.id.contact_number)
        val callTime: TextView = cardView.findViewById(R.id.call_time)
        val callDuration: TextView = cardView.findViewById(R.id.duration_text)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.caller_id_list_item, parent, false) as CardView
        return MyViewHolder(cardView)
    }

    private val bgThread = BackgroundThread()

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val callerID = idList[position]
        holder.callDuration.text = callerID.duration
        holder.callTime.text = HtmlCompat.fromHtml(callerID.dateTimeHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val callerNumber = if(callerID.type == "Outgoing"){
            holder.callerIDIcon.setImageResource(R.drawable.ic_call_outgoing)
            callerID.to
        }else{
            holder.callerIDIcon.setImageResource(R.drawable.ic_icon_call_incoming)
            callerID.from
        }
        holder.callerNumber.text = callerNumber

        bgThread.addToQueue(Runnable {
            val callerName = getContactName(callerNumber, holder.cardView.context)
            if(callerName != ""){
                (holder.cardView.context as Activity).runOnUiThread {
                    holder.callerNumber.text = "${holder.callerNumber.text} ($callerName)"
                }
            }
        })

        when(callerID.status){
            "missed" -> holder.callerIDIcon.setImageResource(R.drawable.ic_call_missed)
            "active" -> {
                holder.callerNumber.setTextColor(Color.parseColor("#00BC00"))
                val iconActiveIcon = ContextCompat.getDrawable(holder.cardView.context, R.drawable.ic_call_active)
                if(callerID.type == "Outgoing")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        iconActiveIcon?.setTint(Color.parseColor("#0000BC"))
                    }
            }
        }

        holder.cardView.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            val tel = if((callerNumber.length == 12) && callerNumber.startsWith("91")) "+$callerNumber" else callerNumber
            intent.data = Uri.parse("tel:$tel")
            holder.cardView.context.startActivity(intent)
        }
    }

    private fun getContactName(phoneNumber: String?, context: Context): String {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection =
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        var contactName = ""
        val cursor= context.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0)
            }
            cursor.close()
        }
        return contactName
    }

    override fun getItemCount() = idList.size
}

class BackgroundThread : Thread() {
    private val runnableList = ArrayList<Runnable>()

    init{
        start()
    }

    override fun run(){
        while(true){
            if(runnableList.isNotEmpty()){
                val runnable = runnableList[0]
                runnable.run()
                runnableList.remove(runnable)
            }
            else sleep(100)
        }
    }

    fun addToQueue(runnable: Runnable){
        runnableList.add(runnable)
    }
}