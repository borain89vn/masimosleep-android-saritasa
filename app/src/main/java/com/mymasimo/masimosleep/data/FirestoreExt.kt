package com.mymasimo.masimosleep.data

import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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