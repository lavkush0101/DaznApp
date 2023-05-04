package com.example.daznassignment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnalyticsViewModel : ViewModel() {
    private val _buttonClickCount = MutableLiveData<Int>()

    var pauseCount = MutableLiveData<String>()
    var pCount = MutableLiveData<Int>()
    var backwardCount = MutableLiveData<String>()
    var bCount = MutableLiveData<Int>()
    var forwardCount = MutableLiveData<String>()
    var fCount = MutableLiveData<Int>()

    init {
        pCount.value = 0
        pauseCount.value = "Pause Count : ${pCount.value}"
        bCount.value = 0
        backwardCount.value = "BackwardCount : ${bCount.value}"
        fCount.value = 0
        forwardCount.value = "ForwardCount : ${fCount.value}"
    }

    val buttonClickCount: LiveData<Int>
        get() = _buttonClickCount

    init {
        _buttonClickCount.value = 0
    }

    fun incrementButtonClickCount() {
        _buttonClickCount.value = (_buttonClickCount.value ?: 0) + 1
    }
}




