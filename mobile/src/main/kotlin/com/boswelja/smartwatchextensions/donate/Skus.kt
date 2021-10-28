package com.boswelja.smartwatchextensions.donate

/**
 * Contains information about donation SKUs.
 */
object Skus {
    // One-time donation SKUs
    private const val DONATION_SMALL = "donation_small"
    private const val DONATION_MEDIUM = "donation_medium"
    private const val DONATION_LARGE = "donation_large"
    private const val DONATION_XLARGE = "donation_xlarge"

    // Recurring donation SKUs
    private const val DONATION_SMALL_RECURRING = "donation_small_recurring"
    private const val DONATION_MEDIUM_RECURRING = "donation_medium_recurring"
    private const val DONATION_LARGE_RECURRING = "donation_large_recurring"
    private const val DONATION_XLARGE_RECURRING = "donation_xlarge_recurring"

    /**
     * A list of all SKUs for one-off donations.
     */
    val ALL_ONE_TIME = listOf(
        DONATION_XLARGE,
        DONATION_LARGE,
        DONATION_MEDIUM,
        DONATION_SMALL
    )

    /**
     * A list of all SKUs for recurring donations.
     */
    val ALL_RECURRING = listOf(
        DONATION_XLARGE_RECURRING,
        DONATION_LARGE_RECURRING,
        DONATION_MEDIUM_RECURRING,
        DONATION_SMALL_RECURRING
    )
}
