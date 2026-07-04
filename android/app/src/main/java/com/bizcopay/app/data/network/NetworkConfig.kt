package com.bizcopay.app.data.network

object NetworkConfig {
    // Change this one constant when switching between environments.
    // Local dev (emulator)  : "http://10.0.2.2:3000"
    // Local dev (USB device): "http://localhost:3000"  + adb reverse tcp:3000 tcp:3000
    // Production            : "https://your-app.up.railway.app"
    const val SERVER_URL     = "https://bizcopay-production.up.railway.app"
    const val HTTP_BASE_URL  = "$SERVER_URL/"
}
