package com.boswelja.smartwatchextensions.donate.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.donate.DonationClient

/**
 * A ViewModel for providing data to [DonateActivity].
 */
class DonateViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * The donation client to use.
     */
    val donationClient = DonationClient(application)

    /**
     * Flow whether the client is connected.
     */
    val clientConnected = donationClient.clientConnected

    /**
     * Flow a list of available one-off donations.
     */
    val oneTimeDonations = donationClient.oneTimeDonations()

    /**
     * Flow a list of recurring donations.
     */
    val recurringDonations = donationClient.recurringDonations()

    override fun onCleared() {
        super.onCleared()
        donationClient.destroy()
    }
}
