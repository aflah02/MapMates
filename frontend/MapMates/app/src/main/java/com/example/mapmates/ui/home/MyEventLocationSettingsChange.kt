package com.example.mapmates.ui.home

import org.greenrobot.eventbus.EventBus

class MyEventLocationSettingsChange(val on:Boolean) {
    companion object {
        var globalState=false //Set this for first time
        /**
         * In some devices change event is called twice. We limit this with internal state.
         */
        fun setChangeAndPost(_on:Boolean) {
            if (globalState !=_on) { //Send Just Change
                globalState = _on;
                EventBus.getDefault().post(MyEventLocationSettingsChange(_on))
            }
        }
    }
}