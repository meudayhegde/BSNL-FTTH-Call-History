package history.call.ftth

import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class CallerID(val index: Int, val status: String, val from: String, val to: String, val type: String, val duration: String, sudoDateTime: String, timeDelta: Duration) {
    val dateTime: String

    init{
        var dateTimeStr = ""
        for(str in sudoDateTime.split(" "))
            dateTimeStr += ((if(str.length == 1) "0$str" else str) + " ")
        dateTime = (LocalDateTime.parse(dateTimeStr.replace("  ", " "), DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy ")) + timeDelta).format(
            DateTimeFormatter.ofPattern("EEE MMM dd hh:mm:ss a  yyyy"))
    }
}