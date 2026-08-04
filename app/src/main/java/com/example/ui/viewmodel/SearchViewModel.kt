package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.PostEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class SearchCategory {
    USERS,
    CONTENT
}

class SearchViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _category = MutableStateFlow(SearchCategory.USERS)
    val category: StateFlow<SearchCategory> = _category.asStateFlow()

    val searchUsersResult: StateFlow<List<UserEntity>> = _query
        .flatMapLatest { text -> repository.searchUsers(text) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val searchPostsResult: StateFlow<List<PostEntity>> = _query
        .flatMapLatest { text -> repository.searchPosts(text) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun selectCategory(cat: SearchCategory) {
        _category.value = cat
    }

    fun selectHashtag(hashtag: String) {
        _query.value = hashtag
        _category.value = SearchCategory.CONTENT
    }
}
