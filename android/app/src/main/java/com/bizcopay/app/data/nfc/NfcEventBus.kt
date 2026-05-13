package com.bizcopay.app.data.nfc

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NfcEventBus {
    private val _uid = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val uid = _uid.asSharedFlow()

    fun emit(uid: String) {
        _uid.tryEmit(uid)
    }
}
