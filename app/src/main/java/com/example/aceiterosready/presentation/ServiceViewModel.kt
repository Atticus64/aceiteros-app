package com.example.aceiterosready.presentation

import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aceiterosready.data.ErrorResponse
import com.example.aceiterosready.data.Resource
import com.example.aceiterosready.data.SignInData
import com.example.aceiterosready.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServiceViewModel: ViewModel() {
    private val repository = UserRepository()

    var isLoggedIn = mutableStateOf(false)
    var token: String? = ""
    var refresh_token = mutableStateOf("")
//    var status = mutableStateOf<Resource<serverStatus>>(null)

    init {
        viewModelScope.launch {
            try {
//                result.value = repository.login()
            } catch (e: Exception) {}
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userInput = SignInData(email, password)
                val response = repository.login(userInput)

                if (response.isSuccessful) {
                    val info = response.body()
                    token = info?.data?.tokens?.accessToken
                    println("Succ")
                    println(info)
                } else {
                    println("not succ")
                }
            } catch (e: Exception) {
                println("exc")
                println(e.toString())
            }
        }
    }

    fun obtenerStatus() {
//        status.value = null
        viewModelScope.launch {
            try {
            } catch (e: Exception) {}
        }
    }



}