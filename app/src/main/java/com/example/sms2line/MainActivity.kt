package com.example.sms2line

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sms2line.databinding.ActivityMainBinding
import com.example.sms2line.email.EmailConfig
import com.example.sms2line.email.EmailResult
import com.example.sms2line.email.SmtpEmailClient
import com.example.sms2line.model.SmsMessage
import com.example.sms2line.service.SmsForwarderService
import com.example.sms2line.storage.PreferencesManager
import com.example.sms2line.storage.SmtpCredentialStorage
import com.example.sms2line.util.MessageFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var smtpStorage: SmtpCredentialStorage
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var emailClient: SmtpEmailClient

    private val requiredPermissions = mutableListOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            updatePermissionStatus()
        } else {
            Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_LONG).show()
            updatePermissionStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        smtpStorage = SmtpCredentialStorage(this)
        preferencesManager = PreferencesManager(this)
        emailClient = SmtpEmailClient()

        setupUI()
        loadSavedSettings()
        updatePermissionStatus()
    }

    private fun setupUI() {
        // Setup port dropdown
        val ports = arrayOf("587 (TLS)", "465 (SSL)", "25 (Plain)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ports)
        binding.editSmtpPort.setAdapter(adapter)

        binding.btnRequestPermissions.setOnClickListener {
            requestPermissions()
        }

        binding.btnSaveConfig.setOnClickListener {
            saveSmtpConfig()
        }

        binding.btnTestEmail.setOnClickListener {
            testEmailConfig()
        }

        binding.switchForwarding.setOnCheckedChangeListener { _, isChecked ->
            handleForwardingToggle(isChecked)
        }

        binding.switchIncludeTimestamp.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.includeTimestamp = isChecked
        }

        binding.switchIncludeSender.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.includeSender = isChecked
        }
    }

    private fun loadSavedSettings() {
        val config = smtpStorage.getConfig()

        if (config.smtpHost.isNotBlank()) {
            binding.editSmtpHost.setText(config.smtpHost)
            binding.editSmtpPort.setText(getPortDisplay(config.smtpPort))
            binding.editSmtpUsername.setText(config.username)
            binding.editSmtpPassword.setText(config.password)
            binding.editFromEmail.setText(config.fromAddress)
            binding.editToEmail.setText(config.toAddress)
        }

        binding.switchForwarding.isChecked = preferencesManager.isForwardingEnabled
        binding.switchIncludeTimestamp.isChecked = preferencesManager.includeTimestamp
        binding.switchIncludeSender.isChecked = preferencesManager.includeSender
    }

    private fun getPortDisplay(port: Int): String {
        return when (port) {
            587 -> "587 (TLS)"
            465 -> "465 (SSL)"
            25 -> "25 (Plain)"
            else -> port.toString()
        }
    }

    private fun parsePort(portText: String): Int {
        return when {
            portText.startsWith("587") -> 587
            portText.startsWith("465") -> 465
            portText.startsWith("25") -> 25
            else -> portText.toIntOrNull() ?: 587
        }
    }

    private fun updatePermissionStatus() {
        val allGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        binding.textPermissionStatus.text = if (allGranted) {
            "All permissions granted"
        } else {
            "Permissions required"
        }

        binding.btnRequestPermissions.isEnabled = !allGranted
        binding.switchForwarding.isEnabled = allGranted && smtpStorage.hasValidConfig()
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    private fun requestBatteryOptimizationExemption() {
        if (isIgnoringBatteryOptimizations()) {
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Battery Optimization")
            .setMessage("For reliable SMS forwarding in the background, this app needs to be excluded from battery optimization. Would you like to disable battery optimization for this app?")
            .setPositiveButton("Yes") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to battery optimization settings
                    try {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        startActivity(intent)
                    } catch (e2: Exception) {
                        Toast.makeText(this, "Please manually disable battery optimization for this app in Settings", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun requestPermissions() {
        permissionLauncher.launch(requiredPermissions)
    }

    private fun getEmailConfigFromUI(): EmailConfig {
        val port = parsePort(binding.editSmtpPort.text.toString())
        return EmailConfig(
            smtpHost = binding.editSmtpHost.text.toString().trim(),
            smtpPort = port,
            username = binding.editSmtpUsername.text.toString().trim(),
            password = binding.editSmtpPassword.text.toString(),
            fromAddress = binding.editFromEmail.text.toString().trim(),
            toAddress = binding.editToEmail.text.toString().trim(),
            useTls = port != 25
        )
    }

    private fun saveSmtpConfig() {
        val config = getEmailConfigFromUI()

        if (!config.isValid()) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        smtpStorage.saveConfig(config)
        Toast.makeText(this, "Configuration saved", Toast.LENGTH_SHORT).show()
        updatePermissionStatus()
    }

    private fun testEmailConfig() {
        val config = getEmailConfigFromUI()

        if (!config.isValid()) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnTestEmail.isEnabled = false
        binding.btnTestEmail.text = "Testing..."

        // Create a fake SMS message for testing
        val testSms = SmsMessage(
            sender = "+1 (555) 123-4567",
            body = "This is a test message from SMS2Email app.\n\nYour email configuration is working correctly! When you receive a real SMS, it will look exactly like this email.",
            timestamp = System.currentTimeMillis()
        )

        val forwardingConfig = preferencesManager.getForwardingConfig("")
        val subject = MessageFormatter.formatSubject(testSms)
        val body = MessageFormatter.formatEmailBody(testSms, forwardingConfig)

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                emailClient.testConnection(config, subject, body)
            }

            binding.btnTestEmail.isEnabled = true
            binding.btnTestEmail.text = "Test Email"

            when (result) {
                is EmailResult.Success -> {
                    Toast.makeText(this@MainActivity, "Test email sent! Check your inbox", Toast.LENGTH_LONG).show()
                }
                is EmailResult.Error -> {
                    Toast.makeText(this@MainActivity, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleForwardingToggle(enabled: Boolean) {
        if (enabled) {
            if (!smtpStorage.hasValidConfig()) {
                binding.switchForwarding.isChecked = false
                Toast.makeText(this, "Please configure and save email settings first", Toast.LENGTH_SHORT).show()
                return
            }

            val allPermissionsGranted = requiredPermissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }

            if (!allPermissionsGranted) {
                binding.switchForwarding.isChecked = false
                Toast.makeText(this, "Please grant all permissions first", Toast.LENGTH_SHORT).show()
                return
            }

            preferencesManager.isForwardingEnabled = true
            startForwarderService()
            Toast.makeText(this, "SMS forwarding enabled", Toast.LENGTH_SHORT).show()

            // Prompt for battery optimization exemption
            requestBatteryOptimizationExemption()
        } else {
            preferencesManager.isForwardingEnabled = false
            stopForwarderService()
            Toast.makeText(this, "SMS forwarding disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startForwarderService() {
        val intent = Intent(this, SmsForwarderService::class.java).apply {
            action = SmsForwarderService.ACTION_START_SERVICE
        }
        startForegroundService(intent)
    }

    private fun stopForwarderService() {
        val intent = Intent(this, SmsForwarderService::class.java).apply {
            action = SmsForwarderService.ACTION_STOP_SERVICE
        }
        startService(intent)
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }
}
