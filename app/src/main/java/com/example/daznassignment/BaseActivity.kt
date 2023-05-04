package com.example.daznassignment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel

abstract class BaseActivity<T : ViewDataBinding, V : ViewModel> : AppCompatActivity() {

    private var mainActivityBinding: T? = null
    private var mViewModel: V? = null

    abstract fun getBindingVariable(): Int
    abstract fun getLayout(): Int
    abstract fun getViewModel(): V


    fun getViewDataBinding():T{
        return mainActivityBinding!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        perFormDataBinding()
    }

    private fun perFormDataBinding(){
        mainActivityBinding=DataBindingUtil.setContentView(this,getLayout())
        mViewModel=when(this.mViewModel){
            null->getViewModel()
            else -> mViewModel
        }
        mainActivityBinding?.setVariable(getBindingVariable(),mViewModel)
        mainActivityBinding?.executePendingBindings()
        mainActivityBinding?.lifecycleOwner=this

    }
}