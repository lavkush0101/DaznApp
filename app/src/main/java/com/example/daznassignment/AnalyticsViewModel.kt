package com.example.daznassignment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnalyticsViewModel : ViewModel() {
    private val _buttonClickCount = MutableLiveData<Int>()

     val pauseCount = MutableLiveData<Int>()
     val backwardCount = MutableLiveData<Int>()
     val forwardCount = MutableLiveData<Int>()

    init {
        pauseCount.value = 0
        backwardCount.value = 0
        forwardCount.value = 0
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




