package de.drick

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface PrefDelegate<T> {
    val state: StateFlow<T>
    fun get(): T
    fun set(value: T)
}

class KDataStore private constructor(ctx: Context, name: String) {
    companion object {
        @Volatile
        private var instances = mutableMapOf<String, KDataStore>()
        fun getInstance(ctx: Context, name: String): KDataStore =
            instances[name] ?: synchronized(this) {
                instances[name] ?: KDataStore(ctx, name).also { instances[name] = it }
            }
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = name)
    private val dataStore = ctx.dataStore
    private val scope = GlobalScope //TODO
    fun stringPref(key: String, defaultValue: String): PrefDelegate<String> =
        BaseDelegate(dataStore, scope, defaultValue, stringPreferencesKey(key))
    //fun stringNAPref(key: String, defaultValue: String?): PrefDelegate<String?> =
    //    BaseNADelegate(dataStore, defaultValue, stringPreferencesKey(key))

    fun intPref(key: String, defaultValue: Int): PrefDelegate<Int> =
        BaseDelegate(dataStore, scope, defaultValue, intPreferencesKey(key))
    fun longPref(key: String, defaultValue: Long): PrefDelegate<Long> =
        BaseDelegate(dataStore, scope, defaultValue, longPreferencesKey(key))
    fun booleanPref(key: String, defaultValue: Boolean): PrefDelegate<Boolean> =
        BaseDelegate(dataStore, scope, defaultValue, booleanPreferencesKey(key))
}

private class BaseDelegate<T: Any>(
    private val ds: DataStore<Preferences>,
    private val scope: CoroutineScope,
    private val defaultValue: T,
    private val pref: Preferences.Key<T>
): PrefDelegate<T> {
    private val internalState = MutableStateFlow(defaultValue)
    override val state: StateFlow<T> = internalState
    private val dataStoreFlow = ds.data.map { it[pref] ?: defaultValue }
    init {
        scope.launch {
            dataStoreFlow.collect { internalState.emit(it) }
        }
    }
    override fun get(): T = state.value
    override fun set(value: T) {
        scope.launch {
            ds.edit {
                it[pref] = value
            }
        }
    }
}

/*internal class BaseNADelegate<T: Any>(
    private val ds: DataStore<Preferences>,
    private val defaultValue: T?,
    private val pref: Preferences.Key<T>
): PrefDelegate<T?> {
    override fun observe(): Flow<T?> = ds.data.map { it[pref] ?: defaultValue}
    override suspend fun get(): T? = observe().first()
    override suspend fun set(value: T?) {
        ds.edit {
            if (value == null) {
                it.remove(pref)
            } else {
                it[pref] = value
            }
        }
    }
}*/
