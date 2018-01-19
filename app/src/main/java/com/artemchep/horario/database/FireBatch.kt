package com.artemchep.horario.database

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class FireBatch {

    private val pathSet: MutableSet<String> = HashSet()
    private val batches: MutableList<Batch<*>> = ArrayList()

    private var failed: Boolean = false

    /**
     * @author Artem Chepurnoy
     */
    private interface Batch<T> {
        val models: MutableList<T>
        val paths: Collection<String>
        val transform: (DocumentSnapshot) -> T
        val type: String
    }

    fun <T> batch(transform: (DocumentSnapshot) -> T, paths: Collection<String>, list: MutableList<T>, type: String): FireBatch {
        batches.add(object : Batch<T> {
            override val models: MutableList<T> = list
            override val paths: Collection<String> = paths
            override val type: String = type
            override val transform: (DocumentSnapshot) -> T = {
                val model = transform(it)
                models.add(model)
                model
            }
        })
        return this
    }

    fun go(callback: (Boolean) -> Unit) {
        // Remember all paths
        pathSet.clear()
        batches.forEach {
            it.paths.forEach {
                pathSet.add(it)
            }
        }
        // Register all listeners
        var empty = true
        val firestore = FirebaseFirestore.getInstance()
        batches.forEach { batch ->
            empty = empty and batch.paths.isEmpty()
            batch.paths.forEach { path ->
                when (batch.type) {
                    "document" -> {
                        Timber.d("FUCK, path=" + path)
                        firestore.document(path).get().addOnCompleteListener { task ->
                            if (failed) return@addOnCompleteListener
                            if (task.isSuccessful) {
                                batch.transform(task.result)
                                pathSet.remove(path)

                                if (pathSet.isEmpty()) {
                                    callback(true)
                                }
                            } else {
                                failed = true
                                callback(false)
                            }
                        }
                    }
                    "collection" -> {
                        firestore.collection(path).get().addOnCompleteListener { task ->
                            if (failed) return@addOnCompleteListener
                            if (task.isSuccessful) {
                                task.result.documents.forEach { doc ->
                                    batch.transform(doc)
                                }

                                pathSet.remove(path)
                                if (pathSet.isEmpty()) {
                                    callback(true)
                                }
                            } else {
                                failed = true
                                callback(false)
                            }
                        }
                    }
                }
            }
        }

        if (empty) {
            callback(true)
        }
    }

}