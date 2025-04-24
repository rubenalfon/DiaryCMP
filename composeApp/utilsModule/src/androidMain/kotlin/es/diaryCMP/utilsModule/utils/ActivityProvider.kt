package es.diaryCMP.utilsModule.utils

import android.app.Activity

class ActivityProvider {
    private var activity: Activity? = null

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    fun getActivity(): Activity? = activity
}