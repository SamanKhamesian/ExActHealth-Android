package com.example.exacthealth.models

import android.app.Application
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.AndroidViewModel

abstract class HealthDataViewModel(application: Application) : AndroidViewModel(application)
{
    lateinit var healthConnectClient: HealthConnectClient
    lateinit var permissions: Set<String>

    init
    {
        healthConnectClient = HealthConnectClient.getOrCreate(application)
    }

    suspend fun hasAllPermissions(): Boolean
    {
        val grantedPermissions = runCatching { healthConnectClient.permissionController.getGrantedPermissions() }.getOrDefault(emptySet())
        return grantedPermissions.containsAll(permissions)
    }

    // Abstract function for custom implementation in subclasses
    abstract fun readData()
}
