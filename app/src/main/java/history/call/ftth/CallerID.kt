package history.call.ftth

import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class CallerID(val index: Int, val status: String, val from: String, val to: String, val type: String, val duration: String, sudoDateTime: String, timeDelta: Duration) {
    val dateTimeHtml: String

    init{
        var dateTimeStr = ""
        for(str in sudoDateTime.split(" "))
            dateTimeStr += ((if(str.length == 1) "0$str" else str) + " ")
        val actualDateTime = LocalDateTime.parse(dateTimeStr.replace("  ", " "), DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy ")) + timeDelta
        val weekDay = when(org.threeten.bp.Period.between(actualDateTime.toLocalDate(), LocalDate.now()).days){ 0 -> "Today"; 1-> "Yesterday"; else -> "'EEE'"}
        dateTimeHtml = actualDateTime.format(
            DateTimeFormatter.ofPattern("'<span style=\"color: black;\"><b>$weekDay</b></span>' MMM dd '<span style=\"color: black;\"><b>'hh:mm'</b></span>':ss '<span style=\"color: black;\"><b>'a'</b></span>'  yyyy"))
    }
}