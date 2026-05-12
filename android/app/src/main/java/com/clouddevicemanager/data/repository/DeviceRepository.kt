package com.clouddevicemanager.data.repository

import com.clouddevicemanager.data.model.Device
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DeviceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeDevices(userId: String): Flow<List<Device>> = callbackFlow {
        val registration = firestore.collection("users")
            .document(userId)
            .collection("devices")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val devices = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(Device::class.java)?.copy(id = doc.id)
                }
                trySend(devices)
            }

        awaitClose { registration.remove() }
    }

    suspend fun addDevice(userId: String, name: String, osVersion: String, region: String): Result<Unit> {
        return runCatching {
            val payload = mapOf(
                "name" to name,
                "osVersion" to osVersion,
                "region" to region,
                "isOnline" to true,
                "createdAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("users")
                .document(userId)
                .collection("devices")
                .add(payload)
                .await()
            Unit
        }
    }

    suspend fun updatePowerState(userId: String, deviceId: String, isOnline: Boolean): Result<Unit> {
        return runCatching {
            firestore.collection("users")
                .document(userId)
                .collection("devices")
                .document(deviceId)
                .update("isOnline", isOnline)
                .await()
            Unit
        }
    }

    suspend fun deleteDevice(userId: String, deviceId: String): Result<Unit> {
        return runCatching {
            firestore.collection("users")
                .document(userId)
                .collection("devices")
                .document(deviceId)
                .delete()
                .await()
            Unit
        }
    }
}