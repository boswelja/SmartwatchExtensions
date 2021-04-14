package com.boswelja.smartwatchextensions.donate

object Skus {
    // One-time donation SKUs
    private const val DONATION_SMALL = "donation_small"
    private const val DONATION_MEDIUM = "donation_medium"
    private const val DONATION_LARGE = "donation_large"
    private const val DONATION_XLARGE = "xlarge_donation"

    // Recurring donation SKUs
    private const val DONATION_SMALL_RECURRING = "small_recurring_donation"
    private const val DONATION_MEDIUM_RECURRING = "medium_recurring_donation"
    private const val DONATION_LARGE_RECURRING = "large_recurring_donation"
    private const val DONATION_XLARGE_RECURRING = "xlarge_recurring_donation"

    val ALL_ONE_TIME = listOf(
        DONATION_XLARGE,
        DONATION_LARGE,
        DONATION_MEDIUM,
        DONATION_SMALL
    )
    val ALL_RECURRING = listOf(
        DONATION_XLARGE_RECURRING,
        DONATION_LARGE_RECURRING,
        DONATION_MEDIUM_RECURRING,
        DONATION_SMALL_RECURRING
    )
}
