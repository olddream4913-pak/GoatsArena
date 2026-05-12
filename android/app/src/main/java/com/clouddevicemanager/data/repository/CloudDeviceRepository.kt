package com.clouddevicemanager.data.repository

import com.clouddevicemanager.data.model.CloudDevice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CloudDeviceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun createDevice(device: CloudDevice): Result<String> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User must be authenticated."))

        return runCatching {
            val payload = hashMapOf(
                "name" to device.name,
                "region" to device.region,
                "androidVersion" to device.androidVersion,
                "status" to device.status,
                "createdAt" to FieldValue.serverTimestamp()
            )

            val docRef = firestore.collection("users")
                .document(uid)
                .collection("devices")
                .add(payload)
                .await()

            docRef.id
        }
    }

    fun observeDevices(): Flow<List<CloudDevice>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close(IllegalStateException("User must be authenticated."))
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(uid)
            .collection("devices")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val devices = snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.toObject(CloudDevice::class.java)?.copy(id = document.id)
                }
                trySend(devices)
            }

        awaitClose { listener.remove() }
    }

    suspend fun getDevice(deviceId: String): Result<CloudDevice?> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User must be authenticated."))

        if (deviceId.isBlank()) {
            return Result.failure(IllegalArgumentException("deviceId cannot be blank."))
        }

        return runCatching {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("devices")
                .document(deviceId)
                .get()
                .await()

            snapshot.toObject(CloudDevice::class.java)?.copy(id = snapshot.id)
        }
    }

    suspend fun updateStatus(deviceId: String, status: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User must be authenticated."))

        if (deviceId.isBlank()) {
            return Result.failure(IllegalArgumentException("deviceId cannot be blank."))
        }

        if (status.isBlank()) {
            return Result.failure(IllegalArgumentException("status cannot be blank."))
        }

        return runCatching {
            firestore.collection("users")
                .document(uid)
                .collection("devices")
                .document(deviceId)
                .update("status", status)
                .await()
            Unit
        }
    }
}