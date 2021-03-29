package com.mymasimo.masimosleep.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@ExperimentalCoroutinesApi
fun Query.asFlow(): Flow<QuerySnapshot> {
    return callbackFlow {
        val subscription = addSnapshotListener { snapshot, ex ->
            when {
                ex != null -> close(ex)
                snapshot == null -> return@addSnapshotListener
                else -> offer(snapshot)
            }
        }
        awaitClose {
            subscription.remove()
        }
    }
}

@ExperimentalCoroutinesApi
fun DocumentReference.asFlow(): Flow<DocumentSnapshot> {
    return callbackFlow {
        val subscription = addSnapshotListener { snapshot, ex ->
            when {
                ex != null -> close(ex)
                snapshot == null -> return@addSnapshotListener
                else -> offer(snapshot)
            }
        }
        awaitClose {
            subscription.remove()
        }
    }
}

suspend inline fun <reified T : Any> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) }
        addOnFailureListener { continuation.resumeWithException(it) }
        addOnCanceledListener { continuation.cancel() }
    }
}