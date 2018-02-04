package com.boswelja.devicemanager.common

import com.boswelja.devicemanager.receiver

object Utils {
    
    public fun requestDeviceAdminPerms(context: Context) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, DeviceAdminReceiver().getWho(context))
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_desc))
        startActivityForResult(intent, Config.DEVICE_ADMIN_REQUEST_CODE)
    }

    public fun isDeviceAdmin(deviceAdminReceiver: deviceAdminReceiver): Boolean {
        return devicePolicyManager!!.isAdminActive(deviceAdminReceiver)
    }

}