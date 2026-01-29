package com.example.sms2line.util

import com.example.sms2line.model.ForwardingConfig
import com.example.sms2line.model.SmsMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MessageFormatter {

    private const val TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private const val SUBJECT_TIME_FORMAT = "MMM dd, HH:mm"

    fun formatSubject(sms: SmsMessage): String {
        val dateFormat = SimpleDateFormat(SUBJECT_TIME_FORMAT, Locale.getDefault())
        val formattedTime = dateFormat.format(Date(sms.timestamp))
        return "New SMS from ${sms.sender} at $formattedTime"
    }

    fun formatEmailBody(sms: SmsMessage, config: ForwardingConfig): String {
        val dateFormat = SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault())
        val formattedTime = dateFormat.format(Date(sms.timestamp))

        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html>")
            appendLine("<head>")
            appendLine("<style>")
            appendLine("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }")
            appendLine(".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }")
            appendLine(".header { background: linear-gradient(135deg, #2196F3, #4CAF50); color: white; padding: 15px; border-radius: 8px; margin-bottom: 20px; }")
            appendLine(".header h2 { margin: 0; }")
            appendLine(".info { color: #666; font-size: 14px; margin-bottom: 15px; }")
            appendLine(".info-row { margin: 5px 0; }")
            appendLine(".label { font-weight: bold; color: #333; }")
            appendLine(".message-box { background-color: #e8f5e9; border-left: 4px solid #4CAF50; padding: 15px; border-radius: 4px; margin: 15px 0; }")
            appendLine(".message-content { font-size: 16px; line-height: 1.5; color: #333; white-space: pre-wrap; }")
            appendLine(".footer { margin-top: 20px; padding-top: 15px; border-top: 1px solid #eee; font-size: 12px; color: #999; text-align: center; }")
            appendLine("</style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("<div class=\"container\">")
            appendLine("<div class=\"header\">")
            appendLine("<h2>📱 SMS Received</h2>")
            appendLine("</div>")
            appendLine("<div class=\"info\">")

            if (config.includeSender) {
                appendLine("<div class=\"info-row\"><span class=\"label\">From:</span> ${sms.sender}</div>")
            }

            if (config.includeTimestamp) {
                appendLine("<div class=\"info-row\"><span class=\"label\">Received:</span> $formattedTime</div>")
            }

            appendLine("</div>")
            appendLine("<div class=\"message-box\">")
            appendLine("<div class=\"message-content\">${sms.body.replace("\n", "<br>")}</div>")
            appendLine("</div>")
            appendLine("<div class=\"footer\">")
            appendLine("Forwarded by SMS2Email")
            appendLine("</div>")
            appendLine("</div>")
            appendLine("</body>")
            appendLine("</html>")
        }
    }

    fun formatSimple(sms: SmsMessage): String {
        return "SMS from ${sms.sender}: ${sms.body}"
    }
}
