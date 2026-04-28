package com.example.bookcatalog.presentation.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookcatalog.domain.usecase.LoginUseCase
import com.example.bookcatalog.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    var email = mutableStateOf("")
    var password = mutableStateOf("")

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)
    var isLoginMode = mutableStateOf(true) // true = Логин, false = Регистрация

    var isSuccess = mutableStateOf(false) // Флаг успешного входа

    fun onAuthClick() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            val result = if (isLoginMode.value) {
                loginUseCase(email.value, password.value)
            } else {
                registerUseCase(email.value, password.value)
            }

            result.onSuccess {
                isSuccess.value = true // Успех! Можно переходить на следующий экран
            }.onFailure {
                errorMessage.value = it.message // Показываем ошибку
            }

            isLoading.value = false
        }
    }

    fun toggleMode() {
        isLoginMode.value = !isLoginMode.value
        errorMessage.value = null
    }
}