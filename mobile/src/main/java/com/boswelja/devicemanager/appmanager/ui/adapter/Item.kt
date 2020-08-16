/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager.ui.adapter

import android.graphics.Bitmap

sealed class Item {
    abstract val id: String

    data class App(
        val icon: Bitmap,
        val packageName: String,
        val label: String,
        val versionText: String
    ) : Item() {
        override val id: String = packageName

        override fun equals(other: Any?): Boolean {
            if (other is App) {
                if (packageName != other.packageName) return false
                if (label != other.label) return false
                if (versionText != other.versionText) return false
                if (id != other.id) return false

                return true
            } else {
                return super.equals(other)
            }
        }

        override fun hashCode(): Int {
            var result = packageName.hashCode()
            result = 31 * result + icon.hashCode()
            result = 31 * result + label.hashCode()
            result = 31 * result + versionText.hashCode()
            result = 31 * result + id.hashCode()
            return result
        }
    }

    data class Header(val label: String, override val id: String) : Item() {
        override fun equals(other: Any?): Boolean {
            if (other is Header) {
                if (label != other.label) return false
                if (id != other.id) return false

                return true
            } else {
                return super.equals(other)
            }
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + label.hashCode()
            result = 31 * result + id.hashCode()
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is Item) {
            if (id != other.id) return false

            return true
        } else {
            return super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
