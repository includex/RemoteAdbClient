package includex.com.dio.event

import includex.com.dio.enum.StateType

class StatusChangedEvent(val state: StateType) {
    public var message: String? = null
}