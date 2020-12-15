package com.boswelja.devicemanager.setup.ui

import android.app.Activity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Node
import java.lang.Exception
import java.util.concurrent.Executor

class ConnectedNodeDummy(private val dummyResult: List<Node>) : Task<List<Node>>() {
    override fun isComplete(): Boolean = true

    override fun isSuccessful(): Boolean = true

    override fun isCanceled(): Boolean = false

    override fun getResult(): List<Node> = dummyResult

    override fun <X : Throwable?> getResult(p0: Class<X>): List<Node>? = result

    override fun getException(): Exception? = null

    override fun addOnSuccessListener(p0: OnSuccessListener<in List<Node>>): Task<List<Node>> {
        p0.onSuccess(result)
        return this
    }

    override fun addOnSuccessListener(
        p0: Executor,
        p1: OnSuccessListener<in List<Node>>
    ): Task<List<Node>> {
        p1.onSuccess(result)
        return this
    }

    override fun addOnSuccessListener(
        p0: Activity,
        p1: OnSuccessListener<in List<Node>>
    ): Task<List<Node>> {
        p1.onSuccess(result)
        return this
    }

    override fun addOnFailureListener(p0: OnFailureListener): Task<List<Node>> {
        exception?.let { p0.onFailure(it) }
        return this
    }

    override fun addOnFailureListener(p0: Executor, p1: OnFailureListener): Task<List<Node>> {
        exception?.let { p1.onFailure(it) }
        return this
    }

    override fun addOnFailureListener(p0: Activity, p1: OnFailureListener): Task<List<Node>> {
        exception?.let { p1.onFailure(it) }
        return this
    }
}
