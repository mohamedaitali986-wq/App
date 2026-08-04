package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.UserEntity
import com.example.data.repository.AppRepository
import com.example.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: UserEntity) : AuthState
}

class AuthViewModel(
    private val repository: AppRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerError = MutableStateFlow<String?>(null)
    val registerError: StateFlow<String?> = _registerError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    val authState: StateFlow<AuthState> = sessionManager.currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                repository.observeUser(userId).flatMapLatest { user ->
                    if (user != null && !user.isSuspended) {
                        flowOf(AuthState.Authenticated(user))
                    } else {
                        sessionManager.clearSession()
                        flowOf(AuthState.Unauthenticated)
                    }
                }
            } else {
                flowOf(AuthState.Unauthenticated)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Loading
        )

    fun login(username: String, passwordRaw: String) {
        if (username.isBlank() || passwordRaw.isBlank()) {
            _loginError.value = "يرجى إدخال اسم المستخدم وكلمة المرور"
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _loginError.value = null
            val result = repository.loginUser(username, passwordRaw)
            result.onSuccess { user ->
                sessionManager.saveSession(user.id)
            }.onFailure { exception ->
                _loginError.value = exception.message ?: "فشل تسجيل الدخول"
            }
            _isSubmitting.value = false
        }
    }

    fun register(username: String, passwordRaw: String, displayName: String) {
        if (username.isBlank()) {
            _registerError.value = "اسم المستخدم مطلوب"
            return
        }
        if (passwordRaw.length < 6) {
            _registerError.value = "كلمة المرور يجب أن تكون ٦ أحرف على الأقل"
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _registerError.value = null
            val result = repository.registerUser(username, passwordRaw, displayName)
            result.onSuccess { user ->
                sessionManager.saveSession(user.id)
            }.onFailure { exception ->
                _registerError.value = exception.message ?: "فشل إنشاء الحساب"
            }
            _isSubmitting.value = false
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun clearErrors() {
        _loginError.value = null
        _registerError.value = null
    }
}
