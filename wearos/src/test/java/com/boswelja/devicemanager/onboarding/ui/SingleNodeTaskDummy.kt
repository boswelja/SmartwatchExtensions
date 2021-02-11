package com.boswelja.devicemanager.onboarding.ui

import android.app.Activity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Node
import java.lang.Exception
import java.util.concurrent.Executor

class SingleNodeTaskDummy(private val dummyResult: Node) : Task<Node>() {
    override fun isComplete(): Boolean = true

    override fun isSuccessful(): Boolean = true

    override fun isCanceled(): Boolean = false

    override fun getResult(): Node = dummyResult

    override fun <X : Throwable?> getResult(p0: Class<X>): Node = result

    override fun getException(): Exception? = null

    override fun addOnCompleteListener(p0: OnCompleteListener<Node>): Task<Node> {
        p0.onComplete(this)
        return this
    }

    override fun addOnSuccessListener(p0: OnSuccessListener<in Node>): Task<Node> {
        p0.onSuccess(result)
        return this
    }

    override fun addOnSuccessListener(
        p0: Executor,
        p1: OnSuccessListener<in Node>
    ): Task<Node> {
        p1.onSuccess(result)
        return this
    }

    override fun addOnSuccessListener(
        p0: Activity,
        p1: OnSuccessListener<in Node>
    ): Task<Node> {
        p1.onSuccess(result)
        return this
    }

    override fun addOnFailureListener(p0: OnFailureListener): Task<Node> {
        exception?.let { p0.onFailure(it) }
        return this
    }

    override fun addOnFailureListener(p0: Executor, p1: OnFailureListener): Task<Node> {
        exception?.let { p1.onFailure(it) }
        return this
    }

    override fun addOnFailureListener(p0: Activity, p1: OnFailureListener): Task<Node> {
        exception?.let { p1.onFailure(it) }
        return this
    }
}
