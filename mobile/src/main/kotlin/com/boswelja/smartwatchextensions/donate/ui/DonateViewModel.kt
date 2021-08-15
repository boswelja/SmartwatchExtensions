package com.boswelja.smartwatchextensions.donate.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.donate.DonationClient

class DonateViewModel(application: Application) : AndroidViewModel(application) {

    val donationClient = DonationClient(application)

    val clientConnected = donationClient.clientConnected
    val oneTimeDonations = donationClient.oneTimeDonations()
    val recurringDonations = donationClient.recurringDonations()

    override fun onCleared() {
        super.onCleared()
        donationClient.destroy()
    }
}
